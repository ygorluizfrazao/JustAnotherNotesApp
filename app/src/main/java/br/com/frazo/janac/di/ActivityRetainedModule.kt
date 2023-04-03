package br.com.frazo.janac.di

import android.content.Context
import br.com.frazo.janac.audio.player.AndroidAudioPlayer
import br.com.frazo.janac.audio.player.AudioPlayer
import br.com.frazo.janac.audio.recorder.AndroidAudioRecorder
import br.com.frazo.janac.audio.recorder.AudioRecorder
import br.com.frazo.janac.util.Dispatchers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
object ActivityRetainedModule {

    @Provides
    @ActivityRetainedScoped
    fun provideAudioRecorder(
        @ApplicationContext appContext: Context,
        dispatchers: Dispatchers
    ): AudioRecorder {
        return AndroidAudioRecorder(appContext, dispatchers.default)
    }

    @Provides
    @ActivityRetainedScoped
    fun provideAudioPlayer(
        @ApplicationContext appContext: Context,
        dispatchers: Dispatchers
    ): AudioPlayer {
        return AndroidAudioPlayer(appContext, dispatcher = dispatchers.default)
    }
}