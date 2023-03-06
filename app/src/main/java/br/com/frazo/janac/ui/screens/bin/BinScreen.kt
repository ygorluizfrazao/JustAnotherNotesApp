package br.com.frazo.janac.ui.screens.bin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.screens.composables.NotesList
import br.com.frazo.janac.ui.theme.dimensions
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.IconResource
import br.com.frazo.janac.ui.util.composables.IndeterminateLoading
import br.com.frazo.janac.ui.util.composables.NoItemsContent

@Composable
fun BinScreen(modifier: Modifier = Modifier) {

    val viewModel = hiltViewModel<BinScreenViewModel>()
    val screenState by viewModel.screenState.collectAsState()
    val notesList by viewModel.notes.collectAsState()
    val clearBinButtonState by viewModel.clearBinButtonExpandedState.collectAsState()

    Screen(
        modifier = modifier,
        notesList = notesList,
        screenState = screenState,
        clearBinButtonState = clearBinButtonState,
        onClearBinClicked = viewModel::clearBin,
        onListState = viewModel::onListState,
        onDeleteNote = viewModel::deleteNote,
        onRestoreNote = viewModel::restoreNote
    )

}

@Composable
fun Screen(
    modifier: Modifier = Modifier,
    notesList: List<Note>,
    screenState: BinScreenViewModel.ScreenState,
    clearBinButtonState: Boolean,
    onClearBinClicked: () -> Unit,
    onListState: (LazyListState) -> Unit,
    onDeleteNote: (Note) -> Unit,
    onRestoreNote: (Note) -> Unit
) {

    ConstraintLayout(
        modifier = modifier
    ) {
        val (contentRef, buttonsRef, countRef) = createRefs()
        AnimatedVisibility(visible = screenState is BinScreenViewModel.ScreenState.Success,
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
            ) {
                Row(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = { onRestoreNote(it) }) {
                        IconResource.fromImageVector(Icons.Filled.Restore, "").ComposeIcon()
                    }
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    IconButton(onClick = { onDeleteNote(it) }) {
                        IconResource.fromImageVector(Icons.Filled.DeleteForever, "").ComposeIcon()
                    }
                }
            }
        }
        AnimatedVisibility(visible = screenState is BinScreenViewModel.ScreenState.NoData,
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

        Row(
            modifier = Modifier
                .wrapContentSize()
                .constrainAs(buttonsRef) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                }
        ) {
            ExtendedFloatingActionButton(
                text = { Text(text = stringResource(id = R.string.clear_bin)) },
                icon = { Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "") },
                onClick = onClearBinClicked,
                expanded = clearBinButtonState,
                modifier = Modifier.padding(MaterialTheme.spacing.small)
            )
        }
    }
}