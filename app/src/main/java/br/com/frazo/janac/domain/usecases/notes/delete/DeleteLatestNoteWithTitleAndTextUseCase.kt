package br.com.frazo.janac.domain.usecases.notes.delete

import br.com.frazo.janac.data.repository.note.NoteRepository
import br.com.frazo.janac.domain.models.Note

class DeleteLatestNoteWithTitleAndTextUseCase(private val noteRepository: NoteRepository) :
    DeleteNoteUseCase<Int> {

    override suspend fun invoke(vararg notes: Note): Int {
        var removed = 0
        notes.forEach {
            removed += noteRepository.removeLatestByTitleAndText(it.title, it.text)
        }
        return removed
    }
}