package br.com.frazo.janac.ui.screens.composables

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.staggeredgrid.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import br.com.frazo.janac.audio.player.AudioPlayerStatus
import br.com.frazo.janac.audio.player.AudioPlayingData
import br.com.frazo.janac.audio.ui.compose.materialv3.AudioPlayerParams
import br.com.frazo.janac.audio.ui.compose.materialv3.rememberAudioPlayerParams
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.theme.dimensions
import br.com.frazo.janac.ui.theme.spacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NotesStaggeredGrid(
    modifier: Modifier,
    notesList: List<Note>,
    notePlaying: Note? = null,
    audioPlayingData: AudioPlayingData? = null,
    audioNoteCallbacks: AudioNoteCallbacks? = null,
    audioPlayerParams: AudioPlayerParams? = rememberAudioPlayerParams(),
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    highlightSentences: List<String> = emptyList(),
    cardFooterContent: (@Composable ColumnScope.(note: Note) -> Unit)? = null
) {
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        columns = StaggeredGridCells.Adaptive(MaterialTheme.dimensions.minNoteCardSize),
        state = gridState,
        verticalItemSpacing = MaterialTheme.spacing.medium,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
        contentPadding = PaddingValues(bottom = MaterialTheme.spacing.extraLarge)
    ) {
        items(notesList, key = {
            it.createdAt?.toInstant()?.epochSecond ?: it.hashCode()
        }) { note ->
            CompactNoteCard(
                modifier = Modifier,
                note = note,
                audioPlayerParams = audioPlayerParams,
                audioNoteCallbacks = audioNoteCallbacks,
                audioPlayingData = if (note == notePlaying) audioPlayingData else AudioPlayingData(
                    AudioPlayerStatus.NOT_INITIALIZED, 0, 0
                ),
                highlightSentences = highlightSentences,
                footerContent = cardFooterContent,
            )
        }
    }

}