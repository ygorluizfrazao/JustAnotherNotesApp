package br.com.frazo.janac.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.R
import br.com.frazo.janac.audio.player.AudioPlayer
import br.com.frazo.janac.audio.player.AudioPlayerStatus
import br.com.frazo.janac.audio.player.AudioPlayingData
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.SearchTermInNotBinnedNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetNotBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.update.BinNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.update.UpdateNoteUseCase
import br.com.frazo.janac.ui.mediator.CallBackUIParticipant
import br.com.frazo.janac.ui.mediator.ContentDisplayMode
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.util.TextResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val getNotBinnedNotesUseCase: GetNotBinnedNotesUseCase<Flow<List<Note>>>,
    private val binNoteUseCase: BinNoteUseCase<Int>,
    private val updateNoteUseCase: UpdateNoteUseCase<Int>,
    private val mediator: UIMediator,
    private val searchTermInNotBinnedNoteUseCase: SearchTermInNotBinnedNoteUseCase,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    sealed class ScreenState {
        object Loading : ScreenState()
        data class Success(val data: List<Note>) : ScreenState()
        object NoData : ScreenState()
        object NoDataForFilter : ScreenState()
        data class Error(val throwable: Throwable) : ScreenState()
    }

    data class EditNoteState(val requested: Boolean, val baseNote: Note = Note("", ""))

    private val uiParticipantRepresentative = CallBackUIParticipant { _, event ->
        handleMediatorMessage(event)
    }

    private val _notes = MutableStateFlow(emptyList<Note>())
    val notes = _notes.asStateFlow()

    private val _filter = MutableStateFlow("")
    val filter = _filter.asStateFlow()

    private val _filteredNotes = MutableStateFlow(_notes.value)
    val filteredNotes = _filteredNotes.asStateFlow()


    private var _showNote = MutableSharedFlow<Int>()
    val showNote = _showNote.asSharedFlow()

    private val _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    private val _editNoteState = MutableStateFlow(EditNoteState(false))
    val editNoteState = _editNoteState.asStateFlow()

    private val _contentDisplayMode = MutableStateFlow(ContentDisplayMode.AS_LIST)
    val contentDisplayMode = _contentDisplayMode.asStateFlow()

    private val startTime = System.currentTimeMillis()
    private var fetchDataFromRepository = false

    private var _audioNotePlayingData =
        MutableStateFlow(AudioPlayingData(AudioPlayerStatus.NOT_INITIALIZED, 0, 0))
    val audioNotePlayingData = _audioNotePlayingData.asStateFlow()
    private var _audioNotePLaying = MutableStateFlow<Note?>(null)
    val audioNotePlaying = _audioNotePLaying.asStateFlow()

    var focusListOnNote: Note? = null

    init {
        mediator.addParticipant(uiParticipantRepresentative)

        viewModelScope.launch {

            startCollectingNotes()
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

    private fun CoroutineScope.startCollectingNotes() {
        launch {
            getNotBinnedNotesUseCase()
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
                    fetchDataFromRepository = true
                    _notes.value =
                        it.sortedByDescending { note ->
                            note.createdAt
                        }
                    mediator.broadcast(
                        uiParticipantRepresentative,
                        UIEvent.NotBinnedNotesFetched(_notes.value)
                    )
                    updateScreenState()
                }
        }
    }

    private fun CoroutineScope.startCollectingFilterChange() {
        launch {
            filter.collectLatest { query ->
                filterNotes(query)
            }
        }
    }

    private fun CoroutineScope.startFilteringOnNotesChange() {
        launch {
            notes.collectLatest {
                filterNotes(filter.value)
            }
        }
    }

    private fun CoroutineScope.startCollectingFilterMessagesUIEvent() {
        launch {
            mediator.broadcastFlowOfEvent(UIEvent.FilterQuery::class).collectLatest { eventPair ->
                eventPair?.let { (_, event) ->
                    val query = (event as UIEvent.FilterQuery).query
                    if (_filter.value != query)
                        _filter.value = query
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

    private fun handleMediatorMessage(event: UIEvent) {

        when (event) {
            is UIEvent.NoteCreated -> emitShowFirstNote()
            is UIEvent.Rollback -> {
                when (event.originalEvent) {
                    is UIEvent.NoteBinned -> {
                        viewModelScope.launch {
                            updateNoteUseCase(
                                event.originalEvent.binnedNote,
                                event.originalEvent.binnedNote.copy(binnedAt = null)
                            )
                            focusListOnNote =
                                event.originalEvent.binnedNote.copy(binnedAt = null)
                        }
                    }
                    else -> Unit
                }
            }
            else -> Unit
        }

    }

    private fun emitShowFirstNote() {
        if (!_showNote.tryEmit(0)) {
            viewModelScope.launch {
                _showNote.emit(0)
            }
        }
    }

    private fun emitShowFirstNote(note: Note) {
        val noteIdx = _filteredNotes.value.indexOf(note)
        if (noteIdx >= 0) {
            if (!_showNote.tryEmit(noteIdx)) {
                viewModelScope.launch {
                    _showNote.emit(noteIdx)
                }
            }
        }
    }

    fun editNewNote() {
        _editNoteState.value = EditNoteState(true)
    }

    fun editNoteClear() {
        _editNoteState.value = EditNoteState(false)
    }

    fun editNote(note: Note) {
        _editNoteState.value = EditNoteState(true, note)
    }

    fun binNote(note: Note) {
        if (note == _audioNotePLaying.value) resetAudioPlayer()
        viewModelScope.launch {
            binNoteUseCase(note)
            mediator.broadcast(uiParticipantRepresentative, UIEvent.NoteBinned(note))
        }
    }

    private fun filterNotes(query: String) {
        if (query.isNotBlank())
            _filteredNotes.value = _notes.value.filter {
                searchTermInNotBinnedNoteUseCase(it, query)
            }
        else
            _filteredNotes.value = _notes.value

        if (!_filteredNotes.value.contains(_audioNotePLaying.value)) {
            resetAudioPlayer()
        }

        focusListOnNote?.let {
            emitShowFirstNote(it)
            focusListOnNote = null
        }
        mediator.broadcast(
            uiParticipantRepresentative,
            UIEvent.NotBinnedNotesFiltered(_filteredNotes.value)
        )
        updateScreenState()
    }

    private fun resetAudioPlayer() {
        audioPlayer.stop()
        _audioNotePlayingData.value =
            AudioPlayingData(AudioPlayerStatus.NOT_INITIALIZED, 0, 0)
        _audioNotePLaying.value = null
    }

    private fun updateScreenState() {
        if (_notes.value.isNotEmpty()) {
            if (filteredNotes.value.isEmpty())
                _screenState.value = ScreenState.NoDataForFilter
            else
                _screenState.value = ScreenState.Success(filteredNotes.value)
        } else {
            if (System.currentTimeMillis() - startTime >= 3000 || fetchDataFromRepository)
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
        if (note == _audioNotePLaying.value)
            audioPlayer.pause()
    }

    private fun resumeAudioNote(note: Note) {
        if (note == _audioNotePLaying.value)
            audioPlayer.resume()
    }

    fun seekAudioNote(note: Note, positionPercent: Float) {
        if (note == _audioNotePLaying.value)
            audioPlayer.seek((positionPercent * _audioNotePlayingData.value.duration).toLong())
    }

}