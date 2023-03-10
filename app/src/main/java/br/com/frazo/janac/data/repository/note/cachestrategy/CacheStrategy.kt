package br.com.frazo.janac.data.repository.note.cachestrategy

interface CacheStrategy<D> {

    fun cache(vararg data: D)

    fun retrieveCache(): List<D>

    fun invalidateCache()

}