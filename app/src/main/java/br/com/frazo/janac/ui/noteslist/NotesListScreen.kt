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
import br.com.frazo.janac.domain.extensions.isNewNote
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.composables.IndeterminateLoading
import br.com.frazo.janac.ui.composables.MyClickableText
import br.com.frazo.janac.ui.composables.NoItemsContent
import br.com.frazo.janac.ui.noteslist.addnote.EditNoteDialog
import br.com.frazo.janac.ui.noteslist.addnote.EditNoteViewModel
import br.com.frazo.janac.ui.theme.dimensions
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.IconResource
import br.com.frazo.janac.ui.util.TextResource

@Composable
fun NotesListScreen(
    modifier: Modifier
) {
    val viewModel = hiltViewModel<NotesListViewModel>()
    val screenState by viewModel.screenState.collectAsState()
    val notesList by viewModel.notes.collectAsState(initial = emptyList())
    val addButtonState by viewModel.addButtonExtended.collectAsState(initial = true)
    val addEditNoteState by viewModel.addEditNoteState.collectAsState()
    val editNoteViewModel = hiltViewModel<EditNoteViewModel>()

    Screen(
        modifier = modifier,
        onListState = viewModel::onListState,
        notesList = notesList,
        screenState = screenState,
        addButtonState = addButtonState,
        onAddClicked = viewModel::editNewNote,
        editNoteState = addEditNoteState,
        onDialogDismiss = {
            viewModel.editNoteClear()
            editNoteViewModel.cancel()
        },
        editNoteViewModel = editNoteViewModel,
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
            notesList.indexOf(it)
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
    editNoteState: NotesListViewModel.EditNoteState,
    onDialogDismiss: () -> Unit,
    editNoteViewModel: EditNoteViewModel,
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
                    text = TextResource.StringResource(
                        R.string.click_to_add_a_note, stringResource(
                            id = R.string.link_alias_to_add_a_note
                        )
                    ).asString(),
                    clickableParts = mapOf(Pair(stringResource(id = R.string.link_alias_to_add_a_note)) {
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
                text = { Text(text = stringResource(id = R.string.add_note)) },
                icon = { Icon(imageVector = Icons.Rounded.Add, contentDescription = "") },
                onClick = onAddClicked,
                expanded = addButtonState,
                modifier = Modifier.padding(MaterialTheme.spacing.small)
            )
        }

        AnimatedVisibility(visible = editNoteState.requested,
            enter = slideInVertically {
                it
            },
            exit = slideOutVertically { -it }) {
            AddEditDialogScreen(onDialogDismiss, editNoteViewModel, editNoteState.baseNote)
        }
    }
}

@Composable
fun AddEditDialogScreen(
    onDismissRequest: () -> Unit,
    editNoteViewModel: EditNoteViewModel,
    noteToEdit: Note
) {

    val noteState by editNoteViewModel.inEditionNote.collectAsState()
    val addNoteUIState by editNoteViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = noteToEdit, block = {
        editNoteViewModel.setForEditing(noteToEdit)
    })

    EditNoteDialog(
        title = noteState.title,
        onTitleChanged = editNoteViewModel::onTitleChanged,
        text = noteState.text,
        onTextChanged = editNoteViewModel::onTextChanged,
        onDismissRequest = onDismissRequest,
        onSaveClicked = editNoteViewModel::save,
        textHint = "",
        titleHint = "",
        titleErrorMessage = when (addNoteUIState) {
            is EditNoteViewModel.UIState.TitleError -> (addNoteUIState as EditNoteViewModel.UIState.TitleError).titleInvalidError.asString()
            else -> ""
        },
        textErrorMessage = when (addNoteUIState) {
            is EditNoteViewModel.UIState.TextError -> (addNoteUIState as EditNoteViewModel.UIState.TextError).textInvalidError.asString()
            else -> ""
        },
        saveButtonEnabled = addNoteUIState is EditNoteViewModel.UIState.CanSave,
        dialogTitle = if (noteToEdit.isNewNote()) stringResource(id = R.string.add_note) else stringResource(
            id = R.string.edit_note
        )
    )
}