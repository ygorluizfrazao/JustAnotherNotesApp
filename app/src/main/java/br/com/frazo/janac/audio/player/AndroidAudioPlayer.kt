package br.com.frazo.janac.audio.player

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File

class AndroidAudioPlayer(
    private val context: Context,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : AudioPlayer {

    private val UPDATE_DATA_INTERVAL_MILLIS = 200L

    private var player: MediaPlayer? = null
    private val _audioPlayingData =
        MutableStateFlow(AudioPlayingData(AudioPlayerStatus.NOT_INITIALIZED, 0, 0))

    override fun start(file: File): Flow<AudioPlayingData> {

        stop()

        player = MediaPlayer.create(context, file.toUri()).apply {
            start()
            setOnCompletionListener {
                this@AndroidAudioPlayer.stop()
            }
        }

        _audioPlayingData.value = _audioPlayingData.value.copy(status = AudioPlayerStatus.PLAYING)
        CoroutineScope(dispatcher).launch {
            startFlowing(player!!.audioSessionId)
                .collectLatest {
                    _audioPlayingData.value = it
                }
        }
        return _audioPlayingData.asStateFlow()
    }

    override fun pause() {
        player?.let {
            it.pause()
            _audioPlayingData.value =
                _audioPlayingData.value.copy(status = AudioPlayerStatus.PAUSED)
        }
    }

    override fun resume() {
        player?.let {
            if (!it.isPlaying)
                it.start()
            _audioPlayingData.value =
                _audioPlayingData.value.copy(status = AudioPlayerStatus.PLAYING)
        }
    }

    override fun stop() {
        player?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.reset()
            it.release()
        }
        player = null
        _audioPlayingData.value = AudioPlayingData(AudioPlayerStatus.NOT_INITIALIZED, 0, 0)
    }

    override fun seek(position: Long) {
        player?.let {
            if(_audioPlayingData.value.status!=AudioPlayerStatus.NOT_INITIALIZED){
                it.seekTo(position.toInt())
            }
        }
    }


    private fun startFlowing(audioSeasonId: Int): Flow<AudioPlayingData> {
        return flow {
            while (true) {
                //finishes the flow
                if (player == null)
                    return@flow

                player?.let {


                    if (it.audioSessionId != audioSeasonId)
                        return@flow
                    val newData = _audioPlayingData.value.copy(
                        duration = it.duration.toLong(),
                        elapsed = it.currentPosition.toLong()
                    )
                    emit(newData)
                }
                delay(UPDATE_DATA_INTERVAL_MILLIS)
            }
        }
    }
}

