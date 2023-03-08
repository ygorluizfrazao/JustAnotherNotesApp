package br.com.frazo.janac.ui.screens.notes

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.DataTransformerUseCase
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
    private val filterDataTransformerUseCase: DataTransformerUseCase<String>
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

    private val _addButtonExtended = MutableStateFlow(true)
    val addButtonExtended = _addButtonExtended.asStateFlow()

    private val _editNoteState = MutableStateFlow(EditNoteState(false))
    val addEditNoteState = _editNoteState.asStateFlow()


    init {
        mediator.addParticipant(uiParticipantRepresentative)

        viewModelScope.launch {

            startCollectingNotes(this)
            startCollectingFilterChange(this)
            startFilteringOnNotesChange(this)

        }
    }

    private suspend fun startCollectingNotes(scope: CoroutineScope) {
        scope.launch {
            getNotBinnedNotesUseCase()
                .catch {
                    _screenState.value = ScreenState.Error(it)
                    mediator.broadCast(
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
                    mediator.broadCast(
                        uiParticipantRepresentative,
                        UIEvent.NotBinnedNotesFetched(_notes.value)
                    )
                }
        }
    }

    private suspend fun startCollectingFilterChange(scope: CoroutineScope) {
        scope.launch {
            filter.collectLatest { query ->
                filterNotes(query)
            }
        }
    }

    private suspend fun startFilteringOnNotesChange(scope: CoroutineScope) {
        scope.launch {
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

    fun onListState(listState: LazyListState?) {
        _addButtonExtended.value = listState == null || listState.firstVisibleItemIndex == 0
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
                filterDataTransformerUseCase(it.toString()).contains(
                    filterDataTransformerUseCase(
                        query
                    )
                )
            }
        else
            _filteredNotes.value = _notes.value
        updateState()
    }

    private fun updateState() {
        if (_notes.value.isNotEmpty()) {
            if (_filteredNotes.value.isEmpty())
                _screenState.value = ScreenState.NoDataForFilter
            else
                _screenState.value = ScreenState.Success(_filteredNotes.value)
        } else {
            ScreenState.NoData
        }
    }

    fun clearFilter() {
        mediator.broadCast(uiParticipantRepresentative,UIEvent.FinishSearchQuery)
        filterNotes("")
    }

}