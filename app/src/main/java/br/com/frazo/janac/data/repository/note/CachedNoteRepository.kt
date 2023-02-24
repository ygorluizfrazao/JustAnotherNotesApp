package br.com.frazo.janac.data.repository.note

import br.com.frazo.janac.data.repository.note.cachestrategy.CacheStrategy
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.util.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import java.time.OffsetDateTime

class CachedNoteRepository(
    private val noteDataSource: NoteDataSource,
    private val cacheStrategy: CacheStrategy<Note>,
    dispatchers: Dispatchers
) : NoteRepository {

//    private val cacheFlow = channelFlow {
//        noteDataSource.getAll().collectLatest {
//            cacheStrategy.invalidateCache()
//            cacheStrategy.cache(*it.toTypedArray())
//            this.send(cacheStrategy.retrieveCache())
//        }
//    }.flowOn(dispatchers.io)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val cacheFlow = noteDataSource.getAll()
        .mapLatest {
            cacheStrategy.invalidateCache()
            cacheStrategy.cache(*it.toTypedArray())
            cacheStrategy.retrieveCache()
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
        return noteDataSource.insertAll(*notes)
    }

    override suspend fun removeNotes(vararg notes: Note): Int {
        return noteDataSource.deleteAll(*notes)
    }

    override suspend fun editNote(oldNote: Note, newNote: Note): Int {
        return noteDataSource.updateNote(oldNote, newNote)
    }

    override suspend fun binNote(note: Note): Int {
        return noteDataSource.updateNote(note, note.copy(binnedAt = OffsetDateTime.now()))
    }

}