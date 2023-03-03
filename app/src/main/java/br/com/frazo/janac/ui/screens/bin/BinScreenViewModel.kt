package br.com.frazo.janac.ui.screens.bin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.notes.read.GetBinnedNotesUseCase
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.mediator.UIParticipant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BinScreenViewModel @Inject constructor(
    val getBinnedNotesUseCase: GetBinnedNotesUseCase<Flow<List<Note>>>,
    val mediator: UIMediator
) : ViewModel(), UIParticipant {

    sealed class ScreenState {
        object Loading : ScreenState()
        data class Success(val data: List<Note>) : ScreenState()
        object NoData : ScreenState()
        data class Error(val throwable: Throwable) : ScreenState()
    }

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes = _notes.asStateFlow()



    init {
        mediator.addParticipant(this)
        viewModelScope.launch {
            getBinnedNotesUseCase()
                .catch {
                    _screenState.value = ScreenState.Error(it)
                }
                .collectLatest {
                    _notes.value = it.sortedByDescending { note ->
                        note.binnedAt
                    }
                    mediator.broadCast(
                        this@BinScreenViewModel,
                        UIEvent.BinnedNotesFetched(it)
                    )
                    if (it.isEmpty())
                        _screenState.value = ScreenState.NoData
                    else
                        _screenState.value = ScreenState.Success(it)
                }
        }
    }
}