package br.com.frazo.jana_c

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.com.frazo.jana_c.ui.IconResource
import br.com.frazo.jana_c.ui.composables.NoteCard
import br.com.frazo.jana_c.ui.theme.NotesAppTheme
import br.com.frazo.jana_c.ui.theme.spacing
import com.example.notesapp.R

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
            Screen("Bin", false, R.string.bin, IconResource.fromImageVector(Icons.Filled.Delete))

//        object AddNote :
//            Screen(
//                "Add Note",
//                false,
//                R.string.add_note,
//                IconResource.fromImageVector(Icons.Filled.Add)
//            )
    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val navigationItems = listOf(
            Screen.NotesList,
            Screen.Bin
        )

        setContent {
            NotesAppTheme {

                val navController = rememberNavController()
                val startDestination by remember {
                    derivedStateOf {
                        navigationItems.find { it.startDestination } ?: navigationItems.first()
                    }
                }
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
                                if (currentDestination?.route != Screen.NotesList.route) {
                                    IconButton(onClick = {
                                        navController.popBackStack()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Filled.ArrowBack,
                                            contentDescription = "Localized description"
                                        )
                                    }
                                }
                            },
                            actions = {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.Filled.ExitToApp,
                                        contentDescription = "Localized description"
                                    )
                                }
                            }
                        )
                    },
                    bottomBar = {
                        NavigationBar {
                            navigationItems.forEach { screen ->
                                NavigationBarItem(
                                    icon = {
                                        BadgedBox(badge = {
                                            Badge {
                                                Text(text = "8")
                                            }
                                        }) {
                                            Icon(
                                                screen.icon.asPainterResource(),
                                                contentDescription = "Some description"
                                            )
                                        }
                                    },
                                    label = { Text(stringResource(screen.resourceId)) },
                                    selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                        currentScreen = screen
                                    }
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = startDestination.route,
                        Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.NotesList.route) {
                            NotesList(
                                navController = navController,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = MaterialTheme.spacing.medium),
                            )
                        }
                        composable(Screen.Bin.route) {

                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotesList(
    navController: NavController,
    modifier: Modifier = Modifier,
    onAddClicked: () -> Unit = {},
) {

    val listState = rememberLazyListState()
    val expandedFabState = remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }
    var showAddDialog by remember {
        mutableStateOf(false)
    }

    ConstraintLayout(
        modifier = modifier
    ) {
        val (contentRef, buttonsRef, countRef) = createRefs()
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(contentRef) {
                    start.linkTo(parent.start)
                    top.linkTo(countRef.bottom)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium)
        ) {
            for (index in 0 until 100) {
                item {
                    NoteCard(
                        title = "This is a title ${index}",
                        text = "This is a note to your future self"
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .wrapContentSize()
                .constrainAs(buttonsRef) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
        ) {
            ExtendedFloatingActionButton(
                text = { Text(text = "Add") },
                icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = "") },
                onClick = {
                    showAddDialog = true
                    onAddClicked.invoke()
                },
                expanded = expandedFabState.value,
                modifier = Modifier.padding(MaterialTheme.spacing.small)
            )
        }
    }

    if (showAddDialog)
        AddNoteDialog(
            navController = navController,
            value = "",
            onDismissRequest = { showAddDialog = false })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteDialog(
    navController: NavController,
    modifier: Modifier = Modifier,
    value: String,
    onDismissRequest: () -> Unit
) {
    var titleValue by remember {
        mutableStateOf(value)
    }
    var textValue by remember {
        mutableStateOf(value)
    }
    val sheetState = rememberSheetState(false)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.wrapContentHeight(),
        sheetState = sheetState
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(MaterialTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedTextField(value = titleValue,
                onValueChange = {
                    titleValue = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.medium),
                singleLine = true,
                label = { Text(text = "Title") },
                placeholder = { Text(text = "Title") })

            OutlinedTextField(value = textValue,
                onValueChange = {
                    textValue = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = MaterialTheme.spacing.medium),
                minLines = 5,
                label = { Text(text = "Text") },
                placeholder = { Text(text = "Text") })

            Button(
                onClick = { /*TODO*/ }) {
                Text(text = "Add")
            }
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium).fillMaxWidth())
        }
    }
}


