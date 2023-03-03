package br.com.frazo.janac.ui.screens.notes

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.notes.read.GetNotBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.update.BinNoteUseCase
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.mediator.UIParticipant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(
    private val getNotBinnedNotesUseCase: GetNotBinnedNotesUseCase<Flow<List<Note>>>,
    private val binNoteUseCase: BinNoteUseCase<Int>,
    private val mediator: UIMediator
) : ViewModel(), UIParticipant {

    sealed class ScreenState {
        object Loading : ScreenState()
        data class Success(val data: List<Note>) : ScreenState()
        object NoData : ScreenState()
        data class Error(val throwable: Throwable) : ScreenState()
    }

    data class EditNoteState(val requested: Boolean, val baseNote: Note = Note("",""))


    private val _notes = MutableStateFlow(emptyList<Note>())
    val notes = _notes.asStateFlow()

    private val _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    private val _addButtonExtended = MutableStateFlow(true)
    val addButtonExtended = _addButtonExtended.asStateFlow()

    private val _editNoteState = MutableStateFlow(EditNoteState(false))
    val addEditNoteState = _editNoteState.asStateFlow()


    init {
        mediator.addParticipant(this)
        viewModelScope.launch {
            getNotBinnedNotesUseCase()
                .catch {
                    _screenState.value = ScreenState.Error(it)
                }
                .collectLatest {
                    _notes.value = it.sortedByDescending { note ->
                        note.createdAt
                    }
                    mediator.broadCast(this@NotesListViewModel, UIEvent.NotBinnedNotesFetched(it))
                    if (it.isEmpty())
                        _screenState.value = ScreenState.NoData
                    else
                        _screenState.value = ScreenState.Success(it)
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

//    override fun receiveMessage(from: UIParticipant, event: UIEvent) {
//        if (event is UIEvent.NoteCreated) {
//            editNoteClear()
//        }
//    }

}