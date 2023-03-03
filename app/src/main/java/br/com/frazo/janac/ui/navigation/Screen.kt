package br.com.frazo.janac.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import br.com.frazo.janac.R
import br.com.frazo.janac.ui.util.IconResource
import br.com.frazo.janac.ui.util.TextResource

sealed class Screen(
    val route: TextResource,
    val icon: IconResource
) {
    object NotesList : Screen(
        TextResource.StringResource(R.string.notes_list),
        IconResource.fromImageVector(Icons.Filled.List)
    )

    object Bin :
        Screen(
            TextResource.StringResource(R.string.bin),
            IconResource.fromImageVector(Icons.Filled.Delete)
        )
}
