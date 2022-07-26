package com.black.community.fragment

import android.net.http.SslError
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.webkit.*
import com.black.base.fragment.BaseFragment
import com.black.base.util.ConstData
import com.black.community.R
import com.black.util.CommonUtil

class FactionNoticeFragment : BaseFragment() {
    private var layout: View? = null
    private var webViewStub: ViewStub? = null
    private var webView: WebView? = null
    private var webViewData: String? = null
    //异步获取数据
    private var otherHandlerThread: HandlerThread? = null
    private var otherHandler: Handler? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        layout = inflater.inflate(R.layout.fragment_faction_notice, null)
        otherHandlerThread = HandlerThread(ConstData.OTHER_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        otherHandlerThread!!.start()
        otherHandler = Handler(otherHandlerThread!!.looper)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (webView == null) {
            otherHandler!!.postDelayed({
                CommonUtil.checkActivityAndRunOnUI(activity) {
                    webViewStub = layout!!.findViewById(R.id.web_view_stub)
                    webView = webViewStub?.inflate() as WebView?
                    webView!!.settings.javaScriptEnabled = true
                    webView!!.settings.javaScriptCanOpenWindowsAutomatically = true
                    webView!!.settings.setSupportMultipleWindows(true)
                    webView!!.webViewClient = object : WebViewClient() {
                        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                            super.onReceivedError(view, request, error)
                            onLoadError(view)
                        }

                        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
                            super.onReceivedError(view, errorCode, description, failingUrl)
                            onLoadError(view)
                        }

                        override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, errorResponse: WebResourceResponse) {
                            super.onReceivedHttpError(view, request, errorResponse)
                            onLoadError(view)
                        }

                        override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                            super.onReceivedSslError(view, handler, error)
                            onLoadError(view)
                        }

                        private fun onLoadError(view: WebView) {
                            view.visibility = View.GONE
                        }
                    }
                    webView!!.webChromeClient = WebChromeClient()
                    loadUrl()
                }
            }, 100)
        }
    }

    override fun onResume() {
        super.onResume()
        loadUrl()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        otherHandler!!.removeCallbacksAndMessages(null)
        if (otherHandlerThread != null) {
            otherHandlerThread!!.quit()
        }
    }

    private fun loadUrl() {
        if (webView != null) {
            webView!!.visibility = View.VISIBLE
            webView!!.loadDataWithBaseURL(null, webViewData, "text/html", "utf-8", null)
        }
    }

    fun setWebViewData(webViewData: String?) {
        this.webViewData = webViewData
        loadUrl()
    }
}