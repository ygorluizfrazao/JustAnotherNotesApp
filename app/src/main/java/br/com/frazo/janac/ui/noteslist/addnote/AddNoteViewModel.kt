package br.com.frazo.janac.ui.noteslist.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.notes.NoteValidator
import br.com.frazo.janac.domain.usecases.notes.NoteValidatorUseCase
import br.com.frazo.janac.domain.usecases.notes.create.AddNoteUseCase
import br.com.frazo.janac.ui.mediator.UIEvent
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.mediator.UIParticipant
import br.com.frazo.janac.ui.util.TextResource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val addNoteUseCase: AddNoteUseCase<Int>,
    private val noteValidatorUseCase: NoteValidatorUseCase,
    private val mediator: UIMediator
) :
    ViewModel(), UIParticipant {

    sealed class UIState {
        object Saved : UIState()
        data class SaveFailed(val throwable: Throwable) : UIState()
        object Editing :
            UIState()

        data class TitleError(
            val titleInvalidError: TextResource = TextResource.RuntimeString(""),
        ) : UIState()

        data class TextError(
            val textInvalidError: TextResource = TextResource.RuntimeString(""),
        ) : UIState()

        data class Error(val throwable: Throwable) : UIState()

        object CanSave: UIState()
    }

    private var _note = MutableStateFlow(Note("", ""))
    val note = _note.asStateFlow()

    private var _uiState = MutableStateFlow<UIState>(UIState.Editing)
    val uiState = _uiState.asStateFlow()


    init {
        mediator.addParticipant(this)
    }

    private fun reset() {
        _note.value = Note("", "")
        _uiState.value = UIState.Editing
    }

    fun onTitleChanged(newTitle: String) {
        _note.value = _note.value.copy(title = newTitle)
        _uiState.value = UIState.Editing
        validate(_note.value, Note::title.name)
    }

    fun onTextChanged(newText: String) {
        _note.value = _note.value.copy(text = newText)
        _uiState.value = UIState.Editing
        validate(_note.value, Note::text.name)
    }

    private fun validateCanSave(){
        val validationResult = noteValidatorUseCase(_note.value,null)
        if (validationResult is NoteValidator.NoteValidatorResult.Valid)
            _uiState.value = UIState.CanSave
    }

    private fun validate(note: Note, field: String){

        when (val validationResult = noteValidatorUseCase(note,field)) {
            is NoteValidator.NoteValidatorResult.Valid -> _uiState.value = UIState.Editing
            is NoteValidator.NoteValidatorResult.InvalidLength -> {
                val minLength = validationResult.minLength
                when (validationResult.field) {
                    Note::title.name ->
                        _uiState.value = UIState.TitleError(
                        titleInvalidError = TextResource.StringResource(
                            R.string.invalid_title,
                            "at least $minLength characters expected."
                        )
                    )
                    Note::text.name -> _uiState.value = UIState.TextError(
                        textInvalidError = TextResource.StringResource(
                            R.string.invalid_text,
                            "at least $minLength characters expected."
                        )
                    )
                    else -> Unit
                }
            }
            is NoteValidator.NoteValidatorResult.Error -> _uiState.value =
                UIState.Error(validationResult.throwable)
        }
        validateCanSave()
    }

    fun save() {
        viewModelScope.launch {
            val result = addNoteUseCase(note.value)
            if (result > 0) {
                _uiState.value = UIState.Saved
                mediator.broadCast(this@AddNoteViewModel, UIEvent.NoteAdded)
                reset()
            } else {
                _uiState.value = UIState.SaveFailed(Throwable("Save Failed"))
            }
        }
    }

    fun cancel() {
        reset()
    }

    override fun receiveMessage(from: UIParticipant, event: UIEvent) {
    }
}
