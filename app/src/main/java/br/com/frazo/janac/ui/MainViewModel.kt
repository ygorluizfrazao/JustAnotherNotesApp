package br.com.frazo.janac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.notes.read.GetBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetNotBinnedNotesUseCase
import br.com.frazo.janac.ui.mediator.CallBackUIParticipant
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.util.TextResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val mediator: UIMediator,
    private val getBinnedNotesUseCase: GetBinnedNotesUseCase<Flow<List<Note>>>,
    private val getNotBinnedNotesUseCase: GetNotBinnedNotesUseCase<Flow<List<Note>>>,
) : ViewModel() {

    private val uiParticipantRepresentative = CallBackUIParticipant { _, event ->
        handleMediatorMessage(event)
    }

    private val _notBinnedNotesCount = MutableStateFlow(0)
    val notBinnedNotesCount = _notBinnedNotesCount.asStateFlow()

    private val _binnedNotesCount = MutableStateFlow(0)
    val binnedNotesCount = _binnedNotesCount.asStateFlow()

    private val _errorMessage = MutableSharedFlow<TextResource>()
    val errorMessage = _errorMessage.asSharedFlow()

    private val _toggleFilter = MutableStateFlow(false)
    val toggleFilter = _toggleFilter.asStateFlow()

    private val _filterQuery = MutableStateFlow("")
    val filterQuery = _filterQuery.asStateFlow()

    init {
        mediator.addParticipant(uiParticipantRepresentative)

        viewModelScope.launch {
            combine(getNotBinnedNotesUseCase(), getBinnedNotesUseCase()) { notBinned, binned ->
                _notBinnedNotesCount.value = notBinned.size
                _binnedNotesCount.value = binned.size
            }.collectLatest {}
        }
    }

    private fun handleMediatorMessage(event: UIEvent) {
        when (event) {

            is UIEvent.Error -> emitErrorMessage(event.message)
            is UIEvent.FilterQuery -> _filterQuery.value = event.query
            is UIEvent.FinishSearchQuery -> resetSearchQuery()

            else -> Unit
        }
    }

    private fun emitErrorMessage(message: TextResource) {
        if (!_errorMessage.tryEmit(message)) {
            viewModelScope.launch {
                _errorMessage.emit(message)
            }
        }
    }

    fun resetSearchQuery(){
        _toggleFilter.value = false
        filter("")
    }

    fun toggleSearch() {
        _toggleFilter.value = !_toggleFilter.value
    }

    fun filter(query: String) {
        _filterQuery.value = query
        mediator.broadCast(uiParticipantRepresentative, UIEvent.FilterQuery(query))
    }

}