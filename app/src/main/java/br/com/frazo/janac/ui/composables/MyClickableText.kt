package br.com.frazo.janac.ui.composables

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.text.buildSpannedString

@Composable
fun MyClickableText(
    modifier: Modifier = Modifier,
    text: String,
    clickableParts: Map<String, (String) -> Unit>,
    normalTextSpanStyle: SpanStyle = LocalTextStyle.current.toSpanStyle()
        .copy(color = LocalContentColor.current),
    clickableTextSpanStyle: SpanStyle = normalTextSpanStyle.copy(color = Color.Blue)
) {
    val placeHolderStart = "|_?_|"
    val placeHolderEnd = "_|?|_"
    var markedText = text
    clickableParts.keys.forEach {
        markedText = markedText.replace(it, "$placeHolderStart$it$placeHolderEnd")
    }
    val splitString = markedText.split(placeHolderStart)
    val annotatedString = buildAnnotatedString {
        splitString.forEach { s ->
            val parts = s.split(placeHolderEnd)
            if (parts.size <= 1) {
                withStyle(style = normalTextSpanStyle) {
                    append(parts.first())
                }
                return@forEach
            }
            withStyle(style = clickableTextSpanStyle) {
                pushStringAnnotation(tag = parts.first(), annotation = parts.first())
                append(parts.first())
            }
            withStyle(style = normalTextSpanStyle) {
                append(parts[1])
            }
        }
    }
    ClickableText(
        modifier = modifier,
        text = annotatedString,
        style = MaterialTheme.typography.bodyMedium,
        onClick = { offset ->
            annotatedString.getStringAnnotations(offset, offset)
                .firstOrNull()?.let { span ->
                    clickableParts[span.tag]?.invoke(span.tag)
                }
        })
}