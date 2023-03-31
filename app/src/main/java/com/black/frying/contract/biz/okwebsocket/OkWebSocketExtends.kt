package com.black.frying.contract.biz.okwebsocket

import com.black.base.util.UrlConfig
import com.black.net.okhttp.OKWebSocketFactory
import com.black.net.okhttp.OkWebSocket
import com.black.net.okhttp.OkWebSocketHelper
import org.json.JSONObject


fun OkWebSocketHelper.sendCommandSymbol() {
    val webSocket = okWebSocket.webSocket
    val jsonObject = JSONObject()
    jsonObject.put("req", "sub_symbol")
    jsonObject.put("symbol", "btc_usdt")
    webSocket.send(jsonObject.toString())
}
fun OkWebSocketHelper.sendCommandUnSymbol() {
    val webSocket = okWebSocket.webSocket
    val jsonObject = JSONObject()
    jsonObject.put("req", "unsub_symbol")
    webSocket.send(jsonObject.toString())
}
fun getMarketOkWebSocket(): OkWebSocket {
    return OKWebSocketFactory.getOkWebSocket(UrlConfig.SOCKET_HOSTS_SOEASTEX + "market")
}