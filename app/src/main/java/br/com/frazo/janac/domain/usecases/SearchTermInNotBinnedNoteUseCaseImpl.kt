package br.com.frazo.janac.domain.usecases

import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.util.DateTimeFormatterFactory

class SearchTermInNotBinnedNoteUseCaseImpl(private val dateTimeFormatterFactory: DateTimeFormatterFactory) :
    SearchTermInNotBinnedNoteUseCase {
    override fun invoke(note: Note, term: String): Boolean {
        return (note.title.uppercase().contains(term.trim().uppercase())
                || note.text.uppercase().contains(term.trim().uppercase())
                || note.createdAt?.format(dateTimeFormatterFactory.datePattern())?.uppercase()
            ?.contains(term) ?: false)
    }
}