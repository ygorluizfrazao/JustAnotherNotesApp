package br.com.frazo.janac.ui.util.composables

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle

@Composable
fun MyClickableText(
    modifier: Modifier = Modifier,
    text: String,
    clickableParts: Map<String, (String) -> Unit>,
    normalTextSpanStyle: SpanStyle = LocalTextStyle.current.toSpanStyle()
        .copy(color = LocalContentColor.current),
    clickableTextSpanStyle: SpanStyle = normalTextSpanStyle.copy(color = Color.Blue)
) {
    HighlightedText(
        text = text,
        highlightedSentences = clickableParts.keys.toList(),
        normalTextSpanStyle = normalTextSpanStyle,
        highlightedSentencesTextSpanStyle = clickableTextSpanStyle
    ) {
        ClickableText(
            modifier = modifier,
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            onClick = { offset ->
                it.getStringAnnotations(offset, offset)
                    .firstOrNull()?.let { span ->
                        clickableParts[span.tag]?.invoke(span.tag)
                    }
            })
    }
}