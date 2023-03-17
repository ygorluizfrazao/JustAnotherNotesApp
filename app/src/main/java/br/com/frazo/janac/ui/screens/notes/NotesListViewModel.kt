package br.com.frazo.janac.ui.screens.notes

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.SearchTermInNotBinnedNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetNotBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.update.BinNoteUseCase
import br.com.frazo.janac.ui.mediator.*
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
    private val mediator: UIMediator,
    private val searchTermInNotBinnedNoteUseCase: SearchTermInNotBinnedNoteUseCase
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


    private var _showFirstNote = MutableSharedFlow<Boolean>()
    val showFirstNote = _showFirstNote.asSharedFlow()

    private val _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    private val _editNoteState = MutableStateFlow(EditNoteState(false))
    val editNoteState = _editNoteState.asStateFlow()

    private val _contentDisplayMode = MutableStateFlow(ContentDisplayMode.AS_LIST)
    val contentDisplayMode = _contentDisplayMode.asStateFlow()

    private val startTime = System.currentTimeMillis()
    private var fetchDataFromRepository = false

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
        super.onCleared()
        mediator.removeParticipant(uiParticipantRepresentative)
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
                    if(_filter.value != query)
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

            else -> Unit
        }

    }

    private fun emitShowFirstNote() {
        if (!_showFirstNote.tryEmit(true)) {
            viewModelScope.launch {
                _showFirstNote.emit(true)
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
        viewModelScope.launch {
            binNoteUseCase(note)
        }
    }

    fun shareNote(note: Note): Intent {
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "${note.title}\n${note.text}")
            type = "text/plain"
        }
        return Intent.createChooser(sendIntent, null)
    }

    private fun filterNotes(query: String) {
        if (query.isNotBlank())
            _filteredNotes.value = _notes.value.filter {
                searchTermInNotBinnedNoteUseCase(it, query)
            }
        else
            _filteredNotes.value = _notes.value
        mediator.broadcast(
            uiParticipantRepresentative,
            UIEvent.NotBinnedNotesFiltered(_filteredNotes.value)
        )
        updateScreenState()
    }

    private fun updateScreenState() {
        if (_notes.value.isNotEmpty()) {
            if (filteredNotes.value.isEmpty())
                _screenState.value = ScreenState.NoDataForFilter
            else
                _screenState.value = ScreenState.Success(filteredNotes.value)
        } else {
            if( System.currentTimeMillis() - startTime >= 3000  || fetchDataFromRepository)
                _screenState.value = ScreenState.NoData
        }
    }

    fun clearFilter() {
        mediator.broadcast(uiParticipantRepresentative, UIEvent.FinishSearchQuery)
        filterNotes("")
    }

}