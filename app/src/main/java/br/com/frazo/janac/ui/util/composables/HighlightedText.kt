package br.com.frazo.janac.ui.util.composables

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

@Composable
fun HighlightedText(
    text: String,
    highlightedSentences: List<String>,
    normalTextSpanStyle: SpanStyle = LocalTextStyle.current.toSpanStyle()
        .copy(color = LocalContentColor.current),
    highlightedSentencesTextSpanStyle: SpanStyle = normalTextSpanStyle
        .copy(
            color = LocalTextSelectionColors.current.handleColor,
            background = LocalTextSelectionColors.current.backgroundColor
        ),
    ignoreCase: Boolean = true,
    content: (@Composable (AnnotatedString) -> Unit)
) {

    var lastAnnotationSize = 0
    val highlightedSentencesFiltered = highlightedSentences.filter { it.trim().isNotBlank() }
    val annotatedString = buildAnnotatedString {

        if (highlightedSentencesFiltered.isNotEmpty()) {
            highlightedSentencesFiltered.forEach { highlightString ->
                text.windowed(highlightString.length, step = 1, partialWindows = true) {

                    if (lastAnnotationSize > 0) {
                        lastAnnotationSize--
                        return@windowed
                    }

                    if ((ignoreCase && it.toString().uppercase() == highlightString.uppercase())
                        || (!ignoreCase && it.toString() == highlightString)
                    ) {
                        withStyle(style = highlightedSentencesTextSpanStyle) {
                            pushStringAnnotation(tag = it.toString(), annotation = it.toString())
                            append(it.toString())
                            lastAnnotationSize += it.length-1
                        }
                        return@windowed
                    }

                    withStyle(style = normalTextSpanStyle) {
                        append(it.first())
                    }
                }
            }
        } else
            withStyle(style = normalTextSpanStyle) {
                append(text)
            }
    }

    content(annotatedString)
}