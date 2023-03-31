package br.com.frazo.janac.ui.screens.notes.editnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.R
import br.com.frazo.janac.audio.player.AudioPlayer
import br.com.frazo.janac.audio.player.AudioPlayerStatus
import br.com.frazo.janac.audio.player.AudioPlayingData
import br.com.frazo.janac.audio.recorder.AudioRecorder
import br.com.frazo.janac.audio.recorder.AudioRecordingData
import br.com.frazo.janac.domain.extensions.isNewNote
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.notes.NoteValidator
import br.com.frazo.janac.domain.usecases.notes.NoteValidatorUseCase
import br.com.frazo.janac.domain.usecases.notes.create.AddNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.update.UpdateNoteUseCase
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.mediator.UIParticipant
import br.com.frazo.janac.ui.util.TextResource
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

class EditNoteViewModel @AssistedInject constructor(
    private val addNoteUseCase: AddNoteUseCase<Int>,
    private val updateNoteUseCase: UpdateNoteUseCase<Int>,
    private val noteValidatorUseCase: NoteValidatorUseCase,
    private val mediator: UIMediator,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
    @Assisted noteToEdit: Note,
) :
    ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(note: Note): EditNoteViewModel
    }

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun provideEditNoteViewModelFactory(
            factory: Factory,
            note: Note
        ): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(note) as T
                }
            }
        }
    }

    sealed class UIState {
        object Saved : UIState()
        data class SaveFailed(val throwable: Throwable) : UIState()
        object Editing :
            UIState()

        data class TitleError(
            val titleInvalidError: TextResource = TextResource.RuntimeString(""),
        ) : UIState()

        data class TextError(
            val textInvalidError: TextResource = TextResource.RuntimeString(""),
        ) : UIState()

        data class Error(val throwable: Throwable) : UIState()

        object CanSave : UIState()
    }

    enum class AudioNoteStatus {
        HAVE_TO_RECORD, CAN_PLAY
    }

    private val uiParticipantRepresentative = object : UIParticipant {}

    private var toEditNote = Note("", "")

    private var _inEditionNote = MutableStateFlow(toEditNote)
    val inEditionNote = _inEditionNote.asStateFlow()

    private var _uiState = MutableStateFlow<UIState>(UIState.Editing)
    val uiState = _uiState.asStateFlow()

    private var _audioRecordFlow = MutableStateFlow<List<AudioRecordingData>>(emptyList())
    val audioRecordFlow = _audioRecordFlow.asStateFlow()
    private var currentAudioFile: File? = null

    private var _audioNoteStatus = MutableStateFlow(AudioNoteStatus.HAVE_TO_RECORD)
    val audioStatus = _audioNoteStatus.asStateFlow()

    private var _audioNotePlayingData =
        MutableStateFlow(AudioPlayingData(AudioPlayerStatus.NOT_INITIALIZED, 0, 0))
    val audioNotePlayingData = _audioNotePlayingData.asStateFlow()


    init {
        setForEditing(noteToEdit)
        mediator.addParticipant(uiParticipantRepresentative)
    }

    fun setForEditing(note: Note) {
        toEditNote = note
        _inEditionNote.value = toEditNote
        validateCanSave()
    }

    private fun reset() {
        _inEditionNote.value = Note("", "")
        _uiState.value = UIState.Editing
        audioRecorder.stopRecording()
        _audioRecordFlow.value = emptyList()
        currentAudioFile?.delete()
    }

    fun onTitleChanged(newTitle: String) {
        _inEditionNote.value = _inEditionNote.value.copy(title = newTitle)
        _uiState.value = UIState.Editing
        validate(_inEditionNote.value, Note::title.name)
    }

    fun onTextChanged(newText: String) {
        _inEditionNote.value = _inEditionNote.value.copy(text = newText)
        _uiState.value = UIState.Editing
        validate(_inEditionNote.value, Note::text.name)
    }

    private fun validateCanSave() {
        val validationResult = noteValidatorUseCase(_inEditionNote.value, null)
        if (validationResult is NoteValidator.NoteValidatorResult.Valid)
            _uiState.value = UIState.CanSave
    }

    private fun validate(note: Note, field: String) {

        when (val validationResult = noteValidatorUseCase(note, field)) {
            is NoteValidator.NoteValidatorResult.Valid -> _uiState.value = UIState.Editing
            is NoteValidator.NoteValidatorResult.InvalidLength -> {
                val minLength = validationResult.minLength
                when (validationResult.field) {
                    Note::title.name ->
                        _uiState.value = UIState.TitleError(
                            titleInvalidError = TextResource.StringResource(
                                R.string.invalid_title,
                                "$minLength"
                            )
                        )
                    Note::text.name -> _uiState.value = UIState.TextError(
                        textInvalidError = TextResource.StringResource(
                            R.string.invalid_text,
                            "$minLength"
                        )
                    )
                    else -> Unit
                }
            }
            is NoteValidator.NoteValidatorResult.Error -> _uiState.value =
                UIState.Error(validationResult.throwable)
        }
        validateCanSave()
    }

    fun save() {
        viewModelScope.launch {
            val result = if (toEditNote.isNewNote()) saveNewNote() else editNote()
            if (result > 0) {
                _uiState.value = UIState.Saved
                mediator.broadcast(
                    uiParticipantRepresentative,
                    if (toEditNote.isNewNote()) UIEvent.NoteCreated(inEditionNote.value) else UIEvent.NoteEdited(
                        toEditNote,
                        inEditionNote.value
                    )
                )
            } else {
                _uiState.value = UIState.SaveFailed(Throwable("Save Failed"))
            }
        }
    }

    private suspend fun saveNewNote(): Int {
        return addNoteUseCase(inEditionNote.value)
    }

    private suspend fun editNote(): Int {
        return updateNoteUseCase(toEditNote, inEditionNote.value)
    }

    fun cancel() {
        reset()
    }

    fun startRecordingAudioNote(audioDirectory: File) {
        viewModelScope.launch {
            _audioRecordFlow.value = emptyList()
            currentAudioFile?.delete()
            currentAudioFile = File(audioDirectory, UUID.randomUUID().toString())
            currentAudioFile?.let { fileOutput ->
                val flow =
                    audioRecorder.startRecording(fileOutput)
                flow.catch {
                    audioRecorder.stopRecording()
                    fileOutput.delete()
                    currentAudioFile = null
                    _uiState.value = UIState.Error(it)
                    UIEvent.Error(TextResource.RuntimeString(it.localizedMessage?:it.message?:"An error has occurred."))
                }
                    .collectLatest {
                        if (_audioRecordFlow.value.size >= 1000)
                            _audioRecordFlow.value =
                                _audioRecordFlow.value - _audioRecordFlow.value.first()
                        _audioRecordFlow.value = _audioRecordFlow.value + it
                    }
            }
        }
    }

    fun stopRecordingAudio() {
        audioRecorder.stopRecording()
        currentAudioFile?.let {
            _audioNoteStatus.value = AudioNoteStatus.CAN_PLAY
        }
    }

    fun playAudioNote() {
        if(_audioNotePlayingData.value.status == AudioPlayerStatus.NOT_INITIALIZED) {
            currentAudioFile?.let { file ->
                viewModelScope.launch {
                    val flow = audioPlayer.start(file)
                    flow.catch {
                        _uiState.value = UIState.Error(it)
                        mediator.broadcast(
                            uiParticipantRepresentative,
                            UIEvent.Error(
                                TextResource.RuntimeString(
                                    it.localizedMessage ?: it.message ?: "An error has occurred."
                                )
                            )
                        )
                        audioPlayer.stop()
                    }.collectLatest {
                        _audioNotePlayingData.value = it
                    }
                }
            }
        }else{
            resumeAudioNote()
        }
    }

    fun pauseAudioNote() {
        audioPlayer.pause()
    }

    private fun resumeAudioNote(){
        audioPlayer.resume()
    }

    fun deleteAudioNote(){
        audioPlayer.stop()
        currentAudioFile?.delete()
        _audioNoteStatus.value = AudioNoteStatus.HAVE_TO_RECORD
    }
}

