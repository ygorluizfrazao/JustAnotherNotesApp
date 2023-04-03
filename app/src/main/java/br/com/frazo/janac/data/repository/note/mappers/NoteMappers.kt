package br.com.frazo.janac.data.repository.note.mappers

import br.com.frazo.janac.data.db.room.entities.RoomNote
import br.com.frazo.janac.domain.models.Note
import java.io.File

fun RoomNote.toNote(audioNoteDir: File? = null): Note {
    return Note(
        title = title,
        text = text,
        audioNote = if (this.audioNote != null && audioNoteDir != null) File(
            audioNoteDir,
            this.audioNote
        ) else null,
        createdAt = createdAt,
        binnedAt = binnedAt
    )
}

fun Note.toRoomNote(id: Int = 0): RoomNote {
    return RoomNote(
        id = id,
        title = title,
        text = text,
        audioNote = audioNote?.name,
        binnedAt = binnedAt
    ).also { roomNote ->
        this.createdAt?.let {
            roomNote.createdAt = createdAt
        }
    }
}