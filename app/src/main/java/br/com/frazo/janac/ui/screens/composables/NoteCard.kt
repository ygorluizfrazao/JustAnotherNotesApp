package br.com.frazo.janac.ui.screens.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
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
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(
                    MaterialTheme.spacing.medium
                )
        ) {

            val (titleRowRef, textRef, contentRef) = createRefs()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(titleRowRef) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    },
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
                        .constrainAs(textRef) {
                            top.linkTo(titleRowRef.bottom)
                            start.linkTo(parent.start)
                        }
                        .padding(top = MaterialTheme.spacing.small),
                    textAlign = TextAlign.Justify,
                    text = it,
                    style = MaterialTheme.typography.bodyMedium
                )

            }

            footerContent?.let {
                Column(modifier = Modifier.constrainAs(contentRef) {
                    top.linkTo(textRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
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
