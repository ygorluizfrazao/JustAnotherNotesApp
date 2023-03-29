package br.com.frazo.janac.audio.player

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioPlayer {

    fun start(file: File): Flow<AudioPlayingData>

    fun pause()

    fun resume()

    fun stop()

}