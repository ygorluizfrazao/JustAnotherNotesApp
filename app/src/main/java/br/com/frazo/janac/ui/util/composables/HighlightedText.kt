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

    val highlightedSentencesFiltered =
        highlightedSentences.filter { it.trim().isNotBlank() }.distinct()

    var annotatedString = buildAnnotatedString {
        withStyle(style = normalTextSpanStyle) {
            append(text)
        }
    }

    highlightedSentencesFiltered.forEach { highlightString ->

        annotatedString = buildAnnotatedString {

            var currentRange = (0..0)
            var lastAnnotationSizeAdded = 0

            annotatedString.windowed(
                highlightString.length,
                step = 1,
                partialWindows = true
            ) { windowChars ->

                currentRange = (currentRange.last..currentRange.last + 1)

                if (lastAnnotationSizeAdded > 0) {
                    lastAnnotationSizeAdded--
                    return@windowed
                }

                if (windowChars.first().toString().isBlank()) {
                    withStyle(style = normalTextSpanStyle) {
                        append(windowChars.first())
                    }
                    return@windowed
                }

                val existingAnnotationsInRange =
                    annotatedString.getStringAnnotations(currentRange.first, currentRange.last)
                if (existingAnnotationsInRange.isNotEmpty()) {
                    existingAnnotationsInRange.forEach { existingAnnotation ->
                        withStyle(style = highlightedSentencesTextSpanStyle) {
                            pushStringAnnotation(
                                tag = existingAnnotation.tag,
                                annotation = existingAnnotation.item
                            )
                            append(existingAnnotation.item)
                            lastAnnotationSizeAdded += existingAnnotation.item.length
                        }
                    }
                    lastAnnotationSizeAdded -= 1
                    return@windowed
                }

                if ((ignoreCase && windowChars.toString()
                        .uppercase() == highlightString.uppercase())
                    || (!ignoreCase && windowChars.toString() == highlightString)
                ) {
                    withStyle(style = highlightedSentencesTextSpanStyle) {
                        pushStringAnnotation(
                            tag = windowChars.toString(),
                            annotation = windowChars.toString()
                        )
                        append(windowChars.toString())
                        lastAnnotationSizeAdded += windowChars.length - 1
                    }
                    return@windowed
                }

                withStyle(style = normalTextSpanStyle) {
                    append(windowChars.first())
                }

            }
        }

    }
    content(annotatedString)
}