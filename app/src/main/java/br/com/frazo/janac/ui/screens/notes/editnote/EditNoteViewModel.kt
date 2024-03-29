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
import br.com.frazo.janac.domain.usecases.notes.delete.DeleteNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.update.UpdateNoteUseCase
import br.com.frazo.janac.ui.mediator.CallBackUIParticipant
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.util.TextResource
import br.com.frazo.janac.util.files.FilesDisposer
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
import javax.inject.Named

class EditNoteViewModel @AssistedInject constructor(
    private val addNoteUseCase: AddNoteUseCase<Int>,
    private val updateNoteUseCase: UpdateNoteUseCase<Int>,
    @Named("DeleteLatestNoteWithTitleAndTextUseCase") private val deleteNoteUseCase: DeleteNoteUseCase<Int>,
    private val noteValidatorUseCase: NoteValidatorUseCase,
    private val mediator: UIMediator,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
    private val filesDisposer: FilesDisposer,
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

    private val uiParticipantRepresentative = CallBackUIParticipant { _, event ->
        handleMediatorMessage(event)
    }

    private var toEditNote = Note("", "")

    private var _inEditionNote = MutableStateFlow(toEditNote)
    val inEditionNote = _inEditionNote.asStateFlow()

    private var _uiState = MutableStateFlow<UIState>(UIState.Editing)
    val uiState = _uiState.asStateFlow()

    private var _audioRecordFlow = MutableStateFlow<List<AudioRecordingData>>(emptyList())
    val audioRecordFlow = _audioRecordFlow.asStateFlow()

    private var _audioNoteStatus = MutableStateFlow(AudioNoteStatus.HAVE_TO_RECORD)
    val audioStatus = _audioNoteStatus.asStateFlow()

    private var _audioNotePlayingData =
        MutableStateFlow(AudioPlayingData(AudioPlayerStatus.NOT_INITIALIZED, 0, 0))
    val audioNotePlayingData = _audioNotePlayingData.asStateFlow()
    private var lastDeletedAudioFile: File? = null


    init {
        setForEditing(noteToEdit)
        mediator.addParticipant(uiParticipantRepresentative)
    }

    override fun onCleared() {
        mediator.removeParticipant(uiParticipantRepresentative)
        super.onCleared()
    }

    private fun handleMediatorMessage(event: UIEvent) {

        when (event) {
            is UIEvent.Rollback -> {
                when (event.originalEvent) {
                    is UIEvent.NoteCreated -> {
                        viewModelScope.launch {
                            deleteNoteUseCase(event.originalEvent.newNote)
                        }
                    }
                    is UIEvent.NoteEdited -> {
                        viewModelScope.launch {
                            updateNoteUseCase(
                                event.originalEvent.newNote,
                                event.originalEvent.oldNote
                            )
                        }
                    }
                    else -> Unit
                }
            }
            else -> Unit
        }

    }

    fun setForEditing(note: Note) {
        toEditNote = note
        _inEditionNote.value = toEditNote
        if (note.audioNote != null && note.audioNote.exists()) {
            _audioNoteStatus.value = AudioNoteStatus.CAN_PLAY
        } else {
            _audioNoteStatus.value = AudioNoteStatus.HAVE_TO_RECORD
        }
        validateCanSave()
    }

    private fun reset() {
        toEditNote = Note("", "")
        _inEditionNote.value = toEditNote
        _uiState.value = UIState.Editing
        audioPlayer.stop()
        audioRecorder.stopRecording()
        if (toEditNote.isNewNote()) {
            _inEditionNote.value.audioNote?.let {
                filesDisposer.moveToBin(it)
            }
        }
        _audioRecordFlow.value = emptyList()
        _audioNoteStatus.value = AudioNoteStatus.HAVE_TO_RECORD
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
                    if (toEditNote.isNewNote()) UIEvent.NoteCreated(_inEditionNote.value) else UIEvent.NoteEdited(
                        toEditNote,
                        _inEditionNote.value
                    )
                )
            } else {
                _uiState.value = UIState.SaveFailed(Throwable("Save Failed"))
            }
        }
    }

    private suspend fun saveNewNote(): Int {
        return addNoteUseCase(_inEditionNote.value)
    }

    private suspend fun editNote(): Int {
        lastDeletedAudioFile = toEditNote.audioNote
        return updateNoteUseCase(toEditNote, _inEditionNote.value)
    }

    fun cancel() {
        reset()
    }

    fun startRecordingAudioNote(audioDirectory: File) {
        viewModelScope.launch {
            _audioRecordFlow.value = emptyList()
            if (toEditNote.isNewNote()) {
                _inEditionNote.value.audioNote?.let {
                    filesDisposer.moveToBin(it)
                }
            } else
                _inEditionNote.value = _inEditionNote.value.copy(audioNote = null)
            _inEditionNote.value = _inEditionNote.value.copy(
                audioNote = File(
                    audioDirectory,
                    UUID.randomUUID().toString() + ".mp3"
                )
            )
            _inEditionNote.value.audioNote?.let { fileOutput ->
                val flow =
                    audioRecorder.startRecording(fileOutput)
                flow.catch {
                    audioRecorder.stopRecording()
                    filesDisposer.moveToBin(fileOutput)
                    _inEditionNote.value = _inEditionNote.value.copy(audioNote = null)
                    _uiState.value = UIState.Error(it)
                    UIEvent.Error(
                        TextResource.RuntimeString(
                            it.localizedMessage ?: it.message ?: "An error has occurred."
                        )
                    )
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
        _audioRecordFlow.value = emptyList()
        _inEditionNote.value.audioNote?.let {
            _audioNoteStatus.value = AudioNoteStatus.CAN_PLAY
        }
    }

    fun playAudioNote() {
        if (_audioNotePlayingData.value.status == AudioPlayerStatus.NOT_INITIALIZED) {
            _inEditionNote.value.audioNote?.let { file ->
                viewModelScope.launch {
                    val flow = audioPlayer.start(file)
                    flow.catch {
                        _uiState.value = UIState.Error(it)
                        mediator.broadcast(
                            uiParticipantRepresentative,
                            UIEvent.Error(
                                TextResource.StringResource(R.string.audio_note_error)
                            )
                        )
                        audioPlayer.stop()
                    }.collectLatest {
                        _audioNotePlayingData.value = it
                    }
                }
            }
        } else {
            resumeAudioNote()
        }
    }

    fun pauseAudioNote() {
        audioPlayer.pause()
    }

    private fun resumeAudioNote() {
        audioPlayer.resume()
    }

    fun deleteAudioNote() {
        audioPlayer.stop()
        if (toEditNote.isNewNote()) {
            _inEditionNote.value.audioNote?.let {
                filesDisposer.moveToBin(it)
            }
        } else
            _inEditionNote.value = _inEditionNote.value.copy(audioNote = null)
        _audioNoteStatus.value = AudioNoteStatus.HAVE_TO_RECORD
    }

    fun seekAudioNote(positionPercent: Float) {
        audioPlayer.seek((positionPercent * _audioNotePlayingData.value.duration).toLong())
    }
}

