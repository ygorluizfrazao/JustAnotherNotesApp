package br.com.frazo.janac.ui.screens.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.theme.dimensions
import br.com.frazo.janac.ui.theme.spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesStaggeredGrid(
    modifier: Modifier,
    notesList: List<Note>,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    cardFooterContent: (@Composable ColumnScope.(note: Note) -> Unit)? = null
) {
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        columns = StaggeredGridCells.Adaptive(MaterialTheme.dimensions.minNoteCardSize),
        state = gridState,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(bottom = MaterialTheme.spacing.extraLarge)
    ) {
        items(notesList, key = {
            it.createdAt?.toInstant()?.epochSecond?:it.hashCode()
        }) { note ->
            NoteCardWithBottomExtraData(
                modifier = Modifier,
                note = note,
                footerContent = cardFooterContent
            )
        }
    }

}