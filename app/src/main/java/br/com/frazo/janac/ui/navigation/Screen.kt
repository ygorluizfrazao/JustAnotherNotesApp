package br.com.frazo.janac.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import br.com.frazo.janac.R
import br.com.frazo.janac.ui.util.IconResource
import br.com.frazo.janac.ui.util.TextResource

private val possibleDestinations = listOf(Screen.NotesList, Screen.Bin)

sealed class Screen(
    val route: TextResource,
    val localizedAlias: TextResource,
    val icon: IconResource
) {
    object NotesList : Screen(
        TextResource.StringResource(R.string.nav_notes_list),
        TextResource.StringResource(R.string.notes_list),
        IconResource.fromImageVector(Icons.Filled.List)
    )

    object Bin :
        Screen(
            TextResource.StringResource(R.string.nav_notes_bin),
            TextResource.StringResource(R.string.bin),
            IconResource.fromImageVector(Icons.Filled.Delete)
        )
}

@Composable
fun NavDestination?.getJANAScreenForRoute(): Screen? {
    this?.route?.let { route ->
        return possibleDestinations.firstOrNull { route == it.route.asString() }
    }
    return null
}