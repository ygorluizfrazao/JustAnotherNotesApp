package br.com.frazo.janac.domain.usecases.notes.delete

import br.com.frazo.janac.domain.models.Note

interface DeleteNoteUseCase<T> {

    suspend operator fun invoke(vararg notes: Note): T
}