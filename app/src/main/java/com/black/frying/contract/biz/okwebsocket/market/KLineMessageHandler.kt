package com.black.frying.contract.biz.okwebsocket.market

import com.black.base.model.future.KLineBean
import com.black.frying.contract.utils.globalGson
import com.black.net.okhttp.OkWebSocketHelper
import org.json.JSONObject

abstract class KLineMessageHandler : OkWebSocketHelper.IMessageHandler{

    override fun observerMessage(channel: String?, data: Any?): Boolean {
        return "push.kline" == channel
    }

    override fun processingMessage(t: Any?) {
        if (t is JSONObject) {
            consumeMessage(globalGson.fromJson(t.toString(), KLineBean::class.java))
        }
    }
    abstract fun consumeMessage(bean: KLineBean)
}