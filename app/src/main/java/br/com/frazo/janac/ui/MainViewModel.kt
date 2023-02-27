package br.com.frazo.janac.ui

import androidx.lifecycle.ViewModel
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.mediator.UIParticipant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    mediator: UIMediator
) : ViewModel(), UIParticipant {

    private val _notBinnedNotesCount = MutableStateFlow(0)
    val notBinnedNotesCount = _notBinnedNotesCount.asStateFlow()

    private val _binnedNotesCount = MutableStateFlow(0)
    val binnedNotesCount = _binnedNotesCount.asStateFlow()

    init {
        mediator.addParticipant(this)
    }

    override fun receiveMessage(from: UIParticipant, event: UIEvent) {
        when (event) {
            is UIEvent.NotBinnedNotesFetched -> {
                _notBinnedNotesCount.value = event.notes.size
            }
            is UIEvent.BinnedNotesFetched -> {
                _binnedNotesCount.value = event.notes.size
            }
            else -> Unit
        }
    }

}