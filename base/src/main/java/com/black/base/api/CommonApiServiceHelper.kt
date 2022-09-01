package com.black.base.api

import android.content.Context
import com.black.base.manager.ApiManager
import com.black.base.model.*
import com.black.base.model.clutter.*
import com.black.base.model.socket.PairDescription
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.RxJavaHelper
import com.black.util.Callback
import com.google.gson.JsonObject

object CommonApiServiceHelper {

    /**
     * 获取K线历史数据
     */
    fun getHistoryKline(
        context: Context?,
        symbol: String,
        interval: String,
        limit: Int,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultData<Kline?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true).getService(CommonApiService::class.java)
            ?.getHistoryKline(symbol,interval, limit)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }


    fun checkUpdate(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultData<Update?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true).getService(CommonApiService::class.java)
            ?.checkUpdate("android")
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun getCountryCodeList(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultDataList<CountryCode?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommonApiService::class.java)
            ?.getCountryCodeList()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun getNoticeInfo(
        context: Context?,
        language: String?,
        pageNum: Int,
        pageSize: Int,
        isSilent: Boolean,
        callback: Callback<HttpRequestResultData<NoticeData?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommonApiService::class.java)
            ?.getNoticeList(language, pageNum, pageSize)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, !isSilent, callback))
    }

    fun getBannerList(
        context: Context?,
        language: String?,
        level: String?,
        type: String?,
        callback: Callback<HttpRequestResultDataList<Banner?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommonApiService::class.java)
            ?.getHomePageMainBannerList(language, level, type)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getForumList(
        context: Context?,
        isShowLoading: Boolean,
        page: Int,
        pageSize: Int,
        callback: Callback<HttpRequestResultData<PagingData<Forum?>?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommonApiService::class.java)
            ?.getForumList(page, pageSize)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun getPairDescription(
        context: Context?,
        coinName: String?,
        lang: String?,
        callback: Callback<HttpRequestResultData<PairDescription?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommonApiService::class.java)
            ?.getPairDescription(coinName, lang)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getNoticeHome(
        context: Context?,
        languageKey: String?,
        pageSize: Int,
        page: Int,
        callback: Callback<NoticeHome?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommonApiService::class.java)
            ?.getNoticeHome(languageKey, pageSize, page)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getGlobalAd(
        context: Context?,
        language: String?,
        noticeName: String?,
        callback: Callback<HttpRequestResultData<GlobalAd?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommonApiService::class.java)
            ?.getGlobalAd(language, noticeName)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getUsdtCnyPrice(
        context: Context?,
        callback: Callback<HttpRequestResultData<CoinUsdtPrice?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommonApiService::class.java)
            ?.getUsdtCnyPrice()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun geetestInit(context: Context?, callback: Callback<HttpRequestResultData<JsonObject?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, true).getService(CommonApiService::class.java)
            ?.geetestInit()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getMyPosterList(
        context: Context?,
        callback: Callback<HttpRequestResultDataList<String?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommonApiService::class.java)
            ?.getMyPosterList()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getInviteUrl(context: Context?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(CommonApiService::class.java)
            ?.getInviteUrl()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }
}