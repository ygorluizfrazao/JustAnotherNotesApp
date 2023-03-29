package br.com.frazo.janac.audio.recorder.visualizer

import android.graphics.Canvas
import br.com.frazo.janac.audio.recorder.AudioRecordingData

interface RecordingVisualizer {

    fun drawGraphics(canvas: Canvas,
                     width: Float = canvas.width.toFloat(),
                     height: Float = canvas.height.toFloat(),
                     offsetX: Float = 0f,
                     offsetY: Float = 0f,
                     audioRecordingData: List<AudioRecordingData>)
}