package com.black.base.api

import android.content.Context
import com.black.base.fragment.BaseFragment
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.socket.TradeOrder
import com.black.base.model.socket.TradeOrderFiex
import com.black.base.model.trade.TradeOrderResult
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.RxJavaHelper
import com.black.base.util.UrlConfig
import com.black.util.Callback

object TradeApiServiceHelper {
    fun createTradeOrder(context: Context?, symbol: String?, direction: String?, totalAmount: String?, price: String?, tradeType: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URL_PRO).getService(TradeApiService::class.java)
                ?.createTradeOrder(symbol, direction, totalAmount, price, tradeType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun cancelTradeOrder(context: Context?, orderId: String?, pair: String?, direction: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(TradeApiService::class.java)
                ?.cancelTradeOrder(orderId, pair, direction)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun cancelTradeOrderFiex(context: Context?, orderId: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URL_PRO).getService(TradeApiService::class.java)
            ?.cancelTradeOrderFiex(orderId)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    fun getTradeOrderRecord(context: Context?, pair: String?, ended: Boolean, page: Int, size: Int, asc: Boolean, startTime: String?, endTime: String?, leverType: String?, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<PagingData<TradeOrder?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(TradeApiService::class.java)
                ?.getTradeOrderRecord(pair, ended, page, size, asc, startTime, endTime, leverType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun getTradeOrderRecordFiex(context: Context?, symbol: String?, state:Int, startTime: String?, endTime: String?, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<TradeOrderResult?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context,false,UrlConfig.ApiType.URL_PRO).getService(TradeApiService::class.java)
            ?.getTradeOrderRecordFiex(symbol, state, startTime, endTime)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
}
