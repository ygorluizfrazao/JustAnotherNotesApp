package br.com.frazo.janac.ui.util.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import br.com.frazo.janac.audio.recorder.AudioRecordingData

@Composable
fun AudioRecordController(
    modifier: Modifier = Modifier,
    recordIconResource: @Composable () -> Unit,
    stopRecordingIconResource: @Composable () -> Unit,
    lineColor: Color = LocalContentColor.current,
    axisColor: Color = LocalTextSelectionColors.current.handleColor,
    timeLabelStyle: TextStyle = LocalTextStyle.current,
    audioRecordingData: List<AudioRecordingData> = emptyList(),
    onRecordRequested: () -> Unit,
    onStopRequested: () -> Unit
) {

    val amplitudes by remember(audioRecordingData) {
            mutableStateOf(audioRecordingData)
    }

    var isRunning by remember {
        mutableStateOf(false)
    }

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .animateContentSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.wrapContentSize(),
            onClick = {
                isRunning = if (isRunning) {
                    onStopRequested()
                    false
                } else {
                    onRecordRequested()
                    true
                }
            }) {
            if (isRunning)
                stopRecordingIconResource()
            else
                recordIconResource()
        }

        AnimatedVisibility(
            visible = amplitudes.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            Row(
                modifier = modifier
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 32.dp)
                        .padding(end = 12.dp)
                        .drawWithContent {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            drawLine(
                                cap = StrokeCap.Butt,
                                strokeWidth = 2f,
                                start = Offset(x = 0f, y = canvasHeight / 2),
                                end = Offset(x = canvasWidth, y = canvasHeight / 2),
                                color = axisColor,
                            )
                            val maxAmp =
                                amplitudes.maxOfOrNull { it.maxAmplitudeInCycle.toFloat() }
                            maxAmp?.let {
                                var lastXR = canvasWidth / 2f
                                var lastXL = canvasWidth / 2f
                                var lastY = 1f
                                val stepX = canvasWidth / 2f / amplitudes.size

                                amplitudes
                                    .asReversed()
                                    .forEach {
                                        val nextY =
                                            canvasHeight - ((it.maxAmplitudeInCycle.toFloat() / maxAmp) * canvasHeight)
                                        drawLine(
                                            strokeWidth = 2f,
                                            start = Offset(x = lastXR, y = lastY),
                                            end = Offset(x = lastXR + stepX, y = nextY),
                                            color = lineColor
                                        )
                                        drawLine(
                                            strokeWidth = 2f,
                                            start = Offset(x = lastXL, y = lastY),
                                            end = Offset(x = lastXL - stepX, y = nextY),
                                            color = lineColor
                                        )

                                        lastXR += stepX
                                        lastXL -= stepX
                                        lastY = nextY
                                    }

                            }
                        }
                )
                val minutes = audioRecordingData.last().elapsedTime / 1000 / 60
                val seconds = audioRecordingData.last().elapsedTime / 1000 % 60
                Text(
                    text = "${minutes.toString().padStart(2, '0')}:${
                        seconds.toString().padStart(2, '0')
                    }",
                    style = timeLabelStyle
                )
            }
        }
    }
}