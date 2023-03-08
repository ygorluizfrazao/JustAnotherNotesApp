package br.com.frazo.janac.di

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import br.com.frazo.janac.data.db.room.RoomAppDatabase
import br.com.frazo.janac.data.repository.note.CachedNoteRepository
import br.com.frazo.janac.data.repository.note.NoteDataSource
import br.com.frazo.janac.data.repository.note.NoteRepository
import br.com.frazo.janac.data.repository.note.RoomNoteDataSource
import br.com.frazo.janac.data.repository.note.cachestrategy.CacheStrategy
import br.com.frazo.janac.data.repository.note.cachestrategy.InMemoryCacheStrategy
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.domain.usecases.DataTransformerUseCase
import br.com.frazo.janac.domain.usecases.TrimmedUppercaseDataTransformerUseCase
import br.com.frazo.janac.domain.usecases.notes.LengthNoteValidator
import br.com.frazo.janac.domain.usecases.notes.NoteValidatorUseCase
import br.com.frazo.janac.domain.usecases.notes.NoteValidatorUseCaseImpl
import br.com.frazo.janac.domain.usecases.notes.create.AddNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.create.AddNoteUseCaseImpl
import br.com.frazo.janac.domain.usecases.notes.delete.DeleteNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.delete.DeleteNoteUseCaseImpl
import br.com.frazo.janac.domain.usecases.notes.read.*
import br.com.frazo.janac.domain.usecases.notes.update.BinNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.update.BinNoteUseCaseImpl
import br.com.frazo.janac.domain.usecases.notes.update.UpdateNoteUseCase
import br.com.frazo.janac.domain.usecases.notes.update.UpdateNoteUseCaseImpl
import br.com.frazo.janac.ui.mediator.UIMediator
import br.com.frazo.janac.ui.mediator.UIMediatorImpl
import br.com.frazo.janac.util.Dispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
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

    @Provides
    @Singleton
    fun provideNoteDataSource(roomAppDatabase: RoomAppDatabase): NoteDataSource {
        return RoomNoteDataSource(roomAppDatabase)
    }

    @Provides
    @Singleton
    fun provideCacheStrategy(): CacheStrategy<Note> {
        return InMemoryCacheStrategy()
    }

    @Provides
    @Singleton
    fun provideDispatchers(): Dispatchers {
        return Dispatchers(
            io = kotlinx.coroutines.Dispatchers.IO,
            default = kotlinx.coroutines.Dispatchers.Default,
            main = kotlinx.coroutines.Dispatchers.Main,
            unconfined = kotlinx.coroutines.Dispatchers.Unconfined
        )
    }

    @Provides
    @Singleton
    fun provideNoteRepository(
        noteDataSource: NoteDataSource,
        cacheStrategy: CacheStrategy<Note>,
        dispatchers: Dispatchers
    ): NoteRepository {
        return CachedNoteRepository(noteDataSource, cacheStrategy, dispatchers)
    }

    @Provides
    @Singleton
    fun provideGetNotesUseCase(noteRepository: NoteRepository): GetNotesUseCase<Flow<List<Note>>> {
        return GetNotesUseCaseImpl(noteRepository)
    }

    @Provides
    @Singleton
    fun provideGetBinnedNotesUseCase(noteRepository: NoteRepository): GetBinnedNotesUseCase<Flow<List<Note>>> {
        return GetBinnedNotesUseCaseImpl(noteRepository)
    }

    @Provides
    @Singleton
    fun provideGetNotBinnedNotesUseCase(noteRepository: NoteRepository): GetNotBinnedNotesUseCase<Flow<List<Note>>> {
        return GetNotBinnedNotesUseCaseImpl(noteRepository)
    }

    @Provides
    @Singleton
    fun provideAddNoteUseCase(noteRepository: NoteRepository): AddNoteUseCase<Int> {
        return AddNoteUseCaseImpl(noteRepository)
    }

    @Provides
    @Singleton
    fun provideEditNoteUseCase(noteRepository: NoteRepository): UpdateNoteUseCase<Int> {
        return UpdateNoteUseCaseImpl(noteRepository)
    }

    @Provides
    @Singleton
    fun provideRemoveNoteUseCase(noteRepository: NoteRepository): DeleteNoteUseCase<Int> {
        return DeleteNoteUseCaseImpl(noteRepository)
    }

    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext appContext: Context): SharedPreferences {
        return EncryptedSharedPreferences.create(
            "secure_shared_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            appContext,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    @Singleton
    fun provideNoteValidatorUseCase(): NoteValidatorUseCase {
        return NoteValidatorUseCaseImpl(LengthNoteValidator())
    }

    @Provides
    @Singleton
    fun provideUIMediator(): UIMediator {
        return UIMediatorImpl()
    }

    @Provides
    @Singleton
    fun provideBinNoteUseCase(noteRepository: NoteRepository): BinNoteUseCase<Int> {
        return BinNoteUseCaseImpl(noteRepository)
    }

    @Provides
    @Singleton
    fun provideDataTransformerUseCase(): DataTransformerUseCase<String> {
        return TrimmedUppercaseDataTransformerUseCase()
    }
}