package br.com.frazo.jana_c.data.db.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.OffsetDateTime

@Entity(tableName = "notes")
data class RoomNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "text") val text: String = "",
    @ColumnInfo(name = "binned_at") val binnedAt: OffsetDateTime? = null
): RoomEntity()