package com.black.base.net

import android.app.Activity
import android.content.Context
import com.black.base.R
import com.black.base.activity.BaseActionBarActivity
import com.black.base.activity.BaseActivity
import com.black.base.api.RetryRequestHelper
import com.black.base.model.HttpRequestResultBase
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.view.MoneyPasswordVerifyWindow
import com.black.base.view.MoneyPasswordVerifyWindow.OnMoneyPasswordListener
import com.black.net.BlackHttpException
import com.black.net.HttpRequestResult
import com.black.net.RequestObserveResult
import com.black.util.CommonUtil
import com.black.util.RSAUtil
import com.google.gson.internal.`$Gson$Types`
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

abstract class NormalObserver2<T>(protected var context: Context? = null) : NetObserver<RequestObserveResult<T?>>() {
    override fun beforeRequest() {
    }

    override fun afterRequest() {
    }

    final override fun error(returnString: Any?, errorType: Int) {
        CommonUtil.checkActivityAndRun(context) {
            var type = ConstData.ERROR_NORMAL
            var error: Any? = ""
            when (errorType) {
                HttpRequestResult.NOTWORK_ERROR -> error = context?.getString(R.string.notwork_error)
                HttpRequestResult.JSON_ERROR -> error = context?.getString(R.string.alert_server_error)
                HttpRequestResult.IO_ERROR -> error = context?.getString(R.string.alert_server_error)
                HttpRequestResult.OTHER_ERROR -> error = returnString
                        ?: context?.getString(R.string.alert_server_error)
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
                    error(type, error)
                } catch (ignored: Exception) {
                }
                val blackHttpException = error
                try {
                    val request = blackHttpException.response().request()
                    val builder = blackHttpException.builder()
                    if (context is Activity) {
                        MoneyPasswordVerifyWindow(context as Activity, object : OnMoneyPasswordListener {
                            override fun onReturn(window: MoneyPasswordVerifyWindow, moneyPassword: String?, timeMill: Boolean) {
                                var moneyPasswordNew: String? = moneyPassword
                                window.dismiss()
                                if (moneyPasswordNew != null) {
                                    moneyPasswordNew = RSAUtil.encryptDataByPublicKey(moneyPasswordNew)
                                }
                                RetryRequestHelper.requestQueryAddMoneyPassword(builder, request, moneyPasswordNew, timeMill, this@NormalObserver2)
                            }

                            override fun onDismiss(window: MoneyPasswordVerifyWindow, dismissType: Int) {}
                        }).show()
                    }
                } catch (e: Exception) {
                }
            } else {
                error(type, error)
            }
        }
    }

    open fun error(type: Int, error: Any?) {
        when (type) {
            ConstData.ERROR_NORMAL -> FryingUtil.showToast(context, error.toString())
            ConstData.ERROR_TOKEN_INVALID -> if (context is BaseActionBarActivity) {
                (context as BaseActionBarActivity).onTokenError()
            } else if (context is BaseActivity) {
                (context as BaseActivity).onTokenError()
            }
            ConstData.ERROR_UNKNOWN ->  //根據情況處理，error 是返回的HttpRequestResultError
                if (error != null && error is HttpRequestResultBase) {
                    FryingUtil.showToast(context, error.message)
                }
        }
    }

    final override fun onReturnResult(result: RequestObserveResult<T?>) {
        if (result.error != null) {
            onError(result.error)
        } else if (result.value != null) {
            CommonUtil.checkActivityAndRun(context, Runnable {
                if (result.value != null) {
                    if (result.value is HttpRequestResultBase && (result.value as HttpRequestResultBase).is403Error()) {
                        error(ConstData.ERROR_TOKEN_INVALID, result)
                        return@Runnable
                    }
                }
                callback(result.value)
            })
        }
    }

    override fun getGenericType(): Type? {
        val superclass = javaClass.genericSuperclass
        if (superclass is Class<*>) {
            throw RuntimeException("Missing type parameter.")
        }
        val parameterized = superclass as ParameterizedType
        return `$Gson$Types`.canonicalize(parameterized.actualTypeArguments[0])
    }

    abstract fun callback(result: T?)

}