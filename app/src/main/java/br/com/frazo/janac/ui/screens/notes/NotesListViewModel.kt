package br.com.frazo.janac.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.SearchTermInNotBinnedNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetNotBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.update.BinNoteUseCase
import br.com.frazo.janac.ui.mediator.CallBackUIParticipant
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
    private val filter = MutableStateFlow("")
    private val _filteredNotes = MutableStateFlow(_notes.value)
    val notes = _filteredNotes.asStateFlow()

    private var _showFirstNote = MutableSharedFlow<Boolean>()
    val showFirstNote = _showFirstNote.asSharedFlow()

    private val _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    private val _editNoteState = MutableStateFlow(EditNoteState(false))
    val editNoteState = _editNoteState.asStateFlow()


    init {
        mediator.addParticipant(uiParticipantRepresentative)

        viewModelScope.launch {

            startCollectingNotes()
            startCollectingFilterChange()
            startFilteringOnNotesChange()

        }
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
                    _notes.value =
                        it.sortedByDescending { note ->
                            note.createdAt
                        }
                    mediator.broadcast(
                        uiParticipantRepresentative,
                        UIEvent.NotBinnedNotesFetched(_notes.value)
                    )
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
            _notes.collectLatest {
                filterNotes(filter.value)
            }
        }
    }

    private fun handleMediatorMessage(event: UIEvent) {

        when (event) {
            is UIEvent.FilterQuery -> filter.value = event.query
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
        updateState()
    }

    private fun updateState() {
        if (_notes.value.isNotEmpty()) {
            if (_filteredNotes.value.isEmpty())
                _screenState.value = ScreenState.NoDataForFilter
            else
                _screenState.value = ScreenState.Success(_filteredNotes.value)
        } else {
            if (_screenState.value != ScreenState.Loading) _screenState.value = ScreenState.NoData
        }
    }

    fun clearFilter() {
        mediator.broadcast(uiParticipantRepresentative, UIEvent.FinishSearchQuery)
        filterNotes("")
    }

}