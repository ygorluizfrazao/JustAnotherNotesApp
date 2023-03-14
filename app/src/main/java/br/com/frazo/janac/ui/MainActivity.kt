package br.com.frazo.janac.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.frazo.janac.R
import br.com.frazo.janac.ui.mediator.ContentDisplayMode
import br.com.frazo.janac.ui.navigation.Navigation
import br.com.frazo.janac.ui.navigation.Screen
import br.com.frazo.janac.ui.navigation.getJANAScreenForRoute
import br.com.frazo.janac.ui.theme.NotesAppTheme
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.IconResource
import br.com.frazo.janac.ui.util.composables.MyTextField
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

        val context = LocalContext.current

        val notBinnedNotesCount by viewModel.notBinnedNotesCount.collectAsState()
        val binnedNotesCount by viewModel.binnedNotesCount.collectAsState()

        val filteredNotBinnedNotesCount by viewModel.filteredNotBinnedNotesCount.collectAsState()
        val filteredBinnedNotesCount by viewModel.filteredBinnedNotesCount.collectAsState()

        val searchQuery by viewModel.filterQuery.collectAsState()
        val contentDisplayMode = viewModel.contentDisplayMode.collectAsState()

        val contentDisplayModeIconResource by remember {
            derivedStateOf {
                when (contentDisplayMode.value) {
                    ContentDisplayMode.AS_LIST -> IconResource.fromImageVector(
                        Icons.Default.ViewList,
                        context.getString(R.string.view_content_as_list)
                    )
                    ContentDisplayMode.AS_STAGGERED_GRID -> IconResource.fromImageVector(
                        Icons.Default.TableChart,
                        context.getString(R.string.view_content_as_grid)
                    )
                }
            }
        }

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        var toggleSearchBar by rememberSaveable {
            mutableStateOf(false)
        }

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
                navController.currentDestination?.route?.let {
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    navController.currentDestination.getJANAScreenForRoute()?.localizedAlias?.asString()
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

                                AnimatedVisibility(visible = !toggleSearchBar) {
                                    IconButton(onClick = {
                                        viewModel.filter("")
                                        toggleSearchBar = !toggleSearchBar
                                    }) {

                                        IconResource.fromImageVector(
                                            Icons.Default.Search,
                                            stringResource(R.string.search)
                                        ).ComposeIcon()

                                    }
                                }

                                IconButton(onClick = {
                                    viewModel.changeContentDisplayMode()
                                }) {
                                    contentDisplayModeIconResource.ComposeIcon()
                                }

                                IconButton(onClick = { finish() }) {
                                    IconResource.fromImageVector(
                                        Icons.Default.ExitToApp,
                                        stringResource(R.string.exit)
                                    ).ComposeIcon()
                                }

                            }
                        )
                }
            },
            bottomBar = {
                NavigationBar {
                    BottomNavigationItems(
                        currentDestination = currentDestination,
                        navController = navController,
                        notBinnedNotes = notBinnedNotesCount,
                        filteredNotBinnedNotes = filteredNotBinnedNotesCount,
                        binnedNotes = binnedNotesCount,
                        filteredBinnedNotes = filteredBinnedNotesCount
                    )
                }
            }
        )
        { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Top
            ) {
                AnimatedVisibility(visible = toggleSearchBar) {

                    val focusRequester = remember { FocusRequester() }

                    MyTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .focusRequester(focusRequester),
                        singleLine = true,
                        value = searchQuery,
                        label = stringResource(id = R.string.search),
                        hint = stringResource(R.string.type_your_search_here),
                        onValueChange = viewModel::filter,
                        trailingIcon = {
                            IconButton(onClick = {
                                viewModel.resetSearchQuery()
                                toggleSearchBar = !toggleSearchBar
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ExpandLess,
                                    contentDescription = stringResource(R.string.search)
                                )
                            }
                        }
                    )

                    LaunchedEffect(key1 = Unit) {
                        focusRequester.requestFocus()
                    }
                }

                Navigation(
                    modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
                    navController = navController
                )
            }
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
        filteredNotBinnedNotes: Int,
        binnedNotes: Int,
        filteredBinnedNotes: Int
    ) {

        val context = LocalContext.current

        NavigationBarItem(
            icon = {
                if (notBinnedNotes > 0) {
                    BadgedBox(badge = {

                        val badgeInfo =
                            if (filteredNotBinnedNotes == Int.MIN_VALUE || filteredNotBinnedNotes == notBinnedNotes)
                                notBinnedNotes.toString()
                            else
                                "$filteredNotBinnedNotes/$notBinnedNotes"

                        Badge {
                            Text(text = badgeInfo)
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
            label = { Text(Screen.NotesList.localizedAlias.asString()) },
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

                        val badgeInfo =
                            if (filteredBinnedNotes == Int.MIN_VALUE || filteredBinnedNotes == binnedNotes)
                                binnedNotes.toString()
                            else
                                "$filteredBinnedNotes/$binnedNotes"

                        Badge {
                            Text(text = badgeInfo)
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
            label = { Text(Screen.Bin.localizedAlias.asString()) },
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


