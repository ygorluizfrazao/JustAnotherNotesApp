package br.com.frazo.janac.audio.recorder

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRecorder {

    fun startRecording(outputFile: File): Flow<AudioRecordingData>

    fun stopRecording()

}