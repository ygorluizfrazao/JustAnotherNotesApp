package br.com.frazo.janac.data.db.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.File
import java.time.OffsetDateTime

@Entity(tableName = "notes")
data class RoomNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "title") val title: String = "",
    @ColumnInfo(name = "text") val text: String = "",
    @ColumnInfo(name = "audio_note") val audioNote: String? = null,
    @ColumnInfo(name = "binned_at") val binnedAt: OffsetDateTime? = null
): RoomEntity()