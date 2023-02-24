package br.com.frazo.janac.domain.usecases.notes.update

import br.com.frazo.janac.data.repository.note.NoteRepository
import br.com.frazo.janac.domain.models.Note

class EditNoteUseCaseImpl(private val notesRepository: NoteRepository) : EditNoteUseCase<Int> {
    override suspend fun invoke(oldNote: Note, newNote: Note): Int {
        return notesRepository.editNote(oldNote, newNote)
    }

}