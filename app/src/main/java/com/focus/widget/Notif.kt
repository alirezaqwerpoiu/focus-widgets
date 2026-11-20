package com.focus.widget

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

/** Persistent notification styled like the 4×1 strip widget. Controlled by the web app's setting. */
object Notif {
    private const val CHANNEL = "focus_timer"
    private const val NOTIF_ID = 1
    private const val K_ONGOING = "focusTracker_ongoing"
    private const val FLAGS = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    fun enabled(c: Context): Boolean =
        c.getSharedPreferences("focus_prefs", Context.MODE_PRIVATE).getString(K_ONGOING, "false") == "true"

    private fun canPost(c: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(c, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(c).areNotificationsEnabled()
    }

    private fun ensureChannel(c: Context) {
        val ch = NotificationChannel(CHANNEL, "تایمر Focus", NotificationManager.IMPORTANCE_LOW)
        c.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)
    }

    fun refresh(c: Context) {
        val nm = NotificationManagerCompat.from(c)
        if (!enabled(c) || !canPost(c)) {
            nm.cancel(NOTIF_ID)
            return
        }
        ensureChannel(c)

        val running = Store.isRunning(c)
        val total = Store.todayTotalSec(c)

        val rv = RemoteViews(c.packageName, R.layout.notif_strip)
        rv.setImageViewBitmap(R.id.ring, Ring.iconBitmap(total, 156))
        rv.setTextViewText(R.id.tag, Store.selectedCategory(c))

        if (running) {
            rv.setTextViewText(R.id.status, "در حال تمرکز")
            rv.setInt(R.id.dot, "setColorFilter", android.graphics.Color.parseColor("#3DFFA0"))
            rv.setViewVisibility(R.id.time, View.GONE)
            rv.setViewVisibility(R.id.chrono, View.VISIBLE)
            rv.setChronometer(R.id.chrono, Store.chronoBase(c), null, true)
        } else {
            rv.setTextViewText(R.id.status, "آماده‌ی شروع")
            rv.setInt(R.id.dot, "setColorFilter", android.graphics.Color.parseColor("#555555"))
            rv.setTextViewText(R.id.time, Updater.hm(total))
            rv.setViewVisibility(R.id.time, View.VISIBLE)
            rv.setViewVisibility(R.id.chrono, View.GONE)
        }
        rv.setViewVisibility(R.id.btnPlay, if (running) View.GONE else View.VISIBLE)
        rv.setViewVisibility(R.id.btnStop, if (running) View.VISIBLE else View.GONE)

        val toggle = PendingIntent.getBroadcast(
            c, 31, Intent(c, NotifReceiver::class.java).setAction(Updater.ACTION_TOGGLE), FLAGS
        )
        val openApp = PendingIntent.getActivity(c, 30, Intent(c, MainActivity::class.java), FLAGS)
        rv.setOnClickPendingIntent(R.id.btnWrap, toggle)
        rv.setOnClickPendingIntent(R.id.root, openApp)

        val b = NotificationCompat.Builder(c, CHANNEL)
            .setSmallIcon(R.drawable.ic_stat_focus)
            .setCustomContentView(rv)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        try {
            nm.notify(NOTIF_ID, b.build())
        } catch (e: SecurityException) {
            // Permission was revoked between the check and the call.
        }
    }
}
