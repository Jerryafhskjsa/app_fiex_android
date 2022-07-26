package com.black.base.api

import android.content.Context
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.PagingData
import com.black.base.model.socket.TradeOrder
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.RxJavaHelper
import com.black.util.Callback

object TradeApiServiceHelper {
    // leverType 订单类型 PHYSICAL 现货， ISOLATED 逐仓杠杆 ALL 全仓杠杆
    fun createTradeOrder(context: Context?, pair: String?, direction: String?, totalAmount: String?, price: String?, leverType: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(TradeApiService::class.java)
                ?.createTradeOrder(pair, direction, totalAmount, price, leverType)
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

    fun getTradeOrderRecord(context: Context?, pair: String?, ended: Boolean, page: Int, size: Int, asc: Boolean, startTime: String?, endTime: String?, leverType: String?, isShowLoading: Boolean, callback: Callback<HttpRequestResultData<PagingData<TradeOrder?>?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(TradeApiService::class.java)
                ?.getTradeOrderRecord(pair, ended, page, size, asc, startTime, endTime, leverType)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }
}
