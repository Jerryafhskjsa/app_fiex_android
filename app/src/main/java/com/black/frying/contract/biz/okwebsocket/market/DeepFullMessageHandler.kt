package com.black.frying.contract.biz.okwebsocket.market

import com.black.base.model.trade.TradeOrderDepth
import com.black.frying.contract.utils.globalGson
import com.black.net.okhttp.OkWebSocketHelper
import org.json.JSONObject

abstract class DeepFullMessageHandler : OkWebSocketHelper.IMessageHandler{

    override fun observerMessage(channel: String?, data: Any?): Boolean {
        return "push.deep.full" == channel
    }

    override fun processingMessage(t: Any?) {
        if (t is JSONObject) {
            val deepBean = globalGson.fromJson(t.toString(), TradeOrderDepth::class.java)
            consumeMessage(deepBean)
        }
    }
    abstract fun consumeMessage(deepBean: TradeOrderDepth)
}