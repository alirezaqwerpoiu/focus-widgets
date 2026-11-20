package com.focus.widget

import android.content.Context
import android.widget.RemoteViews

/** 1×2 widget: progress ring with the start/stop button in its centre, status and time underneath. */
class RingWidget : FocusWidget() {
    override fun build(c: Context): RemoteViews = Updater.buildRing(c)
}
