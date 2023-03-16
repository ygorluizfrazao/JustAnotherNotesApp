package br.com.frazo.janac.audio.player

import android.content.Context
import android.media.MediaPlayer
import androidx.core.net.toUri
import java.io.File
import java.io.IOException

class AndroidAudioPlayer(private val context: Context, audioFile: File? = null) : AudioPlayer {

    private var player: MediaPlayer? = null
    override var audioFile: File? = audioFile
        set(value) {
            stop()
            field = value
        }


    override fun start() {


        player?.let {
            if (!it.isPlaying)
                it.start()
            return
        }

        audioFile?.let {
            player = MediaPlayer.create(context, it.toUri())
            player?.start()
            return
        }

        throw IOException("File is null, cannot initialize")
    }

    override fun pause() {
        player?.pause()
    }

    override fun stop() {
        player?.stop()
        player?.release()
        player = null
    }

}