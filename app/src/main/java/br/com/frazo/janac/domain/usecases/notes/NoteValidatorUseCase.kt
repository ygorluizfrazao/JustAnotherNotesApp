package br.com.frazo.janac.domain.usecases.notes

import br.com.frazo.janac.domain.models.Note

interface NoteValidatorUseCase {

    operator fun invoke(note: Note): NoteValidator.NoteValidatorResult
}