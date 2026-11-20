package com.focus.widget

import android.content.Context
import android.widget.RemoteViews

class TinyWidget : FocusWidget() {
    override fun build(c: Context): RemoteViews = Updater.buildTiny(c)
}
