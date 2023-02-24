package br.com.frazo.janac.domain.usecases.notes

import br.com.frazo.janac.domain.models.Note

class LengthNoteValidator(
    private val titleMinLength: Int = 1,
    private val textMinLength: Int = 0
) : NoteValidator {

    override fun validate(note: Note): NoteValidator.NoteValidatorResult {
        try {
            if (note.title.length < titleMinLength)
                return NoteValidator.NoteValidatorResult.InvalidLength(
                    Note::title.name,
                    titleMinLength
                )
            if (note.text.length < textMinLength)
                return NoteValidator.NoteValidatorResult.InvalidLength(
                    Note::text.name,
                    textMinLength
                )
            return NoteValidator.NoteValidatorResult.Valid
        } catch (e: java.lang.Exception) {
            return NoteValidator.NoteValidatorResult.Error(e)
        }
    }
}