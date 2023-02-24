package br.com.frazo.janac.domain.usecases.notes.update

import br.com.frazo.janac.domain.models.Note

interface EditNoteUseCase<T> {

    suspend operator fun invoke(oldNote: Note, newNote: Note): T
}