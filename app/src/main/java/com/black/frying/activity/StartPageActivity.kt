package com.black.frying.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import android.view.WindowManager
import com.black.base.BaseApplication
import com.black.base.activity.BaseActivity
import com.black.base.api.WalletApiServiceHelper
import com.black.base.manager.ApiManager
import com.black.base.manager.ApiManager2
import com.black.base.util.*
import com.black.frying.service.FutureService
import com.black.lib.permission.Permission
import com.black.lib.permission.ZbPermission
import com.black.lib.permission.ZbPermission.ZbPermissionCallback
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.FileCache
import com.fbsex.exchange.R
import com.tencent.android.tpush.XGPushManager
import java.util.*
import kotlin.math.min

@Route(value = [RouterConstData.START_PAGE])
class StartPageActivity : BaseActivity() {
    private val WAIT_MAX_TIME = 1000//调整等待时间，开始为10s
    private val REQUEST_STORAGE = 100
    private val REQUEST_CAMERA = 200
    private var runnable: Runnable? = null
    private val handler = Handler()
    private var waitTime = 0
    private var checkSign = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        val intent = intent
        if (intent != null && intent.flags and Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT == Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT) {
            finish()
            return
        }
        // 判断是否从推送通知栏打开的
        val message = XGPushManager.onActivityStarted(this)
        if (message != null) {
            if (isTaskRoot) {
                return
            }
            //如果有面板存在则关闭当前的面板
            finish()
            return
        }
        BaseApplication.checkTokenError = true
        clearSocketData()
        ApiManager.clearCache()
        WalletApiServiceHelper.clearCache()
        FileCache(this).clear()

//        setContentView(R.layout.activity_start_page_new)

