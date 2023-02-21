package br.com.frazo.jana_c.data.repository.note

import br.com.frazo.jana_c.data.repository.note.cachestrategy.CacheStrategy
import br.com.frazo.jana_c.domain.models.Note
import br.com.frazo.jana_c.util.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.OffsetDateTime

class CachedNoteRepository(
    private val dataSource: NoteDataSource,
    private val cacheStrategy: CacheStrategy,
    private val dispatchers: Dispatchers
) : NoteRepository {

    override fun getAllNotes(): Flow<List<Note>> {
        val cachedValues = cacheStrategy.retrieveCache()
        if (cachedValues.isNotEmpty())
            return flowOf(cachedValues)
        val newValuesFlow = dataSource.getAll()
        cache(newValuesFlow)
        return newValuesFlow
    }

    override fun getBinnedNotes(): Flow<List<Note>> {
        val cachedValues = cacheStrategy.retrieveCache()
        if (cachedValues.isNotEmpty())
            return flowOf(cachedValues.filter { it.binnedAt != null })
        return getAllNotes().map { list ->
            list.filter { it.binnedAt != null }
        }
    }

    override fun getNotBinnedNotes(): Flow<List<Note>> {
        val cachedValues = cacheStrategy.retrieveCache()
        if (cachedValues.isNotEmpty())
            return flowOf(cachedValues.filter { it.binnedAt == null })
        return getAllNotes().map { list ->
            list.filter { it.binnedAt == null }
        }
    }

    override suspend fun addNotes(vararg notes: Note): Int {
        val inserts = dataSource.insertAll(*notes)
        if (inserts > 0)
            cacheStrategy.invalidateCache()
        return inserts
    }

    override suspend fun removeNotes(vararg notes: Note): Int {
        val deletions = dataSource.deleteAll(*notes)
        if (deletions > 0)
            cacheStrategy.invalidateCache()
        return deletions
    }

    override suspend fun editNote(oldNote: Note, newNote: Note): Int {
        val updates = dataSource.updateNote(oldNote, newNote)
        if (updates > 0)
            cacheStrategy.invalidateCache()
        return updates
    }

    override suspend fun binNote(note: Note): Int {
        val updates = dataSource.updateNote(note, note.copy(binnedAt = OffsetDateTime.now()))
        if (updates > 0)
            cacheStrategy.invalidateCache()
        return updates
    }

    private fun cache(notesFlow: Flow<List<Note>>) {
        CoroutineScope(dispatchers.io).launch {
            notesFlow.lastOrNull()?.let {
                cacheStrategy.cache(*it.toTypedArray())
            }
        }
    }
}