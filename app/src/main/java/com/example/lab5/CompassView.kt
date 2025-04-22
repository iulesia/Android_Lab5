package com.example.lab5

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class CompassView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var direction: Float = 0f
    private val paint = Paint()

    private var directionHistory = listOf<Float>()

    fun updateDirection(dir: Float) {
        direction = dir
        invalidate()
    }

    fun updateDirectionHistory(history: List<String>) {
        directionHistory = history.mapNotNull { labelToDegree(it) }
        invalidate()
    }

    private fun labelToDegree(label: String): Float? {
        return when (label) {
            "N" -> 0f
            "NE" -> 45f
            "E" -> 90f
            "SE" -> 135f
            "S" -> 180f
            "SW" -> 225f
            "W" -> 270f
            "NW" -> 315f
            else -> null
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f
        val radius = (width.coerceAtMost(height) / 2 * 0.8).toFloat()


        paint.color = Color.LTGRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        canvas.drawCircle(cx, cy, radius, paint)

        paint.color = Color.argb(100, 0, 0, 255)
        paint.strokeWidth = 6f
        for (dir in directionHistory) {
            val angleRad = Math.toRadians((-dir).toDouble())
            val hx = (cx + radius * sin(angleRad)).toFloat()
            val hy = (cy - radius * cos(angleRad)).toFloat()
            canvas.drawLine(cx, cy, hx, hy, paint)
        }


        paint.color = Color.RED
        paint.strokeWidth = 10f
        val angle = Math.toRadians((-direction).toDouble())
        val x = (cx + radius * sin(angle)).toFloat()
        val y = (cy - radius * cos(angle)).toFloat()
        canvas.drawLine(cx, cy, x, y, paint)

        paint.color = Color.BLACK
        paint.textSize = 40f
        paint.style = Paint.Style.FILL
        canvas.drawText("N", cx - 20, cy - radius + 50, paint)
    }
}
