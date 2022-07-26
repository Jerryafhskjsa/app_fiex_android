package com.black.base.webview

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.webkit.JsPromptResult
import android.webkit.JsResult
import android.webkit.WebChromeClient
import android.webkit.WebView

open class SimpleWebChromeClient(protected var activity: Activity) : WebChromeClient() {
    override fun onJsAlert(view: WebView, url: String, message: String, result: JsResult): Boolean {
        val b = AlertDialog.Builder(activity)
        b.setTitle("Alert")
        b.setMessage(message)
        b.setPositiveButton(R.string.ok) { _, _ -> result.confirm() }
        b.setCancelable(false)
        b.create().show()
        return true
    }

    override fun onReceivedTitle(view: WebView, title: String) {
        super.onReceivedTitle(view, title)
        if (activity is SimpleWebViewActivity) {
            val simpleWebViewActivity = activity as SimpleWebViewActivity
            simpleWebViewActivity.headTitleView?.text = title
        }
    }

    //设置响应js 的Confirm()函数
    override fun onJsConfirm(view: WebView, url: String, message: String, result: JsResult): Boolean {
        val b = AlertDialog.Builder(activity)
        b.setTitle("Confirm")
        b.setMessage(message)
        b.setPositiveButton(R.string.ok) { _, _ -> result.confirm() }
        b.setNegativeButton(R.string.cancel) { _, _ -> result.cancel() }
        b.create().show()
        return true
    }

    //设置响应js 的Prompt()函数
    override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean { //            final View v = View.inflate(activity, R.layout.prompt_dialog, null);
//            ((TextView) v.findViewById(R.id.prompt_message_text)).setText(message);
//            ((EditText) v.findViewById(R.id.prompt_input_field)).setText(defaultValue);
//            AlertDialog.Builder b = new AlertDialog.Builder(TestAlertActivity.this);
//            b.setTitle("Prompt");
//            b.setView(v);
//            b.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    String value = ((EditText) v.findViewById(R.id.prompt_input_field)).getText().toString().trim();
//                    result.confirm(value);
//                }
//            });
//            b.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    result.cancel();
//                }
//            });
//            b.create().show();
        return true
    }

}