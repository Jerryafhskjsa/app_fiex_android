package com.black.base.net

import android.app.Activity
import android.content.Context
import com.black.base.R
import com.black.base.api.RetryRequestHelper
import com.black.base.model.HttpRequestResultBase
import com.black.base.util.ConstData
import com.black.base.view.LoadingDialog
import com.black.base.view.MoneyPasswordVerifyWindow
import com.black.base.view.MoneyPasswordVerifyWindow.OnMoneyPasswordListener
import com.black.net.BlackHttpException
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.RSAUtil
import com.google.gson.internal.`$Gson$Types`
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

open class HttpCallbackSimple<T> : NetObserver<T> {
    protected var context: Context?
    protected var callback: Callback<T>?
    protected var loadingDialog: LoadingDialog? = null
    private var isShowLoading = false

    constructor(context: Context?, callback: Callback<T>?) {
        this.context = context
        this.callback = callback
    }

    constructor(context: Context?, isShowLoading: Boolean, callback: Callback<T>?) {
        this.context = context
        this.callback = callback
        this.isShowLoading = isShowLoading
        if (isShowLoading && context is Activity) {
            context.runOnUiThread { loadingDialog = LoadingDialog(context) }
        }
    }

    override fun beforeRequest() {
        if (isShowLoading && loadingDialog != null) {
            CommonUtil.checkActivityAndRunOnUI(context) { loadingDialog?.show() }
        }
    }

    override fun afterRequest() {
        if (isShowLoading && loadingDialog != null) {
            CommonUtil.checkActivityAndRunOnUI(context) {
                if (true == loadingDialog?.isShowing) {
                    loadingDialog?.dismiss()
                }
            }
        }
    }

    override fun error(returnString: Any?, errorType: Int) {
        CommonUtil.checkActivityAndRun(context) {
            var type = ConstData.ERROR_NORMAL
            var error: Any? = ""
            when (errorType) {
                HttpRequestResult.NOTWORK_ERROR -> error = context?.getString(R.string.notwork_error)
                HttpRequestResult.JSON_ERROR -> error = context?.getString(R.string.alert_server_error)
                HttpRequestResult.IO_ERROR -> error = context?.getString(R.string.alert_server_error)
                HttpRequestResult.OTHER_ERROR -> error = context?.getString(R.string.alert_server_error)
                HttpRequestResult.ERROR_TOKEN_INVALID -> {
                    type = ConstData.ERROR_TOKEN_INVALID
                    error = context?.getString(R.string.login_over_time)
                }
                HttpRequestResult.ERROR_MISS_MONEY_PASSWORD -> {
                    type = ConstData.ERROR_MISS_MONEY_PASSWORD
                    error = returnString
                }
            }
            if (type == ConstData.ERROR_MISS_MONEY_PASSWORD && error is BlackHttpException) {
                try {
                    afterRequest()
                    if (callback != null) {
                        callback?.error(type, error)
                    }
                } catch (ignored: Exception) {
                }
                val blackHttpException = error
                try {
                    val request = blackHttpException.response().request()
                    val builder = blackHttpException.builder()
                    if (context is Activity) {
                        MoneyPasswordVerifyWindow(context as Activity, object : OnMoneyPasswordListener {
                            override fun onReturn(window: MoneyPasswordVerifyWindow, moneyPassword: String?, timeMill: Boolean) {
                                var password: String? = moneyPassword
                                window.dismiss()
                                if (password != null) {
                                    password = RSAUtil.encryptDataByPublicKey(password)
                                }
                                RetryRequestHelper.requestQueryAddMoneyPassword(builder, request, password, timeMill, this@HttpCallbackSimple)
                            }

                            override fun onDismiss(window: MoneyPasswordVerifyWindow, dismissType: Int) {}
                        }).show()
                    }
                } catch (e: Exception) {
                }
            } else {
                if (callback != null) {
                    //                    error = returnString;
                    callback?.error(type, error)
                }
            }
        }
    }

    override fun onReturnResult(result: T) {
        CommonUtil.checkActivityAndRun(context, Runnable {
            if (result != null) {
                if (result is HttpRequestResultBase && (result as HttpRequestResultBase).is403Error()) {
                    if (callback != null) {
                        callback?.error(ConstData.ERROR_TOKEN_INVALID, result)
                    }
                    return@Runnable
                }
            }
            if (callback != null) {
                callback?.callback(result)
            }
        })
    }

    override fun getGenericType(): Type {
        if (callback != null) {
            return callback!!.genericType
        }
        val superclass = javaClass.genericSuperclass
        if (superclass is Class<*>) {
            throw RuntimeException("Missing type parameter.")
        }
        val parameterized = superclass as ParameterizedType
        return `$Gson$Types`.canonicalize(parameterized.actualTypeArguments[0])
    }
}
