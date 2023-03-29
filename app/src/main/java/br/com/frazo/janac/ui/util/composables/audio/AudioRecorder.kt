package br.com.frazo.janac.ui.util.composables.audio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import br.com.frazo.janac.audio.recorder.AudioRecordingData
import br.com.frazo.janac.audio.recorder.visualizer.MirrorWaveRecordingVisualizer
import br.com.frazo.janac.audio.recorder.visualizer.RecordingVisualizer

@Composable
fun AudioRecorder(
    modifier: Modifier = Modifier,
    recordIcon: @Composable () -> Unit,
    stopIcon: @Composable () -> Unit,
    recordingWaveVisualizer: RecordingVisualizer = MirrorWaveRecordingVisualizer(),
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
                stopIcon()
            else
                recordIcon()
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
                            drawIntoCanvas { canvas ->
                                recordingWaveVisualizer.drawGraphics(
                                    canvas = canvas.nativeCanvas,
                                    audioRecordingData = amplitudes,
                                    width = size.width,
                                    height = size.height
                                )
                            }
                        }
                )
                if(audioRecordingData.isNotEmpty()){
                    val minutes = audioRecordingData.last().elapsedTime / 1000 / 60
                    val seconds = audioRecordingData.last().elapsedTime / 1000 % 60
                    Text(
                        text = "${minutes.toString().padStart(2, '0')}:${
                            seconds.toString().padStart(2, '0')
                        }",
                        style = timeLabelStyle
                    )
                }else{
                    Text(
                        text = "00:00",
                        style = timeLabelStyle
                    )
                }
            }
        }
    }
}