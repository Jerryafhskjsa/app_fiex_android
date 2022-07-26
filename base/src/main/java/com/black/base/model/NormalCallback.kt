package com.black.base.model

import android.content.Context
import com.black.base.activity.BaseActionBarActivity
import com.black.base.activity.BaseActivity
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil.showToast
import com.black.util.Callback

abstract class NormalCallback<T>(protected var context: Context) : Callback<T>() {
    override fun error(type: Int, error: Any?) {
        when (type) {
            ConstData.ERROR_NORMAL -> showToast(context, error.toString())
            ConstData.ERROR_TOKEN_INVALID -> if (context is BaseActionBarActivity) {
                (context as BaseActionBarActivity).onTokenError()
            } else if (context is BaseActivity) {
                (context as BaseActivity).onTokenError()
            }
            ConstData.ERROR_UNKNOWN ->
                //根據情況處理，error 是返回的HttpRequestResultError
                if (error != null && error is HttpRequestResultBase) {
                    showToast(context, error.message)
                }
        }
    }

}