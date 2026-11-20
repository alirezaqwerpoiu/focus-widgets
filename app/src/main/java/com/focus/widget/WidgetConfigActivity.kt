package com.focus.widget

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView

/** Lets the user choose the background opacity of one widget. */
class WidgetConfigActivity : Activity() {

    private var widgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private val labels = listOf("20%", "40%", "60%", "80%", "100%")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_CANCELED)

        widgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val current = Store.alphaLevel(this, widgetId)
        val label = TextView(this).apply {
            text = "شفافیت پس‌زمینه: ${labels[current]}"
            textSize = 18f
            setTextColor(0xFFF2F4F8.toInt())
            gravity = Gravity.CENTER
        }
        val seek = SeekBar(this).apply {
            max = 4
            progress = current
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                    label.text = "شفافیت پس‌زمینه: ${labels[progress]}"
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        }
        val save = Button(this).apply {
            text = "ذخیره"
            setOnClickListener {
                Store.setAlphaLevel(this@WidgetConfigActivity, widgetId, seek.progress)
                Updater.updateAll(this@WidgetConfigActivity)
                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId))
                finish()
            }
        }

        setContentView(LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xFF05060A.toInt())
            setPadding(64, 64, 64, 64)
            addView(label)
            addView(seek)
            addView(save)
        })
    }
}
