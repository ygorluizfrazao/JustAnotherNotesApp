package br.com.frazo.janac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.notes.read.GetBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetNotBinnedNotesUseCase
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.mediator.UIParticipant
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
//        when (event) {
//            is UIEvent.NotBinnedNotesFetched -> {
//                _notBinnedNotesCount.value = event.notes.size
//            }
//            is UIEvent.BinnedNotesFetched -> {
//                _binnedNotesCount.value = event.notes.size
//            }
//            else -> Unit
//        }
    }

}