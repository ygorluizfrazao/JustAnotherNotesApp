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
    listState: LazyListState = rememberLazyListState(),
    highlightSentences: List<String> = emptyList(),
    titleEndContent: (@Composable (note: Note) -> Unit)? = null,
    cardFooterContent: (@Composable ColumnScope.(note: Note) -> Unit)? = null
) {

    LazyColumn(
        state = listState,
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(bottom = MaterialTheme.spacing.extraLarge)
    ) {
        items(notesList, key = {
            it.createdAt?.toInstant()?.epochSecond?:it.hashCode()
        }) { note ->
            NoteCard(
                modifier = Modifier.animateItemPlacement(),
                highlightSentences = highlightSentences,
                note = note,
                titleEndContent = titleEndContent,
                footerContent = cardFooterContent
            )
        }
    }
}