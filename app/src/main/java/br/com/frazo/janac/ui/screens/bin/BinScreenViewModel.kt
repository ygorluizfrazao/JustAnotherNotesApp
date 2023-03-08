package br.com.frazo.janac.ui.screens.bin

import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.notes.delete.DeleteNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.update.UpdateNoteUseCase
import br.com.frazo.janac.ui.mediator.CallBackUIParticipant
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.mediator.UIParticipant
import br.com.frazo.janac.ui.util.TextResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BinScreenViewModel @Inject constructor(
    private val getBinnedNotesUseCase: GetBinnedNotesUseCase<Flow<List<Note>>>,
    private val deleteNoteUseCase: DeleteNoteUseCase<Int>,
    private val updateNoteUseCase: UpdateNoteUseCase<Int>,
    private val mediator: UIMediator
) : ViewModel() {

    sealed class ScreenState {
        object Loading : ScreenState()
        data class Success(val data: List<Note>) : ScreenState()
        object NoData : ScreenState()
        data class Error(val throwable: Throwable) : ScreenState()
    }

    private val uiParticipantRepresentative = object : UIParticipant{}

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes = _notes.asStateFlow()

    private val _clearBinButtonExpandedState = MutableStateFlow(true)
    val clearBinButtonExpandedState = _clearBinButtonExpandedState.asStateFlow()

    init {
        mediator.addParticipant(uiParticipantRepresentative)
        viewModelScope.launch {
            getBinnedNotesUseCase()
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
                    _notes.value = it.sortedByDescending { note ->
                        note.binnedAt
                    }
                    mediator.broadCast(
                        uiParticipantRepresentative,
                        UIEvent.BinnedNotesFetched(it)
                    )
                    if (it.isEmpty())
                        _screenState.value = ScreenState.NoData
                    else
                        _screenState.value = ScreenState.Success(it)
                }
        }
    }

    fun clearBin() {
        viewModelScope.launch {
            val notesToDelete = _notes.value
            var notesDeleted = 0
            notesToDelete.forEach { note ->
                notesDeleted += deleteNoteUseCase(note)
            }

            if (notesDeleted < notesToDelete.size)
                mediator.broadCast(
                    uiParticipantRepresentative,
                    event = UIEvent.Error(TextResource.StringResource(R.string.some_notes_not_deleted))
                )
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            val notesDeleted = deleteNoteUseCase(note)
            if (notesDeleted <= 0)
                mediator.broadCast(
                    uiParticipantRepresentative,
                    event = UIEvent.Error(TextResource.StringResource(R.string.note_not_deleted))
                )
        }
    }

    fun restoreNote(note: Note) {
        viewModelScope.launch {
            val notesUpdated = updateNoteUseCase(note, note.copy(binnedAt = null))
            if (notesUpdated <= 0)
                mediator.broadCast(
                    uiParticipantRepresentative,
                    event = UIEvent.Error(TextResource.StringResource(R.string.note_not_restored))
                )
        }
    }

    fun onListState(listState: LazyListState?) {
        _clearBinButtonExpandedState.value =
            listState == null || listState.firstVisibleItemIndex == 0
    }
}