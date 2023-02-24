package br.com.frazo.janac.ui.noteslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.notes.read.GetNotBinnedNotesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesListViewModel @Inject constructor(
    getNotBinnedNotesUseCase: GetNotBinnedNotesUseCase<Flow<List<Note>>>
) : ViewModel() {

    sealed class ScreenState {
        object Loading : ScreenState()
        data class Success(val data: List<Note>) : ScreenState()
        object NoData : ScreenState()
        data class Error(val throwable: Throwable) : ScreenState()
    }

    private val _notes = MutableStateFlow(emptyList<Note>())
    val notes = _notes.asStateFlow()

    private val _screenState: MutableStateFlow<ScreenState> = MutableStateFlow(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            getNotBinnedNotesUseCase()
                .catch {
                    _screenState.value = ScreenState.Error(it)
                }
                .collectLatest {
                    _notes.value = it
                    if (it.isEmpty())
                        _screenState.value = ScreenState.NoData
                    else
                        _screenState.value = ScreenState.Success(it)
                }
        }
    }
}