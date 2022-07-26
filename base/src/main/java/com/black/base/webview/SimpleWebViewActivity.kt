package com.black.base.webview

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.View
import android.webkit.*
import android.widget.TextView
import com.black.base.R
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.RouterConstData
import com.black.router.annotation.Route
import java.util.*

//WebView,打开相关网页
@Route(RouterConstData.WEB_VIEW)
class SimpleWebViewActivity : BaseActionBarActivity() {
    protected var isFull = false
    @JvmField
    protected var actionBarLayoutId = 0
    protected var title: String? = null
    protected var url: String? = null
    protected var webView: WebView? = null
    var headTitleView: TextView? = null
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_simple_web_view)
        webView = findViewById(R.id.web_view)
        webView?.setWebViewClient(WebViewClient())
        webView?.setFocusable(true)
        webView?.setFocusableInTouchMode(true)
        webView?.getSettings()?.useWideViewPort = true //关键点
        webView?.getSettings()?.loadWithOverviewMode = true
        val metrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metrics)
        val mDensity = metrics.densityDpi
        if (mDensity == 240) {
            webView?.getSettings()?.defaultZoom = WebSettings.ZoomDensity.FAR
        } else if (mDensity == 160) {
            webView?.getSettings()?.defaultZoom = WebSettings.ZoomDensity.MEDIUM
        } else if (mDensity == 120) {
            webView?.getSettings()?.defaultZoom = WebSettings.ZoomDensity.CLOSE
        } else if (mDensity == DisplayMetrics.DENSITY_XHIGH) {
            webView?.getSettings()?.defaultZoom = WebSettings.ZoomDensity.FAR
        } else if (mDensity == DisplayMetrics.DENSITY_TV) {
            webView?.getSettings()?.defaultZoom = WebSettings.ZoomDensity.FAR
        } else {
            webView?.getSettings()?.defaultZoom = WebSettings.ZoomDensity.MEDIUM
        }
        webView?.getSettings()?.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
        webView?.getSettings()?.javaScriptEnabled = true
        webView?.getSettings()?.setSupportZoom(true) // 支持缩放
        webView?.getSettings()?.builtInZoomControls = true
        webView?.getSettings()?.displayZoomControls = false
        webView?.getSettings()?.cacheMode = WebSettings.LOAD_NO_CACHE
        webView?.getSettings()?.domStorageEnabled = true
        webView?.getSettings()?.databaseEnabled = true
        webView?.getSettings()?.setAppCacheEnabled(true)
        webView?.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY)
        synchronousWebCookies(this, url, webViewCookie)
        webView?.addJavascriptInterface(SimpleJavaScriptInterface(this), "frying_add")
        webView?.webViewClient = SimpleWebViewClient(this)
        //设置响应js 的Alert()函数
        webView?.webChromeClient = SimpleWebChromeClient(this)
        webView?.loadUrl(if (url == null) "" else url)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getActionBarLayoutId(): Int {
        title = intent.getStringExtra(ConstData.TITLE)
        url = intent.getStringExtra(ConstData.URL)
        actionBarLayoutId = intent.getIntExtra(ConstData.ACTION_BAR_LAYOUT_ID, -1)
        return if (TextUtils.isEmpty(title)) {
            0
        } else {
            if (actionBarLayoutId != -1) {
                actionBarLayoutId
            } else {
                R.layout.action_bar_left_back
            }
        }
    }

    override fun initActionBarView(view: View) {
        if (!TextUtils.isEmpty(title)) {
            headTitleView = view.findViewById(R.id.action_bar_title)
            headTitleView?.text = title
        }
    }

    override fun onBackPressed() {
        if (webView!!.canGoBack()) {
            webView!!.goBack()
        } else {
            setResult(Activity.RESULT_CANCELED)
            finish()
            overridePendingTransition(0, 0)
        }
    }

    fun syncCookie(url: String?): Boolean {
        val cookieManager = CookieManager.getInstance()
        cookieManager.setCookie(url, "platform=Android")
        val token = CookieUtil.getToken(this)
        if (TextUtils.isEmpty(token)) {
            cookieManager.setCookie(url, "Authorization=$token")
        }
        val newCookie = cookieManager.getCookie(url)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            val cookieSyncManager = CookieSyncManager.createInstance(this)
            cookieSyncManager.sync()
        }
        return !TextUtils.isEmpty(newCookie)
    }

    //创建一个拼接cookie的容器,为什么这么拼接，大家查阅一下http头Cookie的结构
    //            sbCookie.append(_mApplication.getUserInfo().getSessionID());//拼接sessionId
    private val webViewCookie: List<String?>
        private get() {
            val sbCookie = StringBuilder() //创建一个拼接cookie的容器,为什么这么拼接，大家查阅一下http头Cookie的结构
            //            sbCookie.append(_mApplication.getUserInfo().getSessionID());//拼接sessionId
            sbCookie.append(String.format("domain=%s", ".ggtoken.xin"))
            sbCookie.append(String.format(";path=%s", "/"))
            sbCookie.append(String.format(";Platform=%s", "Android"))
            val token = CookieUtil.getToken(this)
            if (!TextUtils.isEmpty(token)) {
                sbCookie.append(String.format(";Authorization=%s", token))
            }
            val cookies: MutableList<String?> = ArrayList()
            cookies.add(String.format("domain=%s", ".ggtoken.xin"))
            cookies.add(String.format(";path=%s", "/"))
            cookies.add(String.format(";Platform=%s", "Android"))
            if (!TextUtils.isEmpty(token)) {
                cookies.add(String.format(";Authorization=%s", token))
            }
            return cookies
        }

    fun synchronousWebCookies(context: Context, url: String?, cookies: List<String?>?) {
        if (!TextUtils.isEmpty(url)) if (cookies != null && cookies.size > 0) {
            val cookieSyncManager: CookieSyncManager
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                cookieSyncManager = CookieSyncManager.createInstance(context)
                cookieSyncManager.sync()
            } else {
                CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)
            }
            val cookieManager = CookieManager.getInstance()
            cookieManager.setAcceptCookie(true)
            cookieManager.removeSessionCookie() // 移除
            cookieManager.removeAllCookie()
            cookieManager.setCookie(url, String.format("Domain=%s", ".ggtoken.xin"))
            cookieManager.setCookie(url, String.format("Path=%s", "/"))
            cookieManager.setCookie(url, String.format("Platform=%s", "Android"))
            val token = CookieUtil.getToken(context)
            if (!TextUtils.isEmpty(token)) {
                cookieManager.setCookie(url, String.format("Authorization=%s", token))
            }
            CookieSyncManager.getInstance().sync() //同步cookie
            val newCookie = cookieManager.getCookie(url)
        }
    }
}