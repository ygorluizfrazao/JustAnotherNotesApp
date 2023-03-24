package br.com.frazo.janac.ui.screens.bin

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.mediator.ContentDisplayMode
import br.com.frazo.janac.ui.screens.composables.NotesList
import br.com.frazo.janac.ui.screens.composables.NotesStaggeredGrid
import br.com.frazo.janac.ui.theme.dimensions
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.IconResource
import br.com.frazo.janac.ui.util.TextResource
import br.com.frazo.janac.ui.util.composables.IconTextRow
import br.com.frazo.janac.ui.util.composables.IndeterminateLoading
import br.com.frazo.janac.ui.util.composables.MyClickableText
import br.com.frazo.janac.ui.util.composables.NoItemsContent
import br.com.frazo.janac.ui.util.goToAppSettings
import br.com.frazo.janac.ui.util.permissions.base.strategy.AskingStrategy
import br.com.frazo.janac.ui.util.permissions.base.strategy.rememberUserDrivenAskingStrategy
import br.com.frazo.janac.ui.util.permissions.materialv3.WithPermission
import br.com.frazo.janac.ui.util.permissions.materialv3.createPermissionDialogProperties
import br.com.frazo.janac.util.DateTimeFormatterFactory
import kotlinx.coroutines.launch

@Composable
fun BinScreen(modifier: Modifier = Modifier) {

    val viewModel = hiltViewModel<BinScreenViewModel>()
    Screen(
        modifier = modifier,
        viewModel = viewModel,
    )

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Screen(
    modifier: Modifier = Modifier,
    viewModel: BinScreenViewModel
) {

    val screenState by viewModel.screenState.collectAsState()
    val filteredNotesList = viewModel.filteredNotes.collectAsState()
    val allNotes = viewModel.notes.collectAsState()
    val deleteButtonExtendedState = remember {
        mutableStateOf(true)
    }
    val contentDisplayMode by viewModel.contentDisplayMode.collectAsState()



    ConstraintLayout(
        modifier = modifier
    ) {
        val (contentRef, buttonsRef, countRef) = createRefs()
        AnimatedVisibility(
            modifier = Modifier
                .fillMaxSize()
                .constrainAs(contentRef) {
                    start.linkTo(parent.start)
                    top.linkTo(countRef.bottom)
                    end.linkTo(parent.end)
                    bottom.linkTo(parent.bottom)
                },
            visible = screenState is BinScreenViewModel.ScreenState.Success,
            enter = slideInVertically {
                it
            },
            exit = slideOutVertically { -it }) {

            AnimatedVisibility(visible = contentDisplayMode == ContentDisplayMode.AS_LIST) {

                val listState = rememberLazyListState()
                val firstVisibleIndex =
                    remember { derivedStateOf { listState.firstVisibleItemIndex } }

                DisplayContentAsList(
                    filteredNotesList = filteredNotesList,
                    viewModel = viewModel,
                    listState = listState
                )

                LaunchedEffect(key1 = firstVisibleIndex.value) {
                    deleteButtonExtendedState.value = firstVisibleIndex.value == 0
                }

            }

            AnimatedVisibility(visible = contentDisplayMode == ContentDisplayMode.AS_STAGGERED_GRID) {

                val gridState = rememberLazyStaggeredGridState()
                val firstVisibleIndex =
                    remember { derivedStateOf { gridState.firstVisibleItemIndex } }

                DisplayContentAsGrid(
                    gridState = gridState,
                    filteredNotesList = filteredNotesList,
                    viewModel = viewModel
                )

                LaunchedEffect(key1 = firstVisibleIndex.value) {
                    deleteButtonExtendedState.value = firstVisibleIndex.value == 0
                }
            }

        }

        AnimatedVisibility(visible = screenState is BinScreenViewModel.ScreenState.NoData || screenState is BinScreenViewModel.ScreenState.Error,
            enter = slideInVertically {
                it
            },
            exit = slideOutVertically { -it }) {
            NoItemsContent(
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(contentRef) {
                        start.linkTo(parent.start)
                        top.linkTo(countRef.bottom)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    },
                text = stringResource(R.string.all_clear_here),
                icon = IconResource.fromImageVector(Icons.Default.CheckCircle),
                iconModifier = Modifier
                    .padding(bottom = MaterialTheme.spacing.medium)
                    .size(MaterialTheme.dimensions.mediumIconSize)
            )
        }

        AnimatedVisibility(visible = screenState is BinScreenViewModel.ScreenState.NoDataForFilter,
            enter = slideInVertically { it },
            exit = slideOutVertically { -it }) {

            NoItemsContent(
                text = stringResource(R.string.no_data_for_query),
                iconModifier = Modifier
                    .padding(bottom = MaterialTheme.spacing.medium)
                    .size(MaterialTheme.dimensions.mediumIconSize),
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(contentRef) {
                        start.linkTo(parent.start)
                        top.linkTo(countRef.bottom)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    },
                icon = IconResource.fromImageVector(Icons.Default.SearchOff)
            ) {
                MyClickableText(
                    modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                    text = TextResource.StringResource(
                        R.string.click_to_clear_search, stringResource(
                            id = R.string.here
                        )
                    ).asString(),
                    clickableParts = mapOf(Pair(stringResource(id = R.string.here)) {
                        viewModel.clearFilter()
                    })
                )
            }

        }

        AnimatedVisibility(visible = screenState is BinScreenViewModel.ScreenState.Loading,
            enter = slideInVertically {
                it
            },
            exit = slideOutVertically { -it }) {
            IndeterminateLoading(
                loadingText = stringResource(R.string.loading),
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(contentRef) {
                        start.linkTo(parent.start)
                        top.linkTo(countRef.bottom)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    })
        }

        DeleteButton(
            modifier = Modifier
                .wrapContentSize()
                .constrainAs(buttonsRef) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
                .padding(
                    vertical = MaterialTheme.spacing.small
                ),
            expanded = deleteButtonExtendedState,
            filteredNotesList = filteredNotesList,
            allNotes = allNotes
        ) {
            viewModel.clearBin()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayContentAsList(
    modifier: Modifier = Modifier,
    filteredNotesList: State<List<Note>>,
    listState: LazyListState = rememberLazyListState(),
    context: Context = LocalContext.current,
    viewModel: BinScreenViewModel
) {

    val filter by viewModel.filter.collectAsState()

    var clicked by rememberSaveable {
        mutableStateOf(false)
    }

    val userDrivenAskingStrategy =
        rememberUserDrivenAskingStrategy(
            type = AskingStrategy.STOP_ASKING_ON_USER_DENIAL,
            permissions = listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            canStart = { clicked }
        )

    NotesList(
        modifier = modifier,
        notesList = filteredNotesList.value,
        listState = listState,
        highlightSentences = listOf(filter),
        titleEndContent = { note ->
            note.binnedAt?.let {
                IconTextRow(
                    modifier = Modifier
                        .wrapContentHeight()
                        .wrapContentWidth()
                        .padding(horizontal = MaterialTheme.spacing.small),
                    iconResource = IconResource.fromImageVector(Icons.Default.Recycling),
                    textResource = TextResource.RuntimeString(
                        note.binnedAt.format(
                            DateTimeFormatterFactory(context = context).datePattern()
                        )
                    )
                )
            }
        }
    ) {
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {

            WithPermission(
                userDrivenAskingStrategy = userDrivenAskingStrategy,
                permissionDialogProperties = createPermissionDialogProperties(
                    onGrantButtonClicked = { _, _ ->
                        context.goToAppSettings()
                        clicked = false
                    },
                ),
                initialStateContent = {
                    IconButton(onClick = { clicked = true }) {
                        IconResource.fromImageVector(
                            Icons.Default.Warning,
                            ""
                        ).ComposeIcon()
                    }
                }) { _, permissionMap ->

                val permissionsMapState = remember {
                    mutableStateOf(permissionMap)
                }

                val grantedPermissions = remember {
                    derivedStateOf {
                        permissionsMapState.value.filter { (_, granted) -> granted }
                    }
                }

                val deniedPermissions = remember {
                    derivedStateOf {
                        permissionsMapState.value.filter { (_, granted) -> !granted }
                    }
                }

                val coroutineScope = rememberCoroutineScope()
                if (deniedPermissions.value.isEmpty()) {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            Toast.makeText(
                                context,
                                "Granted : ${grantedPermissions.value}",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }) {
                        IconResource.fromImageVector(
                            Icons.Default.CheckCircle,
                            ""
                        ).ComposeIcon()
                    }
                } else {
                    IconButton(onClick = {
                        coroutineScope.launch {
                            Toast.makeText(
                                context,
                                "Granted: ${grantedPermissions.value}\nDenied: ${deniedPermissions.value}",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }) {
                        IconResource.fromImageVector(
                            Icons.Default.Cancel,
                            ""
                        ).ComposeIcon()
                    }
                }
            }


            IconButton(onClick = { viewModel.restoreNote(it) }) {
                IconResource.fromImageVector(Icons.Filled.Restore, "").ComposeIcon()
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            IconButton(onClick = { viewModel.deleteNote(it) }) {
                IconResource.fromImageVector(Icons.Filled.DeleteForever, "").ComposeIcon()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DisplayContentAsGrid(
    modifier: Modifier = Modifier,
    filteredNotesList: State<List<Note>>,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    viewModel: BinScreenViewModel
) {

    val filter by viewModel.filter.collectAsState()

    NotesStaggeredGrid(
        modifier = modifier,
        notesList = filteredNotesList.value,
        gridState = gridState,
        highlightSentences = listOf(filter)
    ) {
        Row(
            modifier = Modifier
                .wrapContentHeight()
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = { viewModel.restoreNote(it) }) {
                IconResource.fromImageVector(Icons.Filled.Restore, "").ComposeIcon()
            }
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
            IconButton(onClick = { viewModel.deleteNote(it) }) {
                IconResource.fromImageVector(Icons.Filled.DeleteForever, "").ComposeIcon()
            }
        }
    }

}

@Composable
private fun DeleteButton(
    modifier: Modifier = Modifier,
    filteredNotesList: State<List<Note>>,
    allNotes: State<List<Note>>,
    expanded: State<Boolean>,
    onClick: () -> Unit
) {

    val filtered = remember {
        derivedStateOf {
            filteredNotesList.value.size < allNotes.value.size && filteredNotesList.value.isNotEmpty()
        }
    }

    val textResource by remember {
        derivedStateOf {
            if (filtered.value) {
                TextResource.StringResource(R.string.clear_filtered_bin)
            } else
                TextResource.StringResource(R.string.clear_bin)
        }
    }

    val iconResource: IconResource =
        IconResource.fromImageVector(Icons.Default.DeleteForever, textResource.asString())

    AnimatedVisibility(
        modifier = modifier,
        visible = filteredNotesList.value.isNotEmpty()
    ) {
        ExtendedFloatingActionButton(
            text = { Text(text = textResource.asString()) },
            icon = { iconResource.ComposeIcon() },
            onClick = onClick,
            expanded = expanded.value,
        )
    }
}