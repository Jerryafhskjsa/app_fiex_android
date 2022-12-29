package com.black.base.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.SkinAppCompatDelegateImpl
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.black.base.BaseApplication
import com.black.base.R
import com.black.base.api.UserApiServiceHelper
import com.black.base.lib.FryingSingleToast
import com.black.base.model.*
import com.black.base.model.user.UserInfo
import com.black.base.util.*
import com.black.base.view.LoadingDialog
import com.black.base.viewmodel.BaseViewModel
import com.black.lib.permission.Permission
import com.black.lib.permission.PermissionHelper
import com.black.net.HttpCookieUtil
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.RouteCheckHelper
import com.black.util.Callback
import com.black.util.CommonUtil
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.util.*
import kotlin.math.abs

open class BaseActionBarActivity : AppCompatActivity(), PermissionHelper, GeeTestInterface, RouteCheckHelper {
    lateinit var fryingHelper: FryingHelper
    protected lateinit var nullAmount: String
    protected var gson = Gson()
    protected lateinit var mContext: Context
    protected lateinit var prefs: SharedPreferences
    protected lateinit var imm: InputMethodManager
    private var needGeeTest = false
    protected var geeTestHelper: GeeTestHelper? = null
    private var loadingDialog: LoadingDialog? = null

    companion object {
        init {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }
    }

    override fun getDelegate(): AppCompatDelegate {
        return SkinAppCompatDelegateImpl.get(this, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fryingHelper = FryingHelper(this)
        needGeeTest = needGeeTest()
        if (needGeeTest) {
            geeTestHelper = GeeTestHelper(this)
        }
        nullAmount = getString(R.string.number_default)
        mContext = this
        prefs = getSharedPreferences(packageName, Context.MODE_PRIVATE)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        loadingDialog = FryingUtil.getLoadDialog(this, "")
        initActionBar()
        //沉浸式代码配置
        //当FitsSystemWindows设置 true 时，会在屏幕最上方预留出状态栏高度的 padding
        //        StatusBarUtil.setRootViewFitsSystemWindows(this, true);
        //设置状态栏透明
        StatusBarUtil.setTranslucentStatus(this)
        if (isStatusBarDark()) {
            //一般的手机的状态栏文字和图标都是白色的, 可如果你的应用也是纯白色的, 或导致状态栏文字看不清
            //所以如果你是这种情况,请使用以下代码, 设置状态使用深色文字图标风格, 否则你可以选择性注释掉这个if内容
            if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
                //如果不支持设置深色风格 为了兼容总不能让状态栏白白的看不清, 于是设置一个状态栏颜色为半透明,
                //这样半透明+白=灰, 状态栏的文字能看得清
                StatusBarUtil.setStatusBarColor(this, getStatusBarColor())
            }
        } else {
            StatusBarUtil.setStatusBarColor(this, getStatusBarColor())
            StatusBarUtil.setTranslucentStatus(this)
        }
    }

    protected open fun getViewModel(): BaseViewModel<*>? {
        return null
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

    override fun onCreateView(name: String?, context: Context?, attrs: AttributeSet?): View? {
        return super.onCreateView(name, context, attrs)
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
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar?.let { initToolbarViews(it) }
        val appBarLayout = findViewById<AppBarLayout>(R.id.app_bar_layout)
        if (appBarLayout != null && titleView != null) {
            titleView.visibility = View.GONE
            appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { layout, verticalOffset ->
                val isAppBarHidden = abs(verticalOffset) >= layout.totalScrollRange
                if (isAppBarHidden) {
                    titleView.visibility = View.VISIBLE
                } else {
                    titleView.visibility = View.GONE
                }
                onAppBarStatusChanged(isAppBarHidden)
            })
        }
    }

    protected open fun onAppBarStatusChanged(isAppBarHidden: Boolean) {}

    protected open fun getTitleText(): String? {
        return null
    }

    protected open fun initToolbarViews(toolbar: Toolbar) {}

    protected open fun getStatusBarColor(): Int {
        return 0x55000000
    }

    protected open fun isStatusBarDark(): Boolean {
        return CookieUtil.getNightMode(mContext)
    }

    protected open fun needGeeTest(): Boolean {
        return false
    }

