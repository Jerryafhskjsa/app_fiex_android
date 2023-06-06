package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.util.Log
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.clutter.Kline
import com.black.base.model.future.DeepBean
import com.black.base.model.future.FundRateBean
import com.black.base.model.future.IndexPriceBean
import com.black.base.model.future.MarkPriceBean
import com.black.base.model.socket.*
import com.black.base.model.trade.TradeOrderDepth
import com.black.base.model.trade.TradeOrderOneDepth
import com.black.base.model.user.UserBalance
import com.black.base.util.*
import com.black.net.HttpCookieUtil
import com.black.net.HttpRequestResult
import com.black.net.okhttp.OkWebSocketHelper
import com.black.net.websocket.*
import com.black.util.Callback
import com.black.util.CommonUtil
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class FiexSocketManager(context: Context, handler: Handler) {
    private var TAG: String = FiexSocketManager::class.java.simpleName
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
    var currentUFuturePair: String? = null
    var kLineTimeStep: String? = null
    var kLineTimeStepSecond: Long = 0
    var kLineId: String? = ""


    private lateinit var socketSetting: WebSocketSetting
    private lateinit var futureSocketSetting: WebSocketSetting

    //用户数据相关
    private var userDataListener: SocketListener? = UserDataListener()

    //所有币种行情相关
    private var tickerDataListener: SocketListener? = TickerStatusListener()

    //交易对行情相关
    private var subStatusSocketListener: SocketListener? = SubStatusDataListener()

    //交易对k线相关
    private var pairKlineSocketListener: SocketListener? = PairKlineListener()

    /*****future*****/
    private var futureSymbolListener: SocketListener? = FutureSymbolListener()
    private var futureTickersListener: SocketListener? = FutureTickersListener()
    private var futureMarkPriceListener:SocketListener? = FutureMarkPriceListener()
    private var futureKlineListener:SocketListener? = FutureKlineListener()

    /*****future*****/


    init {
        mCcontext = context
        mHandler = handler
        currentPair = SocketUtil.getCurrentPair(mCcontext!!)
        currentUFuturePair = CookieUtil.getCurrentFutureUPair(mCcontext!!)
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
        val socketUrl = UrlConfig.getSpotSocketHostSoeasyEx(context)
        Log.d("666666","socketUrl = "+socketUrl)
        socketSetting = WebSocketSetting()
        socketSetting.connectUrl = socketUrl
        socketSetting.connectionLostTimeout = 60//心跳间隔时间
        socketSetting.setReconnectWithNetworkChanged(true)//设置网络状态发生改变自动重连
        val futurSocketUrl = UrlConfig.getSocketHostSoeasyEx(context,"market")
        Log.d("666666","futurSocketUrl = "+futurSocketUrl)
        futureSocketSetting = WebSocketSetting()
        futureSocketSetting.connectUrl = futurSocketUrl
        futureSocketSetting.connectionLostTimeout = 60//心跳间隔时间
        futureSocketSetting.setReconnectWithNetworkChanged(true)//设置网络状态发生改变自动重连
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_USER, socketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_SUBSTATUS, socketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_PAIR_KLINE, socketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_TICKETS, socketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL, futureSocketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_FUTURE_SUB_TICKER, futureSocketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_FUTURE_SUB_KLINE, futureSocketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_FUTURE_SUB_MARK_PRICE,futureSocketSetting)
    }


    fun startConnect(socketName: String?) {
        WebSocketHandler.getWebSocket(socketName)?.start()
    }
    fun startConnectAll() {
        var socketMap = WebSocketHandler.getAllWebSocket()
        socketMap.forEach {
            Log.d(TAG, "start all socketMap,key = " + it.key)
            Log.d(TAG, "start all socketMap,state = " + it.value.socketState)
            Log.d(TAG, "start all socketMap,isListenerEmpty = " + it.value.isListenerEmpty)
            it.value.start()
        }
        startPingTimer()
    }

    fun stopConnect(socketName: String?) {
        WebSocketHandler.getWebSocket(socketName)?.disConnect()
    }

    fun stopConnectAll() {
        removeListenerAll()
        var socketMap = WebSocketHandler.getAllWebSocket()
        socketMap.forEach {
            Log.d(TAG, "stop all socketMap,key = " + it.key)
            Log.d(TAG, "stop all socketMap,state = " + it.value.socketState)
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
        Log.d(TAG, "addListener,socketKey = $socketKey,isConnected = $isConnected")
        if (!isConnected) {
            Log.d(TAG, "addListener,socketKey = $socketKey,----start")
            socketMar.start()
        }
        if (socketMar != null) {
            var listener: SocketListener? = null
            when (socketKey) {
                SocketUtil.WS_USER -> {
                    listener = if (userDataListener != null) {
                        userDataListener
                    } else {
                        UserDataListener()
                    }
                }
                SocketUtil.WS_SUBSTATUS -> {
                    listener = if (subStatusSocketListener != null) {
                        subStatusSocketListener
                    } else {
                        SubStatusDataListener()
                    }
                }
                SocketUtil.WS_TICKETS -> {
                    listener = if (tickerDataListener != null) {
                        tickerDataListener
                    } else {
                        TickerStatusListener()
                    }
                }
                SocketUtil.WS_PAIR_KLINE -> {
                    listener = if (pairKlineSocketListener != null) {
                        pairKlineSocketListener
                    } else {
                        PairKlineListener()
                    }
                }
                SocketUtil.WS_FUTURE_SUB_SYMBOL -> {
                    listener = if (futureSymbolListener != null) {
                        futureSymbolListener
                    } else {
                        FutureSymbolListener()
                    }
                }
                SocketUtil.WS_FUTURE_SUB_TICKER -> {
                    listener = if (futureTickersListener != null) {
                        futureTickersListener
                    } else {
                        FutureTickersListener()
                    }
                }
                SocketUtil.WS_FUTURE_SUB_KLINE -> {
                    listener = if (futureKlineListener != null) {
                        futureKlineListener
                    } else {
                        FutureTickersListener()
                    }
                }
                SocketUtil.WS_FUTURE_SUB_MARK_PRICE ->{
                    listener = if (futureMarkPriceListener != null) {
                        futureMarkPriceListener
                    } else {
                        FutureMarkPriceListener()
                    }
                }
            }
            socketMar.addListener(listener)
        }
    }

    fun removeListener(socketKey: String?) {
        when (socketKey) {
            SocketUtil.WS_USER -> {
                WebSocketHandler.getWebSocket(socketKey)?.removeListener(userDataListener)
                userDataListener = null
            }
            SocketUtil.WS_SUBSTATUS -> {
                WebSocketHandler.getWebSocket(socketKey)?.removeListener(subStatusSocketListener)
                subStatusSocketListener = null
            }
            SocketUtil.WS_TICKETS -> {
                WebSocketHandler.getWebSocket(socketKey)?.removeListener(tickerDataListener)
                tickerDataListener = null
            }
            SocketUtil.WS_PAIR_KLINE -> {
                WebSocketHandler.getWebSocket(socketKey)?.removeListener(pairKlineSocketListener)
                pairKlineSocketListener = null
            }
            SocketUtil.WS_FUTURE_SUB_SYMBOL -> {
                WebSocketHandler.getWebSocket(socketKey)?.removeListener(futureSymbolListener)
                futureSymbolListener = null
            }
            SocketUtil.WS_FUTURE_SUB_MARK_PRICE ->{
                WebSocketHandler.getWebSocket(socketKey)?.removeListener(futureMarkPriceListener)
                futureMarkPriceListener = null
            }
            SocketUtil.WS_FUTURE_SUB_KLINE ->{
                WebSocketHandler.getWebSocket(socketKey)?.removeListener(futureKlineListener)
                futureKlineListener = null
            }
        }
    }

    private fun removeListenerAll() {
        var socketMgrList = WebSocketHandler.getAllWebSocket()
        var listener: SocketListener? = null
        socketMgrList.forEach {
            when (it.key) {
                SocketUtil.WS_USER -> {
                    listener = userDataListener
                }
                SocketUtil.WS_SUBSTATUS -> {
                    listener = subStatusSocketListener
                }
                SocketUtil.WS_TICKETS -> {
                    listener = tickerDataListener
                }
                SocketUtil.WS_PAIR_KLINE -> {
                    listener = pairKlineSocketListener
                }
                SocketUtil.WS_FUTURE_SUB_SYMBOL -> {
                    listener = futureSymbolListener
                }
                SocketUtil.WS_FUTURE_SUB_TICKER -> {
                    listener = futureTickersListener
                }
                SocketUtil.WS_FUTURE_SUB_KLINE -> {
                    listener = futureKlineListener
                }
                SocketUtil.WS_FUTURE_SUB_MARK_PRICE ->{
                    listener = futureMarkPriceListener
                }
            }
            it.value.removeListener(listener)
        }
        userDataListener = null
        subStatusSocketListener = null
        tickerDataListener = null
        pairKlineSocketListener = null
        futureSymbolListener = null
        futureTickersListener = null
        futureKlineListener = null
        futureMarkPriceListener = null
    }

    private fun addListenerAll() {
        var socketMgrList = WebSocketHandler.getAllWebSocket()
        var listener: SocketListener? = null
        socketMgrList.forEach {
            when (it.key) {
                SocketUtil.WS_USER -> {
                    listener = if (userDataListener != null) {
                        userDataListener
                    } else {
                        UserDataListener()
                    }
                }
                SocketUtil.WS_SUBSTATUS -> {
                    listener = if (subStatusSocketListener != null) {
                        subStatusSocketListener
                    } else {
                        SubStatusDataListener()
                    }
                }
                SocketUtil.WS_TICKETS -> {
                    listener = if (tickerDataListener != null) {
                        tickerDataListener
                    } else {
                        TickerStatusListener()
                    }
                }
                SocketUtil.WS_PAIR_KLINE -> {
                    listener = if (pairKlineSocketListener != null) {
                        pairKlineSocketListener
                    } else {
                        PairKlineListener()
                    }
                }
                SocketUtil.WS_FUTURE_SUB_SYMBOL -> {
                    listener = if (futureSymbolListener != null) {
                        futureSymbolListener
                    } else {
                        FutureSymbolListener()
                    }
                }
                SocketUtil.WS_FUTURE_SUB_TICKER -> {
                    listener = if (futureTickersListener != null) {
                        futureTickersListener
                    } else {
                        FutureTickersListener()
                    }
                }
                SocketUtil.WS_FUTURE_SUB_MARK_PRICE ->{
                    listener = if (futureMarkPriceListener != null) {
                        futureMarkPriceListener
                    } else {
                        FutureMarkPriceListener()
                    }
                }
                SocketUtil.WS_FUTURE_SUB_KLINE -> {
                    listener = if (futureKlineListener != null) {
                        futureKlineListener
                    } else {
                        FutureKlineListener()
                    }
                }
            }
            it.value.addListener(listener)
        }
    }

    fun sendPing() {
        Log.d(TAG, "sendPing")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("ping", "ping")
            var allSocket: Map<String, WebSocketManager>? = WebSocketHandler.getAllWebSocket()
            if (allSocket != null) {
                for ((key, value) in allSocket) {
                    Log.d(
                        TAG,
                        "socket state =," + value.socketState + "listener is empty= " + value.isListenerEmpty
                    )
                    if (value.isConnect) {
                        value?.send(jsonObject.toString())
                    }
                }
            }
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    fun startListenKLine() {
        currentPair = SocketUtil.getCurrentPair(mCcontext!!)
        Log.d(TAG, "startListenKline = $currentPair")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subKline")
            jsonObject.put("symbol", currentPair?.uppercase())
            jsonObject.put("type", kLineTimeStep)
            WebSocketHandler.getWebSocket(SocketUtil.WS_PAIR_KLINE).send(jsonObject.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    fun startListenPair(pair: String?) {
        Log.d(TAG, "startListenPair = $pair")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subSymbol")
            jsonObject.put("symbol", pair)
            WebSocketHandler.getWebSocket(SocketUtil.WS_SUBSTATUS)?.send(jsonObject.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    fun startListenUser() {
        Log.d(TAG, "startListenUser")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subUser")
            jsonObject.put("token", HttpCookieUtil.getWsToken(mCcontext))
            WebSocketHandler.getWebSocket(SocketUtil.WS_USER)?.send(jsonObject.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    fun startListenTickers() {
        Log.d(TAG, "startListenTickers")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subStats")
            WebSocketHandler.getWebSocket(SocketUtil.WS_TICKETS).send(jsonObject.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    /**
     * 合约监听交易对行情
     * "req":"sub_symbol",
    "  symbol":"btc_usdt"
     */
    fun startListenFutureSymbol(symbol: String) {
        Log.d(TAG, "startListenFutureSymbol---")
        Log.d("666666","startListenFutureSymbol->pair = "+symbol)
        currentUFuturePair = symbol
        try {
            val jsonObject = JSONObject()
            jsonObject.put("req", "sub_symbol")
            jsonObject.put("symbol", symbol)
            WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_SYMBOL)
                ?.send(jsonObject.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    /**
     * 合约监听所有交易对24小时行情
     * "req":"sub_ticker",
    "  symbol":"btc_usdt" 不传为订阅所有
     */
    fun startListenFutureTickers() {
        Log.d(TAG, "startListenFutureTickers---")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("req", "sub_ticker")
            WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_TICKER)
                ?.send(jsonObject.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    /**
     * 合约监听所有交易对K线
     * "req":"sub_kline",
    "  symbol":"btc_usdt" 不传为订阅所有
     */
    fun startListenFutureKline() {
        currentPair = SocketUtil.getCurrentPair(mCcontext!!)
        Log.d(TAG, "startListenKline = $currentPair")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("req", "sub_kline")
            jsonObject.put("symbol", currentPair?.lowercase())
            jsonObject.put("type", kLineTimeStep)
            WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_KLINE)
                ?.send(jsonObject.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    /**
     * 合约监听所有交易对标记价格
     * "req":"sub_mark_price",
    "  symbol":"btc_usdt" 不传为订阅所有
     */
    fun startListenMarkPrice() {
        Log.d(TAG, "startListenMarkPrice---")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("req", "sub_mark_price")
            WebSocketHandler.getWebSocket(SocketUtil.WS_FUTURE_SUB_MARK_PRICE)
                ?.send(jsonObject.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    /**
     * 用户相关
     */
    inner class UserDataListener() : SimpleListener() {
        override fun onDisconnect() {
            Log.d(TAG, "UserDataListener onDisconnect")
        }

        override fun onConnected() {
            Log.d(TAG, "UserDataListener onConnected")
            startListenUser()
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d(TAG, "UserDataListener message = $message")
            if (message.equals("succeed")) {
                return
            }
            if (message.equals("invalid_ws_token")) {
                mCcontext?.let { getWsToken(it) }
                return
            }
            CommonUtil.postHandleTask(mHandler) {
                var data: JSONObject? = null
                try {
                    data = JSONObject(message)
                    if (data != null) {
                        var resType = data.getString("resType")
                        var resultData = data.getString("data")
                        Log.d(TAG, "UserDataListener resType = $resType")
                        Log.d(TAG, "UserDataListener resultData = $resultData")
                        when (resType) {
                            //余额变更
                            "uBalance" -> {
                                var balance: UserBalance? = null
                                try {
                                    val jsonObject: JsonObject =
                                        JsonParser().parse(resultData) as JsonObject
                                    balance = gson.fromJson<UserBalance?>(
                                        jsonObject.toString(),
                                        object : TypeToken<UserBalance?>() {}.type
                                    )
                                } catch (e: Exception) {
                                    FryingUtil.printError(e)
                                }
                                if (balance != null) {
                                    SocketDataContainer.onUserBalanceChangedFiex(balance)
                                }
                            }
                            //用户成交(暂时没用)
                            "uTrade" -> {

                            }
                            //用户订单
                            "uOrder" -> {
                                var tradeOrderFiex: TradeOrderFiex? = null
                                try {
                                    val jsonObject: JsonObject =
                                        JsonParser().parse(resultData) as JsonObject
                                    tradeOrderFiex = gson.fromJson<TradeOrderFiex?>(
                                        jsonObject.toString(),
                                        object : TypeToken<TradeOrderFiex?>() {}.type
                                    )
                                } catch (e: Exception) {
                                    FryingUtil.printError(e)
                                }
                                if (tradeOrderFiex != null) {
                                    SocketDataContainer.onUserOrderChangedFiex(tradeOrderFiex)
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    FryingUtil.printError(e)
                }
            }
        }
    }

    //获取ws-token
    private fun getWsToken(context: Context) {
        UserApiServiceHelper.getWsToken(context!!, object : Callback<HttpRequestResultString?>() {
            override fun error(type: Int, error: Any?) {
            }

            override fun callback(result: HttpRequestResultString?) {
                if (result != null && result.code == HttpRequestResult.SUCCESS) {
                    var wsToken = result.data
                    HttpCookieUtil.saveWsToken(context, wsToken)
                    startListenUser()
                }
            }
        })
    }

    /**
     * 所有现货交易对行情相关
     */
    inner class TickerStatusListener() : SimpleListener() {
        override fun onDisconnect() {
            Log.d(TAG, "tickerStatus onDisconnect")
        }

        override fun onConnected() {
            Log.d(TAG, "tickerStatus onConnected")
            startListenTickers()
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
//            d(TAG, "tickerStatus->onMessage = $message")
            if (message.equals("succeed")) {
                return
            }
            CommonUtil.postHandleTask(mHandler) {
                var data: JSONObject? = null
                try {
                    data = JSONObject(message)
                    if (data != null) {
                        var resType = data.getString("resType")
//                d(TAG, "tickerStatus->resType = $resType")
//                d(TAG, "tickerStatus->data = " + data.getString("data"))
                        when (resType) {
                            "qStats" -> {
                                var result = data.getString("data")
                                var pairQuo: PairStatusNew? = null
                                try {
                                    val jsonObject: JsonObject =
                                        JsonParser().parse(result) as JsonObject
//                            d(TAG, "tickerStatus->jsonObject = $jsonObject")
                                    pairQuo = gson.fromJson<PairStatusNew?>(
                                        jsonObject.toString(),
                                        object : TypeToken<PairStatusNew?>() {}.type
                                    )
                                } catch (e: Exception) {
                                    FryingUtil.printError(e)
                                }
                                if (pairQuo != null) {
                                    SocketDataContainer.updatePairStatusData(
                                        mCcontext,
                                        mHandler,
                                        pairQuo,
                                        false
                                    )
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    FryingUtil.printError(e)
                }
            }
        }
    }

    /**
     * 现货k线相关
     */
    inner class PairKlineListener() : SimpleListener() {
        override fun onDisconnect() {
            Log.d(TAG, "PairKline onDisconnect")
        }

        override fun onConnected() {
            Log.d(TAG, "PairKline onConnected")
            startListenKLine()
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d(TAG, "PairKline->onMessage = $message")
            if (message.equals("succeed")) {
                return
            }
            CommonUtil.postHandleTask(mHandler) {
                var data: JSONObject? = null
                try {
                    data = JSONObject(message)
                    if (data != null) {
                        Log.d(TAG, "PairKline->resType = " + data.getString("resType"))
                        var resultData = data.getString("data")
                        val kline = gson.fromJson<Kline>(
                            resultData.toString(),
                            object : TypeToken<Kline?>() {}.type
                        )
                        var klineItem = KLineItem()
                        if (kline != null) {
                            if (kline?.a != null) {
                                klineItem.a = kline?.a?.toDouble()!!
                            }
                            if (kline?.c != null) {
                                klineItem.c = kline?.c?.toDouble()!!
                            }
                            if (kline?.h != null) {
                                klineItem.h = kline?.h?.toDouble()!!
                            }
                            if (kline?.l != null) {
                                klineItem.l = kline?.l?.toDouble()!!
                            }
                            if (kline?.o != null) {
                                klineItem.o = kline?.o?.toDouble()!!
                            }
                            if (kline?.t != null) {
                                klineItem.t = kline?.t?.div(1000)
                            }
                            if (kline?.v != null) {
                                klineItem.v = kline?.v?.toDouble()!!
                            }
                            if (kline?.s.equals(currentPair)) {
                                SocketDataContainer.addKLineData(
                                    currentPair,
                                    mHandler,
                                    kLineId,
                                    ConstData.DEPTH_SPOT_TYPE,
                                    klineItem
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

    /**
     * 现货交易对相关
     */
    inner class SubStatusDataListener() : SimpleListener() {
        override fun onDisconnect() {
            Log.d(TAG, "SubStatusDataListener onDisconnect")
        }

        override fun onConnected() {
            Log.d(TAG, "SubStatusDataListener onConnected")
            currentPair = SocketUtil.getCurrentPair(mCcontext!!)
            Log.d(TAG, "SubStatusDataListener currentPair = "+currentPair)
            startListenPair(currentPair)
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            if (message.equals("succeed")) {
                return
            }
            Log.d(TAG, "SubStatusDataListener->onMessage = $message")
            CommonUtil.postHandleTask(mHandler) {
                var data: JSONObject? = null
                try {
                    data = JSONObject(message)
                    var resType = data.getString("resType")
//                    d(TAG, "subStatus->resType = $resType")
                    var resultData = data.getString("data")
//                    d(TAG, "subStatus->data = $resultData")
                    val jsonObject: JsonObject = JsonParser().parse(resultData) as JsonObject
                    when (resType) {
                        //50挡深度
                        "qAllDepth" -> {
                            val allDepth: TradeOrderDepth? = gson.fromJson<TradeOrderDepth?>(
                                jsonObject.toString(),
                                object : TypeToken<TradeOrderDepth?>() {}.type
                            )
                            if (allDepth != null) {
                                SocketDataContainer.updateQuotationOrderNewDataFiex(
                                    mCcontext,
                                    ConstData.DEPTH_SPOT_TYPE,
                                    mHandler,
                                    currentPair,
                                    allDepth,
                                    true
                                )
                            }
                        }
                        "qDepth" -> {
                            val oneDepth: TradeOrderOneDepth? =
                                gson.fromJson<TradeOrderOneDepth?>(
                                    jsonObject.toString(),
                                    object : TypeToken<TradeOrderOneDepth??>() {}.type
                                )
                            if (oneDepth != null) {
                                var allDepthData = TradeOrderDepth()
                                var direction = oneDepth?.m
                                var desArray: Array<String?>? =
                                    arrayOf(oneDepth?.p, oneDepth?.q)
                                if (direction.equals("1")) {//BID
                                    var bidArray = arrayOf(desArray)
                                    allDepthData?.b = bidArray
                                }
                                if (direction.equals("2")) {//ASK
                                    var askArray = arrayOf(desArray)
                                    allDepthData?.a = askArray
                                }
                                allDepthData.s = oneDepth?.s
//                            SocketDataContainer.updateQuotationOrderNewDataFiex(mCcontext,mHandler,currentPair,allDepthData,false)
                            }
                        }
                        //当前交易对成交数据
                        "qDeal" -> {
//                            d(TAG, "qDeal->data = $resultData")
                            val pairDeal: PairDeal? = gson.fromJson<PairDeal?>(
                                jsonObject.toString(),
                                object : TypeToken<PairDeal?>() {}.type
                            )
                            if (pairDeal != null) {
                                SocketDataContainer.getCurrentPairDeal(mHandler, pairDeal,ConstData.DEPTH_SPOT_TYPE)
                            }
                        }
                        //当前交易对24小时行情
                        "qStats" -> {
                            val pairQuo: PairQuotation? = gson.fromJson<PairQuotation?>(
                                jsonObject.toString(),
                                object : TypeToken<PairQuotation?>() {}.type
                            )
                            if (pairQuo != null) {
                                SocketDataContainer.getCurrentPairQuotation(mHandler, pairQuo)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    FryingUtil.printError(e)
                }
            }
        }
    }

    /**
     * 合约交易对相关
     */
    inner class FutureSymbolListener() : SimpleListener() {
        override fun onConnected() {
            Log.d(TAG, "SymbolListener---->ßonConnected")
            currentUFuturePair = CookieUtil.getCurrentFutureUPair(mCcontext!!)
            CookieUtil.getCurrentFutureUPair(mCcontext!!)?.let { startListenFutureSymbol(it) }
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
//            d(TAG, "SymbolListener->onMessage = $message")
            if (message.equals("succeed") || message.equals("pong")) {
                return
            }
            CommonUtil.postHandleTask(mHandler) {
                var data: JSONObject? = null
                try {
                    data = JSONObject(message)
                    Log.d("12376156", data.toString())
                    if (data.has("channel")) {
                        var channel = data.get("channel");
                        var data = data.get("data");
//                        val jsonObject: JsonObject = JsonParser().parse(data) as JsonObject
//                        d(TAG, "SymbolListener message = $channel")
                        Log.d(TAG, "SymbolListener->onMessage = $message")
                        when (channel) {
                            "push.ticker" -> { //行情，更新涨跌幅
                                val tickerBean = gson.fromJson<PairQuotation>(
                                    data.toString(),
                                    object : TypeToken<PairQuotation?>() {}.type
                                )
                                if(tickerBean != null){
                                    SocketDataContainer.updateFutureCurrentPairQuotation(mHandler,tickerBean)
                                }
                            }
                            "push.index.price" -> { //指数价格
                                val indexPriceBean = gson.fromJson<IndexPriceBean>(
                                    data.toString(),
                                    object : TypeToken<IndexPriceBean?>() {}.type
                                )
                                if (indexPriceBean != null) {
                                    SocketDataContainer.updateIndexPrice(mHandler, indexPriceBean)
                                }
                            }
                            "push.mark.price" -> { //标记价格
                                val markPriceBean = gson.fromJson<MarkPriceBean>(
                                    data.toString(),
                                    object : TypeToken<MarkPriceBean?>() {}.type
                                )
                                if (markPriceBean != null) {
                                    Log.d("1111",currentPair)
                                    SocketDataContainer.updateMarkPrice(mHandler, markPriceBean)
                                }
                            }
                            "push.agg.ticker" -> { //聚合行情

                            }
                            "push.deal" -> { //实时成交,更新当前价格
                                val dealBean = gson.fromJson<PairDeal>(
                                    data.toString(),
                                    object : TypeToken<PairDeal?>() {}.type
                                )
                                if(dealBean != null){
                                   // SocketDataContainer.getCurrentPairDeal(mHandler, dealBean,ConstData.DEPTH_FUTURE_TYPE)
                                    SocketDataContainer.updateFutureCurrentPairDeal(mHandler,dealBean)
                                }
                            }
                            "push.deep" -> { //深度
                                val deepBean = gson.fromJson<DeepBean>(
                                    data.toString(),
                                    object : TypeToken<DeepBean?>() {}.type
                                )
                            }
                            "push.deep.full" -> { //全部深度
                                val allDepth: TradeOrderDepth? = gson.fromJson<TradeOrderDepth?>(
                                    data.toString(),
                                    object : TypeToken<TradeOrderDepth?>() {}.type
                                )
                                Log.d(TAG, "SymbolListener futureCurrentPair = $currentUFuturePair")
                                if (allDepth != null) {
                                    SocketDataContainer.updateQuotationOrderNewDataFiex(
                                        mCcontext,
                                        ConstData.DEPTH_FUTURE_TYPE,
                                        mHandler,
                                        currentUFuturePair,
                                        allDepth,
                                        true
                                    )
                                }
                            }
                            "push.fund.rate" -> {//资金费率
                                val fundRateBean = gson.fromJson<FundRateBean>(
                                    data.toString(),
                                    object : TypeToken<FundRateBean?>() {}.type
                                )
                                if(fundRateBean != null){
                                    SocketDataContainer.updateFundRate(mHandler,fundRateBean)
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    FryingUtil.printError(e)
                }
            }
        }
    }

    /**
     * 合约24小时所有交易对行情
     */
    inner class FutureTickersListener() : SimpleListener() {
        override fun onConnected() {
            Log.d(TAG, "FutureTickersListener---->ßonConnected")
            startListenFutureTickers()
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d(TAG, "FutureTickersListener->onMessage = $message")
            if (message.equals("succeed") || message.equals("pong")) {
                return
            }
            CommonUtil.postHandleTask(mHandler) {
                var data: JSONObject? = null
                try {
                    data = JSONObject(message)
                    if (data.has("channel")) {
                        var channel = data.get("channel");
                        var data = data.get("data");
                        Log.d(TAG, "FutureTickersListener message = $channel")
                        when (channel) {
                            "push.ticker" -> { //行情
                                val tickerBean = gson.fromJson<PairStatusNew>(
                                    data.toString(),
                                    object : TypeToken<PairStatusNew?>() {}.type
                                )
                                if (tickerBean != null) {
                                    SocketDataContainer.updateFuturePairStatusData(
                                        mCcontext,
                                        mHandler,
                                        tickerBean,
                                        false
                                    )
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    FryingUtil.printError(e)
                }
            }
        }
    }

    /**
     * 合约24小时所有交易对K线
     */
    inner class FutureKlineListener() : SimpleListener() {
        override fun onConnected() {
            Log.d(TAG, "FutureKlineListener---->ßonConnected")
            startListenFutureKline()
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d(TAG, "FutureKlineListener->onMessage = $message")
            if (message.equals("succeed")) {
                return
            }
            CommonUtil.postHandleTask(mHandler) {
                var data: JSONObject? = null
                try {
                    data = JSONObject(message)
                    Log.d(TAG, "FutureKlineListener->channel = " + data.getString("channel"))
                    var resultData = data.get("data");
                    val kline = gson.fromJson<Kline>(
                        resultData.toString(),
                        object : TypeToken<Kline?>() {}.type
                    )
                    var klineItem = KLineItem()
                    if (kline != null) {
                        if (kline?.a != null) {
                            klineItem.a = kline?.a?.toDouble()!!
                        }
                        if (kline?.c != null) {
                            klineItem.c = kline?.c?.toDouble()!!
                        }
                        if (kline?.h != null) {
                            klineItem.h = kline?.h?.toDouble()!!
                        }
                        if (kline?.l != null) {
                            klineItem.l = kline?.l?.toDouble()!!
                        }
                        if (kline?.o != null) {
                            klineItem.o = kline?.o?.toDouble()!!
                        }
                        if (kline?.t != null) {
                            klineItem.t = kline?.t?.div(1000)
                        }
                        if (kline.v != null) {
                            klineItem.v = kline?.v?.toDouble()!!
                        }
                        if (kline.s.equals(currentPair?.lowercase())) {
                            SocketDataContainer.addKLineData(
                                currentPair?.lowercase(),
                                mHandler,
                                kLineId,
                                ConstData.DEPTH_FUTURE_TYPE,
                                klineItem
                            )
                        }
                    }
                } catch (e: JSONException) {
                    FryingUtil.printError(e)
                }
            }
        }
    }


    /**
     * 合约标记价格订阅
     */
    inner class FutureMarkPriceListener() : SimpleListener() {
        override fun onConnected() {
            Log.d(TAG, "FutureMarkPriceListener---->ßonConnected")
            startListenMarkPrice()
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d(TAG, "FutureMarkPriceListener->onMessage = $message")
            if (message.equals("succeed") || message.equals("pong")) {
                return
            }
            CommonUtil.postHandleTask(mHandler) {
                var data: JSONObject? = null
                try {
                    data = JSONObject(message)
                    if (data.has("channel")) {
                        var channel = data.get("channel")
                        var data = data.get("data")
                        when (channel) {
                            "push.mark.price" -> { //标记价格
                                val markPriceBean = gson.fromJson<MarkPriceBean>(
                                    data.toString(),
                                    object : TypeToken<MarkPriceBean?>() {}.type
                                )
                                if (markPriceBean != null) {
                                    SocketDataContainer.updateMarkPrice(mHandler, markPriceBean)
                                }
                            }
                        }
                    }
                } catch (e: JSONException) {
                    FryingUtil.printError(e)
                }
            }
        }
    }
}