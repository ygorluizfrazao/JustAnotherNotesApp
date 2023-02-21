package br.com.frazo.jana_c.data.repository.note.mappers

import br.com.frazo.jana_c.data.db.room.entities.RoomNote
import br.com.frazo.jana_c.domain.models.Note

fun RoomNote.toNote(): Note{
    return Note(
        title = title,
        text = text,
        createdAt = createdAt
    )
}

fun Note.toRoomNote(): RoomNote{
    return RoomNote(
        id = 0,
        title = title,
        text = text
    )
}