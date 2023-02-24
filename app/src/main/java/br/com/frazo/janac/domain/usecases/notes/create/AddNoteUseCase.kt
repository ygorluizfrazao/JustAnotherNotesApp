package br.com.frazo.janac.domain.usecases.notes.create

import br.com.frazo.janac.domain.models.Note

interface AddNoteUseCase<T> {

    suspend operator fun invoke(vararg notes: Note): T
}