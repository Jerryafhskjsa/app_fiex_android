package com.black.frying.contract.viewmodel

import androidx.lifecycle.ViewModel
import com.black.base.util.UrlConfig
import com.black.frying.service.FutureService.symbol
import com.black.net.okhttp.OKWebSocketFactory
import com.black.net.okhttp.OkWebSocket
import com.black.net.okhttp.OkWebSocketHelper
import org.json.JSONObject

class FuturesViewModel : ViewModel() {
    // TODO: Implement the ViewModel

    var okWebSocketHelper: OkWebSocketHelper? = null
    fun startConnect() {
//        UrlConfig.SOCKET_HOSTS_SOEASTEX
//        UrlConfig.SPOT_SOCKET_HOSTS_SOEASTEX
//        OkWebSocket.INSTANCE.connection(UrlConfig.SOCKET_HOSTS_SOEASTEX + "market")
//        OkWebSocket.INSTANCE.connection(UrlConfig.SPOT_SOCKET_HOSTS_SOEASTEX)

        val okWebSocket =
            OKWebSocketFactory.getOkWebSocket(UrlConfig.SOCKET_HOSTS_SOEASTEX + "market")
        okWebSocketHelper = OkWebSocketHelper(okWebSocket)
    }


    fun testSend() {
        okWebSocketHelper?.apply {
            val webSocket = okWebSocket.webSocket
            val jsonObject = JSONObject()
            jsonObject.put("req", "sub_symbol")
            jsonObject.put("symbol", "btc_usdt")
            webSocket.send(jsonObject.toString())
        }
//        okWebSocketHelper?.testSend();
    }

    fun disConnect() {
        okWebSocketHelper?.apply {
            okWebSocket.disconnect()
        }
//        OkWebSocket.INSTANCE.disconnect()
    }

    override fun onCleared() {
        super.onCleared()
//        OkWebSocket.INSTANCE.disconnect()
    }
}