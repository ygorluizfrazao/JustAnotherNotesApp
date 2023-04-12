package br.com.frazo.janac.data.db.room.dao

import androidx.room.*
import br.com.frazo.janac.data.db.room.entities.RoomNote
import kotlinx.coroutines.flow.Flow
import java.time.OffsetDateTime

@Dao
interface NotesDAO {

    @Query("SELECT * FROM notes")
    fun getAll(): Flow<List<RoomNote>>

    @Query("SELECT * FROM notes WHERE NOT binned_at IS NULL")
    fun getBinnedNotes(): Flow<List<RoomNote>>

    @Query("SELECT * FROM notes WHERE binned_at IS NULL")
    fun getNotBinnedNotes(): Flow<List<RoomNote>>

    @Query("SELECT * FROM notes WHERE created_at = :createdAt")
    suspend fun getByCreationDate(createdAt: OffsetDateTime): List<RoomNote>

    @Query("SELECT * FROM notes WHERE title = :title AND text = :text")
    suspend fun getByTitleAndText(title: String, text: String): List<RoomNote>

    @Update
    fun updateNote(roomNote: RoomNote): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg roomNote: RoomNote): List<Long>

    @Delete
    suspend fun deleteAll(vararg roomNote: RoomNote): Int

    @Query("DELETE FROM notes")
    suspend fun deleteAllNoFilter()

}