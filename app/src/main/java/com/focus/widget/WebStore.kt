package com.focus.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

/** Synchronous key/value bridge used by the web app in place of localStorage. */
class WebStore(private val c: Context) {

    private fun prefs() = c.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE)

    @JavascriptInterface
    fun getItem(key: String): String? = prefs().getString(key, null)

    @JavascriptInterface
    fun setItem(key: String, value: String) {
        prefs().edit().putString(key, value).apply()
        Updater.updateAll(c)
    }

    @JavascriptInterface
    fun removeItem(key: String) {
        prefs().edit().remove(key).apply()
        Updater.updateAll(c)
    }

    /** Asks the launcher to add one of our widgets to the home screen. Returns false if it is not supported. */
    @JavascriptInterface
    fun pinWidget(kind: String): Boolean {
        val cls: Class<*> = when (kind) {
            "tiny" -> TinyWidget::class.java
            "tall" -> TallWidget::class.java
            "small" -> SmallWidget::class.java
            "strip" -> StripWidget::class.java
            "card" -> CardWidget::class.java
            "week" -> WeekWidget::class.java
            "month" -> MonthWidget::class.java
            "ring" -> RingWidget::class.java
            else -> return false
        }
        val mgr = AppWidgetManager.getInstance(c)
        if (!mgr.isRequestPinAppWidgetSupported) return false
        // The bridge runs on a background thread; the request has to be made from the main thread.
        Handler(Looper.getMainLooper()).post {
            mgr.requestPinAppWidget(ComponentName(c, cls), null, null)
        }
        return true
    }
}
