package br.com.frazo.jana_c.di

import android.content.Context
import br.com.frazo.jana_c.data.db.room.RoomAppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRoomAppDatabase(
        @ApplicationContext appContext: Context
    ): RoomAppDatabase {
        return RoomAppDatabase.getDataBase(appContext)
    }
}