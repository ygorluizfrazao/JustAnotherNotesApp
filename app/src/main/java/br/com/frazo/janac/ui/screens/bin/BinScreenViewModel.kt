package br.com.frazo.janac.ui.screens.bin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.R
import br.com.frazo.janac.audio.player.AudioPlayer
import br.com.frazo.janac.audio.player.AudioPlayerStatus
import br.com.frazo.janac.audio.player.AudioPlayingData
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.SearchTermInBinnedNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.delete.DeleteNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.update.UpdateNoteUseCase
import br.com.frazo.janac.ui.mediator.ContentDisplayMode
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.mediator.UIParticipant
import br.com.frazo.janac.ui.util.TextResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BinScreenViewModel @Inject constructor(
    private val getBinnedNotesUseCase: GetBinnedNotesUseCase<Flow<List<Note>>>,
    private val deleteNoteUseCase: DeleteNoteUseCase<Int>,
    private val updateNoteUseCase: UpdateNoteUseCase<Int>,
    private val mediator: UIMediator,
    private val searchTermInBinnedNoteUseCase: SearchTermInBinnedNoteUseCase,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    sealed class ScreenState {
        object Loading : ScreenState()
        data class Success(val data: List<Note>) : ScreenState()
        object NoData : ScreenState()
        object NoDataForFilter : ScreenState()
        data class Error(val throwable: Throwable) : ScreenState()
    }

    private val uiParticipantRepresentative = object : UIParticipant {}

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes = _notes.asStateFlow()

    private val _filter = MutableStateFlow("")
    val filter = _filter.asStateFlow()

    private val _filteredNotes = MutableStateFlow(_notes.value)
    val filteredNotes = _filteredNotes.asStateFlow()

    private val _contentDisplayMode = MutableStateFlow(ContentDisplayMode.AS_LIST)
    val contentDisplayMode = _contentDisplayMode.asStateFlow()

    private val startTime = System.currentTimeMillis()
    private var fetchDataFromRepository = false

    private var _audioNotePlayingData =
        MutableStateFlow(AudioPlayingData(AudioPlayerStatus.NOT_INITIALIZED, 0, 0))
    val audioNotePlayingData = _audioNotePlayingData.asStateFlow()
    private var _audioNotePLaying = MutableStateFlow<Note?>(null)
    val audioNotePlaying = _audioNotePLaying.asStateFlow()

    init {
        mediator.addParticipant(uiParticipantRepresentative)
        viewModelScope.launch {

            startCollectingBinnedNotes()
            startCollectingFilterChange()
            startFilteringOnNotesChange()
            startCollectingFilterMessagesUIEvent()
            startCollectingContentDisplayModeEvents()

        }
    }

    override fun onCleared() {
        mediator.removeParticipant(uiParticipantRepresentative)
        super.onCleared()
    }

    private fun CoroutineScope.startCollectingBinnedNotes() {
        launch {
            getBinnedNotesUseCase()
                .catch {
                    _screenState.value = ScreenState.Error(it)
                    mediator.broadcast(
                        uiParticipantRepresentative,
                        UIEvent.Error(
                            TextResource.StringResource(
                                R.string.generic_error,
                                it.localizedMessage ?: it.message ?: ""
                            ),
                            it
                        )
                    )
                }
                .collectLatest {
                    _notes.value =
                        it.sortedByDescending { note ->
                            note.binnedAt
                        }
                    mediator.broadcast(
                        uiParticipantRepresentative,
                        UIEvent.BinnedNotesFetched(_notes.value)
                    )
                    fetchDataFromRepository = true
                    updateScreenState()
                }
        }
    }

    private fun CoroutineScope.startCollectingFilterChange() {
        launch {
            _filter.collectLatest { query ->
                filterNotes(query)
            }
        }
    }

    private fun CoroutineScope.startFilteringOnNotesChange() {
        launch {
            _notes.collectLatest {
                filterNotes(_filter.value)
            }
        }
    }

    private fun CoroutineScope.startCollectingFilterMessagesUIEvent() {
        launch {
            mediator.broadcastFlowOfEvent(UIEvent.FilterQuery::class).collectLatest { eventPair ->
                eventPair?.let { (_, event) ->
                    _filter.value = (event as UIEvent.FilterQuery).query
                }
            }
        }
    }

    private fun CoroutineScope.startCollectingContentDisplayModeEvents() {
        launch {
            mediator.broadcastFlowOfEvent(UIEvent.ContentDisplayModeChanged::class)
                .collectLatest { eventPair ->
                    eventPair?.let { (_, event) ->
                        _contentDisplayMode.value =
                            (event as UIEvent.ContentDisplayModeChanged).newContentDisplayMode
                    }
                }
        }
    }

    fun clearBin() {
        viewModelScope.launch {
            val notesToDelete = filteredNotes.value
            var notesDeleted = 0
            notesToDelete.forEach { note ->
                notesDeleted += deleteNoteUseCase(note)
            }

            if (notesDeleted < notesToDelete.size)
                mediator.broadcast(
                    uiParticipantRepresentative,
                    event = UIEvent.Error(TextResource.StringResource(R.string.some_notes_not_deleted))
                )
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            val notesDeleted = deleteNoteUseCase(note)
            if (notesDeleted <= 0)
                mediator.broadcast(
                    uiParticipantRepresentative,
                    event = UIEvent.Error(TextResource.StringResource(R.string.note_not_deleted))
                )
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            val notesUpdated = updateNoteUseCase(note, note.copy(binnedAt = null))
            if (notesUpdated <= 0)
                mediator.broadcast(
                    uiParticipantRepresentative,
                    event = UIEvent.Error(TextResource.StringResource(R.string.note_not_restored))
                )
        }
    }

    private fun filterNotes(query: String) {
        if (query.isNotBlank())
            _filteredNotes.value = _notes.value.filter {
                searchTermInBinnedNoteUseCase(it, query)
            }
        else
            _filteredNotes.value = _notes.value
        mediator.broadcast(
            uiParticipantRepresentative,
            UIEvent.BinnedNotesFiltered(_filteredNotes.value)
        )
        updateScreenState()
    }

    private fun updateScreenState() {
        if (_notes.value.isNotEmpty()) {
            if (_filteredNotes.value.isEmpty())
                _screenState.value = ScreenState.NoDataForFilter
            else
                _screenState.value = ScreenState.Success(_filteredNotes.value)
        } else {
            if( System.currentTimeMillis() - startTime >= 3000 || fetchDataFromRepository)
                _screenState.value = ScreenState.NoData
        }
    }

    fun clearFilter() {
        mediator.broadcast(uiParticipantRepresentative, UIEvent.FinishSearchQuery)
        filterNotes("")
    }

    fun playAudioNote(note: Note) {

        if (note != _audioNotePLaying.value) {
            audioPlayer.stop()
            _audioNotePlayingData.value = AudioPlayingData(AudioPlayerStatus.NOT_INITIALIZED, 0, 0)
            _audioNotePLaying.value = note
        }

        if (_audioNotePlayingData.value.status == AudioPlayerStatus.NOT_INITIALIZED) {
            note.audioNote?.let { file ->
                viewModelScope.launch {
                    val flow = audioPlayer.start(file)
                    flow.catch {
                        _screenState.value = ScreenState.Error(it)
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
            resumeAudioNote(note)
        }
    }

    fun pauseAudioNote(note: Note) {
        if(note == _audioNotePLaying.value)
            audioPlayer.pause()
    }

    private fun resumeAudioNote(note: Note) {
        if(note == _audioNotePLaying.value)
            audioPlayer.resume()
    }

    fun seekAudioNote(note: Note, positionPercent: Float) {
        if(note == _audioNotePLaying.value)
            audioPlayer.seek((positionPercent * _audioNotePlayingData.value.duration).toLong())
    }

}