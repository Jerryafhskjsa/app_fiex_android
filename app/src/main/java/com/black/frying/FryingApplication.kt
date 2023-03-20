package com.black.frying

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.text.TextUtils
import android.util.Log
import android.webkit.WebView
import androidx.multidex.MultiDex
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.api.TagAliasCallback
import cn.jpush.android.ups.JPushUPSManager
import com.black.base.BaseApplication
import com.black.base.model.FryingExchangeRates
import com.black.base.model.FryingLanguage
import com.black.base.model.FryingStyleChange
import com.black.base.util.*
import com.black.im.config.CustomFaceConfig
import com.black.im.config.GeneralConfig
import com.black.im.util.TUIKit
import com.black.lib.typeface.CustomerTypefaceSpanManager
import com.black.lib.typeface.TypefaceManager
import com.black.net.websocket.WebSocketHandler
import com.black.router.BlackRouter
import com.black.user.R
import com.black.util.CommonUtil
import com.tencent.android.tpush.XGIOperateCallback
import com.tencent.android.tpush.XGPushConfig
import com.tencent.android.tpush.XGPushManager
import com.tencent.imsdk.TIMSdkConfig
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.QbSdk.PreInitCallback
import com.umeng.analytics.MobclickAgent
import io.reactivex.plugins.RxJavaPlugins
import skin.support.SkinCompatManager
import skin.support.SkinCompatManager.SkinLoaderListener
import skin.support.app.SkinAppCompatViewInflater
import skin.support.app.SkinCardViewInflater
import skin.support.constraint.app.SkinConstraintViewInflater
import skin.support.design.app.SkinMaterialViewInflater
//import zendesk.core.AnonymousIdentity
//import zendesk.core.Zendesk
//import zendesk.support.Support
import java.util.*

