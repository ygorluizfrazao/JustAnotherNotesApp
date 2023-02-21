package br.com.frazo.jana_c.data.db.room.entities

import androidx.room.ColumnInfo
import java.time.OffsetDateTime

abstract class RoomEntity {
    @ColumnInfo(name = "created_at")
    var createdAt: OffsetDateTime = OffsetDateTime.now()

    @ColumnInfo(name = "modified_at")
    var modifiedAt: OffsetDateTime = OffsetDateTime.now()
}