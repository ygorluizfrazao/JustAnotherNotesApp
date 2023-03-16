package br.com.frazo.janac.di.assisted

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.screens.notes.editnote.EditNoteViewModel
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface ViewModelFactoryProvider {
    fun editNoteViewFactory(): EditNoteViewModel.Factory
}

@Composable
inline fun <reified T : ViewModel, D> assistedViewModel(data: D): T {

    val factoryProvider = EntryPointAccessors.fromActivity(
        LocalContext.current as Activity,
        ViewModelFactoryProvider::class.java
    )

    return when (T::class) {
        EditNoteViewModel::class -> {
            val factory = factoryProvider.editNoteViewFactory()
            viewModel(
                factory = EditNoteViewModel.provideEditNoteViewModelFactory(
                    factory,
                    data as Note
                )
            )
        }
        else -> throw ClassNotFoundException("There is no assisted ViewModel class ${T::class}")
    }
}