package br.com.frazo.janac.audio.ui.compose.materialv3

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import br.com.frazo.janac.audio.player.AudioPlayerStatus
import br.com.frazo.janac.audio.player.AudioPlayingData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioPlayer(
    modifier: Modifier = Modifier,
    audioPlayingData: AudioPlayingData,
    timeLabelStyle: TextStyle = LocalTextStyle.current,
    playIcon: @Composable () -> Unit,
    onPlay: () -> Unit,
    pauseIcon: @Composable () -> Unit,
    onPause: () -> Unit,
    onSeekPosition: (Float) -> Unit,
    endIcon: (@Composable () -> Unit)? = null,
    onEndIconClicked: (() -> Unit)? = null
) {

    val progress = remember(audioPlayingData) {
        if (audioPlayingData.duration == 0L || audioPlayingData.status == AudioPlayerStatus.NOT_INITIALIZED)
            return@remember 0f
        with(audioPlayingData) {
            return@remember elapsed / duration.toFloat()
        }
    }

    val sliderInteractionSource = MutableInteractionSource()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize()
            .animateContentSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            modifier = Modifier.wrapContentSize(),
            onClick = {
                when (audioPlayingData.status) {
                    AudioPlayerStatus.NOT_INITIALIZED, AudioPlayerStatus.PAUSED -> onPlay()
                    AudioPlayerStatus.PLAYING -> onPause()
                }
            }) {
            when (audioPlayingData.status) {
                AudioPlayerStatus.NOT_INITIALIZED, AudioPlayerStatus.PAUSED -> playIcon()
                AudioPlayerStatus.PLAYING -> pauseIcon()
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Slider(
                value = progress,
                onValueChange = onSeekPosition,
                valueRange = (0f..1f),
                thumb = {
                    BadgedBox(
                        badge = {
                            val minutes = audioPlayingData.elapsed / 1000 / 60
                            val seconds = audioPlayingData.elapsed / 1000 % 60
                            Badge(modifier = Modifier.align(Alignment.TopCenter)) {
                                Text(
                                    text = "${minutes.toString().padStart(2, '0')}:${
                                        seconds.toString().padStart(2, '0')
                                    }",
                                    style = timeLabelStyle
                                )
                            }
                        }) {
                        SliderDefaults.Thumb(interactionSource = sliderInteractionSource)
                    }
                })
        }

        if (endIcon != null && onEndIconClicked != null) {
            IconButton(
                modifier = Modifier
                    .wrapContentSize(),
                onClick = {
                    onEndIconClicked()
                }) {
                endIcon()
            }
        }
    }
}

//TODO(Viewmodel independent audioplayer)

//@Composable
//fun AudioPlayer(
//    modifier: Modifier = Modifier,
//    coroutineScope: CoroutineScope = rememberCoroutineScope(),
//    playIcon: @Composable () -> Unit,
//    pauseIcon: @Composable () -> Unit,
//    deleteIcon: (@Composable () -> Unit)? = null,
//    timeLabelStyle: TextStyle = LocalTextStyle.current,
//    audioPlayer: AudioPlayer,
//    audioFile: File,
//    onDelete: (() -> Unit)? = null,
//    onError: (Throwable) -> Unit
//) {
//
//    val audioPlayerData = rememberSaveable {
//        mutableStateOf(AudioPlayingData(AudioPlayerStatus.NOT_INITIALIZED, 0, 0))
//    }
//
//    AudioPlayer(
//        modifier = modifier,
//        audioPlayingData = audioPlayerData.value,
//        playIcon = playIcon,
//        pauseIcon = pauseIcon,
//        onPlay = {
//            if (audioPlayerData.value.status == AudioPlayerStatus.NOT_INITIALIZED) {
//                coroutineScope.launch {
//                    audioPlayer.start(audioFile).catch {
//                        onError(it)
//                    }.collectLatest {
//                        audioPlayerData.value = it
//                    }
//                }
//            } else {
//                audioPlayer.resume()
//            }
//        },
//        onPause = {
//            audioPlayer.pause()
//        },
//        timeLabelStyle = timeLabelStyle,
//        onSeekPosition = {
//            audioPlayer.seek((it * audioPlayerData.value.duration).toLong())
//        },
//        endIcon = deleteIcon,
//        onEndIconClicked = onDelete
//    )
//}