    open fun resetStatusBarTheme(isDark: Boolean) {
        StatusBarUtil.setTranslucentStatus(this)
        if (isDark) {
            if (!StatusBarUtil.setStatusBarDarkTheme(this, true)) {
                StatusBarUtil.setStatusBarColor(this, getStatusBarColor())
            }
        } else {
            StatusBarUtil.setStatusBarColor(this, getStatusBarColor())
            StatusBarUtil.setTranslucentStatus(this)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        if (getViewModel() != null) {
            getViewModel()?.onDestroy()
        }
        if (needGeeTest && geeTestHelper != null) {
            geeTestHelper?.release()
        }
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


    open fun resetStatusBarColor() { //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            if (CookieUtil.getNightMode(this)) {
//                window.setStatusBarColor(getResources().getColor(R.color.white));
//            } else {
//                window.setStatusBarColor(getResources().getColor(R.color.black_real));
//            }
////            window.setStatusBarColor(Color.TRANSPARENT);
//        }
    }

    protected open fun initActionBar() {
        val id = getActionBarLayoutId()
        val actionBar = supportActionBar ?: return
        if (Build.VERSION.SDK_INT >= 21) {
            actionBar.elevation = 0f
        }
        if (id == 0) {
            actionBar.hide()
        } else {
            actionBar.setDisplayShowHomeEnabled(false)
            actionBar.setDisplayShowCustomEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
            val view = layoutInflater.inflate(id, null)
            val layoutParams = ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)
            actionBar.setCustomView(view, layoutParams)
            actionBar.customView.parent
            val parent = view.parent as Toolbar
            parent.setContentInsetsAbsolute(0, 0)
            initActionBarView(view)
        }
    }

    open fun getActionBarLayoutId(): Int {
        return 0
    }

    open fun initActionBarView(view: View) {}

    open fun onBackClick(view: View?) {
        finish()
//        overridePendingTransition(0, 0)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val routeUri = intent?.getStringExtra("routeUri")
        BlackRouter.getInstance().build(routeUri).go(this)
    }

    protected open fun checkUserAndDoing(callback: Runnable?, homeFragmentIndex: Int) {
        fryingHelper?.checkUserAndDoing(callback, homeFragmentIndex)
    }

    //    protected void openActivity(Class activity) {
//        openActivity(activity, null);
//    }

//    protected void openActivity(Class activity, Bundle extras) {
//        Intent intent = new Intent(mContext, activity);
//        intent.setPackage(getPackageName());
//        if (extras != null) {
//            intent.putExtras(extras);
//        }
//        startActivity(intent);
//        if (activity.equals(HomePageActivity.class)) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        }
//        overridePendingTransition(0, 0);
//    }

//    protected void openActivityForResult(Class activity, int requestCode, Bundle extras) {
//        Intent intent = new Intent(mContext, activity);
//        intent.setPackage(getPackageName());
//        if (extras != null) {
//            intent.putExtras(extras);
//        }
//        startActivityForResult(intent, requestCode);
//        overridePendingTransition(0, 0);
//    }

    //    protected void openActivity(Class activity) {
//        openActivity(activity, null);
//    }
//    protected void openActivity(Class activity, Bundle extras) {
//        Intent intent = new Intent(mContext, activity);
//        intent.setPackage(getPackageName());
//        if (extras != null) {
//            intent.putExtras(extras);
//        }
//        startActivity(intent);
//        if (activity.equals(HomePageActivity.class)) {
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        }
//        overridePendingTransition(0, 0);
//    }
//    protected void openActivityForResult(Class activity, int requestCode, Bundle extras) {
//        Intent intent = new Intent(mContext, activity);
//        intent.setPackage(getPackageName());
//        if (extras != null) {
//            intent.putExtras(extras);
//        }
//        startActivityForResult(intent, requestCode);
//        overridePendingTransition(0, 0);
//    }
    protected open fun chooseCountryCode() { //        Intent intent = new Intent(mContext, ChooseCountryCodeActivity.class);
//        intent.setPackage(getPackageName());
//        startActivityForResult(intent, ConstData.CHOOSE_COUNTRY_CODE);
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ConstData.CHOOSE_COUNTRY_CODE -> onCountryCodeChoose(resultCode, data)
        }
    }

    protected open fun onCountryCodeChoose(resultCode: Int, data: Intent?) {}

    open fun hideSoftKeyboard() {
        if (currentFocus != null) {
            imm?.hideSoftInputFromWindow(currentFocus!!.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    protected open fun hideSoftKeyboard(view: View?) {
        imm?.hideSoftInputFromWindow(window.decorView.windowToken, 0)
        //        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
//        imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


    protected open fun getUserInfo(callBack: Callback<UserInfo?>?) {
        getUserInfo(callBack, false)
    }

    //获取用户信息，完成登录
    protected open fun getUserInfo(callBack: Callback<UserInfo?>?, isShowLoading: Boolean) {
        val token = CookieUtil.getToken(this)
        if (TextUtils.isEmpty(token)) {
            return
        }
        UserApiServiceHelper.getUserInfo(mContext, isShowLoading, object : NormalCallback<HttpRequestResultData<UserInfo?>?>(mContext) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error!!)
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

    //发送数据更新通知
    protected open fun sendLoginBroadcast(type: Int) {
        val intent = Intent()
        intent.action = SocketUtil.ACTION_SOCKET_COMMAND
        intent.setPackage(packageName)
        intent.putExtra(SocketUtil.SOCKET_COMMAND, type)
        sendBroadcast(intent)
    }

    //发送数据更新通知
    open fun sendSocketCommandChangedBroadcast(type: Int) {
        val intent = Intent()
        intent.action = SocketUtil.ACTION_SOCKET_COMMAND
        intent.setPackage(packageName)
        intent.putExtra(SocketUtil.SOCKET_COMMAND, type)
        sendBroadcast(intent)
    }

    //发送数据更新通知
    protected open fun sendSocketCommandChangedBroadcast(type: Int, bundle: Bundle?) {
        val intent = Intent()
        intent.action = SocketUtil.ACTION_SOCKET_COMMAND
        intent.setPackage(packageName)
        intent.putExtra(SocketUtil.SOCKET_COMMAND, type)
        if (bundle != null && !bundle.isEmpty) {
            intent.putExtra(SocketUtil.SOCKET_COMMAND_EXTRAS, bundle)
        }
        sendBroadcast(intent)
    }

    protected open fun getCropImagePath(): String? {
        return CommonUtil.getCatchFilePath(mContext) + "/crop.jpg"
    }

    //截取图片
    protected open fun cropImage(uri: Uri?, outputX: Int, outputY: Int, requestCode: Int) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        val file = File(getCropImagePath())
        var outUri: Uri? = null
        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // "com.fbsex.exchange.fileProvider"即是在清单文件中配置的authorities
            outUri = FileProvider.getUriForFile(mContext, "com.fbsex.exchange.fileProvider", file)
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } else {
            outUri = Uri.fromFile(file)
        }
        val resInfoList: List<*> = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        if (resInfoList.isEmpty()) {
            return
        }
        val resInfoIterator = resInfoList.iterator()
        while (resInfoIterator.hasNext()) {
            val resolveInfo = resInfoIterator.next() as ResolveInfo
            val packageName = resolveInfo.activityInfo.packageName
            grantUriPermission(packageName, outUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outUri)
        intent.putExtra("crop", "true")
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        intent.putExtra("outputX", outputX)
        intent.putExtra("outputY", outputY)
        intent.putExtra("outputFormat", "JPEG")
        intent.putExtra("noFaceDetection", true)
        startActivityForResult(intent, requestCode)
    }

    protected open fun getRename(file: String?): String {
        val hash = Calendar.getInstance().time.hashCode()
        val imgPath = CommonUtil.getCatchFilePath(mContext)
        val f = File(file)
        return imgPath + hash.toString() + f.name
    }

    protected open fun renameImageFile(file: String?): String? {
        val newName = getRename(file)
        val photoFile = File(file)
        if (photoFile.exists()) {
            val toFile = File(newName)
            photoFile.renameTo(toFile)
        }
        return newName
    }

    private var requestCameraPermissionCallback: Runnable? = null
    private var requestStoragePermissionCallback: Runnable? = null
    private var requestCallPermissionCallback: Runnable? = null
    private var requestMicrophonePermissionCallback: Runnable? = null

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

    override fun startVerify(geeTestCallback: GeeTestCallback?) {
        if (needGeeTest && geeTestHelper != null) {
//            geeTestHelper?.startVerify(geeTestCallback)
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

    //token过期的处理
    open fun onTokenError(error: Any?) {
        if (BaseApplication.checkTokenError) {
            refreshToken(error)
            BaseApplication.checkTokenError = false
        }
    }

    private fun refreshToken(error: Any?){
        var path:String? = null
        if(error is Request){
            path = error?.url()?.url()?.path
        }
        if(CookieUtil.getUserInfo(mContext) != null && path != null){
            if(path.contains("/uc/")){
                HttpCookieUtil.deleteCookies(mContext)
                CookieUtil.deleteUserInfo(mContext)
                BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext)
            }
            if(path.contains("/pro/")){
                UserApiServiceHelper.getProToken(mContext!!, object:Callback<HttpRequestResultData<ProTokenResult?>?>() {
                    override fun error(type: Int, error: Any?) {
                        if(type == ConstData.ERROR_TOKEN_INVALID){
                            UserApiServiceHelper.getTicket(mContext!!, object:Callback<HttpRequestResultString?>() {
                                override fun error(type: Int, error: Any?) {
                                    if(type == ConstData.ERROR_TOKEN_INVALID){
                                        FryingUtil.showToast(mContext, getString(R.string.login_over_time), FryingSingleToast.ERROR)
                                        CookieUtil.deleteUserInfo(mContext)
                                        CookieUtil.deleteToken(mContext)
                                        //退回到主界面并要求登录
                                        BlackRouter.getInstance().build(RouterConstData.HOME_PAGE)
                                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            .go(mContext) { _, _ ->
                                                if (!FryingUtil.checkRouteUri(mContext, RouterConstData.HOME_PAGE)) {
                                                    finish()
                                                }
                                                BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext)
                                            }
                                    }
                                }
                                override fun callback(result: HttpRequestResultString?) {
                                    if(result != null && result.code == HttpRequestResult.SUCCESS){
                                        var ticket: String? = result.data
                                        HttpCookieUtil.saveTicket(mContext,ticket)
                                    }else{
                                        HttpCookieUtil.deleteCookies(mContext)
                                        CookieUtil.deleteUserInfo(mContext)
                                        BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext)
                                    }
                                }
                            })
                        }
                    }
                    override fun callback(result: HttpRequestResultData<ProTokenResult?>?) {
                        if(result != null && result.code == HttpRequestResult.SUCCESS){
                            var proTokenResult: ProTokenResult? = result.data
                            var proToken = proTokenResult?.proToken
                            var proTokenExpiredTime =proTokenResult?.expireTime
                            HttpCookieUtil.saveProToken(mContext,proToken)
                            HttpCookieUtil.saveProTokenExpiredTime(mContext,proTokenExpiredTime.toString())
                        }else{
                            HttpCookieUtil.deleteCookies(mContext)
                            CookieUtil.deleteUserInfo(mContext)
                            BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext)
                        }
                    }
                })
            }
        }
    }

    open fun fixupLocale(ctx: Context, newLocale: Locale) {
        val res = ctx.resources
        val config = res.configuration
        val curLocale = getLocale(ctx)
        if (curLocale != null && curLocale != newLocale) {
            Locale.setDefault(newLocale)
            val conf = Configuration(config)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                conf.setLocale(newLocale)
            }
            res.updateConfiguration(conf, res.displayMetrics)
        }
    }

    open fun getLocale(context: Context): Locale? {
        val fryingLanguage = LanguageUtil.getLanguageSetting(context)
        return fryingLanguage?.locale
    }

    protected open fun postHandleTask(handler: Handler?, runnable: Runnable?) {
        CommonUtil.postHandleTask(handler, runnable)
    }

    protected open fun postHandleTask(handler: Handler?, runnable: Runnable?, delayTime: Long) {
        CommonUtil.postHandleTask(handler, runnable, delayTime)
    }

    protected open fun initSpinnerSelectItem(spinner: Spinner, list: MutableList<Any>) {
        val spAdapter: ArrayAdapter<*> = ArrayAdapter<Any>(mContext, R.layout.spinner_item_2, list)
        spAdapter.setDropDownViewResource(R.layout.spinner_drop_down_item)
        spinner.adapter = spAdapter
    }

    protected open fun openSystemBrowse(url: String?) {
        val intent = Intent()
        intent.data = Uri.parse(url) //Url 就是你要打开的网址
        intent.action = Intent.ACTION_VIEW
        this.startActivity(intent) //启动浏览器
    }

    override fun routeCheck(uri: String, beforePath: String?, requestCode: Int, flags: Int, extras: Bundle?) {
        if (beforePath != null && beforePath.contains(RouterConstData.LOGIN)) {
            fryingHelper.checkUserAndDoing(Runnable {
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

//    protected abstract inner class NormalCallback<T> : Callback<T>() {
//        override fun error(type: Int, error: Any?) {
//            when (type) {
//                ConstData.ERROR_NORMAL -> FryingUtil.showToast(mContext, error.toString())
//                ConstData.ERROR_TOKEN_INVALID -> onTokenError(error)
//                ConstData.ERROR_UNKNOWN ->
//                    //根據情況處理，error 是返回的HttpRequestResultError
//                    if (error != null && error is HttpRequestResultBase) {
//                        FryingUtil.showToast(mContext, error.message)
//                    }
//            }
//        }
//    }
}