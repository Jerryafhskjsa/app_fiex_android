package com.black.frying.contract.biz.okwebsocket.market

import com.black.base.util.UrlConfig
import com.black.frying.FryingApplication
import com.black.net.HttpCookieUtil
import com.black.net.okhttp.OKWebSocketFactory
import com.black.net.okhttp.OkWebSocket
import com.black.net.okhttp.OkWebSocketHelper
import org.json.JSONObject

fun OkWebSocketHelper.sendCommandUserListenKey() {
    val jsonObject = JSONObject()
    val listenKey = HttpCookieUtil.getListenKey(FryingApplication.instance())
        ?: //更新key
        return
    jsonObject.put("req", "sub_user")
    jsonObject.put("listenKey", listenKey)
    okWebSocket.sendMsg(jsonObject.toString())
}

fun OkWebSocketHelper.sendCommandSymbol(coinPair:String="btc_usdt") {
    val jsonObject = JSONObject()
    jsonObject.put("req", "sub_symbol")
    jsonObject.put("symbol", coinPair)
    okWebSocket.sendMsg(jsonObject.toString())
}

fun OkWebSocketHelper.sendCommandUnSymbol() {
    val jsonObject = JSONObject()
    jsonObject.put("req", "unsub_symbol")
    okWebSocket.sendMsg(jsonObject.toString())
}

fun getMarketOkWebSocket(): OkWebSocket {
    return  OKWebSocketFactory.getOkWebSocket(UrlConfig.SOCKET_HOSTS_SOEASTEX + "market")
}

fun getUserOkWebSocket(): OkWebSocket {
    return OKWebSocketFactory.getOkWebSocket(UrlConfig.SOCKET_HOSTS_SOEASTEX + "user")
}