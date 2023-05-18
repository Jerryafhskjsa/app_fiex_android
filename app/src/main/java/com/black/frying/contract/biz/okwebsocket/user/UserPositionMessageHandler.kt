package com.black.frying.contract.biz.okwebsocket.user

import com.black.base.model.future.UserBalanceBean
import com.black.base.model.future.UserPositionBean
import com.black.frying.contract.utils.globalGson
import com.black.net.okhttp.OkWebSocketHelper
import com.google.gson.Gson
import org.json.JSONObject

abstract class UserPositionMessageHandler : OkWebSocketHelper.IMessageHandler {
    override fun observerMessage(channel: String?, data: Any?): Boolean {
        return "user.position" == channel
    }

    override fun processingMessage(t: Any) {
        if (t is JSONObject) {
            consumeMessage(globalGson.fromJson(t.toString(), UserPositionBean::class.java))
        }
//        else if (t is JSONArray) {
//
//        }
    }

    abstract fun consumeMessage(bean: UserPositionBean)
}