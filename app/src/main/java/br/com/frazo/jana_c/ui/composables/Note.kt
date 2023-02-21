package br.com.frazo.jana_c.ui.composables

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.constraintlayout.compose.ConstraintLayout
import br.com.frazo.jana_c.ui.theme.spacing
import com.example.notesapp.R

@Composable
fun NoteCard(
    modifier: Modifier = Modifier, title: String, text: String
) {
    Card(
        modifier = modifier
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = MaterialTheme.spacing.small, vertical = MaterialTheme.spacing.medium)
        ) {

            val (titleRowRef, textRef) = createRefs()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .constrainAs(titleRowRef) {
                        start.linkTo(parent.start)
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    },
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Justify,
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))
                Icon(
                    painter = painterResource(id = R.drawable.baseline_lightbulb_24),
                    contentDescription = "Category",
                    tint = MaterialTheme.colorScheme.tertiary
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
                text = text,
                style = MaterialTheme.typography.bodySmall
            )

        }
    }
}