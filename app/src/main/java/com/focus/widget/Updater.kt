package com.focus.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews

/** Builds the RemoteViews for every widget and pushes them to the home screen. */
object Updater {
    const val ACTION_TOGGLE = "com.focus.widget.TOGGLE"
    private val RUN_COLOR = Color.parseColor("#FF453A")
    private val START_COLOR = Color.parseColor("#3DFFA0")
    private val DARK = Color.parseColor("#04160D")

    fun hm(sec: Long): String = "%d:%02d".format(sec / 3600, (sec % 3600) / 60)

    private fun openApp(c: Context): PendingIntent =
        PendingIntent.getActivity(
            c, 10, Intent(c, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun toggle(c: Context, cls: Class<*>): PendingIntent =
        PendingIntent.getBroadcast(
            c, 0, Intent(c, cls).setAction(ACTION_TOGGLE),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun statusTxt(running: Boolean) = if (running) "در حال تمرکز" else "آماده‌ی شروع"

    private fun applyTimes(rv: RemoteViews, c: Context, running: Boolean, sec: Long, timeId: Int, chronoId: Int) {
        rv.setTextViewText(timeId, hm(sec))
        rv.setViewVisibility(timeId, if (running) View.GONE else View.VISIBLE)
        rv.setViewVisibility(chronoId, if (running) View.VISIBLE else View.GONE)
        rv.setChronometer(chronoId, Store.chronoBase(c), null, running)
    }

    private fun applyButton(rv: RemoteViews, c: Context, running: Boolean, btnId: Int, cls: Class<*>) {
        rv.setTextViewText(btnId, if (running) "توقف" else "شروع")
        rv.setTextColor(btnId, if (running) Color.WHITE else DARK)
        rv.setInt(btnId, "setBackgroundColor", if (running) RUN_COLOR else START_COLOR)
        rv.setOnClickPendingIntent(btnId, toggle(c, cls))
    }

    fun buildTiny(c: Context): RemoteViews {
        val rv = RemoteViews(c.packageName, R.layout.widget_tiny)
        val running = Store.isRunning(c)
        rv.setImageViewBitmap(R.id.ring, Ring.bitmap(Store.todayTotalSec(c), 160))
        rv.setTextViewText(R.id.icon, if (running) "■" else "▶")
        rv.setTextColor(R.id.icon, if (running) RUN_COLOR else START_COLOR)
        rv.setOnClickPendingIntent(R.id.root, toggle(c, TinyWidget::class.java))
        return rv
    }

    fun buildSmall(c: Context): RemoteViews {
        val rv = RemoteViews(c.packageName, R.layout.widget_small)
        val running = Store.isRunning(c)
        val sec = Store.todayTotalSec(c)
        rv.setImageViewBitmap(R.id.ring, Ring.bitmap(sec, 180))
        applyTimes(rv, c, running, sec, R.id.time, R.id.chrono)
        applyButton(rv, c, running, R.id.btn, SmallWidget::class.java)
        rv.setOnClickPendingIntent(R.id.root, openApp(c))
        return rv
    }

    fun buildTall(c: Context): RemoteViews {
        val rv = RemoteViews(c.packageName, R.layout.widget_tall)
        val running = Store.isRunning(c)
        val sec = Store.todayTotalSec(c)
        rv.setImageViewBitmap(R.id.ring, Ring.bitmap(sec, 160))
        rv.setTextViewText(R.id.status, statusTxt(running))
        applyTimes(rv, c, running, sec, R.id.time, R.id.chrono)
        applyButton(rv, c, running, R.id.btn, TallWidget::class.java)
        rv.setOnClickPendingIntent(R.id.root, openApp(c))
        return rv
    }

    fun buildCard(c: Context): RemoteViews {
        val rv = RemoteViews(c.packageName, R.layout.widget_card)
        val running = Store.isRunning(c)
        val sec = Store.todayTotalSec(c)
        val pct = (sec * 100L / Store.goalSec(c)).coerceIn(0L, 100L).toInt()
        rv.setTextViewText(R.id.status, statusTxt(running))
        applyTimes(rv, c, running, sec, R.id.time, R.id.chrono)
        rv.setTextViewText(R.id.pct, "$pct% از هدف ۴ ساعت")
        rv.setProgressBar(R.id.bar, 100, pct, false)
        applyButton(rv, c, running, R.id.btn, CardWidget::class.java)
        rv.setOnClickPendingIntent(R.id.root, openApp(c))
        return rv
    }

    fun buildStrip(c: Context): RemoteViews {
        val rv = RemoteViews(c.packageName, R.layout.widget_strip)
        val running = Store.isRunning(c)
        val sec = Store.todayTotalSec(c)
        rv.setImageViewBitmap(R.id.ring, Ring.bitmap(sec, 150))
        rv.setTextViewText(R.id.status, statusTxt(running))
        applyTimes(rv, c, running, sec, R.id.time, R.id.chrono)
        applyButton(rv, c, running, R.id.btn, StripWidget::class.java)
        rv.setOnClickPendingIntent(R.id.root, openApp(c))
        return rv
    }

    fun buildWeek(c: Context): RemoteViews {
        val rv = RemoteViews(c.packageName, R.layout.widget_week)
        val start = Persian.weekStart()
        val cur = Charts.weekTotal(c, start)
        val prev = Charts.weekTotal(c, Persian.addDays(start, -7))
        val delta = when {
            cur == 0L && prev == 0L -> ""
            prev == 0L -> "این هفته ${hm(cur)} کار کردی"
            cur > prev -> "${hm(cur - prev)} بیشتر از هفته‌ی قبل"
            cur < prev -> "${hm(prev - cur)} کمتر از هفته‌ی قبل"
            else -> "دقیقاً مثل هفته‌ی قبل"
        }
        rv.setTextViewText(R.id.weekTotal, hm(cur))
        rv.setTextViewText(R.id.delta, delta)
        rv.setImageViewBitmap(R.id.weekImg, Charts.week(c))
        rv.setOnClickPendingIntent(R.id.root, openApp(c))
        return rv
    }

    fun buildMonth(c: Context): RemoteViews {
        val rv = RemoteViews(c.packageName, R.layout.widget_month)
        val now = Persian.now()
        val name = "${Persian.months[now.get(android.icu.util.Calendar.MONTH)]} ${now.get(android.icu.util.Calendar.YEAR)}"
        rv.setTextViewText(R.id.monthName, name)
        rv.setTextViewText(R.id.monthTotal, hm(Charts.monthTotal(c)))
        rv.setImageViewBitmap(R.id.calImg, Charts.month(c))
        rv.setOnClickPendingIntent(R.id.root, openApp(c))
        return rv
    }

    fun buildRing(c: Context): RemoteViews {
        val rv = RemoteViews(c.packageName, R.layout.widget_ring)
        val running = Store.isRunning(c)
        val sec = Store.todayTotalSec(c)
        rv.setImageViewBitmap(R.id.ring, Ring.bitmap(sec, 180))
        rv.setImageViewResource(R.id.icon, if (running) R.drawable.ic_stop else R.drawable.ic_play)
        rv.setTextViewText(R.id.status, statusTxt(running))
        rv.setTextViewText(R.id.time, hm(sec))
        rv.setOnClickPendingIntent(R.id.btn, toggle(c, RingWidget::class.java))
        rv.setOnClickPendingIntent(R.id.root, openApp(c))
        return rv
    }

    private val BG_RES = intArrayOf(
        R.drawable.bg_widget_a20, R.drawable.bg_widget_a40, R.drawable.bg_widget_a60,
        R.drawable.bg_widget_a80, R.drawable.bg_widget_a100
    )

    /** Sets this widget instance's background opacity (every layout has a root view). */
    fun applyBg(rv: RemoteViews, c: Context, widgetId: Int) {
        rv.setInt(R.id.root, "setBackgroundResource", BG_RES[Store.alphaLevel(c, widgetId).coerceIn(0, 4)])
    }

    fun updateAll(c: Context) {
        val mgr = AppWidgetManager.getInstance(c)
        val providers: List<Pair<Class<*>, (Context) -> RemoteViews>> = listOf(
            TinyWidget::class.java to { ctx: Context -> buildTiny(ctx) },
            SmallWidget::class.java to { ctx: Context -> buildSmall(ctx) },
            TallWidget::class.java to { ctx: Context -> buildTall(ctx) },
            CardWidget::class.java to { ctx: Context -> buildCard(ctx) },
            StripWidget::class.java to { ctx: Context -> buildStrip(ctx) },
            WeekWidget::class.java to { ctx: Context -> buildWeek(ctx) },
            MonthWidget::class.java to { ctx: Context -> buildMonth(ctx) },
            RingWidget::class.java to { ctx: Context -> buildRing(ctx) }
        )
        for ((cls, build) in providers) {
            for (id in mgr.getAppWidgetIds(ComponentName(c, cls))) {
                val rv = build(c)
                applyBg(rv, c, id)
                mgr.updateAppWidget(id, rv)
            }
        }
        Notif.refresh(c)
    }
}