class FryingApplication : BaseApplication() {
    companion object {
        private var tag = FryingApplication::class.java.simpleName
        private const val GO_BACK_TIME_OUT = 60 * 1000.toLong()
        private const val APP_ID = "vetZwzol" //创蓝活体
        private const val APP_KEY = "YsuR9CdK" //创蓝活体
        private const val LICENSE_ID = "FBSex-face-android" //创蓝活体

        private const val BIND_ALIAS = 100001
        private const val BIND_TAG = 100002
        private var STAY_BACKGROUND_TIME = ConstData.APP_STAY_BACKGROUND_TIME//app退回到后台的时间

        private var instance:FryingApplication? = null
        fun instance() = instance!!
    }
    private var currentActivity: Activity? = null
    private var goBackTime: Long? = null
    private val handler = Handler()
    //退回到后台60s后关闭socket连接
    private val backStayedTimerRunnable = object : Runnable {
        override fun run() {
            STAY_BACKGROUND_TIME--
            if (STAY_BACKGROUND_TIME <= 0) {
                if(!isAppOnForeground){
                    SocketUtil.sendSocketCommandBroadcast(currentActivity, SocketUtil.COMMAND_STOP)
                }
            } else {
                mHandler.postDelayed(this, ConstData.ONE_SECOND_MILLIS.toLong())
            }
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }

    fun getCurrentActivity():Activity?{
        return currentActivity
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        //初始化信鸽
//        initXGPush()
        RxJavaPlugins.setErrorHandler { throwable -> CommonUtil.printError(applicationContext, throwable) }
        //初始化极光推送
//        initJPush();
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        initPRC()
        //初始化友盟
//        UMConfigure.init(applicationContext, UMConfigure.DEVICE_TYPE_PHONE, "5d75f996570df376960000b5")
        //实人认证初始化
        initX5()
        //清理webview的缓存
        WebView(applicationContext).destroy()
        //初始化腾讯i
        initTencentIM()
        //initLanguageItems(applicationContext)
        LanguageUtil.changeAppLanguage(this, FryingLanguage(Locale.ENGLISH,0,getString(R.string.language_english)),true)
        StyleChangeUtil.setStyleChangeSetting(this, FryingStyleChange(0,"绿涨红跌"))
        ExchangeRatesUtil.setExChangeRatesSetting(this, FryingExchangeRates(1, "USD"))
        initFilters()
        BlackRouter.getInstance().init(this)
        BlackRouter.getInstance().setWebViewPath(RouterConstData.WEB_VIEW)
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                setLocale()
                if (FryingUtil.checkRouteUri(activity, RouterConstData.LOGIN)) { //进入登陆界面，不在响应tokenError
                    checkTokenError = false
                }
                if (!LanguageUtil.isSameWithSetting(activity)) {
                    LanguageUtil.changeAppLanguage(activity, FryingLanguage(Locale.ENGLISH, 0, getString(
                        com.black.base.R.string.language_english)), true)
                }
                //ExchangeRatesUtil.setExChangeRatesSetting(activity, FryingExchangeRates(0,"CNY"))
            }

            override fun onActivityStarted(activity: Activity) {
                //如果mFinalCount ==1，说明是从后台到前台
                //回到前台，如果是超过1分钟
                if (goBackTime != null && FryingUtil.needShowProtectActivity(activity) && System.currentTimeMillis() - goBackTime!! > GO_BACK_TIME_OUT) { //清除goBackTime
                    goBackTime = null
                    //弹出锁屏保护界面
                    val protectType = CookieUtil.getAccountProtectType(applicationContext)
                    val token = CookieUtil.getToken(applicationContext)
                    if (protectType == ConstData.ACCOUNT_PROTECT_GESTURE && !TextUtils.isEmpty(token)) {
                        //手势密码验证
                        val bundle = Bundle()
                        bundle.putBoolean(ConstData.CHECK_UN_BACK, true)
                        BlackRouter.getInstance().build(RouterConstData.GESTURE_PASSWORD_CHECK)
                                .with(bundle)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                .go(activity) { routeResult, error ->
                                    if (error != null) {
                                        FryingUtil.printError(activity, error)
                                    }
                                }
                    } else if (protectType == ConstData.ACCOUNT_PROTECT_FINGER) {
                        val fingerPrintStatus = CommonUtil.getFingerPrintStatus(activity)
                        if (fingerPrintStatus != 1) {
                            CookieUtil.setAccountProtectType(activity, ConstData.ACCOUNT_PROTECT_NONE)
                        } else { //指纹验证
                            val bundle = Bundle()
                            bundle.putBoolean(ConstData.CHECK_UN_BACK, true)
                            BlackRouter.getInstance().build(RouterConstData.FINGER_PRINT_CHECK)
                                    .with(bundle)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    .go(activity) { routeResult, error ->
                                        if (error != null) {
                                            FryingUtil.printError(activity, error)
                                        }
                                    }
                        }
                    }
                } else {
                    goBackTime = null
                }
            }

            override fun onActivityResumed(activity: Activity) {
                MobclickAgent.onResume(activity)
                currentActivity = activity
                if(isAppOnForeground){
                    if(STAY_BACKGROUND_TIME <= 0){
                        SocketUtil.sendSocketCommandBroadcast(activity, SocketUtil.COMMAND_RESUME)
                    }
                    STAY_BACKGROUND_TIME = ConstData.APP_STAY_BACKGROUND_TIME
                    WebSocketHandler.registerNetworkChangedReceiver(applicationContext)
                }
            }

            override fun onActivityPaused(activity: Activity) {
                MobclickAgent.onResume(activity)
            }

            override fun onActivityStopped(activity: Activity) {
                //如果mFinalCount ==0，说明是前台到后台
//                if (FryingUtil.checkActivityRouter(activity, RouterConstData.HOME_PAGE)) {
//                    //主界面停止时 关闭监听行情，挂单
//                    SocketUtil.sendSocketCommandBroadcast(activity, SocketUtil.COMMAND_QUOTA_CLOSE);
//                    SocketUtil.sendSocketCommandBroadcast(activity, SocketUtil.COMMAND_ORDER_CLOSE);
//                }
                Log.d(tag, "onActivityStopped->isAppOnForeground = $isAppOnForeground")
                if (!isAppOnForeground && FryingUtil.needShowProtectActivity(activity)) { //记录当前退回后台时间
                    goBackTime = System.currentTimeMillis()
                }
                if(!isAppOnForeground){
                    WebSocketHandler.unRegisterNetworkChangeReceiver(applicationContext)
                    mHandler.post(backStayedTimerRunnable)
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {}
            override fun onActivityDestroyed(activity: Activity) {
                if (FryingUtil.checkRouteUri(activity, RouterConstData.LOGIN)) {
                    //离开登陆界面，恢复响应tokenError
//                    checkTokenError = true;
                }
            }
        })
        TypefaceManager.init(this)
        CustomerTypefaceSpanManager.init(this)
        initSkinLoader()
//        initZendesk()
    }


