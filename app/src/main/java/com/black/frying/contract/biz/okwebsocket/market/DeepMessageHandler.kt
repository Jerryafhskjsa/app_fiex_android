package com.black.frying.contract.biz.okwebsocket.market

import com.black.base.model.future.DeepBean
import com.black.frying.contract.utils.globalGson
import com.black.net.okhttp.OkWebSocketHelper
import org.json.JSONObject

abstract class DeepMessageHandler : OkWebSocketHelper.IMessageHandler{

    override fun observerMessage(channel: String?, data: Any?): Boolean {
        return "push.deep" == channel
    }

    override fun processingMessage(t: Any?) {
        if (t is JSONObject) {
            val deepBean = globalGson.fromJson(t.toString(), DeepBean::class.java)
            consumeMessage(deepBean)
        }
    }
    abstract fun consumeMessage(deepBean: DeepBean)
}