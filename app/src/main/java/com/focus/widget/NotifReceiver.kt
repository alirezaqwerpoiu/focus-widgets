package com.focus.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Handles the start/stop button on the notification. */
class NotifReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, intent: Intent) {
        if (intent.action == Updater.ACTION_TOGGLE) {
            Store.toggle(c)
            Updater.updateAll(c)
        }
    }
}
