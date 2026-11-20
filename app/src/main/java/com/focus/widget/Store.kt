package com.focus.widget

import android.content.Context
import android.os.SystemClock
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

/**
 * Same data as the web app. The web app stores its state in localStorage, which is bridged
 * into the same SharedPreferences file, so the widgets and the app always agree.
 */
object Store {
    private const val PREFS = "focus_prefs"
    private const val K_DAILY = "focusTracker_daily"
    private const val K_STATE = "focusTracker_state"
    private const val K_GOAL = "focusTracker_goal"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun keyOf(millis: Long): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
    private fun todayKey(): String = keyOf(System.currentTimeMillis())

    private fun readDaily(c: Context): JSONObject =
        try { JSONObject(prefs(c).getString(K_DAILY, "{}") ?: "{}") } catch (e: Exception) { JSONObject() }

    private fun readState(c: Context): JSONObject =
        try { JSONObject(prefs(c).getString(K_STATE, "{}") ?: "{}") } catch (e: Exception) { JSONObject() }

    /** Selected category, stored by the web app as a plain string. */
    fun selectedCategory(c: Context): String =
        prefs(c).getString("focusTracker_selectedCategory", null)?.trim('"')?.takeIf { it.isNotEmpty() } ?: "بدون دسته"

    /** Background opacity level per widget: 0..4 = 20, 40, 60, 80, 100 %. Default is 100 %. */
    fun alphaLevel(c: Context, widgetId: Int): Int = prefs(c).getInt("wid_alpha_$widgetId", 4)

    fun setAlphaLevel(c: Context, widgetId: Int, level: Int) {
        prefs(c).edit().putInt("wid_alpha_$widgetId", level.coerceIn(0, 4)).apply()
    }

    fun goalSec(c: Context): Long = prefs(c).getString(K_GOAL, null)?.toLongOrNull() ?: 14400L

    fun isRunning(c: Context): Boolean {
        val st = readState(c)
        return st.optBoolean("isRunning", false) && st.optString("startTime", "null") != "null"
    }

    /** Same formula as the web app's live(): elapsedSeconds + (now - startTime). */
    private fun sessionSec(c: Context): Long {
        if (!isRunning(c)) return 0L
        val st = readState(c)
        val start = Instant.parse(st.getString("startTime")).toEpochMilli()
        val elapsed = st.optLong("elapsedSeconds", 0L)
        return elapsed + maxOf(0L, (System.currentTimeMillis() - start) / 1000L)
    }

    /** Total for the day containing [millis], including the running session when it is today. */
    fun totalFor(c: Context, millis: Long): Long {
        val key = keyOf(millis)
        val base = readDaily(c).optLong(key, 0L)
        return if (key == todayKey()) base + sessionSec(c) else base
    }

    fun todayTotalSec(c: Context): Long = totalFor(c, System.currentTimeMillis())

    fun setDay(c: Context, key: String, sec: Long) {
        val d = readDaily(c)
        d.put(key, sec)
        prefs(c).edit().putString(K_DAILY, d.toString()).apply()
    }

    fun allDays(c: Context): Map<String, Long> {
        val d = readDaily(c)
        val out = HashMap<String, Long>()
        d.keys().asSequence().forEach { k -> out[k] = d.optLong(k, 0L) }
        out[todayKey()] = todayTotalSec(c)
        return out
    }

    /** Base for the Chronometer so that it shows today's total and keeps counting while running. */
    fun chronoBase(c: Context): Long = SystemClock.elapsedRealtime() - todayTotalSec(c) * 1000L

    fun toggle(c: Context) {
        if (isRunning(c)) stop(c) else start(c)
    }

    private fun start(c: Context) {
        val now = Instant.ofEpochMilli(System.currentTimeMillis()).toString()
        val st = JSONObject()
            .put("isRunning", true)
            .put("startTime", now)
            .put("elapsedSeconds", 0)
            .put("sessStart", now)
            .put("task", "")
        prefs(c).edit().putString(K_STATE, st.toString()).apply()
    }

    private fun stop(c: Context) {
        val total = sessionSec(c)
        val key = todayKey()
        val d = readDaily(c)
        d.put(key, d.optLong(key, 0L) + total)
        val st = JSONObject()
            .put("isRunning", false)
            .put("startTime", JSONObject.NULL)
            .put("elapsedSeconds", 0)
        prefs(c).edit()
            .putString(K_DAILY, d.toString())
            .putString(K_STATE, st.toString())
            .apply()
    }
}
