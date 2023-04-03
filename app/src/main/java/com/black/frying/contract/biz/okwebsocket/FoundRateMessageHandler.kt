package com.black.frying.contract.biz.okwebsocket

import com.black.base.model.future.FundRateBean
import com.black.base.model.socket.PairQuotation
import com.black.net.okhttp.OkWebSocketHelper
import com.google.gson.Gson
import org.json.JSONObject

abstract class FoundRateMessageHandler : OkWebSocketHelper.IMessageHandler {
    private val gson = Gson()
    override fun observerMessage(channel: String?, data: Any?): Boolean {
        return "push.fund.rate" == channel
    }

    override fun processingMessage(t: Any) {
        if (t is JSONObject) {
            val bean = gson.fromJson(t.toString(), FundRateBean::class.java)
            consumeMessage(bean)
        }
//        else if (t is JSONArray) {
//
//        }
    }

    abstract fun consumeMessage(bean: FundRateBean)
}