package br.com.frazo.janac.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.frazo.janac.R
import br.com.frazo.janac.ui.navigation.Navigation
import br.com.frazo.janac.ui.navigation.Screen
import br.com.frazo.janac.ui.theme.NotesAppTheme
import br.com.frazo.janac.ui.theme.spacing
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesAppTheme {

                val navController = rememberNavController()
                val viewModel = hiltViewModel<MainViewModel>()

                Screen(
                    navController = navController,
                    navStarDestination = Screen.NotesList,
                    viewModel = viewModel
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Screen(
        navController: NavHostController,
        navStarDestination: Screen,
        viewModel: MainViewModel
    ) {

        val notBinnedCount by viewModel.notBinnedNotesCount.collectAsState()
        val binnedNotesCount by viewModel.binnedNotesCount.collectAsState()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val context = LocalContext.current

        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        modifier = Modifier
                            .padding(MaterialTheme.spacing.medium),
                        action = {
                            TextButton(
                                onClick = { data.dismiss() },
                            ) { Text(data.visuals.actionLabel ?: "") }
                        }
                    ) {
                        Text(data.visuals.message)
                    }
                }
            },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            navController.currentDestination?.route
                                ?: navStarDestination.route.asString(),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        if (currentDestination?.route != navStarDestination.route.asString()) {
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
                        IconButton(onClick = {
                            viewModel.simulateError()
                        }) {
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
                        notBinnedNotes = notBinnedCount,
                        binnedNotes = binnedNotesCount
                    )
                }
            }
        )
        { innerPadding ->
            Navigation(modifier = Modifier.padding(innerPadding), navController = navController)
        }

        LaunchedEffect(key1 = Unit) {

            viewModel.errorMessage.collect { errorMessage ->
                scope.launch {
                    snackbarHostState.showSnackbar(
                        errorMessage.asString(context),
                        actionLabel = context.getString(R.string.uppercase_dismiss),
                        duration = SnackbarDuration.Short
                    )
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

        val context = LocalContext.current
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
            label = { Text(Screen.NotesList.route.asString()) },
            selected =
            currentDestination?.hierarchy?.any {
                it.route == Screen.NotesList.route.asString()
            } == true,
            onClick = {
                navController.navigate(Screen.NotesList.route.asString(context = context)) {
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
            label = { Text(Screen.Bin.route.asString()) },
            selected =
            currentDestination?.hierarchy?.any {
                it.route == Screen.Bin.route.asString()
            } == true,
            onClick = {
                navController.navigate(Screen.Bin.route.asString(context)) {
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


