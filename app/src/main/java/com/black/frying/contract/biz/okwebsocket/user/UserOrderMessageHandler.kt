package com.black.frying.contract.biz.okwebsocket.user

import com.black.base.model.future.UserBalanceBean
import com.black.base.model.future.UserOrderBean
import com.black.base.model.future.UserPositionBean
import com.black.base.model.future.UserTradeBean
import com.black.frying.contract.utils.globalGson
import com.black.net.okhttp.OkWebSocketHelper
import com.google.gson.Gson
import org.json.JSONObject

abstract class UserOrderMessageHandler : OkWebSocketHelper.IMessageHandler {
    override fun observerMessage(channel: String?, data: Any?): Boolean {
        return "user.order" == channel
    }

    override fun processingMessage(t: Any) {
        if (t is JSONObject) {
            consumeMessage(globalGson.fromJson(t.toString(), UserOrderBean::class.java))
        }
//        else if (t is JSONArray) {
//
//        }
    }

    abstract fun consumeMessage(bean: UserOrderBean)
}