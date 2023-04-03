package br.com.frazo.janac.audio.player

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class AudioPlayingData(val status: AudioPlayerStatus, val duration: Long, val elapsed: Long) :
    Parcelable

enum class AudioPlayerStatus{
    NOT_INITIALIZED, PLAYING, PAUSED
}