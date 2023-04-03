package br.com.frazo.janac.ui.screens.composables

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import br.com.frazo.highlighted_text_compose.HighlightedText
import br.com.frazo.janac.R
import br.com.frazo.janac.audio.player.AudioPlayingData
import br.com.frazo.janac.audio.ui.compose.materialv3.AudioPlayer
import br.com.frazo.janac.audio.ui.compose.materialv3.AudioPlayerCallbacks
import br.com.frazo.janac.audio.ui.compose.materialv3.AudioPlayerParams
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.theme.spacing

@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    highlightSentences: List<String> = emptyList(),
    audioPlayingData: AudioPlayingData? = null,
    audioPlayerParams: AudioPlayerParams? = null,
    audioNoteCallbacks: AudioNoteCallbacks? = null,
    titleEndContent: (@Composable (note: Note) -> Unit)? = null,
    footerContent: (@Composable ColumnScope.(note: Note) -> Unit)? = null
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    MaterialTheme.spacing.medium
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {

                HighlightedText(
                    text = note.title,
                    highlightedSentences = highlightSentences,
                    normalTextSpanStyle = MaterialTheme.typography.titleMedium.toSpanStyle()
                        .copy(fontWeight = FontWeight.Bold),
                    highlightedSentencesTextSpanStyle = MaterialTheme.typography.titleMedium.copy(
                        color = LocalTextSelectionColors.current.handleColor,
                        background = LocalTextSelectionColors.current.backgroundColor
                    ).toSpanStyle()
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Start,
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                titleEndContent?.invoke(note)
            }

            HighlightedText(
                text = note.text,
                highlightedSentences = highlightSentences,
                normalTextSpanStyle = MaterialTheme.typography.bodyMedium.toSpanStyle(),
                highlightedSentencesTextSpanStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = LocalTextSelectionColors.current.handleColor,
                    background = LocalTextSelectionColors.current.backgroundColor
                ).toSpanStyle()
            ) {
                Text(
                    modifier = Modifier
                        .padding(top = MaterialTheme.spacing.small),
                    textAlign = TextAlign.Justify,
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            note.audioNote?.let {
                if (audioNoteCallbacks != null && audioPlayerParams != null && audioPlayingData != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = MaterialTheme.spacing.medium),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = MaterialTheme.spacing.medium),
                            color = LocalContentColor.current,
                        )

                        Text(
                            text = stringResource(R.string.audio_note),
                        )
                        AudioPlayer(
                            audioPlayingData = audioPlayingData,
                            audioPlayerParams = audioPlayerParams,
                            audioPlayerCallbacks = AudioPlayerCallbacks(
                                onPlay = { audioNoteCallbacks.onPlay(note) },
                                onPause = { audioNoteCallbacks.onPause(note) },
                                onSeekPosition = { audioNoteCallbacks.onSeekPosition(note, it) },
                                onEndIconClicked = {
                                    audioNoteCallbacks.onEndIconClicked?.invoke(
                                        note
                                    )
                                }
                            )
                        )
                    }
                }
            }

            footerContent?.let {
                Column(
                    modifier = Modifier
                        .wrapContentHeight()
                        .fillMaxWidth()
                ) {
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
    }
}

data class AudioNoteCallbacks(
    val onPlay: (note: Note) -> Unit,
    val onPause: (note: Note) -> Unit,
    val onSeekPosition: (note: Note, positionPercent: Float) -> Unit,
    val onEndIconClicked: ((note: Note) -> Unit)? = null
)