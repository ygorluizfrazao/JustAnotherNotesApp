package br.com.frazo.janac.ui.screens.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import br.com.frazo.janac.R
import br.com.frazo.janac.audio.player.AudioPlayingData
import br.com.frazo.janac.audio.ui.compose.materialv3.AudioPlayerParams
import br.com.frazo.janac.audio.ui.compose.materialv3.rememberAudioPlayerParams
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.IconResource
import br.com.frazo.janac.ui.util.TextResource
import br.com.frazo.janac.ui.util.composables.IconTextRow
import br.com.frazo.janac.util.DateTimeFormatterFactory

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CompactNoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    audioPlayingData: AudioPlayingData? = null,
    audioPlayerParams: AudioPlayerParams? = rememberAudioPlayerParams(),
    audioNoteCallbacks: AudioNoteCallbacks? = null,
    highlightSentences: List<String> = emptyList(),
    createdAtIconResource: IconResource = IconResource.fromImageVector(Icons.Default.CalendarToday),
    binnedAtIconResource: IconResource = IconResource.fromImageVector(Icons.Default.Recycling),
    footerContent: (@Composable ColumnScope.(note: Note) -> Unit)? = null
) {

    val context = LocalContext.current

    NoteCard(
        modifier = modifier,
        highlightSentences = highlightSentences,
        note = note,
        audioPlayingData = audioPlayingData,
        audioPlayerParams = audioPlayerParams,
        audioNoteCallbacks = audioNoteCallbacks
    ) {
        if (note.createdAt != null || note.binnedAt != null) {

            FlowRow(
                modifier = Modifier.padding(top = MaterialTheme.spacing.medium),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                note.createdAt?.let {
                    Column(modifier = Modifier) {
                        Text(
                            text = stringResource(R.string.created_at),
                            style = MaterialTheme.typography.labelSmall
                        )
                        IconTextRow(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth(),
                            iconResource = createdAtIconResource,
                            textResource = TextResource.RuntimeString(
                                note.createdAt.format(
                                    DateTimeFormatterFactory(context = context).datePattern()
                                )
                            ),
                            highlightSentences = highlightSentences,
                            textStyle = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                note.binnedAt?.let {
                    Column(modifier = Modifier) {
                        Text(
                            text = stringResource(R.string.binned_at),
                            style = MaterialTheme.typography.labelSmall
                        )
                        IconTextRow(
                            modifier = Modifier
                                .wrapContentHeight()
                                .wrapContentWidth(),
                            iconResource = binnedAtIconResource,
                            textResource = TextResource.RuntimeString(
                                note.binnedAt.format(
                                    DateTimeFormatterFactory(context = context).datePattern()
                                )
                            ),
                            highlightSentences = highlightSentences,
                            textStyle = MaterialTheme.typography.labelSmall
                        )
                    }
                }

            }
        }

        footerContent?.let {
            Divider(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(top = MaterialTheme.spacing.medium),
                color = LocalContentColor.current
            )
            this.it(note)
        }
    }

}