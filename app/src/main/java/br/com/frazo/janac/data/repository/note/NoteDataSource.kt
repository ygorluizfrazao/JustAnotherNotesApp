package br.com.frazo.janac.data.repository.note

import br.com.frazo.janac.domain.models.Note
import kotlinx.coroutines.flow.Flow

interface NoteDataSource {

    fun getAll() : Flow<List<Note>>

    fun getBinnedNotes() : Flow<List<Note>>

    fun getNotBinnedNotes(): Flow<List<Note>>

    suspend fun insertAll(vararg notes: Note): Int

    suspend fun deleteAll(vararg notes: Note): Int

    suspend fun updateNote(oldNote: Note, newNote: Note): Int

}