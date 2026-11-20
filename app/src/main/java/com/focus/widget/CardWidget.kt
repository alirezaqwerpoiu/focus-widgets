package com.focus.widget

import android.content.Context
import android.widget.RemoteViews

class CardWidget : FocusWidget() {
    override fun build(c: Context): RemoteViews = Updater.buildCard(c)
}
