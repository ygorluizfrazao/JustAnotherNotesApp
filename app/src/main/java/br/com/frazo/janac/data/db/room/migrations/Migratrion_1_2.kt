package br.com.frazo.janac.data.db.room.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Suppress("ClassName")
object Migratrion_1_2: Migration(1,2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE `notes` ADD `audio_note` TEXT DEFAULT NULL")
    }
}