    fun initZendesk(){
//        Zendesk.INSTANCE.init(this, "https://fiexsupport.zendesk.com", "a825c816c9ce113e9b2c8e680961e6bf42c2194c1f12709c", "mobile_sdk_client_b6b89bd116298645a3df")
//        val identity = AnonymousIdentity()
//        Zendesk.INSTANCE.setIdentity(identity)
//        Support.INSTANCE.init(Zendesk.INSTANCE)
    }

    /**
     * 创蓝活体认证
     */
    private fun initPRC() {
//        RPCSDKManager.getInstance().init(this@FryingApplication, APP_ID, APP_KEY, LICENSE_ID)
//        //定义活体检测动作
//        val list: MutableList<LivenessTypeEnum> = ArrayList()
//        list.add(LivenessTypeEnum.Eye)
//        list.add(LivenessTypeEnum.HeadLeftOrRight)
//        RPCSDKManager.getInstance().setLivenessTypeEnum(list)
    }

    /**
     * APP是否处于前台唤醒状态
     *
     * @return
     */
    val isAppOnForeground: Boolean
        get() {
            val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val packageName = applicationContext.packageName
            val appProcesses = activityManager?.runningAppProcesses
                    ?: return false
            for (appProcess in appProcesses) {
                if (appProcess.processName == packageName && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true
                }
            }
            return false
        }

    //换肤框架初始化完成之后进行极光推送初始化
    private open inner class FryingSkinLoaderListener() : SkinLoaderListener {

        override fun onSuccess() {
            Log.e("initSkinLoader", "onSuccess =======")
            //初始化极光推送
//            initJPush()
        }

        override fun onFailed(errMsg: String?) {
            Log.e("initSkinLoader", "onFailed =======")
            //初始化极光推送
//            initJPush()
        }

        override fun onStart() {
            Log.e("initSkinLoader", "start =======")
        }

    }

    var isInitSkinLoader = false
    /**
     * Must call init first
     */
    private fun initSkinLoader() {
        //Log.e("initSkinLoader", "isInitSkinLoader =======:$isInitSkinLoader")
        if (isInitSkinLoader) {
            return
        }
        isInitSkinLoader = true
        val skinCompatManager = SkinCompatManager.withoutActivity(this)
                .addInflater(SkinAppCompatViewInflater()) // 基础控件换肤初始化
                .addInflater(SkinMaterialViewInflater()) // material design 控件换肤初始化[可选]
                .addInflater(SkinConstraintViewInflater()) // ConstraintLayout 控件换肤初始化[可选]
                .addInflater(SkinCardViewInflater()) // CardView v7 控件换肤初始化[可选]
                .setSkinStatusBarColorEnable(false) // 关闭状态栏换肤，默认打开[可选]
                .setSkinWindowBackgroundEnable(false) // 关闭windowBackground换肤，默认打开[可选]
        //设置默认为夜间模式
        if (!CookieUtil.hasSetNightMode(this)) {
//            skinCompatManager.loadSkin("night.skin", object : FryingSkinLoaderListener() {
//                override fun onStart() {
//                    super.onStart()
//                }
//
//                override fun onSuccess() {
//                    super.onSuccess()
//                    CookieUtil.setNightMode(applicationContext, true)
//                }
//
//                override fun onFailed(errMsg: String?) {
//                    super.onFailed(errMsg)
//                    CookieUtil.setNightMode(applicationContext, false)
//                }
//            }, SkinCompatManager.SKIN_LOADER_STRATEGY_ASSETS)
        } else {
//            skinCompatManager.loadSkin(FryingSkinLoaderListener())
        }
        skinCompatManager.loadSkin(FryingSkinLoaderListener())
    }

