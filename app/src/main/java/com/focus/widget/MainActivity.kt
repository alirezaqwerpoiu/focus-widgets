package com.focus.widget

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient

/** Shows the original Focus web app. Widgets and the notification read the same data through WebStore. */
class MainActivity : Activity() {

    private lateinit var web: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        web = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            webViewClient = WebViewClient()
            addJavascriptInterface(WebStore(this@MainActivity), "AndroidStore")
        }
        setContentView(web)
        web.loadUrl("file:///android_asset/index.html")

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
    }

    override fun onResume() {
        super.onResume()
        web.onResume()
        web.resumeTimers()
        // The timer may have been changed from a widget or the notification while the app was in the background.
        web.evaluateJavascript(
            "(function(){ if (typeof st !== 'undefined') { " +
            "var s = JSON.parse(localStorage.getItem('focusTracker_state') || 'null'); " +
            "if (s) st = s; if (typeof refreshAll === 'function') refreshAll(); } })();",
            null
        )
        Notif.refresh(this)
    }

    override fun onPause() {
        web.onPause()
        web.pauseTimers()
        super.onPause()
    }

    override fun onBackPressed() {
        if (web.canGoBack()) web.goBack() else super.onBackPressed()
    }
}
