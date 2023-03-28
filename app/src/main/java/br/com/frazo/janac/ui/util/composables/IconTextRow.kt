package br.com.frazo.janac.ui.util.composables

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import br.com.frazo.highlighted_text_compose.HighlightedText
import br.com.frazo.janac.ui.theme.spacing
import br.com.frazo.janac.ui.util.IconResource
import br.com.frazo.janac.ui.util.TextResource

@Composable
fun IconTextRow(
    modifier: Modifier,
    iconResource: IconResource,
    textResource: TextResource,
    highlightSentences: List<String> = emptyList(),
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.labelMedium
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = iconResource.asPainterResource(),
            contentDescription = iconResource.contentDescription
        )
        HighlightedText(
            text = textResource.asString(),
            highlightedSentences = highlightSentences,
            normalTextSpanStyle = textStyle.toSpanStyle(),
            highlightedSentencesTextSpanStyle = textStyle.copy(
                color = LocalTextSelectionColors.current.handleColor,
                background = LocalTextSelectionColors.current.backgroundColor
            ).toSpanStyle()
        ) {

            Text(
                modifier = Modifier.padding(MaterialTheme.spacing.small),
                style = textStyle,
                text = it
            )

        }
    }
}