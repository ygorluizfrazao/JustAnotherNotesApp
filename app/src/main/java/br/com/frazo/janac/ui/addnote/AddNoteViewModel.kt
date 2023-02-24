package br.com.frazo.janac.ui.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.notes.NoteValidator
import br.com.frazo.janac.domain.usecases.notes.NoteValidatorUseCase
import br.com.frazo.janac.domain.usecases.notes.create.AddNoteUseCase
import br.com.frazo.janac.ui.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddNoteViewModel @Inject constructor(
    private val addNoteUseCase: AddNoteUseCase<Int>,
    private val noteValidatorUseCase: NoteValidatorUseCase
) :
    ViewModel() {

    sealed class UIState {
        object Saved : UIState()
        data class SaveFailed(val throwable: Throwable) : UIState()
        object Editing :
            UIState()

        data class EditingError(
            val titleInvalidError: UiText = UiText.RuntimeString(""),
            val textInvalidError: UiText = UiText.RuntimeString("")
        ) : UIState()

        data class Error(val throwable: Throwable) : UIState()
    }


    private var _note = MutableStateFlow(Note("", ""))
    val note = _note.asStateFlow()

    private var _uiState = MutableStateFlow<UIState>(UIState.Editing)
    val uiState = _uiState.asStateFlow()

    fun onTitleChanged(newTitle: String) {
        _note.value = _note.value.copy(title = newTitle)
        when (val validationResult = noteValidatorUseCase(note.value)) {
            is NoteValidator.NoteValidatorResult.Valid -> _uiState.value = UIState.Editing
            is NoteValidator.NoteValidatorResult.InvalidLength -> {
                val minLength = validationResult.minLength
                when (validationResult.field) {
                    Note::title.name -> _uiState.value = UIState.EditingError(
                        titleInvalidError = UiText.StringResource(
                            R.string.invalid_title,
                            minLength
                        )
                    )
                    Note::text.name -> _uiState.value = UIState.EditingError(
                        titleInvalidError = UiText.StringResource(
                            R.string.invalid_text,
                            minLength
                        )
                    )
                    else -> UIState.EditingError()
                }
            }
            is NoteValidator.NoteValidatorResult.Error -> _uiState.value =
                UIState.Error(validationResult.throwable)
        }
    }

    fun onTextChanged(newText: String) {
        _note.value = _note.value.copy(text = newText)
        when (val validationResult = noteValidatorUseCase(note.value)) {
            is NoteValidator.NoteValidatorResult.Valid -> _uiState.value = UIState.Editing
            is NoteValidator.NoteValidatorResult.InvalidLength -> {
                val minLength = validationResult.minLength
                when (validationResult.field) {
                    Note::title.name -> _uiState.value = UIState.EditingError(
                        titleInvalidError = UiText.StringResource(
                            R.string.invalid_title,
                            minLength
                        )
                    )
                    Note::text.name -> _uiState.value = UIState.EditingError(
                        titleInvalidError = UiText.StringResource(
                            R.string.invalid_text,
                            minLength
                        )
                    )
                    else -> UIState.EditingError()
                }
            }
            is NoteValidator.NoteValidatorResult.Error -> _uiState.value =
                UIState.Error(validationResult.throwable)
        }
    }

    fun save() {
        viewModelScope.launch {
            val result = addNoteUseCase(note.value)
            if (result > 0)
                _uiState.value = UIState.Saved
            else
                _uiState.value = UIState.SaveFailed(Throwable("Save Failed"))
        }
    }
}
