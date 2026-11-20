package com.focus.widget

import android.content.Context
import android.widget.RemoteViews

class TallWidget : FocusWidget() {
    override fun build(c: Context): RemoteViews = Updater.buildTall(c)
}
