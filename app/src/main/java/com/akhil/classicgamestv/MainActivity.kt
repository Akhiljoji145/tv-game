package com.akhil.classicgamestv

import android.app.Activity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.webkit.JavascriptInterface

class MainActivity : Activity() {
    private lateinit var web: WebView
    private var pageLoaded = false
    private val pendingKeys = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        )

        web = WebView(this).apply {
            addJavascriptInterface(object {
                @JavascriptInterface fun exitApp() { runOnUiThread { finish() } }
            }, "AndroidTV")
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            isFocusable = true
            isFocusableInTouchMode = true
            isClickable = true
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    pageLoaded = true
                    requestFocus(View.FOCUS_DOWN)
                    pendingKeys.toList().forEach { sendKeyToGame(it) }
                    pendingKeys.clear()
                }
            }
        }
        setContentView(web)
        web.requestFocus(View.FOCUS_DOWN)
        web.loadUrl("file:///android_asset/index.html")
    }

    private fun mapRemoteKey(keyCode: Int): String? = when (keyCode) {
        KeyEvent.KEYCODE_DPAD_UP -> "ArrowUp"
        KeyEvent.KEYCODE_DPAD_DOWN -> "ArrowDown"
        KeyEvent.KEYCODE_DPAD_LEFT -> "ArrowLeft"
        KeyEvent.KEYCODE_DPAD_RIGHT -> "ArrowRight"
        KeyEvent.KEYCODE_DPAD_CENTER,
        KeyEvent.KEYCODE_ENTER,
        KeyEvent.KEYCODE_NUMPAD_ENTER -> "Enter"
        KeyEvent.KEYCODE_BACK,
        KeyEvent.KEYCODE_ESCAPE -> "Escape"
        else -> null
    }

    private fun sendKeyToGame(key: String) {
        if (!pageLoaded) {
            pendingKeys.add(key)
            return
        }
        // Dispatch both through the game's explicit TV handler and as a DOM key event.
        val quoted = org.json.JSONObject.quote(key)
        web.evaluateJavascript(
            "(function(){var k=$quoted;if(typeof window.tvKey==='function'){window.tvKey(k);} " +
                "document.dispatchEvent(new KeyboardEvent('keydown',{key:k,bubbles:true}));})();",
            null
        )
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val key = mapRemoteKey(event.keyCode)
        if (key != null) {
            if (event.action == KeyEvent.ACTION_DOWN && event.repeatCount == 0) {
                sendKeyToGame(key)
            }
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    @Deprecated("Deprecated in Android")
    override fun onBackPressed() {
        sendKeyToGame("Escape")
    }
}
