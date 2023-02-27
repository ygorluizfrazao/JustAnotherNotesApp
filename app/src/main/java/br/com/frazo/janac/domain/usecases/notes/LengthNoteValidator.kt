package br.com.frazo.janac.domain.usecases.notes

import br.com.frazo.janac.domain.models.Note

class LengthNoteValidator(
    private val titleMinLength: Int = 1,
    private val textMinLength: Int = 1
) : NoteValidator {

    override fun validate(note: Note, field: String?): NoteValidator.NoteValidatorResult {
        try {

            if (field.isNullOrBlank()) {
                val titleValidation = validateTitle(note)
                if (titleValidation !is NoteValidator.NoteValidatorResult.Valid)
                    return titleValidation
                return validateText(note)
            }

            if (field == Note::title.name) {
                return validateTitle(note)
            }

            if (field == Note::text.name) {
                return validateText(note)
            }

            return NoteValidator.NoteValidatorResult.Valid
        } catch (e: java.lang.Exception) {
            return NoteValidator.NoteValidatorResult.Error(e)
        }
    }

    private fun validateTitle(note: Note): NoteValidator.NoteValidatorResult {
        if (note.title.length < titleMinLength)
            return NoteValidator.NoteValidatorResult.InvalidLength(
                Note::title.name,
                titleMinLength
            )
        return NoteValidator.NoteValidatorResult.Valid
    }

    private fun validateText(note: Note): NoteValidator.NoteValidatorResult {
        if (note.text.length < textMinLength)
            return NoteValidator.NoteValidatorResult.InvalidLength(
                Note::text.name,
                textMinLength
            )
        return NoteValidator.NoteValidatorResult.Valid
    }

}