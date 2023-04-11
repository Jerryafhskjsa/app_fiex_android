package com.black.frying.contract.biz.okwebsocket.market

import com.black.base.model.socket.PairQuotation
import com.black.net.okhttp.OkWebSocketHelper
import com.google.gson.Gson
import org.json.JSONObject

abstract class SincePriceMessageHandler : OkWebSocketHelper.IMessageHandler {
    private val gson = Gson()
    override fun observerMessage(channel: String?, data: Any?): Boolean {
        return "push.ticker" == channel
    }

    override fun processingMessage(t: Any) {
        if (t is JSONObject) {
            val pairQuotation = gson.fromJson(t.toString(), PairQuotation::class.java)
            consumeMessage(pairQuotation)
        }
//        else if (t is JSONArray) {
//
//        }
    }

    abstract fun consumeMessage(pairQuotation: PairQuotation)
}