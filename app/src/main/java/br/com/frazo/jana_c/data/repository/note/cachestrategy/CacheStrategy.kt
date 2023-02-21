package br.com.frazo.jana_c.data.repository.note.cachestrategy

import br.com.frazo.jana_c.domain.models.Note

interface CacheStrategy {

    fun cache(vararg notes: Note)

    fun retrieveCache(): List<Note>

    fun invalidateCache()
}