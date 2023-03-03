package br.com.frazo.janac.ui.screens.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
import br.com.frazo.janac.R
import br.com.frazo.janac.domain.models.Note
import br.com.frazo.janac.ui.theme.spacing

@Composable
fun NoteCard(
    modifier: Modifier = Modifier,
    note: Note,
    footerContent: (@Composable (note: Note)->Unit)? = null
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Justify,
                    text = note.title,
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                ElevatedAssistChip(
                    onClick = { /* Do something! */ },
                    label = { Text("Idea") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_lightbulb_24),
                            contentDescription = "Category",
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                    }
                )
            }

            Text(
                modifier = Modifier
                    .constrainAs(textRef) {
                        top.linkTo(titleRowRef.bottom)
                        start.linkTo(parent.start)
                    }
                    .padding(top = MaterialTheme.spacing.small),
                textAlign = TextAlign.Justify,
                text = note.text,
                style = MaterialTheme.typography.bodyMedium
            )

            footerContent?.let {
                Column(modifier = Modifier.constrainAs(contentRef){
                    top.linkTo(textRef.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }) {
                    Divider(modifier.fillMaxWidth().padding(top = MaterialTheme.spacing.medium))
                    it(note)
                }
            }
        }
    }
}