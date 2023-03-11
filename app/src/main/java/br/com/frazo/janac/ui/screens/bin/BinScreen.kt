package br.com.frazo.janac.ui.screens.bin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.screens.composables.NotesList
import br.com.frazo.janac.ui.theme.dimensions
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.IconResource
import br.com.frazo.janac.ui.util.TextResource
import br.com.frazo.janac.ui.util.composables.IconTextRow
import br.com.frazo.janac.ui.util.composables.IndeterminateLoading
import br.com.frazo.janac.ui.util.composables.MyClickableText
import br.com.frazo.janac.ui.util.composables.NoItemsContent
import br.com.frazo.janac.util.DateTimeFormatterFactory

@Composable
fun BinScreen(modifier: Modifier = Modifier) {

    val viewModel = hiltViewModel<BinScreenViewModel>()
    Screen(
        modifier = modifier,
        viewModel = viewModel,
    )

}

@Composable
fun Screen(
    modifier: Modifier = Modifier,
    viewModel: BinScreenViewModel
) {

    val screenState by viewModel.screenState.collectAsState()
    val filteredNotesList = viewModel.filteredNotes.collectAsState()
    val allNotes = viewModel.notes.collectAsState()
    val listState = rememberLazyListState()

    val context = LocalContext.current


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
                notesList = filteredNotesList.value,
                listState = listState,
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
            listState = listState,
            filteredNotesList = filteredNotesList,
            allNotes = allNotes
        ) {
            viewModel.clearBin()
        }
    }
}

@Composable
fun DeleteButton(
    modifier: Modifier = Modifier,
    listState: LazyListState,
    filteredNotesList: State<List<Note>>,
    allNotes: State<List<Note>>,
    onClick: () -> Unit
) {

    val expandedState by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }

    val filtered  = remember {
        derivedStateOf {
            filteredNotesList.value.size < allNotes.value.size && filteredNotesList.value.isNotEmpty()
        }
    }

    val textResource by remember {
        derivedStateOf{
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
            modifier = modifier,
            text = { Text(text = textResource.asString()) },
            icon = { iconResource.ComposeIcon() },
            onClick = onClick,
            expanded = expandedState,
        )
    }
}