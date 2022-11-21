package com.black.base.api

import android.content.Context
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.HttpRequestResultData
import com.black.base.model.future.DepthBean
import com.black.base.model.future.MarkPriceBean
import com.black.base.model.future.SymbolBean
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.RxJavaHelper
import com.black.base.util.UrlConfig
import com.black.util.Callback

object FutureApiServiceHelper {

    /**
     * 获取深度列表
     */
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
            .getService(FutureApiService::class.java)
            ?.getDepth(symbol, level)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取交易对列表
     */
    fun getSymbolList(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<SymbolBean>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getSymbolList()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取交易对列表
     */
    fun getMarkPrice(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultBean<ArrayList<MarkPriceBean>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
            ?.getMarkPrice()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }


}