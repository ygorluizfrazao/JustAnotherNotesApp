package br.com.frazo.janac.domain.usecases

import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.util.DateTimeFormatterFactory

class SearchTermInBinnedNoteUseCaseImpl(private val dateTimeFormatterFactory: DateTimeFormatterFactory) :
    SearchTermInBinnedNoteUseCase {
    override fun invoke(note: Note, term: String): Boolean {
        return note.title.uppercase().contains(term.trim().uppercase())
                || note.text.uppercase().contains(term.trim().uppercase())
                || note.binnedAt?.format(dateTimeFormatterFactory.datePattern())?.uppercase()
            ?.contains(term) ?: false
    }
}