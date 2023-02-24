package br.com.frazo.janac.domain.usecases.notes.update

import br.com.frazo.janac.data.repository.note.NoteRepository
import br.com.frazo.janac.domain.models.Note

class BinNoteUseCaseImpl(private val notesRepository: NoteRepository) : BinNoteUseCase<Int> {
    override suspend fun invoke(note: Note): Int {
        return notesRepository.binNote(note)
    }
}