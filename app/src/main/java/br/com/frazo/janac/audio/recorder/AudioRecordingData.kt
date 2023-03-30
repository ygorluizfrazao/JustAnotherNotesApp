package br.com.frazo.janac.audio.recorder

sealed class AudioRecordingData {
    object NotStarted: AudioRecordingData()
    data class Recording(val elapsedTime: Long, val maxAmplitudeInCycle: Int): AudioRecordingData()
    data class Paused(val elapsedTime: Long, val maxAmplitudeInCycle: Int): AudioRecordingData()
}