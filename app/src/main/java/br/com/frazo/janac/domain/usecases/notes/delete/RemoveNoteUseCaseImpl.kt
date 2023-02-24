package br.com.frazo.janac.domain.usecases.notes.delete

import br.com.frazo.janac.data.repository.note.NoteRepository
import br.com.frazo.janac.domain.models.Note

class RemoveNoteUseCaseImpl(private val noteRepository: NoteRepository) : RemoveNoteUseCase<Int> {
    override suspend fun invoke(vararg note: Note): Int {
        return noteRepository.removeNotes(*note)
    }
}