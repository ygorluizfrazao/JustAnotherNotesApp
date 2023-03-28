package br.com.frazo.janac.ui.util.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
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
import br.com.frazo.janac.ui.util.IconResource
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.schedule
import kotlin.random.Random

@Composable
fun AudioRecordController(
    modifier: Modifier = Modifier,
    recordIconResource: IconResource = IconResource.fromImageVector(Icons.Default.Mic),
    stopRecordingIconResource: IconResource = IconResource.fromImageVector(Icons.Default.Stop),
    lineColor: Color = LocalContentColor.current,
    timeLabelStyle: TextStyle = LocalTextStyle.current,
    amplitudesList: List<Float> = emptyList()
) {

    var amplitudes by remember {
        mutableStateOf(amplitudesList)
    }

    var isRunning by remember {
        mutableStateOf(false)
    }

    var timer: Timer? by remember {
        mutableStateOf(Timer())
    }

    var elapsedTime by remember {
        mutableStateOf(0L)
    }

    val scope = rememberCoroutineScope()

    Row(
        modifier = modifier
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.wrapContentSize(),
            onClick = {
                scope.launch {
                    if (isRunning) {
                        amplitudes = emptyList()
                        timer?.cancel()
                        isRunning = false
                        elapsedTime = 0
                    } else {
                        timer = Timer()
                        elapsedTime = 0
                        timer?.schedule(0, 100) {
                            if (amplitudes.size >= 30)
                                amplitudes = amplitudes - amplitudes.first()
                            amplitudes = amplitudes + Random.nextFloat()
                            elapsedTime += 100
                        }
                        isRunning = true
                    }
                }
            }) {
            if (isRunning)
                stopRecordingIconResource.ComposeIcon()
            else
                recordIconResource.ComposeIcon()
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
                        .defaultMinSize(minHeight = 16.dp)
                        .padding(horizontal = 12.dp)
                        .drawWithContent {
                            val canvasWidth = size.width
                            val canvasHeight = size.height
                            drawLine(
                                cap = StrokeCap.Butt,
                                strokeWidth = 2f,
                                start = Offset(x = 0f, y = canvasHeight / 2),
                                end = Offset(x = canvasWidth, y = canvasHeight / 2),
                                color = Color.Blue,
                            )
                            if (amplitudes.isNotEmpty()) {
                                val maxAmp = amplitudes.max()
                                var lastXR = canvasWidth / 2f
                                var lastXL = canvasWidth / 2f
                                var lastY = 1f
                                val stepX = canvasWidth / 2f / amplitudes.size

                                amplitudes
                                    .asReversed()
                                    .forEach {
                                        val nextY =
                                            canvasHeight - ((it / maxAmp) * canvasHeight)
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
                val minutes = elapsedTime / 1000 / 60
                val seconds = elapsedTime / 1000 % 60
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