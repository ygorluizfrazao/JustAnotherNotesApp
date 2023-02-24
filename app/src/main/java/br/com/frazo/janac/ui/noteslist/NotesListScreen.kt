package br.com.frazo.janac.ui.noteslist

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.text.buildSpannedString
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.composables.IndeterminateLoading
import br.com.frazo.janac.ui.composables.MyClickableText
import br.com.frazo.janac.ui.composables.NoItemsContent
import br.com.frazo.janac.ui.theme.dimensions
import br.com.frazo.janac.ui.theme.spacing

@Composable
fun NotesListScreen(
    navController: NavController,
    modifier: Modifier
) {
    val viewModel = hiltViewModel<NotesListViewModel>()
    val screenState by viewModel.screenState.collectAsState()
    val notesList by viewModel.notes.collectAsState(initial = emptyList())

    NotesList(modifier = modifier, notesList = notesList, screenState = screenState)
//    Box(modifier = modifier) {
//        AnimatedVisibility(visible = screenState is NotesListViewModel.ScreenState.Success,
//            enter = slideInVertically {
//                it
//            },
//            exit = slideOutVertically { -it }) {
//            NotesList(
//                notesList = notesList,
//                onAddClicked = {
//                    Toast.makeText(context, "Add Clicked", Toast.LENGTH_SHORT).show()
//                }
//            )
//        }
//        AnimatedVisibility(visible = screenState is NotesListViewModel.ScreenState.NoData,
//            enter = slideInVertically {
//                it
//            },
//            exit = slideOutVertically { -it }) {
//            NoItemsContent(text = "No notes", modifier = Modifier.fillMaxSize())
//        }
//        AnimatedVisibility(visible = screenState is NotesListViewModel.ScreenState.Loading,
//            enter = slideInVertically {
//                it
//            },
//            exit = slideOutVertically { -it }) {
//            IndeterminateLoading(loadingText = "Loading", modifier = Modifier.fillMaxSize())
//        }
//    }
}

@Composable
fun NotesList(
    modifier: Modifier = Modifier,
    notesList: List<Note>,
    screenState: NotesListViewModel.ScreenState,
    onAddClicked: () -> Unit = {},
) {

    val listState = rememberLazyListState()
    val expandedFabState = remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }

    ConstraintLayout(
        modifier = modifier
    ) {
        val (contentRef, buttonsRef, countRef) = createRefs()
        AnimatedVisibility(visible = screenState is NotesListViewModel.ScreenState.Success,
            enter = slideInVertically {
                it
            },
            exit = slideOutVertically { -it }) {
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
                items(notesList) {
                    NoteCard(
                        title = it.title,
                        text = it.text
                    )
                }
            }
        }
        AnimatedVisibility(visible = screenState is NotesListViewModel.ScreenState.NoData,
            enter = slideInVertically {
                it
            },
            exit = slideOutVertically { -it }) {
            NoItemsContent(
                text = stringResource(R.string.no_content),
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
                    }){
                MyClickableText(modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                    text = "Click here to add a note.",
                    clickableParts = mapOf(Pair("here") {
                    Log.d("Clicked", it)
                }))
            }
        }

        AnimatedVisibility(visible = screenState is NotesListViewModel.ScreenState.Loading,
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
                onClick = onAddClicked,
                expanded = expandedFabState.value,
                modifier = Modifier.padding(MaterialTheme.spacing.small)
            )
        }
    }
}