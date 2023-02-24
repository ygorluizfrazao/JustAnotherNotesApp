package br.com.frazo.janac.domain.usecases.notes.read

import br.com.frazo.janac.data.repository.note.NoteRepository
import br.com.frazo.janac.domain.models.Note
import kotlinx.coroutines.flow.Flow


class GetNotesUseCaseImpl(private val notesRepository: NoteRepository) :
    GetNotesUseCase<Flow<List<Note>>> {

    override fun invoke(): Flow<List<Note>> {
        return notesRepository.getAllNotes()
    }

}