package br.com.frazo.janac.domain.usecases

import br.com.frazo.janac.domain.models.Note

interface SearchTermInNotBinnedNoteUseCase {

    operator fun invoke(note: Note, term: String): Boolean
}