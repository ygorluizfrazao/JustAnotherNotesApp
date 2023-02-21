package br.com.frazo.jana_c.data.repository.note

import br.com.frazo.jana_c.domain.models.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {

    fun getAllNotes(): Flow<List<Note>>

    fun getBinnedNotes(): Flow<List<Note>>

    fun getNotBinnedNotes(): Flow<List<Note>>

    suspend fun addNotes(vararg notes: Note): Int

    suspend fun removeNotes(vararg notes: Note): Int

    suspend fun editNote(oldNote: Note, newNote: Note): Int

    suspend fun binNote(note: Note): Int
}