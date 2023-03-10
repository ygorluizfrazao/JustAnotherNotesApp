package br.com.frazo.janac.domain.usecases.notes

import br.com.frazo.janac.domain.models.Note

class NoteValidatorUseCaseImpl(
    private vararg val validators: NoteValidator
) : NoteValidatorUseCase {

    override fun invoke(note: Note, field: String?): NoteValidator.NoteValidatorResult {
        validators.forEach {validator ->
            val res = validator.validate(note, field)
            if(res !is NoteValidator.NoteValidatorResult.Valid)
                return res
        }
        return NoteValidator.NoteValidatorResult.Valid
    }

}