        val sign = CommonUtil.getSignInfo(this)
        val rightSign = SecretUtil.getCertificate("sign")
        checkSign = Arrays.equals(sign, rightSign)
        if (!checkSign) {
            FryingUtil.showSignError(this)
        }

//        val ds = "981298.08889999"
//        val d = CommonUtil.parseDouble(ds)
//        Log.e("test", "d:" + d)
//        Log.e("test", "d:" + CommonUtil.formatNumberNoGroupScale(d, RoundingMode.FLOOR, 0, 8))
//        val dd = BigDecimal(d!!)
//        Log.e("test", "dd:" + dd)
//        Log.e("test", "dd:" + CommonUtil.formatNumberNoGroupScale(dd, RoundingMode.FLOOR, 0, 8))
//        val dd2 = BigDecimal(ds)
//        Log.e("test", "dd2:" + dd2)
//        Log.e("test", "dd2:" + CommonUtil.formatNumberNoGroupScale(dd2, RoundingMode.FLOOR, 0, 8))
//        Log.e("test", "dd2:" + getTypeName(1214))
//        Log.e("test", "dd2:" + getTypeName("123214"))
//        Log.e("test", "dd2:" + getTypeName(true))
    }

    fun <T> getTypeName(obj: T): String {
        val type = (obj as Any).javaClass.simpleName
        return type
    }

    private fun clearSocketData() {
        SocketUtil.clearAll(this)
        SocketDataContainer.clearAll()
    }

    override fun routeCheck(
        uri: String,
        beforePath: String?,
        requestCode: Int,
        flags: Int,
        extras: Bundle?
    ) {
        //不需要打开需要登录的目标
    }

    var needWaitTime = 0
    override fun onResume() {
        super.onResume()
        runnable = object : Runnable {
            override fun run() {
                if (isFinishing || !checkSign) {
                    return;
                }
                Log.d("StartPageActivity", "hasInitJGPush = " + BaseApplication.hasInitJGPush)
                if (!BaseApplication.hasInitJGPush) {
                    waitTime += 100
                    if (waitTime >= WAIT_MAX_TIME) {
                        gotoWorkPage();
                    } else {
                        handler.postDelayed(this, 100);
                    }
                } else {
                    //PUSH初始化完成之后等待3秒进入，防止signal 7 (SIGBUS)
                    if (needWaitTime == 0) {
                        needWaitTime = min(WAIT_MAX_TIME, waitTime + 3000)
                    }
                    waitTime += 100
                    if (waitTime >= needWaitTime) {
                        gotoWorkPage();
                    } else {
                        handler.postDelayed(this, 100);
                    }
                }
            }
        }
        normalNext();
    }

    override fun onPause() {
        super.onPause()
        if (runnable != null) {
            handler.removeCallbacks(runnable)
        }
        runnable = null
    }

    private fun gotoWorkPage() {
        gotoMainWorkActivity()
        //项目启动的时候初始化合约交易对

        //        if (isNewApp()) {
//            SharedPreferences.Editor editor = prefs.edit();
//            editor.putBoolean(ConstData.IS_NEW_APP, false);
//            editor.apply();
//
//            BlackRouter.getInstance().build(RouterConstData.GUID).go(mContext, new RouteCallback() {
//                @Override
//                public void onResult(boolean routeResult, Throwable error) {
//                    if (routeResult) {
//                        finish();
//                    }
//                    if (error != null) {
//                        error.printError();
//                    }
//                }
//            });
//        } else {
//            gotoMainWorkActivity();
//        }
    }

    private fun normalNext() {
        if (TextUtils.isEmpty(CookieUtil.getCookie(mContext, ConstData.REQUEST_CAMERA))) {
            CookieUtil.saveCookie(mContext, ConstData.REQUEST_CAMERA, "1")
            requestCameraPermission()
        } else if (TextUtils.isEmpty(CookieUtil.getCookie(mContext, ConstData.REQUEST_STORAGE))) {
            CookieUtil.saveCookie(mContext, ConstData.REQUEST_STORAGE, "1")
            requestStoragePermission()
        } else {
            handleGotoNext()
        }
    }

    private fun requestCameraPermission() {
        requestPermission(REQUEST_CAMERA, Permission.CAMERA, Runnable {
            CookieUtil.saveCookie(mContext, ConstData.REQUEST_CAMERA, "1")
            if (TextUtils.isEmpty(CookieUtil.getCookie(mContext, ConstData.REQUEST_STORAGE))) {
                requestStoragePermission()
            } else {
                handleGotoNext()
            }
        })
    }

    private fun requestStoragePermission() {
        requestPermission(REQUEST_STORAGE, Permission.STORAGE, Runnable {
            CookieUtil.saveCookie(mContext, ConstData.REQUEST_STORAGE, "1")
            handleGotoNext()
        })
    }

    private fun requestPermission(requestCode: Int, permissions: Array<String>, next: Runnable?) {
        ZbPermission.needPermission(this, requestCode, permissions, object : ZbPermissionCallback {
            override fun permissionSuccess(requestCode: Int) {
                next?.run()
            }

            override fun permissionFail(requestCode: Int) {
                next?.run()
            }
        })
    }

    private fun handleGotoNext() {
        if (runnable != null) {
            handler.postDelayed(runnable, if (BaseApplication.hasInitJGPush) 0 else 1000.toLong())
        }
    }

    override fun gotoMainWorkActivity() {
        var routerPath: String
        routerPath = RouterConstData.HOME_PAGE
        if (!TextUtils.isEmpty(CookieUtil.getToken(mContext))) { //快捷登录必须有token
            val protectType = CookieUtil.getAccountProtectType(mContext)
            if (protectType == ConstData.ACCOUNT_PROTECT_GESTURE) { //验证手势面是否存在
                if (!TextUtils.isEmpty(CookieUtil.getGesturePassword(mContext))) {
                    routerPath = RouterConstData.GESTURE_PASSWORD_CHECK
                }
            } else if (protectType == ConstData.ACCOUNT_PROTECT_FINGER) {
                val fingerPrintStatus = CommonUtil.getFingerPrintStatus(this)
                if (fingerPrintStatus != 1) {
                    CookieUtil.setAccountProtectType(this, ConstData.ACCOUNT_PROTECT_NONE)
                } else {
                    routerPath = RouterConstData.FINGER_PRINT_CHECK
                }
            }
        }
        //        routerPath = "TestFixed";
        val bundle = Bundle()
        bundle.putBoolean(ConstData.CHECK_UN_BACK, true)
        bundle.putString(ConstData.NEXT_ACTION, RouterConstData.HOME_PAGE)
        BlackRouter.getInstance().build(routerPath)
            .with(bundle)
            .go(this) { routeResult, error ->
                if (error != null) {
                    FryingUtil.printError(error)
                }
                if (routeResult) {
                    finish()
                }
            }
    }
}

