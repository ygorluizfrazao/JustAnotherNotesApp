package br.com.frazo.janac.audio.ui.visualizer

import android.graphics.Canvas

interface RecordingVisualizer {

    fun drawGraphics(canvas: Canvas,
                     width: Float = canvas.width.toFloat(),
                     height: Float = canvas.height.toFloat(),
                     offsetX: Float = 0f,
                     offsetY: Float = 0f,
                     amplitudes: List<Float>)
}