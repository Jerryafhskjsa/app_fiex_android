package com.black.base.webview

import android.app.Activity
import android.webkit.JavascriptInterface

class SimpleJavaScriptInterface(protected var activity: Activity) {
    @JavascriptInterface
    fun onBackPressed() {
        activity.runOnUiThread { activity.onBackPressed() }
    }

    @JavascriptInterface
    fun finish() {
        activity.runOnUiThread { activity.finish() }
    }

}