    fun setLocale() {
        val fryingLanguage = LanguageUtil.getLanguageSetting(this)
        if (fryingLanguage != null && !LanguageUtil.isSameWithSetting(this)) {
            val config = baseContext.resources.configuration
            Locale.setDefault(fryingLanguage.locale)
            config.locale = fryingLanguage.locale
            baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
        }
    }

    private fun initX5() {
        val cb: PreInitCallback = object : PreInitCallback {
            override fun onViewInitFinished(arg0: Boolean) {
                // TODO Auto-generated method stub
                //x5內核初始化完成的回调，为true表示x5内核加载成功，否则表示x5内核加载失败，会自动切换到系统内核。
            }

            override fun onCoreInitFinished() {
                // TODO Auto-generated method stub
            }
        }
        //x5内核初始化接口
        QbSdk.initX5Environment(applicationContext, cb)
    }

    private var xgAccount: String? = null
    private var xgTag: String? = null

    //信鸽初始化
    private fun initXGPush() {
        if (isXGRegister) {
            return
        }
        CommonUtil.postHandleTask(handler, {
            isXGRegister = true
            XGPushConfig.enableDebug(applicationContext, CommonUtil.isApkInDebug(applicationContext))
            XGPushManager.registerPush(applicationContext, object : XGIOperateCallback {
                override fun onSuccess(data: Any?, flag: Int) {
                    isXGRegister = false
                    //token在设备卸载重装的时候有可能会变
//                        xgBind();
                }

                override fun onFail(data: Any?, errCode: Int, msg: String?) {
                    isXGRegister = false
                    //token在设备卸载重装的时候有可能会变
                }
            })
        }, 0)
        //注意在3.2.2 版本信鸽对账号绑定和解绑接口进行了升级具体详情请参考API文档。
//        XGPushManager.bindAccount(getApplicationContext(), "XINGE");
//        xgBindAccount();
//设置标签
//        XGPushManager.setTag(getApplicationContext(), "XINGE");
//        xgBindTag();
    }

    fun xgBind() {
        if (isXGBind) {
            return
        }
        isXGBind = true
        val userInfo = CookieUtil.getUserInfo(applicationContext)
        val userId = if (userInfo == null) "XINGE" else userInfo.id
        val account = if (FryingUtil.isReal(applicationContext)) "REAL_$userId" else "DEV_$userId"
        var tag = "XINGE"
        val language = LanguageUtil.getLanguageSetting(applicationContext)
        tag = if (language == null || language.languageCode == 1) {
            if (FryingUtil.isReal(applicationContext)) "REAL_LANGUAGE_CHINESE" else "DEV_LANGUAGE_CHINESE"
        } else {
            if (FryingUtil.isReal(applicationContext)) "REAL_LANGUAGE_ENGLISH" else "DEV_LANGUAGE_ENGLISH"
        }
        if (TextUtils.equals(account, xgAccount) && TextUtils.equals(xgTag, tag)) {
            isXGBind = false
            return
        }
        xgAccount = account
        xgTag = tag
        xgBindAccount(xgAccount)
        xgBindTag(xgTag)
    }

    fun xgBindAccount(xgAccount: String?) {
        handler.postDelayed({
            XGPushManager.delAllAccount(applicationContext, object : XGIOperateCallback {
                override fun onSuccess(o: Any?, i: Int) {
                    doXGBind(xgAccount)
                }

                override fun onFail(o: Any?, i: Int, s: String?) {
                    doXGBind(xgAccount)
                }
            })
        }, 3000)
    }

    fun doXGBind(xgAccount: String?) {
        XGPushManager.bindAccount(applicationContext, xgAccount, object : XGIOperateCallback {
            override fun onSuccess(o: Any?, i: Int) {
                isXGBind = false
            }

            override fun onFail(o: Any?, i: Int, s: String?) {
                isXGBind = false
            }
        })
    }

