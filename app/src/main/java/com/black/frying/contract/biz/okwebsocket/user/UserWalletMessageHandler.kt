package com.black.frying.contract.biz.okwebsocket.user

import com.black.base.model.future.UserBalanceBean
import com.black.net.okhttp.OkWebSocketHelper
import com.google.gson.Gson
import org.json.JSONObject

abstract class UserWalletMessageHandler : OkWebSocketHelper.IMessageHandler {
    val gson = Gson()
    override fun observerMessage(channel: String?, data: Any?): Boolean {
        return "user.balance" == channel
    }

    override fun processingMessage(t: Any) {
        if (t is JSONObject) {
            val userBalanceBean = gson.fromJson(t.toString(), UserBalanceBean::class.java)
            consumeMessage(userBalanceBean)
        }
//        else if (t is JSONArray) {
//
//        }
    }

    abstract fun consumeMessage(userBalanceBean: UserBalanceBean)
}