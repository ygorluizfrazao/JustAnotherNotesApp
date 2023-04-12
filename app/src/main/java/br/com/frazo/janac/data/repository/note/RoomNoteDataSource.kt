package br.com.frazo.janac.data.repository.note

import br.com.frazo.janac.data.db.room.RoomAppDatabase
import br.com.frazo.janac.data.db.room.dao.NotesDAO
import br.com.frazo.janac.data.db.room.entities.RoomNote
import br.com.frazo.janac.data.repository.note.mappers.toNote
import br.com.frazo.janac.data.repository.note.mappers.toRoomNote
import br.com.frazo.janac.domain.models.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File

class RoomNoteDataSource(database: RoomAppDatabase, private val audioNotesDir: File) : NoteDataSource {

    private val notesDAO: NotesDAO = database.notesDAO()

    override fun getAll(): Flow<List<Note>> {
        return notesDAO.getAll().map { list ->
            list.map { it.toNote(audioNotesDir) }
        }
    }

    override fun getBinnedNotes(): Flow<List<Note>> {
        return notesDAO.getBinnedNotes().map { list ->
            list.map { it.toNote(audioNotesDir) }
        }
    }

    override fun getNotBinnedNotes(): Flow<List<Note>> {
        return notesDAO.getNotBinnedNotes().map { list ->
            list.map { it.toNote(audioNotesDir) }
        }
    }

    override suspend fun getNotesByTitleAndText(title: String, text: String): List<Note> {
        return notesDAO.getByTitleAndText(title,text).map { it.toNote(audioNotesDir) }
    }

    override suspend fun insertAll(vararg notes: Note): Int {
        return notesDAO.insertAll(*notes.map { it.toRoomNote() }.toTypedArray()).size
    }

    override suspend fun deleteAll(vararg notes: Note): Int {

        var itemsDeleted = 0
        notes.map { it.toRoomNote() }.forEach {
            val toDeleteNotes = notesDAO.getByCreationDate(it.createdAt)
            itemsDeleted += notesDAO.deleteAll(*toDeleteNotes.toTypedArray())
        }

        return itemsDeleted
    }

    override suspend fun updateNote(oldNote: Note, newNote: Note): Int {
        if (oldNote.createdAt == null) throw Exception("Creation date cannot be null.")
        var updatesMade = 0
        val foundNotes = notesDAO.getByCreationDate(oldNote.createdAt)
        foundNotes.let { roomNotes ->
            roomNotes.forEach {
                updatesMade += notesDAO.updateNote(newNote.toRoomNote(it.id))
            }
        }
        return updatesMade
    }
}