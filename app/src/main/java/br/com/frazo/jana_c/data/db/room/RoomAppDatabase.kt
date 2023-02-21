package br.com.frazo.jana_c.data.db.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import br.com.frazo.jana_c.data.db.room.converters.Converters
import br.com.frazo.jana_c.data.db.room.dao.NotesDAO
import br.com.frazo.jana_c.data.db.room.entities.RoomNote


@Database(
    entities = [RoomNote::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class RoomAppDatabase : RoomDatabase() {

    abstract fun notesDAO(): NotesDAO

    companion object {

        @Volatile
        private var INSTANCE: RoomAppDatabase? = null

        fun getDataBase(context: Context): RoomAppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RoomAppDatabase::class.java,
                    "jana_c_db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}