package br.com.frazo.janac.domain.usecases.notes.update

import br.com.frazo.janac.domain.models.Note

interface UpdateNoteUseCase<T> {

    suspend operator fun invoke(oldNote: Note, newNote: Note): T
}