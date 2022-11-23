package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.util.Log
import com.airbnb.lottie.model.layer.NullLayer
import com.black.base.api.UserApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.clutter.Kline
import com.black.base.model.socket.*
import com.black.base.model.trade.TradeOrderDepth
import com.black.base.model.trade.TradeOrderOneDepth
import com.black.base.model.user.UserBalance
import com.black.base.util.*
import com.black.net.HttpCookieUtil
import com.black.net.HttpRequestResult
import com.black.net.websocket.*
import com.black.util.Callback
import com.black.util.CommonUtil
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class FiexSocketManager(context: Context, handler: Handler) {
    private var tag: String = FiexSocketManager::class.java.simpleName
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
    private var timerStart:Boolean = false
    private var pingTimerTask:TimerTask? = null
    var currentPair: String? = null

    var kLineTimeStep: String? = null
    var kLineTimeStepSecond: Long = 0
    var kLineId: String? = ""


    private lateinit var socketSetting: WebSocketSetting

    //用户数据相关
    private var userDataListener: SocketListener? = UserDataListener()

    //所有币种行情相关
    private var tickerDataListener: SocketListener? = TickerStatusListener()

    //交易对行情相关
    private var subStatusSocketListener: SocketListener? = SubStatusDataListener()

    //交易对k线相关
    private var pairKlineSocketListener: SocketListener? = PairKlineListener()


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
        if(pingTimerTask == null){
            pingTimerTask = object :TimerTask(){
                override fun run() {
                    sendPing()
                }
            }
        }
        if(!timerStart){
            pingTimer?.schedule(pingTimerTask, Date(), 5000)
            timerStart = true
        }
    }

    private fun endPingTimer(){
        if(pingTimer != null){
            pingTimer?.cancel()
            pingTimer = null
            pingTimerTask?.cancel()
            pingTimerTask = null
            timerStart = false
        }
    }

    private fun initSocketManager(context: Context?) {
        var socketUrl = UrlConfig.getSocketHostFiex(context!!)
        socketSetting = WebSocketSetting()
        socketSetting.connectUrl = socketUrl
        socketSetting.connectionLostTimeout = 60//心跳间隔时间
        socketSetting.setReconnectWithNetworkChanged(true)//设置网络状态发生改变自动重连
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_USER, socketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_SUBSTATUS, socketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_PAIR_KLINE, socketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_TICKETS, socketSetting)
    }


    fun startConnect(socketName: String?) {
        WebSocketHandler.getWebSocket(socketName)?.start()
    }

    fun startConnectAll() {
        var socketMap = WebSocketHandler.getAllWebSocket()
        addListenerAll()
        socketMap.forEach {
            Log.d(tag,"start all socketMap,key = "+it.key)
            Log.d(tag,"start all socketMap,state = "+it.value.socketState)
            Log.d(tag,"start all socketMap,isListenerEmpty = "+it.value.isListenerEmpty)
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
            Log.d(tag,"stop all socketMap,key = "+it.key)
            Log.d(tag,"stop all socketMap,state = "+it.value.socketState)
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
        }
    }

    fun removeListenerAll() {
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
            }
            it.value.removeListener(listener)
        }
        userDataListener = null
        subStatusSocketListener = null
        tickerDataListener = null
        pairKlineSocketListener = null
    }

    fun addListenerAll() {
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
            }
            it.value.addListener(listener)
        }
    }

    fun sendPing() {
        Log.d(tag, "sendPing")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("ping", "ping")
            var userSocket = WebSocketHandler.getWebSocket(SocketUtil.WS_USER)
            var subStatusSocket = WebSocketHandler.getWebSocket(SocketUtil.WS_SUBSTATUS)
            var ticketSocket = WebSocketHandler.getWebSocket(SocketUtil.WS_TICKETS)
            var pairKlineSocket = WebSocketHandler.getWebSocket(SocketUtil.WS_PAIR_KLINE)
            Log.d(tag, "userSocket state =,"+userSocket.socketState+"listener is empty= "+userSocket.isListenerEmpty)
            Log.d(tag, "subStatusSocket state =,"+subStatusSocket.socketState+"listener is empty= "+subStatusSocket.isListenerEmpty)
            Log.d(tag, "ticketSocket state =,"+ticketSocket.socketState+"listener is empty= "+ticketSocket.isListenerEmpty)
            Log.d(tag, "pairKlineSocket state =,"+pairKlineSocket.socketState+"listener is empty= "+pairKlineSocket.isListenerEmpty)
            if(userSocket.isConnect){
                userSocket?.send(jsonObject.toString())
            }
            if(subStatusSocket.isConnect){
                subStatusSocket?.send(jsonObject.toString())
            }
            if(ticketSocket.isConnect){
                ticketSocket?.send(jsonObject.toString())
            }
            if(pairKlineSocket.isConnect){
                pairKlineSocket?.send(jsonObject.toString())
            }
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    fun startListenKLine() {
        currentPair = SocketUtil.getCurrentPair(mCcontext!!)
        Log.d(tag, "startListenKline = $currentPair")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subKline")
            jsonObject.put("symbol", currentPair)
            jsonObject.put("type", kLineTimeStep)
            WebSocketHandler.getWebSocket(SocketUtil.WS_PAIR_KLINE).send(jsonObject.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    fun startListenPair(pair: String?) {
        Log.d(tag, "startListenPair = $pair")
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
        Log.d(tag, "startListenUser")
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
        Log.d(tag, "startListenTickers")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subStats")
            WebSocketHandler.getWebSocket(SocketUtil.WS_TICKETS).send(jsonObject.toString())
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    /**
     * 用户相关
     */
    inner class UserDataListener() : SimpleListener() {
        override fun onDisconnect() {
            Log.d(tag, "UserDataListener onDisconnect")
        }
        override fun onConnected() {
            Log.d(tag, "UserDataListener onConnected")
            startListenUser()
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d(tag, "UserDataListener message = $message")
            if(message.equals("succeed")){
                return
            }
            if (message.equals("invalid_ws_token")) {
                mCcontext?.let { getWsToken(it) }
                return
            }
            var data: JSONObject? = null
            try {
                data = JSONObject(message)
            } catch (e: JSONException) {
                FryingUtil.printError(e)
            }
            if (data != null) {
                var resType = data.getString("resType")
                var resultData = data.getString("data")
                Log.d(tag, "UserDataListener resType = $resType")
                Log.d(tag, "UserDataListener resultData = $resultData")
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
     * 所有交易对行情相关
     */
    inner class TickerStatusListener() : SimpleListener() {
        override fun onDisconnect() {
            Log.d(tag, "tickerStatus onDisconnect")
        }
        override fun onConnected() {
            Log.d(tag, "tickerStatus onConnected")
            startListenTickers()
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
//            Log.d(tag, "tickerStatus->onMessage = $message")
            if(message.equals("succeed")){
                return
            }
            var data: JSONObject? = null
            try {
                data = JSONObject(message)
            } catch (e: JSONException) {
                FryingUtil.printError(e)
            }
            if (data != null) {
                var resType = data.getString("resType")
//                Log.d(tag, "tickerStatus->resType = $resType")
//                Log.d(tag, "tickerStatus->data = " + data.getString("data"))
                when (resType) {
                    "qStats" -> {
                        var result = data.getString("data")
                        var pairQuo: PairStatusNew? = null
                        try {
                            val jsonObject: JsonObject = JsonParser().parse(result) as JsonObject
//                            Log.d(tag, "tickerStatus->jsonObject = $jsonObject")
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
        }
    }

    /**
     * k线相关
     */
    inner class PairKlineListener() : SimpleListener() {
        override fun onDisconnect() {
            Log.d(tag, "PairKline onDisconnect")
        }
        override fun onConnected() {
            Log.d(tag, "PairKline onConnected")
            startListenKLine()
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d(tag, "PairKline->onMessage = $message")
            if(message.equals("succeed")){
                return
            }
            CommonUtil.postHandleTask(mHandler) {
                var data: JSONObject? = null
                try {
                    data = JSONObject(message)
                } catch (e: JSONException) {
                    FryingUtil.printError(e)
                }
                if (data != null) {
                    Log.d(tag, "PairKline->resType = " + data.getString("resType"))
                    var resultData = data.getString("data")
                    val kline = gson.fromJson<Kline>(
                        resultData.toString(),
                        object : TypeToken<Kline?>() {}.type
                    )
                    var klineItem = KLineItem()
                    if(kline != null){
                        if(kline?.a != null){
                            klineItem.a = kline?.a?.toDouble()!!
                        }
                        if(kline?.c != null){
                            klineItem.c = kline?.c?.toDouble()!!
                        }
                        if(kline?.h != null){
                            klineItem.h = kline?.h?.toDouble()!!
                        }
                        if(kline?.l != null){
                            klineItem.l = kline?.l?.toDouble()!!
                        }
                        if(kline?.o != null){
                            klineItem.o = kline?.o?.toDouble()!!
                        }
                        if(kline?.t != null){
                            klineItem.t = kline?.t?.div(1000)
                        }
                        if(kline?.v != null){
                            klineItem.v = kline?.v?.toDouble()!!
                        }
                        if (kline?.s.equals(currentPair)) {
                            SocketDataContainer.addKLineData(currentPair, mHandler, kLineId, klineItem)
                        }
                    }
                }
            }
        }
    }

    /**
     * 交易对相关
     */
    inner class SubStatusDataListener() : SimpleListener() {
        override fun onDisconnect() {
            Log.d(tag, "SubStatusDataListener onDisconnect")
        }
        override fun onConnected() {
            Log.d(tag, "SubStatusDataListener onConnected")
            currentPair = SocketUtil.getCurrentPair(mCcontext!!)
            startListenPair(currentPair)
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            if(message.equals("succeed")){
                return
            }
            var data: JSONObject? = null
            try {
                data = JSONObject(message)
                if (data != null) {
                    var resType = data.getString("resType")
//                    Log.d(tag, "subStatus->resType = $resType")
                    var resultData = data.getString("data")
//                    Log.d(tag, "subStatus->data = $resultData")
                    val jsonObject: JsonObject = JsonParser().parse(resultData) as JsonObject
                    when (resType) {
                        //50挡深度
                        "qAllDepth" -> {
                            val allDepth: TradeOrderDepth? = gson.fromJson<TradeOrderDepth?>(
                                jsonObject.toString(),
                                object : TypeToken<TradeOrderDepth?>() {}.type
                            )
                            if(allDepth != null){
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
                            val oneDepth: TradeOrderOneDepth? = gson.fromJson<TradeOrderOneDepth?>(
                                jsonObject.toString(),
                                object : TypeToken<TradeOrderOneDepth??>() {}.type
                            )
                            if(oneDepth != null){
                                var allDepthData = TradeOrderDepth()
                                var direction = oneDepth?.m
                                var desArray:Array<String?>? = arrayOf(oneDepth?.p, oneDepth?.q)
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
//                            Log.d(tag, "qDeal->data = $resultData")
                            val pairDeal: PairDeal? = gson.fromJson<PairDeal?>(
                                jsonObject.toString(),
                                object : TypeToken<PairDeal??>() {}.type
                            )
                            if (pairDeal != null) {
                                SocketDataContainer.getCurrentPairDeal(mHandler, pairDeal)
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
                }
            } catch (e: JSONException) {
                FryingUtil.printError(e)
            }
        }
    }
}