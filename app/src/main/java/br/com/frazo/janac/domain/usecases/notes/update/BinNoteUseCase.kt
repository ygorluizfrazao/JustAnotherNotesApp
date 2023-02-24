package br.com.frazo.janac.domain.usecases.notes.update

import br.com.frazo.janac.domain.models.Note

interface BinNoteUseCase<T> {

    suspend operator fun invoke(note: Note): T
}