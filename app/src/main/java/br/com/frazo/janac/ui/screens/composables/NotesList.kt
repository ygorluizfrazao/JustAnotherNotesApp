package br.com.frazo.janac.ui.screens.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.theme.spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesList(
    modifier: Modifier,
    notesList: List<Note>,
    onListState: (LazyListState) -> Unit,
    cardFooterContent: (@Composable (note: Note) -> Unit)? = null
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
                note = note,
                footerContent = cardFooterContent
            )
        }
    }
}