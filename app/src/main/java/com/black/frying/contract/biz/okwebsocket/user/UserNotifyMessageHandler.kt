package com.black.frying.contract.biz.okwebsocket.user

import com.black.base.model.future.*
import com.black.frying.contract.utils.globalGson
import com.black.net.okhttp.OkWebSocketHelper
import com.google.gson.Gson
import org.json.JSONObject

abstract class UserNotifyMessageHandler : OkWebSocketHelper.IMessageHandler {
    override fun observerMessage(channel: String?, data: Any?): Boolean {
        return "user.notify" == channel
    }

    override fun processingMessage(t: Any) {
        if (t is JSONObject) {
            consumeMessage(globalGson.fromJson(t.toString(), UserNotifyBean::class.java))
        }
//        else if (t is JSONArray) {
//
//        }
    }

    abstract fun consumeMessage(bean: UserNotifyBean)
}