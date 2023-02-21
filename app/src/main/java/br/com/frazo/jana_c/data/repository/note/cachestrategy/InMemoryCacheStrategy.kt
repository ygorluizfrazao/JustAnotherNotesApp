package br.com.frazo.jana_c.data.repository.note.cachestrategy

import br.com.frazo.jana_c.domain.models.Note

class InMemoryCacheStrategy : CacheStrategy {

    private val cachedNotes = mutableListOf<Note>()

    override fun cache(vararg notes: Note) {
        cachedNotes.addAll(notes)
    }

    override fun retrieveCache(): List<Note> {
        return cachedNotes
    }

    override fun invalidateCache() {
        cachedNotes.clear()
    }

}