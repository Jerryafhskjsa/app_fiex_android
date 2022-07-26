package com.black.base.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.http.SslError
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.webkit.*
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.black.base.BaseApplication
import com.black.base.R
import com.black.base.api.UserApiServiceHelper
import com.black.base.lib.FryingSingleToast
import com.black.base.model.HttpRequestResultBase
import com.black.base.model.HttpRequestResultData
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.view.LoadingDialog
import com.black.base.viewmodel.BaseViewModel
import com.black.lib.permission.Permission
import com.black.lib.permission.PermissionHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.RouteCheckHelper
import com.black.util.Callback
import com.black.util.CommonUtil
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.gson.Gson
import kotlin.math.abs

open class BaseActivity : Activity(), PermissionHelper, GeeTestInterface, RouteCheckHelper {
    protected lateinit var prefs: SharedPreferences
    protected lateinit var imm: InputMethodManager
    protected lateinit var mContext: Context
    protected lateinit var nullAmount: String
    protected var gson = Gson()
    protected lateinit var fryingHelper: FryingHelper
    private var needGeeTest = false
    protected var geeTestHelper: GeeTestHelper? = null
    private var loadingDialog: LoadingDialog? = null

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(Color.TRANSPARENT);
//        }
        fryingHelper = FryingHelper(this)
        needGeeTest = needGeeTest()
        if (needGeeTest) {
            geeTestHelper = GeeTestHelper(this)
        }
        nullAmount = getString(R.string.number_default)
        prefs = getSharedPreferences(packageName, Context.MODE_PRIVATE)
        mContext = this
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        loadingDialog = FryingUtil.getLoadDialog(this, "")
        //沉浸式代码配置
//当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
//        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
//设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this)
        if (isStatusBarDark()) { //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
//所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
            if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) { //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
//这样半透明+白=灰, 状态栏的文字能看得清
                StatusBarUtil.setStatusBarColor(this, getStatusBarColor())
            }
        } else {
            StatusBarUtil.setStatusBarColor(this, getStatusBarColor())
            StatusBarUtil.setTranslucentStatus(this)
        }
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        setHeaderStatusBar()
        initToolbar()
    }

    override fun setContentView(@LayoutRes layoutResID: Int) {
        super.setContentView(layoutResID)
        setHeaderStatusBar()
        initToolbar()
    }

    protected open fun setHeaderStatusBar() {
        val headerView = findViewById<View>(R.id.header_layout)
        if (headerView == null) {
            StatusBarUtil.setRootViewFitsSystemWindows(this, true)
        } else {
            headerView.fitsSystemWindows = true
            //            int headerPaddingLeft = headerView.getPaddingLeft();
//            int headerPaddingTop = headerView.getPaddingTop() + StatusBarUtil.getStatusBarHeight(this);
//            int headerPaddingRight = headerView.getPaddingRight();
//            int headerPaddingBottom = headerView.getPaddingBottom();
//            headerView.setPadding(headerPaddingLeft, headerPaddingTop, headerPaddingRight, headerPaddingBottom);
        }
    }

    protected open fun initToolbar() {
        val titleText = getTitleText()
        val titleView = findViewById<TextView>(R.id.action_bar_title)
        if (!TextUtils.isEmpty(titleText)) {
            if (titleView != null) {
                titleView.text = titleText
            }
            val titleBigView = findViewById<TextView>(R.id.action_bar_title_big)
            if (titleBigView != null) {
                titleBigView.text = titleText
            }
        }
        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        toolbar?.let { initToolbarViews(it) }
        val appBarLayout: AppBarLayout? = findViewById(R.id.app_bar_layout)
        if (appBarLayout != null && titleView != null) {
            titleView.visibility = View.GONE
            appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { layout, verticalOffset ->
                if (abs(verticalOffset) >= layout.totalScrollRange) {
                    titleView.visibility = View.VISIBLE
                } else {
                    titleView.visibility = View.GONE
                }
            })
        }
    }

    protected open fun getTitleText(): String? {
        return null
    }

    protected open fun initToolbarViews(toolbar: Toolbar) {}

    open fun onBackClick(view: View?) {
        finish()
        overridePendingTransition(0, 0)
    }

    protected open fun getViewModel(): BaseViewModel<*>? {
        return null
    }

    override fun onResume() {
        super.onResume()
        fryingHelper?.onResume()
        if (getViewModel() != null) {
            getViewModel()?.onResume()
        }
    }

    override fun onPause() {
        super.onPause()
        if (getViewModel() != null) {
            getViewModel()?.onPause()
        }
    }

    override fun onStop() {
        super.onStop()
        if (getViewModel() != null) {
            getViewModel()?.onStop()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val routeUri = intent?.getStringExtra("routeUri")
        BlackRouter.getInstance().build(routeUri).go(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (getViewModel() != null) {
            getViewModel()?.onDestroy()
        }
        if (needGeeTest && geeTestHelper != null) {
            geeTestHelper?.release()
        }
    }

    protected open fun needGeeTest(): Boolean {
        return false
    }

    protected open fun checkUserAndDoing(callback: Runnable?, homeFragmentIndex: Int) {
        fryingHelper?.checkUserAndDoing(callback, homeFragmentIndex)
    }

    protected open fun isNewApp(): Boolean {
        return prefs?.getBoolean(ConstData.IS_NEW_APP, true) ?: true
    }

    //是否有登录历史
    protected open fun hasLoginHistory(): Boolean {
        return false
    }

    protected open fun getStatusBarColor(): Int {
        return 0x55000000
    }

    protected open fun isStatusBarDark(): Boolean {
        return CookieUtil.getNightMode(mContext)
    }

    //进入工作页面
    protected open fun gotoMainWorkActivity() {
//        val c: Class<*>? = null
        //        if (!TextUtils.isEmpty(CookieUtil.getToken(mContext))) {
//            //快捷登录必须有token
//            int protectType = CookieUtil.getAccountProtectType(mContext);
//            if (protectType == 1) {
//                //验证手势面是否存在
//                if (TextUtils.isEmpty(CookieUtil.getGesturePassword(mContext))) {
//                    c = HomePageActivity.class;
//                } else {
//                    c = GesturePasswordLoginActivity.class;
//                }
//            } else if (protectType == 2) {
//                //设置为指纹登录，验证指纹
//                int fingerPrintStatus = CommonUtil.getFingerPrintStatus(mContext);
//                if (fingerPrintStatus == 1) {
//                    c = FingerPrintLoginActivity.class;
//                } else {
//                    c = HomePageActivity.class;
//                }
//            }
//            else{
//                c = HomePageActivity.class;
//            }
//        } else {
//            c = HomePageActivity.class;
//        }
//        Intent openMainActivity = new Intent(mContext, HomePageActivity.class);
//        startActivity(openMainActivity);
//        finish();
//        overridePendingTransition(0, 0);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ConstData.CHOOSE_COUNTRY_CODE -> onCountryCodeChoose(resultCode, data)
        }
    }

    protected open fun onCountryCodeChoose(resultCode: Int, data: Intent?) {}

    protected open fun getUserInfo(callBack: Callback<UserInfo?>?) {
        getUserInfo(callBack, false)
    }

    //获取用户信息，完成登录
    protected open fun getUserInfo(callBack: Callback<UserInfo?>?, isShowLoading: Boolean) {
        val token = CookieUtil.getToken(this)
        if (TextUtils.isEmpty(token)) {
            return
        }
        UserApiServiceHelper.getUserInfo(mContext, isShowLoading, object : NormalCallback<HttpRequestResultData<UserInfo?>?>() {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                callBack?.error(0, error)
            }

            override fun callback(returnData: HttpRequestResultData<UserInfo?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    //获取信息成功，保存信息并跳转主页
                    CookieUtil.saveUserInfo(mContext, returnData.data)
                    //记录真实姓名
                    CookieUtil.saveUserName(mContext, returnData.data?.displayName)
                    //记录userId
                    CookieUtil.saveUserId(mContext, returnData.data?.id)
                    callBack?.callback(returnData.data)
                } else {
                    callBack?.error(0, getString(R.string.alert_login_failed_try_again))
                }
            }
        })
    }

    protected open fun hideSoftKeyboard() {
        if (currentFocus != null) {
            imm?.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    protected open fun hideSoftKeyboard(view: View?) {
        imm?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        //        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
//        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    //发送数据更新通知
    open fun sendPairChangedBroadcast(type: Int) {
        val intent = Intent()
        intent.action = SocketUtil.ACTION_SOCKET_COMMAND
        intent.setPackage(packageName)
        intent.putExtra(SocketUtil.SOCKET_COMMAND, type)
        sendBroadcast(intent)
    }

    //发送数据更新通知
    protected open fun sendPairChangedBroadcast(type: Int, bundle: Bundle?) {
        val intent = Intent()
        intent.action = SocketUtil.ACTION_SOCKET_COMMAND
        intent.setPackage(packageName)
        intent.putExtra(SocketUtil.SOCKET_COMMAND, type)
        if (bundle != null && !bundle.isEmpty) {
            intent.putExtra(SocketUtil.SOCKET_COMMAND_EXTRAS, bundle)
        }
        sendBroadcast(intent)
    }

    protected open fun postHandleTask(handler: Handler?, runnable: Runnable?) {
        CommonUtil.postHandleTask(handler, runnable)
    }

    protected open fun postHandleTask(handler: Handler?, runnable: Runnable?, delayTime: Long) {
        CommonUtil.postHandleTask(handler, runnable, delayTime)
    }

    //token过期的处理
    open fun onTokenError() {
        if (BaseApplication.checkTokenError) {
            BaseApplication.checkTokenError = false
            FryingUtil.showToast(mContext, getString(R.string.login_over_time), FryingSingleToast.ERROR)
            CookieUtil.deleteUserInfo(this)
            CookieUtil.deleteToken(this)
            //        CookieUtil.setAccountProtectType(this, 0);
//        CookieUtil.setGesturePassword(this, null);
//        CookieUtil.setAccountProtectJump(this, false);
//        CookieUtil.saveUserId(this, null);
//        CookieUtil.saveUserName(this, null);
            sendPairChangedBroadcast(SocketUtil.COMMAND_USER_LOGOUT)
            //退回到主界面并要求登录
            BlackRouter.getInstance().build(RouterConstData.HOME_PAGE)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .go(this) { _, _ ->
                        if (!FryingUtil.checkRouteUri(mContext, RouterConstData.HOME_PAGE)) {
                            finish()
                        }
                        BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext)
                    }
        }
    }

    override fun requestCameraPermissions(callback: Runnable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Permission.CAMERA.isEmpty()) {
                callback?.run()
            } else {
                val check = ContextCompat.checkSelfPermission(this, Permission.CAMERA[0])
                if (check == PackageManager.PERMISSION_GRANTED) { //调用相机
                    callback?.run()
                } else {
                    requestCameraPermissionCallback = callback
                    requestPermissions(Permission.CAMERA, ConstData.REQUEST_CAMERA_PR)
                }
            }
        } else {
            callback?.run()
        }
    }

    private var requestCameraPermissionCallback: Runnable? = null
    private var requestStoragePermissionCallback: Runnable? = null
    private var requestCallPermissionCallback: Runnable? = null
    private var requestMicrophonePermissionCallback: Runnable? = null

    override fun requestStoragePermissions(callback: Runnable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Permission.STORAGE.isEmpty()) {
                callback?.run()
            } else {
                val check = ContextCompat.checkSelfPermission(this, Permission.STORAGE[0])
                if (check == PackageManager.PERMISSION_GRANTED) { //调用相机
                    callback?.run()
                } else {
                    requestStoragePermissionCallback = callback
                    requestPermissions(Permission.STORAGE, ConstData.REQUEST_STORAGE_PR)
                }
            }
        } else {
            callback?.run()
        }
    }

    override fun requestCallPermissions(callback: Runnable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Permission.PHONE.isEmpty()) {
                callback?.run()
            } else {
                val check = ContextCompat.checkSelfPermission(this, Permission.PHONE[0])
                if (check == PackageManager.PERMISSION_GRANTED) { //调用相机
                    callback?.run()
                } else {
                    requestCallPermissionCallback = callback
                    requestPermissions(Permission.PHONE, ConstData.REQUEST_CALL_PR)
                }
            }
        } else {
            callback?.run()
        }
    }

    override fun requestMicrophonePermissions(callback: Runnable?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Permission.STORAGE.isEmpty()) {
                callback?.run()
            } else {
                val check = ContextCompat.checkSelfPermission(this, Permission.MICROPHONE[0])
                if (check == PackageManager.PERMISSION_GRANTED) { //调用相机
                    callback?.run()
                } else {
                    requestMicrophonePermissionCallback = callback
                    requestPermissions(Permission.MICROPHONE, ConstData.REQUEST_MICROPHONE_PR)
                }
            }
        } else {
            callback?.run()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                ConstData.REQUEST_CAMERA_PR -> if (requestCameraPermissionCallback != null) {
                    requestCameraPermissionCallback?.run()
                    requestCameraPermissionCallback = null
                }
                ConstData.REQUEST_STORAGE_PR -> if (requestStoragePermissionCallback != null) {
                    requestStoragePermissionCallback?.run()
                    requestStoragePermissionCallback = null
                }
                ConstData.REQUEST_CALL_PR -> if (requestCallPermissionCallback != null) {
                    requestCallPermissionCallback?.run()
                    requestCallPermissionCallback = null
                }
                ConstData.REQUEST_MICROPHONE_PR -> if (requestMicrophonePermissionCallback != null) {
                    requestMicrophonePermissionCallback?.run()
                    requestMicrophonePermissionCallback = null
                }
            }
        }
    }

    override fun startVerify(geeTestCallback: GeeTestCallback?) {
        if (needGeeTest && geeTestHelper != null) {
            geeTestHelper?.startVerify(geeTestCallback)
        }
    }

    override fun routeCheck(uri: String, beforePath: String?, requestCode: Int, flags: Int, extras: Bundle?) {
        if (beforePath != null && beforePath.contains(RouterConstData.LOGIN)) {
            fryingHelper?.checkUserAndDoing(Runnable {
                val bundle = Bundle()
                if (extras != null) {
                    bundle.putAll(extras)
                }
                BlackRouter.getInstance().build(uri)
                        .with(bundle)
                        .withRequestCode(requestCode)
                        .addFlags(flags)
                        .goFinal(mContext) { _, error ->
                            if (error != null) {
                                FryingUtil.printError(error)
                            }
                        }
            }, 1)
        }
    }

    open fun showLoading() {
        if (loadingDialog != null && !loadingDialog!!.isShowing) {
            runOnUiThread { loadingDialog?.show() }
        }
    }

    open fun hideLoading() {
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            runOnUiThread { loadingDialog?.dismiss() }
        }
    }

    protected abstract inner class NormalCallback<T> : Callback<T>() {
        override fun error(type: Int, error: Any?) {
            when (type) {
                ConstData.ERROR_NORMAL -> FryingUtil.showToast(mContext, error.toString())
                ConstData.ERROR_TOKEN_INVALID -> onTokenError()
                ConstData.ERROR_UNKNOWN ->
                    //根據情況處理，error 是返回的HttpRequestResultError
                    if (error != null && error is HttpRequestResultBase) {
                        FryingUtil.showToast(mContext, error.message)
                    }
            }
        }
    }

    protected open fun initInnerWebView(webView: WebView) {
        webView.settings.javaScriptEnabled = true
        webView.setBackgroundColor(0)
        //        webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        webView.getBackground().setAlpha(0);
        webView.settings.javaScriptCanOpenWindowsAutomatically = true
        webView.settings.setSupportMultipleWindows(true)
        webView.webViewClient = object : WebViewClient() {
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
        webView.webChromeClient = WebChromeClient()
    }
}