package br.com.frazo.janac.data.repository.note.mappers

import br.com.frazo.janac.data.db.room.entities.RoomNote
import br.com.frazo.janac.domain.models.Note

fun RoomNote.toNote(): Note {
    return Note(
        title = title,
        text = text,
        createdAt = createdAt,
        binnedAt = binnedAt
    )
}

fun Note.toRoomNote(): RoomNote {
    return RoomNote(
        id = 0,
        title = title,
        text = text,
        binnedAt = binnedAt
    ).also { roomNote ->
        this.createdAt?.let {
            roomNote.createdAt = createdAt
        }
    }
}