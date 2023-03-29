package br.com.frazo.janac.audio.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class AndroidAudioRecorder(private val context: Context) :
    AudioRecorder {

    private val UPDATE_DATA_INTERVAL_MILLIS = 100L

    private var recorder: MediaRecorder? = null
    private var audioRecordingDataFlowID: String? = null

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }
    }

    override fun startRecording(outputFile: File): Flow<AudioRecordingData> {
        stopRecording()
        recorder = createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)
            prepare()
            start()
        }

        UUID.randomUUID().toString().apply {
            audioRecordingDataFlowID = this
            return startFlowingAudioRecordingData(this)
        }

    }

    override fun stopRecording() {
        audioRecordingDataFlowID = null
        recorder?.stop()
        recorder?.release()
        recorder = null
    }

    override fun pause() {
        recorder?.pause()
    }

    override fun resume() {
        recorder?.resume()
    }

    private fun startFlowingAudioRecordingData(flowId: String): Flow<AudioRecordingData> {

        return flow {

            val startTime = System.currentTimeMillis()
            var lastEmitTime = startTime

            while (true) {
                val currentTime = System.currentTimeMillis()
                val elapsedTime = currentTime - startTime
                val cycleTime = currentTime - lastEmitTime
                lastEmitTime = currentTime

                //finishes the previous flow
                if (flowId != audioRecordingDataFlowID)
                    break

                emit(AudioRecordingData(elapsedTime, cycleTime, recorder?.maxAmplitude ?: 0))
                delay(UPDATE_DATA_INTERVAL_MILLIS)
            }
        }
    }
}