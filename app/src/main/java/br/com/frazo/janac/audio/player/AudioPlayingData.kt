package br.com.frazo.janac.audio.player

data class AudioPlayingData(val status: AudioPlayerStatus, val duration: Long, val elapsed: Long)

enum class AudioPlayerStatus{
    NOT_INITIALIZED, PLAYING, PAUSED
}