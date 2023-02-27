package br.com.frazo.janac.ui.noteslist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.composables.IndeterminateLoading
import br.com.frazo.janac.ui.composables.MyClickableText
import br.com.frazo.janac.ui.composables.NoItemsContent
import br.com.frazo.janac.ui.noteslist.addnote.AddNoteDialog
import br.com.frazo.janac.ui.noteslist.addnote.AddNoteViewModel
import br.com.frazo.janac.ui.theme.dimensions
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.IconResource

@Composable
fun NotesListScreen(
    modifier: Modifier
) {
    val viewModel = hiltViewModel<NotesListViewModel>()
    val screenState by viewModel.screenState.collectAsState()
    val notesList by viewModel.notes.collectAsState(initial = emptyList())
    val addButtonState by viewModel.addButtonExtended.collectAsState(initial = true)
    val addDialogState by viewModel.addDialogState.collectAsState(initial = false)
    val addNoteViewModel = hiltViewModel<AddNoteViewModel>()

    Screen(
        modifier = modifier,
        onListState = viewModel::onListState,
        notesList = notesList,
        screenState = screenState,
        addButtonState = addButtonState,
        onAddClicked = viewModel::addNoteClicked,
        addDialogState = addDialogState,
        onDialogDismiss = {
            viewModel.dismissDialog()
            addNoteViewModel.cancel()
        },
        addNoteViewModel = addNoteViewModel,
        onBinNote = viewModel::binNote,
        onEditNote = viewModel::editNote
    )

}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesList(
    modifier: Modifier,
    notesList: List<Note>,
    onListState: (LazyListState) -> Unit,
    onBinNote: (Note) -> Unit,
    onEditNote: (Note) -> Unit
) {
    val listState = rememberLazyListState()
    onListState(listState)
    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
    ) {
        items(notesList, key = {
            it.createdAt?:it.hashCode()
        }) { note ->
            NoteCard(
                modifier = Modifier.animateItemPlacement(),
                title = note.title,
                text = note.text
            ) {
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { onEditNote(note) }) {
                        IconResource.fromImageVector(Icons.Filled.Edit, "").ComposeIcon()
                    }
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    IconButton(onClick = { onBinNote(note) }) {
                        IconResource.fromImageVector(Icons.Filled.Delete, "").ComposeIcon()
                    }
                }
            }
        }
    }
}

@Composable
fun Screen(
    modifier: Modifier = Modifier,
    notesList: List<Note>,
    screenState: NotesListViewModel.ScreenState,
    addButtonState: Boolean,
    onAddClicked: () -> Unit = {},
    onListState: (LazyListState) -> Unit,
    addDialogState: Boolean,
    onDialogDismiss: () -> Unit,
    addNoteViewModel: AddNoteViewModel,
    onBinNote: (Note) -> Unit,
    onEditNote: (Note) -> Unit
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (contentRef, buttonsRef, countRef) = createRefs()
        AnimatedVisibility(visible = screenState is NotesListViewModel.ScreenState.Success,
            enter = slideInVertically {
                it
            },
            exit = slideOutVertically { -it }) {

            NotesList(
                modifier = Modifier
                    .fillMaxSize()
                    .constrainAs(contentRef) {
                        start.linkTo(parent.start)
                        top.linkTo(countRef.bottom)
                        end.linkTo(parent.end)
                        bottom.linkTo(parent.bottom)
                    },
                notesList = notesList,
                onListState = onListState,
                onBinNote = onBinNote,
                onEditNote = onEditNote
            )
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
                    }) {
                MyClickableText(
                    modifier = Modifier.padding(top = MaterialTheme.spacing.small),
                    text = "Click here to add a note.",
                    clickableParts = mapOf(Pair("here") {
                        onAddClicked()
                    })
                )
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
                expanded = addButtonState,
                modifier = Modifier.padding(MaterialTheme.spacing.small)
            )
        }

        AnimatedVisibility(visible = addDialogState,
            enter = slideInVertically {
                it
            },
            exit = slideOutVertically { -it }) {
            AddDialogScreen(onDialogDismiss, addNoteViewModel)
        }
    }
}

@Composable
fun AddDialogScreen(onDismissRequest: () -> Unit, addNoteViewModel: AddNoteViewModel) {
    val noteState by addNoteViewModel.note.collectAsState()
    val addNoteUIState by addNoteViewModel.uiState.collectAsState()

    AddNoteDialog(
        title = noteState.title,
        onTitleChanged = addNoteViewModel::onTitleChanged,
        text = noteState.text,
        onTextChanged = addNoteViewModel::onTextChanged,
        onDismissRequest = onDismissRequest,
        onSaveClicked = addNoteViewModel::save,
        textHint = "",
        titleHint = "",
        titleErrorMessage = when (addNoteUIState) {
            is AddNoteViewModel.UIState.TitleError -> (addNoteUIState as AddNoteViewModel.UIState.TitleError).titleInvalidError.asString()
            else -> ""
        },
        textErrorMessage = when (addNoteUIState) {
            is AddNoteViewModel.UIState.TextError -> (addNoteUIState as AddNoteViewModel.UIState.TextError).textInvalidError.asString()
            else -> ""
        },
        saveButtonEnabled = addNoteUIState is AddNoteViewModel.UIState.CanSave
    )
}