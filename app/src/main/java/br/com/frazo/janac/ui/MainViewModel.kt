package br.com.frazo.janac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.notes.read.GetBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetNotBinnedNotesUseCase
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.mediator.UIParticipant
import br.com.frazo.janac.ui.util.TextResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    mediator: UIMediator,
    private val getBinnedNotesUseCase: GetBinnedNotesUseCase<Flow<List<Note>>>,
    private val getNotBinnedNotesUseCase: GetNotBinnedNotesUseCase<Flow<List<Note>>>,
) : ViewModel(), UIParticipant {

    private val _notBinnedNotesCount = MutableStateFlow(0)
    val notBinnedNotesCount = _notBinnedNotesCount.asStateFlow()

    private val _binnedNotesCount = MutableStateFlow(0)
    val binnedNotesCount = _binnedNotesCount.asStateFlow()

    private val _errorMessage = MutableSharedFlow<TextResource>()
    val errorMessage = _errorMessage.asSharedFlow()

    init {
        mediator.addParticipant(this)
        viewModelScope.launch {
            combine(getNotBinnedNotesUseCase(), getBinnedNotesUseCase()) { notBinned, binned ->
                _notBinnedNotesCount.value = notBinned.size
                _binnedNotesCount.value = binned.size
                notBinned.size + binned.size
            }.collectLatest {}
        }
    }

    override fun receiveMessage(from: UIParticipant, event: UIEvent) {
        if (event is UIEvent.Error) {
            if (!_errorMessage.tryEmit(event.message)) {
                viewModelScope.launch {
                    _errorMessage.emit(event.message)
                }
            }
        }
    }

    fun simulateError(){
        val randomString = TextResource.RuntimeString(getRandomString(20))
        if (!_errorMessage.tryEmit(randomString)) {
            viewModelScope.launch {
                _errorMessage.emit(randomString)
            }
        }
    }

    private fun getRandomString(length: Int) : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

}