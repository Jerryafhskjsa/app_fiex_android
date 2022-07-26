package com.black.base.webview

import android.app.Activity
import android.graphics.Bitmap
import android.net.http.SslError
import android.webkit.CookieManager
import android.webkit.SslErrorHandler
import android.webkit.WebView
import android.webkit.WebViewClient

class SimpleWebViewClient(protected var activity: Activity) : WebViewClient() {
    //        this.loadingDialog = FryingUtil.getLoadDialog(activity, "");
//    protected LoadingDialog loadingDialog;
    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
        view?.loadUrl(url)
        return true
    }

    override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
        //super.onReceivedSslError(view, handler, error);
        handler?.proceed()
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        //        if (loadingDialog != null && !loadingDialog.isShowing()) {
//            loadingDialog.show();
//        }
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        val cookieManager = CookieManager.getInstance()
        val CookieStr = cookieManager.getCookie(url)
        //        if (loadingDialog != null && loadingDialog.isShowing()) {
//            loadingDialog.dismiss();
//        }
        super.onPageFinished(view, url)
    }

}