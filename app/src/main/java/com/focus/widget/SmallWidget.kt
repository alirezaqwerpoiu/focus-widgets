package com.focus.widget

import android.content.Context
import android.widget.RemoteViews

class SmallWidget : FocusWidget() {
    override fun build(c: Context): RemoteViews = Updater.buildSmall(c)
}
