package br.com.frazo.janac.ui.screens.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.composables.HighlightedText

@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    highlightSentences: List<String> = emptyList(),
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
                        .copy(fontWeight = FontWeight.Bold)
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
                normalTextSpanStyle = MaterialTheme.typography.bodyMedium.toSpanStyle()
            ) {

                Text(
                    modifier = Modifier
                        .padding(top = MaterialTheme.spacing.small),
                    textAlign = TextAlign.Justify,
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )

            }

//            Button(
//                modifier = Modifier.size(40.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
//                shape = CircleShape,
//                onClick = {
//
//                }) {
//
//            }

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
