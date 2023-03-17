package br.com.frazo.janac.di

import br.com.frazo.janac.ui.util.permissions.StateBasedPermissionAdapter
import br.com.frazo.janac.ui.util.permissions.providers.PermissionProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Named

@Module
@InstallIn(ViewModelComponent::class)
object MainViewModelModule {

    @Provides
    @ViewModelScoped
    fun provideRecordAudioStateBasedPermissionAdapter(
        @Named("RecordAudioPermissionProvider") recordAudioPermissionProvider: PermissionProvider<String>
    ): List<StateBasedPermissionAdapter> {
        return listOf(StateBasedPermissionAdapter(recordAudioPermissionProvider))
    }
}