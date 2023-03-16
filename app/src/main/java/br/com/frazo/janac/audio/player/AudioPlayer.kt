package br.com.frazo.janac.audio.player

import java.io.File

interface AudioPlayer {

    var audioFile: File?

    fun start()

    fun pause()

    fun stop()

}