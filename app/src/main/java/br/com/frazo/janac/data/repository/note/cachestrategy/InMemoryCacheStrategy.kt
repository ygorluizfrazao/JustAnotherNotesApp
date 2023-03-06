package br.com.frazo.janac.data.repository.note.cachestrategy

import br.com.frazo.janac.domain.models.Note
import java.util.Collections

class InMemoryCacheStrategy : CacheStrategy<Note> {

    private val cachedNotes = Collections.synchronizedList(mutableListOf<Note>())

    override fun cache(vararg data: Note) {
        cachedNotes.addAll(data)
    }

    override fun retrieveCache(): List<Note> {
        return cachedNotes
    }

    override fun invalidateCache() {
        cachedNotes.clear()
    }

}