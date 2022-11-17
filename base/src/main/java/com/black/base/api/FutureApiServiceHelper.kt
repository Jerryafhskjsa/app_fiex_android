package com.black.base.api

import android.content.Context
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.HttpRequestResultData
import com.black.base.model.future.DepthBean
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.RxJavaHelper
import com.black.base.util.UrlConfig
import com.black.util.Callback

object FutureApiServiceHelper {

    fun getDepthData(
        context: Context?,
        symbol: String,
        level: Int,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<DepthBean?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(CommonApiService::class.java)
            ?.getDepth(symbol, level)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

}