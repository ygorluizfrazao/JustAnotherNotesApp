package br.com.frazo.jana_c.data.repository.note

import br.com.frazo.jana_c.domain.models.Note
import kotlinx.coroutines.flow.Flow

interface NoteDataSource {

    fun getAll() : Flow<List<Note>>

    fun getBinnedNotes() : Flow<List<Note>>

    fun getNotBinnedNotes(): Flow<List<Note>>

    suspend fun insertAll(vararg notes: Note)

    suspend fun deleteAll(vararg notes: Note): Int

}