package br.com.frazo.janac.data.db.room

import android.content.Context
import androidx.room.*
import br.com.frazo.janac.data.db.room.converters.Converters
import br.com.frazo.janac.data.db.room.dao.NotesDAO
import br.com.frazo.janac.data.db.room.entities.RoomNote
import br.com.frazo.janac.data.db.room.migrations.Migratrion_1_2


@Database(
    entities = [RoomNote::class],
    version = 2
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
                ).addMigrations(Migratrion_1_2)
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}