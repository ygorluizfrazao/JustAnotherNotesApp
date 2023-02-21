package br.com.frazo.jana_c.data.repository.note

import br.com.frazo.jana_c.data.db.room.RoomAppDatabase
import br.com.frazo.jana_c.data.db.room.dao.NotesDAO
import br.com.frazo.jana_c.data.db.room.entities.RoomNote
import br.com.frazo.jana_c.data.repository.note.mappers.toNote
import br.com.frazo.jana_c.data.repository.note.mappers.toRoomNote
import br.com.frazo.jana_c.domain.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map

class RoomNoteDataSource(database: RoomAppDatabase) : NoteDataSource {

    private val notesDAO: NotesDAO = database.notesDAO()

    override fun getAll(): Flow<List<Note>> {
        return notesDAO.getAll().map { list ->
            list.map { it.toNote() }
        }
    }

    override fun getBinnedNotes(): Flow<List<Note>> {
        return notesDAO.getBinnedNotes().map { list ->
            list.map { it.toNote() }
        }
    }

    override fun getNotBinnedNotes(): Flow<List<Note>> {
        return notesDAO.getNotBinnedNotes().map { list ->
            list.map { it.toNote() }
        }
    }

    override suspend fun insertAll(vararg notes: Note): Int {
        return notesDAO.insertAll(*notes.map { it.toRoomNote() }.toTypedArray()).size
    }

    override suspend fun deleteAll(vararg notes: Note): Int {
        return notesDAO.deleteAll(*notes.map { it.toRoomNote() }.toTypedArray())
    }

    override suspend fun updateNote(oldNote: Note, newNote: Note): Int {
        var updatesMade = 0
        val foundNotes = notesDAO.getByTitleAndText(oldNote.title, oldNote.text).lastOrNull()
        foundNotes?.let { roomNotes ->
            roomNotes.forEach {
                val newRoomNote = RoomNote(
                    it.id,
                    newNote.title,
                    newNote.text,
                    it.binnedAt
                )
                updatesMade += notesDAO.updateNote(newRoomNote)
            }
        }
        return updatesMade
    }

}