    fun xgBindTag(xgTag: String?) {
        /*
        中文：
        REAL_LANGUAGE_CHINESE
        DEV_LANGUAGE_CHINESE
        英文：
        REAL_LANGUAGE_ENGLISH
        DEV_LANGUAGE_ENGLISH
         */
        XGPushManager.setTag(applicationContext, xgTag)
    }

    //初始化腾讯im
    private fun initTencentIM() {
        // 配置 Config，请按需配置
        val configs = TUIKit.configs
        val sdkConfig = TIMSdkConfig(ConstData.TENCENT_APP_ID)
        sdkConfig.enableLogPrint(CommonUtil.isApkInDebug(applicationContext))
        configs.setSdkConfig(sdkConfig)
        configs.setCustomFaceConfig(CustomFaceConfig())
        val generalConfig = GeneralConfig()
        generalConfig.enableLogPrint(CommonUtil.isApkInDebug(applicationContext))
        configs.setGeneralConfig(generalConfig)
        TUIKit.init(this, ConstData.TENCENT_APP_ID, configs)
    }

    //极光推送
    private fun initJPush() {
        //Log.e("initJPush", "initJPush start")
        JPushInterface.setDebugMode(CommonUtil.isApkInDebug(applicationContext))
        JPushUPSManager.registerToken(applicationContext, "ae8781e09d96a07c25e13deb", null, "") {
            //Log.e("initJPush", "initJPush onResult")
            hasInitJGPush = true
        }
    }

    private val mHandler = Handler(Handler.Callback { msg ->
        when (msg?.what) {
            BIND_ALIAS -> {
                val alias = msg.obj?.toString()
                alias?.let {
                    JPushInterface.setAlias(applicationContext, alias, BindAliasCallback(alias))
                }
            }
            BIND_TAG -> {
                val tags = msg.obj as MutableSet<String?>?
                tags?.let {
                    JPushInterface.setTags(applicationContext, tags, BindTagCallback(tags))
                }
            }
        }
        false
    })

    fun jPushBind() {
        val userInfo = CookieUtil.getUserInfo(applicationContext)
        val userId = if (userInfo == null) "JPUSH" else userInfo.id
        val account = if (FryingUtil.isReal(applicationContext)) "REAL_$userId" else "DEV_$userId"
        var tag = "JPUSH"
        val language = LanguageUtil.getLanguageSetting(applicationContext)
        tag = if (language == null || language.languageCode == 1) {
            if (FryingUtil.isReal(applicationContext)) "REAL_LANGUAGE_CHINESE" else "DEV_LANGUAGE_CHINESE"
        } else {
            if (FryingUtil.isReal(applicationContext)) "REAL_LANGUAGE_ENGLISH" else "DEV_LANGUAGE_ENGLISH"
        }
        val tags: MutableSet<String?> = HashSet()
        tags.add(tag)
//        JPushInterface.setAlias(applicationContext, 1, account)
//        JPushInterface.setTags(applicationContext, 1, tags)
        bindJPushAlias(account)
        bindJPushTags(tags)
    }

    inner class BindAliasCallback(private val alias: String) : TagAliasCallback {

        override fun gotResult(code: Int, alias1: String?, tags: MutableSet<String?>?) {
            if (code == 6002) {
                // 延迟 60 秒来调用 Handler 设置别名
                mHandler.sendMessageDelayed(mHandler.obtainMessage(BIND_ALIAS, alias), 1000 * 10);
            }
        }

    }

    inner class BindTagCallback(private val tags: MutableSet<String?>?) : TagAliasCallback {

        override fun gotResult(code: Int, alias1: String?, tags1: MutableSet<String?>?) {
            if (code == 6002) {
                // 延迟 60 秒来调用 Handler 设置别名
                mHandler.sendMessageDelayed(mHandler.obtainMessage(BIND_TAG, tags), 1000 * 10);
            }
        }

    }

    private fun bindJPushAlias(alias: String) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(BIND_ALIAS, alias), if (hasInitJGPush) 0 else 3000);
    }

    private fun bindJPushTags(tags: MutableSet<String?>) {
        mHandler.sendMessageDelayed(mHandler.obtainMessage(BIND_TAG, tags), if (hasInitJGPush) 0 else 3000);
    }
}