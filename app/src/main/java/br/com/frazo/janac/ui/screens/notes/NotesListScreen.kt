package br.com.frazo.janac.ui.screens.notes

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.extensions.isNewNote
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.screens.composables.EditNoteDialog
import br.com.frazo.janac.ui.screens.composables.NotesList
import br.com.frazo.janac.ui.screens.notes.editnote.EditNoteViewModel
import br.com.frazo.janac.ui.theme.dimensions
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.IconResource
import br.com.frazo.janac.ui.util.TextResource
import br.com.frazo.janac.ui.util.composables.IconTextRow
import br.com.frazo.janac.ui.util.composables.IndeterminateLoading
import br.com.frazo.janac.ui.util.composables.MyClickableText
import br.com.frazo.janac.ui.util.composables.NoItemsContent
import br.com.frazo.janac.util.DateTimeFormatterFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun NotesListScreen(
    modifier: Modifier
) {

    val viewModel = hiltViewModel<NotesListViewModel>()

    Screen(
        modifier = modifier,
        viewModel = viewModel,
    )

}

@Composable
fun Screen(
    modifier: Modifier = Modifier,
    viewModel: NotesListViewModel
) {

    val screenState by viewModel.screenState.collectAsState()
    val notesList by viewModel.notes.collectAsState(initial = emptyList())
    val editNoteState by viewModel.editNoteState.collectAsState()
    val editNoteViewModel = hiltViewModel<EditNoteViewModel>()

    val listState = rememberLazyListState()
    val addButtonExtendedState by remember {
        derivedStateOf { listState.firstVisibleItemIndex == 0  }
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    ConstraintLayout(
        modifier = modifier
    ) {
        val (contentRef, buttonsRef, countRef) = createRefs()
        AnimatedVisibility(visible = screenState is NotesListViewModel.ScreenState.Success,
            enter = slideInVertically { it },
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
                listState = listState,
                titleEndContent = { note ->
                    note.createdAt?.let {
                        IconTextRow(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth()
                                .padding(horizontal = MaterialTheme.spacing.small),
                            iconResource = IconResource.fromImageVector(Icons.Default.CalendarToday),
                            textResource = TextResource.RuntimeString(
                                note.createdAt.format(
                                    DateTimeFormatterFactory(context = context).datePattern()
                                )
                            )
                        )
                    }
                }
            ) { note ->
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { viewModel.editNote(note) }) {
                        IconResource.fromImageVector(Icons.Filled.Edit, "").ComposeIcon()
                    }
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    IconButton(onClick = { viewModel.binNote(note) }) {
                        IconResource.fromImageVector(Icons.Filled.Delete, "").ComposeIcon()
                    }
                }
            }

        }

        AnimatedVisibility(visible = screenState is NotesListViewModel.ScreenState.NoData || screenState is NotesListViewModel.ScreenState.Error,
            enter = slideInVertically { it },
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
                            id = R.string.here
                        )
                    ).asString(),
                    clickableParts = mapOf(Pair(stringResource(id = R.string.here)) {
                        viewModel.editNewNote()
                    })
                )
            }

        }

        AnimatedVisibility(visible = screenState is NotesListViewModel.ScreenState.NoDataForFilter,
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

        AnimatedVisibility(visible = screenState is NotesListViewModel.ScreenState.Loading,
            enter = slideInVertically { it },
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
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = stringResource(id = R.string.add_note)
                    )
                },
                onClick = viewModel::editNewNote,
                expanded = addButtonExtendedState,
                modifier = Modifier.padding(vertical = MaterialTheme.spacing.small)
            )
        }

        AnimatedVisibility(visible = editNoteState.requested,
            enter = slideInVertically { it },
            exit = slideOutVertically { -it }) {
            EditDialogScreen({
                viewModel.editNoteClear()
                editNoteViewModel.cancel()
            }, editNoteViewModel, editNoteState.baseNote)
        }
    }

    LaunchedEffect(key1 = Unit) {
        viewModel.showFirstNote.collectLatest { showFirstNote ->
            if (showFirstNote) {
                coroutineScope.launch {
                    listState.animateScrollToItem(0)
                }
            }
        }
    }

}

@Composable
fun EditDialogScreen(
    onDismissRequest: () -> Unit,
    editNoteViewModel: EditNoteViewModel,
    noteToEdit: Note
) {

    val inEditionNote by editNoteViewModel.inEditionNote.collectAsState()
    val addNoteUIState by editNoteViewModel.uiState.collectAsState()

    LaunchedEffect(key1 = addNoteUIState) {
        if (addNoteUIState is EditNoteViewModel.UIState.Saved) {
            onDismissRequest()
        }
    }

    LaunchedEffect(key1 = noteToEdit, block = {
        editNoteViewModel.setForEditing(noteToEdit)
    })

    EditNoteDialog(
        title = inEditionNote.title,
        onTitleChanged = editNoteViewModel::onTitleChanged,
        text = inEditionNote.text,
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