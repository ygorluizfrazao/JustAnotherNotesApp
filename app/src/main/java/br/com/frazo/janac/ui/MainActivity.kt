package br.com.frazo.janac.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.frazo.janac.R
import br.com.frazo.janac.ui.noteslist.NotesListScreen
import br.com.frazo.janac.ui.theme.NotesAppTheme
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.IconResource
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    sealed class Screen(
        val route: String,
        val startDestination: Boolean,
        @StringRes val resourceId: Int,
        val icon: IconResource
    ) {
        object NotesList : Screen(
            "Notes List",
            true,
            R.string.notes_list,
            IconResource.fromImageVector(Icons.Filled.List)
        )

        object Bin :
            Screen(
                "Bin",
                false,
                R.string.bin,
                IconResource.fromImageVector(Icons.Filled.Delete)
            )

    }

    private val navigationItems = listOf(
        Screen.NotesList,
        Screen.Bin
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesAppTheme {

                val navController = rememberNavController()
                val startDestination by remember {
                    derivedStateOf {
                        navigationItems.find { it.startDestination } ?: navigationItems.first()
                    }
                }

                val viewModel = hiltViewModel<MainViewModel>()
                val notBinnedCount by viewModel.notBinnedNotesCount.collectAsState()
                val binnedNotesCount by viewModel.binnedNotesCount.collectAsState()

                Screen(
                    navController = navController,
                    startDestination = startDestination,
                    notBinnedNotes = notBinnedCount,
                    binnedNotes = binnedNotesCount
                )

            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
        startDestination: Screen,
        notBinnedNotes: Int,
        binnedNotes: Int
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        var currentScreen by remember {
            mutableStateOf(startDestination)
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            stringResource(id = currentScreen.resourceId),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        if (currentDestination?.route != startDestination.route) {
                            IconButton(onClick = {
                                navController.popBackStack()
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = ""
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = { }) {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = ""
                            )
                        }
                        IconButton(onClick = { finish() }) {
                            Icon(
                                imageVector = Icons.Filled.ExitToApp,
                                contentDescription = ""
                            )
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar {
                    BottomNavigationItems(
                        currentDestination = currentDestination,
                        navController = navController,
                        notBinnedNotes = notBinnedNotes,
                        binnedNotes = binnedNotes
                    )
                }
            }
        )
        { innerPadding ->
            NavHost(
                navController,
                startDestination = startDestination.route,
                Modifier.padding(innerPadding)
            ) {
                currentScreen = navigationItems.find {
                    it.route == currentDestination?.route
                } ?: startDestination
                composable(Screen.NotesList.route) {
                    NotesListScreen(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = MaterialTheme.spacing.medium)
                    )
                }
                composable(Screen.Bin.route) {

                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun RowScope.BottomNavigationItems(
        currentDestination: NavDestination?,
        navController: NavHostController,
        notBinnedNotes: Int,
        binnedNotes: Int
    ) {
        NavigationBarItem(
            icon = {
                if (notBinnedNotes > 0) {
                    BadgedBox(badge = {
                        Badge {
                            Text(text = notBinnedNotes.toString())
                        }
                    }) {
                        Icon(
                            Screen.NotesList.icon.asPainterResource(),
                            contentDescription = Screen.NotesList.icon.contentDescription
                        )
                    }
                } else {
                    Icon(
                        Screen.NotesList.icon.asPainterResource(),
                        contentDescription = Screen.NotesList.icon.contentDescription
                    )
                }
            },
            label = { Text(stringResource(Screen.NotesList.resourceId)) },
            selected =
            currentDestination?.hierarchy?.any { it.route == Screen.NotesList.route } == true,
            onClick = {
                navController.navigate(Screen.NotesList.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )

        NavigationBarItem(
            icon = {
                if (binnedNotes > 0) {
                    BadgedBox(badge = {
                        Badge {
                            Text(text = binnedNotes.toString())
                        }
                    }) {
                        Icon(
                            Screen.Bin.icon.asPainterResource(),
                            contentDescription = Screen.Bin.icon.contentDescription
                        )
                    }
                } else {
                    Icon(
                        Screen.Bin.icon.asPainterResource(),
                        contentDescription = Screen.Bin.icon.contentDescription
                    )
                }
            },
            label = { Text(stringResource(Screen.Bin.resourceId)) },
            selected =
            currentDestination?.hierarchy?.any { it.route == Screen.Bin.route } == true,
            onClick = {
                navController.navigate(Screen.Bin.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }
}


