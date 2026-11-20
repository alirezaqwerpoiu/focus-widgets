package com.focus.widget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.icu.util.Calendar
import android.icu.util.PersianCalendar
import kotlin.math.min

/** Persian calendar helpers. Weeks start on Saturday, as in the web app. */
object Persian {
    val months = listOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )
    val weekdays = listOf("ش", "ی", "د", "س", "چ", "پ", "ج")

    fun now(): PersianCalendar = PersianCalendar()

    fun addDays(millis: Long, n: Int): Long {
        val pc = PersianCalendar()
        pc.timeInMillis = millis
        pc.add(Calendar.DAY_OF_MONTH, n)
        return pc.timeInMillis
    }

    fun dayOfMonth(millis: Long): Int {
        val pc = PersianCalendar()
        pc.timeInMillis = millis
        return pc.get(Calendar.DAY_OF_MONTH)
    }

    /** Saturday of the current Persian week. */
    fun weekStart(): Long {
        val now = PersianCalendar()
        val offset = now.get(Calendar.DAY_OF_WEEK) % 7 // Saturday = 0 ... Friday = 6
        return addDays(now.timeInMillis, -offset)
    }

    fun monthStart(y: Int, m: Int): Long {
        val pc = PersianCalendar()
        pc.clear()
        pc.set(y, m, 1)
        return pc.timeInMillis
    }
}

object Charts {
    private fun canvasBitmap(w: Int, h: Int): Bitmap =
        Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565).apply { eraseColor(Ring.BG) }

    private fun text(cv: Canvas, s: String, x: Float, y: Float, size: Float, color: Int, bold: Boolean = false) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            textAlign = Paint.Align.CENTER
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
        val baseline = y - (p.descent() + p.ascent()) / 2f
        cv.drawText(s, x, baseline, p)
    }

    fun weekTotal(c: Context, start: Long): Long =
        (0..6).fold(0L) { acc, i -> acc + Store.totalFor(c, Persian.addDays(start, i)) }

    fun monthTotal(c: Context): Long {
        val now = Persian.now()
        val first = Persian.monthStart(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        val days = PersianCalendar().also { it.timeInMillis = first }.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (0 until days).fold(0L) { acc, i -> acc + Store.totalFor(c, Persian.addDays(first, i)) }
    }

    /** 4x2 week strip: seven rings, Saturday to Friday. */
    fun week(c: Context): Bitmap {
        val w = 600
        val h = 260
        val bmp = canvasBitmap(w, h)
        val cv = Canvas(bmp)
        val start = Persian.weekStart()
        val todayKey = Store.keyOf(System.currentTimeMillis())
        val cellW = w / 7f
        val r = min(cellW * 0.36f, 36f)
        for (i in 0 until 7) {
            val millis = Persian.addDays(start, i)
            val isToday = Store.keyOf(millis) == todayKey
            val cx = cellW * (i + 0.5f)
            val cy = 112f
            Ring.drawRing(cv, cx, cy, r, r * 0.22f, Store.totalFor(c, millis))
            text(cv, Persian.dayOfMonth(millis).toString(), cx, cy, 24f, Color.WHITE, isToday)
            text(cv, Persian.weekdays[i], cx, 232f, 22f,
                if (isToday) Color.WHITE else Color.parseColor("#8B919C"), isToday)
        }
        return bmp
    }

    /** 4x4 month grid: the same calendar as the web app, with future days dimmed. */
    fun month(c: Context): Bitmap {
        val w = 480
        val h = 480
        val bmp = canvasBitmap(w, h)
        val cv = Canvas(bmp)
        val now = Persian.now()
        val todayDay = now.get(Calendar.DAY_OF_MONTH)
        val first = Persian.monthStart(now.get(Calendar.YEAR), now.get(Calendar.MONTH))
        val firstCal = PersianCalendar().also { it.timeInMillis = first }
        val offset = firstCal.get(Calendar.DAY_OF_WEEK) % 7
        val days = firstCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        val cellW = w / 7f
        val gridTop = 44f
        val cellH = (h - gridTop) / 6f
        val muted = Color.parseColor("#8B919C")
        for (i in 0 until 7) text(cv, Persian.weekdays[i], cellW * (i + 0.5f), 22f, 20f, muted)

        val r = min(cellW, cellH) * 0.34f
        for (n in 1..days) {
            val idx = offset + n - 1
            val cx = cellW * ((idx % 7) + 0.5f)
            val cy = gridTop + cellH * ((idx / 7) + 0.5f)
            val future = n > todayDay
            val millis = Persian.addDays(first, n - 1)
            Ring.drawRing(cv, cx, cy, r, r * 0.22f, if (future) 0L else Store.totalFor(c, millis))
            val color = if (future) Color.parseColor("#5B616D") else Color.WHITE
            text(cv, n.toString(), cx, cy, r * 0.8f, color, n == todayDay)
        }
        return bmp
    }
}
