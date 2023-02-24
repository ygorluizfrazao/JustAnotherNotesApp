package br.com.frazo.janac.domain.usecases.notes.delete

import br.com.frazo.janac.domain.models.Note

interface RemoveNoteUseCase<T> {

    suspend operator fun invoke(vararg note: Note): T
}