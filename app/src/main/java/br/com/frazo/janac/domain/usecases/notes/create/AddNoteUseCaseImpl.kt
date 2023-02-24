package br.com.frazo.janac.domain.usecases.notes.create

import br.com.frazo.janac.data.repository.note.NoteRepository
import br.com.frazo.janac.domain.models.Note

class AddNoteUseCaseImpl(private val notesRepository: NoteRepository): AddNoteUseCase<Int> {

    override suspend fun invoke(vararg notes: Note): Int {
        return notesRepository.addNotes(*notes)
    }
}