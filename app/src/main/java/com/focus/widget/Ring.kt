package com.focus.widget

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF

/** Draws progress rings. RemoteViews cannot draw arcs, so everything is rendered to bitmaps. */
object Ring {
    val BG: Int = Color.parseColor("#0F1119")

    // Same thresholds and colours as the web app.
    private val tiers: List<Pair<Long, String?>> = listOf(
        0L to null, 600L to "#c22e2e", 3600L to "#c28a26", 7200L to "#d4c02a",
        10800L to "#16a34a", 14400L to "#26c2b3", 18000L to "#9c27b0",
        21600L to "#d32f0f", 25200L to "#ff6f00", 28800L to "#ffd700", 32400L to "#8e969f"
    )

    fun colorFor(sec: Long): Int? {
        val hex = tiers.lastOrNull { sec >= it.first }?.second ?: return null
        return Color.parseColor(hex)
    }

    fun drawRing(canvas: Canvas, cx: Float, cy: Float, r: Float, strokeW: Float, sec: Long) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeW
            strokeCap = Paint.Cap.ROUND
        }
        val rect = RectF(cx - r, cy - r, cx + r, cy + r)
        paint.color = Color.argb(26, 255, 255, 255)
        canvas.drawArc(rect, 0f, 360f, false, paint)

        val color = colorFor(sec)
        if (color != null && sec > 0) {
            val fraction = (sec / 14400f).coerceIn(0f, 1f)
            paint.color = color
            canvas.drawArc(rect, -90f, 360f * fraction, false, paint)
        }
    }

    /** Transparent ring for the notification's large icon. */
    fun iconBitmap(sec: Long, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val stroke = size * 0.1f
        drawRing(Canvas(bmp), size / 2f, size / 2f, size / 2f - stroke, stroke, sec)
        return bmp
    }

    /** Standalone square ring. RGB_565 keeps the bitmap small enough for RemoteViews. */
    fun bitmap(sec: Long, size: Int): Bitmap {
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        bmp.eraseColor(BG)
        val stroke = size * 0.08f
        drawRing(Canvas(bmp), size / 2f, size / 2f, size / 2f - stroke, stroke, sec)
        return bmp
    }
}
