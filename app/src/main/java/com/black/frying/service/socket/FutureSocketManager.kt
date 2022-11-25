package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.util.Log
import com.black.base.model.clutter.Kline
import com.black.base.model.future.*
import com.black.base.util.*
import com.black.net.websocket.*

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * 合约相关的Socket
 */
class FutureSocketManager(context: Context, handler: Handler) {
    private var tag: String = FutureSocketManager::class.java.simpleName
    private var gson: Gson = Gson()
        get() {
            if (field == null) {
                field = Gson()
            }
            return field
        }
    private var mCcontext: Context? = null
    private var mHandler: Handler? = null
    private var pingTimer: Timer? = null
    private var timerStart: Boolean = false
    private var pingTimerTask: TimerTask? = null
    var currentPair: String? = null


    private lateinit var socketSetting: WebSocketSetting

    //合约交易对相关
    private var symbolListener: SocketListener? = SymbolListener()


    init {
        mCcontext = context
        mHandler = handler
        currentPair = SocketUtil.getCurrentPair(mCcontext!!)
        initSocketManager(mCcontext)
        addListenerAll()
        startConnectAll()
    }

    private fun startPingTimer() {
        if (pingTimer == null) {
            pingTimer = Timer()
        }
        if (pingTimerTask == null) {
            pingTimerTask = object : TimerTask() {
                override fun run() {
                    sendPing()
                }
            }
        }
        if (!timerStart) {
            pingTimer?.schedule(pingTimerTask, Date(), 5000)
            timerStart = true
        }
    }

    private fun endPingTimer() {
        if (pingTimer != null) {
            pingTimer?.cancel()
            pingTimer = null
            pingTimerTask?.cancel()
            pingTimerTask = null
            timerStart = false
        }
    }

    private fun initSocketManager(context: Context?) {
        var socketUrl = UrlConfig.getFutureMarketSocketUrl()
        socketSetting = WebSocketSetting()
        socketSetting.connectUrl = socketUrl
        socketSetting.connectionLostTimeout = 60//心跳间隔时间
        socketSetting.setReconnectWithNetworkChanged(true)//设置网络状态发生改变自动重连
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL, socketSetting)
    }


    fun startConnect(socketName: String?) {
        WebSocketHandler.getWebSocket(socketName)?.start()
    }

    fun startConnectAll() {
        var socketManager = WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL)
        addListenerAll()
        socketManager.start()
        startPingTimer()
    }

    fun stopConnect(socketName: String?) {
        WebSocketHandler.getWebSocket(socketName)?.disConnect()
    }

    fun stopConnectAll() {
        removeListenerAll()
        var socketMap = WebSocketHandler.getAllWebSocket()
        socketMap.forEach {
            Log.d(tag, "stop all socketMap,key = " + it.key)
            Log.d(tag, "stop all socketMap,state = " + it.value.socketState)
            it.value.disConnect()
        }
        endPingTimer()
    }

    fun destorySocketAll() {
        var socketMap = WebSocketHandler.getAllWebSocket()
        socketMap.forEach {
            it.value.destroy()
        }
    }

    fun addListener(socketKey: String?) {
        var socketMar = WebSocketHandler.getWebSocket(socketKey)
        var isConnected = socketMar.isConnect
        Log.d(tag, "addListener,socketKey = $socketKey,isConnected = $isConnected")
        if (!isConnected) {
            Log.d(tag, "addListener,socketKey = $socketKey,----start")
            socketMar.start()
        }
        if (socketMar != null) {
            var listener: SocketListener? = null
            when (socketKey) {
//                SocketUtil.WS_USER -> {
//                    listener = if (userDataListener != null) {
//                        userDataListener
//                    } else {
//                        UserDataListener()
//                    }
//                }
            }
            socketMar.addListener(listener)
        }
    }

    fun removeListener(socketKey: String?) {
        when (socketKey) {
            SocketUtil.WS_USER -> {
                WebSocketHandler.getWebSocket(socketKey)?.removeListener(symbolListener)
                symbolListener = null
            }

        }
    }

    fun removeListenerAll() {
        var socketMgrList = WebSocketHandler.getAllWebSocket()
        var listener: SocketListener? = null
        socketMgrList.forEach {
            when (it.key) {
                SocketUtil.WS_FUTURE_SUB_SYMBOL -> {
                    listener = symbolListener
                }

            }
            it.value.removeListener(listener)
        }
        symbolListener = null
    }

    fun addListenerAll() {
        var socketManager = WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL)
        var listener: SocketListener? = null

        listener = if (symbolListener != null) {
            symbolListener
        } else {
            SymbolListener()
        }
        socketManager.addListener(listener)
    }

    fun sendPing() {
        Log.d(tag, "心跳----->")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("ping", "ping")
            var socketManager = WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL)

            Log.d(
                tag,
                "userSocket state =," + socketManager.socketState + "listener is empty= " + socketManager.isListenerEmpty
            )
            if (socketManager.isConnect) {
                socketManager?.send("ping")
            }
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    /**
     * "req":"sub_symbol",
    "  symbol":"btc_usdt"
     */
    fun startSymbolListener() {
        Log.d(tag, "startSymbolListener---")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("req", "sub_symbol")
            jsonObject.put("symbol", "btc_usdt")
            WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL)
                ?.send(jsonObject.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    /**
     * 交易对相关
     */
    inner class SymbolListener() : SimpleListener() {
        override fun onConnected() {
            Log.d(tag, "SymbolListener---->ßonConnected")
            startSymbolListener()
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
//            Log.d(tag, "SymbolListener->onMessage = $message")
            if (message.equals("succeed") || message.equals("pong")) {
                return
            }
            var data: JSONObject? = null
            try {
                data = JSONObject(message)
                if (data.has("channel")) {
                    var channel = data.get("channel");
                    var data = data.get("data");
                    Log.d(tag, "SymbolListener message = $channel")
                    when (channel) {
                        "push.ticker" -> { //行情
                            val tickerBean = gson.fromJson<TickerBean>(
                                data.toString(),
                                object : TypeToken<TickerBean?>() {}.type
                            )
                        }
                        "push.index.price" -> { //指数价格
                            val indexPriceBean = gson.fromJson<IndexPriceBean>(
                                data.toString(),
                                object : TypeToken<IndexPriceBean?>() {}.type
                            )
                        }
                        "push.mark.price" -> { //标记价格
                            val markPriceBean = gson.fromJson<MarkPriceBean>(
                                data.toString(),
                                object : TypeToken<MarkPriceBean?>() {}.type
                            )
                        }
                        "push.agg.ticker" -> { //聚合行情

                        }
                        "push.deal" -> { //实时成交
                            val dealBean = gson.fromJson<DealBean>(
                                data.toString(),
                                object : TypeToken<DealBean?>() {}.type
                            )
                        }
                        "push.deep" -> { //深度
                            val deepBean = gson.fromJson<DeepBean>(
                                data.toString(),
                                object : TypeToken<DeepBean?>() {}.type
                            )
                        }
                        "push.deep.full" -> { //全部深度
                            val deepFullBean = gson.fromJson<DeepFullBean>(
                                data.toString(),
                                object : TypeToken<DeepFullBean?>() {}.type
                            )
                        }
                        "push.fund.rate" -> {//资金费率
                            val fundRateBean = gson.fromJson<FundRateBean>(
                                data.toString(),
                                object : TypeToken<FundRateBean?>() {}.type
                            )
                        }
                    }
                }
            } catch (e: JSONException) {
                FryingUtil.printError(e)
            }

        }
    }


}