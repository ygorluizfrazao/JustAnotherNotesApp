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
    dispatchers: Dispatchers
) : NoteRepository {

    private val cacheFlow = flow {
        getAllNotes().collectLatest {
            cacheStrategy.invalidateCache()
            cacheStrategy.cache(*it.toTypedArray())
            this.emit(cacheStrategy.retrieveCache())
        }
    }.flowOn(dispatchers.io)

    override fun getAllNotes(): Flow<List<Note>> {
        return cacheFlow
    }

    override fun getBinnedNotes(): Flow<List<Note>> {
        return cacheFlow.map { list ->
            list.filter {
                it.binnedAt != null
            }
        }
    }

    override fun getNotBinnedNotes(): Flow<List<Note>> {
        return cacheFlow.map { list ->
            list.filter {
                it.binnedAt == null
            }
        }
    }

    override suspend fun addNotes(vararg notes: Note): Int {
        return dataSource.insertAll(*notes)
    }

    override suspend fun removeNotes(vararg notes: Note): Int {
        return dataSource.deleteAll(*notes)
    }

    override suspend fun editNote(oldNote: Note, newNote: Note): Int {
        return dataSource.updateNote(oldNote, newNote)
    }

    override suspend fun binNote(note: Note): Int {
        return dataSource.updateNote(note, note.copy(binnedAt = OffsetDateTime.now()))
    }

}