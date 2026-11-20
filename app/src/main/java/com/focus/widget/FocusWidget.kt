package com.focus.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

/** Shared behaviour for all Focus widgets: draw on update, toggle the timer on the toggle action. */
abstract class FocusWidget : AppWidgetProvider() {

    abstract fun build(c: Context): RemoteViews

    override fun onUpdate(c: Context, mgr: AppWidgetManager, ids: IntArray) {
        for (id in ids) {
            val rv = build(c)
            Updater.applyBg(rv, c, id)
            mgr.updateAppWidget(id, rv)
        }
    }

    override fun onReceive(c: Context, intent: Intent) {
        if (intent.action == Updater.ACTION_TOGGLE) {
            Store.toggle(c)
            Updater.updateAll(c)
        } else {
            super.onReceive(c, intent)
        }
    }
}
