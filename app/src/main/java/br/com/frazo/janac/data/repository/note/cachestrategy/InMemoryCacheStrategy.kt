package br.com.frazo.janac.data.repository.note.cachestrategy

import br.com.frazo.janac.domain.models.Note

class InMemoryCacheStrategy : CacheStrategy<Note> {

    private var cachedNotes = listOf<Note>()

    override fun cache(vararg data: Note) {
        cachedNotes = cachedNotes + data.toList()
    }

    override fun retrieveCache(): List<Note> {
        return cachedNotes
    }

    override fun invalidateCache() {
        cachedNotes = emptyList()
    }

}