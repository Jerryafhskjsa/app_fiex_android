package com.black.frying.contract.biz.okwebsocket.market

import com.black.base.model.future.DealBean
import com.black.base.model.future.IndexPriceBean
import com.black.base.model.future.MarkPriceBean
import com.black.base.model.trade.TradeOrderDepth
import com.black.frying.contract.utils.globalGson
import com.black.net.okhttp.OkWebSocketHelper
import org.json.JSONObject

/**
 * 指数价格
 */
abstract class DealMessageHandler : OkWebSocketHelper.IMessageHandler{

    override fun observerMessage(channel: String?, data: Any?): Boolean {
        return "push.deal" == channel
    }

    override fun processingMessage(t: Any?) {
        if (t is JSONObject) {
            consumeMessage(globalGson.fromJson(t.toString(), DealBean::class.java))
        }
    }
    abstract fun consumeMessage(bean: DealBean)
}