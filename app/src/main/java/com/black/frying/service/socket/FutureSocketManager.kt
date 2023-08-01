package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.util.Log
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.*
import com.black.base.util.*
import com.black.net.HttpCookieUtil
import com.black.net.websocket.*
import com.black.util.Callback

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
    private var mContext: Context? = null
    private var mHandler: Handler? = null
    private var pingTimer: Timer? = null
    private var timerStart: Boolean = false
    private var pingTimerTask: TimerTask? = null
    var currentPair: String? = null


    private lateinit var socketSetting: WebSocketSetting
    private lateinit var socketUserSetting: WebSocketSetting

    //合约交易对相关
    private var symbolListener: SocketListener? = SymbolListener()
    private var userListener: SocketListener? = UserListener()


    init {
        mContext = context
        mHandler = handler
        currentPair = SocketUtil.getCurrentPair(mContext!!)
        initSocketManager(mContext)
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
        var socketUrl = UrlConfig.getSocketHostSoeasyEx(context,"market")

        socketSetting = WebSocketSetting()
        socketSetting.connectUrl = socketUrl
        socketSetting.connectionLostTimeout = 60//心跳间隔时间
        socketSetting.setReconnectWithNetworkChanged(true)//设置网络状态发生改变自动重连
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL, socketSetting)

        var socketUserUrl = UrlConfig.getSocketHostSoeasyEx(context,"user")
        socketUserSetting = WebSocketSetting()
        socketUserSetting.connectUrl = socketUserUrl
        socketUserSetting.connectionLostTimeout = 60//心跳间隔时间
        socketUserSetting.setReconnectWithNetworkChanged(true)//设置网络状态发生改变自动重连

        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_FUTURE_SUB_USER, socketUserSetting)
    }


    fun startConnect(socketName: String?) {
        WebSocketHandler.getWebSocket(socketName)?.start()
    }

    fun startConnectAll() {
        var socketManager = WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL)
        var socketUserManager = WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_USER)
        addListenerAll()
        socketManager.start()
        socketUserManager.start()
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
                SocketUtil.WS_FUTURE_SUB_USER -> {
                    listener = userListener
                }

            }
            it.value.removeListener(listener)
        }
        symbolListener = null
        userListener = null
    }

    fun addListenerAll() {
        var socketManager = WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL)
        var socketUserManager = WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_USER)

        var listener: SocketListener? = null
        var listener1: SocketListener? = null

        listener = if (symbolListener != null) {
            symbolListener
        } else {
            SymbolListener()
        }

        listener1 = if (userListener != null) {
            userListener
        } else {
            UserListener()
        }

        socketManager.addListener(listener)

        socketUserManager.addListener(listener1)
    }

    fun sendPing() {
        Log.d(tag, "心跳----->")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("ping", "ping")
            var socketManager = WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL)
            var sockeUserManager = WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_USER)

            Log.d(
                tag,
                "Socket state =," + socketManager.socketState + "listener is empty= " + socketManager.isListenerEmpty
            )
            Log.d(
                tag,
                "UserSocket state =," + sockeUserManager.socketState + "listener is empty= " + sockeUserManager.isListenerEmpty
            )
            if (socketManager.isConnect) {
                socketManager?.send("ping")
            }
            if (sockeUserManager.isConnect) {
                sockeUserManager?.send("ping")
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

            var jsonObject1 = JSONObject();
            jsonObject1.put("req", "sub_mark_price")
            jsonObject1.put("symbol", "btc_usdt")
            WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL)
                ?.send(jsonObject1.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    fun startUserListener() {
        Log.d(tag, "startUserListener---")
        try {
            val jsonObject = JSONObject()
            var listenKey = HttpCookieUtil.getListenKey(mContext)
            if (listenKey == null) {
                getListenKey(mContext!!)
                return
            }
            jsonObject.put("req", "sub_user")
            jsonObject.put("listenKey", listenKey) //“listenKey”:”上一步获取的listenKey”
            WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_USER)
                ?.send(jsonObject.toString())
            Log.d(tag, "startUserListener---" + jsonObject.toString())
        } catch (e: Exception) {
            Log.d(tag, "startUserListener---" + e.toString())
            FryingUtil.printError(e)
        }
    }

    inner class UserListener() : SimpleListener() {
        override fun onConnected() {
            Log.d(tag, "UserListener---->onConnected")
            startUserListener()
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d(tag, "UserListener->onMessage = $message")
            if (message.equals("succeed") || message.equals("pong")) {
                return
            }
            if (message.equals("invalid_listen_key")) {
                getListenKey(mContext!!)
                return
            }
            var data: JSONObject? = null
            try {
                data = JSONObject(message)
                if (data.has("channel")) {
                    var channel = data.get("channel")
                    var data = data.get("data")
                    when (channel) {
                        "user.balance" -> {
                            val userBalanceBean = gson.fromJson<UserBalanceBean>(
                                data.toString(),
                                object : TypeToken<UserBalanceBean?>() {}.type
                            )
                            Log.d("ttttttt-->UserListener", userBalanceBean.toString());
                            SocketDataContainer.updateFUserBalance(mHandler, userBalanceBean)
                        }
                        "user.position" -> {
                            val userPositionBean = gson.fromJson<UserPositionBean>(
                                data.toString(),
                                object : TypeToken<UserPositionBean?>() {}.type
                            )
                            Log.d("ttttttt-->UserListener", userPositionBean.toString());
                            SocketDataContainer.updatePosition(mHandler,userPositionBean)
                        }
                       /* "user.trade" -> {
                            val userTradeBean = gson.fromJson<UserTradeBean>(
                                data.toString(),
                                object : TypeToken<UserTradeBean?>() {}.type
                            )
                            Log.d("ttttttt-->UserListener", userTradeBean.toString());

                        }

                        */
                        "user.order" -> {
                            val userOrderBean = gson.fromJson<UserOrderBean>(
                                data.toString(),
                                object : TypeToken<UserOrderBean?>() {}.type
                            )
                            Log.d("ttttttt-->UserListener", userOrderBean.toString());
                            SocketDataContainer.updateOrder(mHandler,userOrderBean)
                        }
                        "user.notify" -> {

                        }
                    }
                }
            } catch (e: JSONException) {

            }

        }
    }

    /**
     * 获取listen-key
     */
    private fun getListenKey(context: Context) {
        FutureApiServiceHelper.getListenKey(context!!, false,
            object : Callback<HttpRequestResultBean<String>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->getListenKey", error.toString());
                }

                override fun callback(returnData: HttpRequestResultBean<String>?) {
                    if (returnData != null) {
                        Log.d("ttttttt-->getListenKey", returnData.toString());
                        var listenKey = returnData.result
                        HttpCookieUtil.saveListenKey(context, listenKey)
                    }
                }

            })
    }

    /**
     * 交易对相关
     */
    inner class SymbolListener() : SimpleListener() {
        override fun onConnected() {
            Log.d(tag, "SymbolListener---->onConnected")
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
//                    Log.d(tag, "SymbolListener channel = $channel")
                    when (channel) {
                        "push.ticker" -> { //行情
                            val tickerBean = gson.fromJson<TickerBean>(
                                data.toString(),
                                object : TypeToken<TickerBean?>() {}.type
                            )
                            FutureSocketData.onTicketChange(tickerBean)
//                            Log.d(tag, "SymbolListener->onMessage = ${tickerBean.toString()}")
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
//
                            //FutureSocketData.onMarkPriceChange(markPriceBean)
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