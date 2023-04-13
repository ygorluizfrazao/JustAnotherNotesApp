package br.com.frazo.janac.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.notes.read.GetBinnedNotesUseCase
import br.com.frazo.janac.domain.usecases.notes.read.GetNotBinnedNotesUseCase
import br.com.frazo.janac.ui.mediator.*
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

    sealed class SnackBarData(
        val message: TextResource,
        val action: Pair<TextResource, () -> Unit>? = null
    ) {
        class Error(
            message: TextResource,
            val throwable: Throwable? = null,
            action: Pair<TextResource, () -> Unit>? = null
        ) : SnackBarData(message, action)

        class Message(
            message: TextResource,
            action: Pair<TextResource, () -> Unit>? = null
        ) : SnackBarData(message, action)
    }

    private val uiParticipantRepresentative = CallBackUIParticipant { sender, event ->
        handleMediatorMessage(sender, event)
    }

    private val _notBinnedNotesCount = MutableStateFlow(0)
    val notBinnedNotesCount = _notBinnedNotesCount.asStateFlow()

    private val _binnedNotesCount = MutableStateFlow(0)
    val binnedNotesCount = _binnedNotesCount.asStateFlow()

    private val _filteredNotBinnedNotesCount = MutableStateFlow(Int.MIN_VALUE)
    val filteredNotBinnedNotesCount = _filteredNotBinnedNotesCount.asStateFlow()

    private val _filteredBinnedNotesCount = MutableStateFlow(Int.MIN_VALUE)
    val filteredBinnedNotesCount = _filteredBinnedNotesCount.asStateFlow()

    private val _snackBarData = MutableSharedFlow<SnackBarData>()
    val snackBarData = _snackBarData.asSharedFlow()

    private val _filterQuery = MutableStateFlow("")
    val filterQuery = _filterQuery.asStateFlow()

    private val _contentDisplayMode = MutableStateFlow(ContentDisplayMode.AS_LIST)
    val contentDisplayMode = _contentDisplayMode.asStateFlow()

    init {
        mediator.addParticipant(uiParticipantRepresentative)

        viewModelScope.launch {
            combine(getNotBinnedNotesUseCase(), getBinnedNotesUseCase()) { notBinned, binned ->
                _notBinnedNotesCount.value = notBinned.size
                _binnedNotesCount.value = binned.size
            }.collectLatest {}
        }
    }

    override fun onCleared() {
        super.onCleared()
        mediator.removeParticipant(uiParticipantRepresentative)
    }

    private fun handleMediatorMessage(sender: UIParticipant, event: UIEvent) {
        when (event) {
            is UIEvent.Error -> emitMessage(SnackBarData.Error(event.message, event.throwable))
            is UIEvent.FilterQuery -> _filterQuery.value = event.query
            is UIEvent.FinishSearchQuery -> resetSearchQuery()
            is UIEvent.BinnedNotesFiltered -> _filteredBinnedNotesCount.value =
                event.filteredNotes.size
            is UIEvent.NotBinnedNotesFiltered -> _filteredNotBinnedNotesCount.value =
                event.filteredNotes.size
            is UIEvent.NoteCreated -> emitMessage(
                SnackBarData.Message(
                    message = TextResource.StringResource(
                        R.string.note_created
                    ),
                    action = Pair(TextResource.StringResource(R.string.undo)) {
                        mediator.inform(uiParticipantRepresentative, UIEvent.Rollback(event), listOf(sender))
                    }
                )
            )
            is UIEvent.NoteEdited -> emitMessage(
                SnackBarData.Message(
                    message = TextResource.StringResource(
                        R.string.note_edited
                    ),
                    action = Pair(TextResource.StringResource(R.string.undo)) {
                        mediator.inform(uiParticipantRepresentative, UIEvent.Rollback(event), listOf(sender))
                    }
                )
            )
            else -> Unit
        }
    }

    private fun emitMessage(snackBarData: SnackBarData) {
        if (!_snackBarData.tryEmit(snackBarData)) {
            viewModelScope.launch {
                _snackBarData.emit(snackBarData)
            }
        }
    }

    fun resetSearchQuery() {
        filter("")
    }

    fun filter(query: String) {
        _filterQuery.value = query
        mediator.broadcast(uiParticipantRepresentative, UIEvent.FilterQuery(query))
    }

    fun changeContentDisplayMode(displayMode: ContentDisplayMode? = null) {
        displayMode?.let {

            _contentDisplayMode.value = displayMode
            mediator.broadcast(
                uiParticipantRepresentative,
                UIEvent.ContentDisplayModeChanged(displayMode)
            )
            return
        }

        if ((contentDisplayMode.value.ordinal + 1) >= ContentDisplayMode.values().size) {
            changeContentDisplayMode(ContentDisplayMode.values()[0])
        } else {
            changeContentDisplayMode(ContentDisplayMode.values()[contentDisplayMode.value.ordinal + 1])
        }
    }

}