package br.com.frazo.janac.data.repository.note.cachestrategy

import br.com.frazo.janac.domain.models.Note

interface CacheStrategy<D> {

    fun cache(vararg data: D)

    fun retrieveCache(): List<D>

    fun invalidateCache()
}