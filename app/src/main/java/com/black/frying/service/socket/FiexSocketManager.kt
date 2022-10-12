package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.util.Log
import com.black.base.model.socket.KLineItem
import com.black.base.model.socket.PairDeal
import com.black.base.model.socket.PairQuotation
import com.black.base.model.trade.TradeOrderDepth
import com.black.base.model.trade.TradeOrderOneDepth
import com.black.base.util.*
import com.black.net.HttpCookieUtil
import com.black.net.websocket.*
import com.black.util.CommonUtil
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject

class FiexSocketManager(context: Context, handler: Handler){
    private var tag: String = FiexSocketManager::class.java.simpleName
    private var gson: Gson = Gson()
        get() {
            if (field == null) {
                field = Gson()
            }
            return field
        }
    private var mCcontext:Context? = null
    private var mHandler:Handler? = null
    var currentPair:String? = null

    var kLineTimeStep: String? = null
    var kLineTimeStepSecond: Long = 0
    var kLineId: String? = null

//    private var userSocketMgr:WebSocketManager
//    private var tickerSocketMgr:WebSocketManager
//    private var subStatusSocketMgr:WebSocketManager
//    private var pairKlineMgr:WebSocketManager

    private lateinit var socketSetting:WebSocketSetting
    //用户数据相关
    private var userDataListener:SocketListener = UserDataListener()
    //所有币种行情相关
    private var tickerDataListener:SocketListener = TickerStatusListener()
    //交易对行情相关
    private var subStatusSocketListener:SocketListener = SubStatusDataListener()
    //交易对k线相关
    private var pairKlineSocketListener:SocketListener = PairKlineListener()




    init {
        mCcontext = context
        mHandler = handler
        currentPair = SocketUtil.getCurrentPair(mCcontext!!)
        initSocketManager(mCcontext)
        addListenerAll()
        startConnectAll()
//        startConnect(SocketUtil.WS_SUBSTATUS)
    }

    private fun initSocketManager(context:Context?){
        var socketUrl = UrlConfig.getSocketHostFiex(context!!)
        socketSetting = WebSocketSetting()
        socketSetting.connectUrl = socketUrl
        socketSetting.connectionLostTimeout = 5//心跳间隔时间
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_USER,socketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_SUBSTATUS,socketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_PAIR_KLINE,socketSetting)
        WebSocketHandler.initGeneralWebSocket(SocketUtil.WS_TICKETS,socketSetting)
    }


    fun startConnect(socketName:String?){
        WebSocketHandler.getWebSocket(socketName)?.start()
    }

    fun startConnectAll(){
        var socketMap = WebSocketHandler.getAllWebSocket()
        addListenerAll()
        socketMap.forEach{
            it.value.start()
        }
    }

    fun stopConnect(socketName:String?){
        WebSocketHandler.getWebSocket(socketName)?.disConnect()
    }
    fun stopConnectAll(){
        removeListenerAll()
        var socketMap = WebSocketHandler.getAllWebSocket()
        socketMap.forEach{
            it.value.disConnect()
        }
    }

    fun destorySocketAll(){
        var socketMap = WebSocketHandler.getAllWebSocket()
        socketMap.forEach{
            it.value.destroy()
        }
    }

    fun addListener(socketKey:String?){
        var socketMar = WebSocketHandler.getWebSocket(socketKey)
        if(socketMar != null){
            var listener:SocketListener? = null
            when(socketKey){
                SocketUtil.WS_USER ->{
                    listener = userDataListener
                }
                SocketUtil.WS_SUBSTATUS ->{
                    listener = subStatusSocketListener
                }
                SocketUtil.WS_TICKETS ->{
                    listener = tickerDataListener
                }
                SocketUtil.WS_PAIR_KLINE ->{
                    listener = pairKlineSocketListener
                }
            }
            socketMar.addListener(listener)
        }
    }

    fun removeListener(socketKey: String?) {
        var listener: SocketListener? = null
        when (socketKey) {
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
        WebSocketHandler.getWebSocket(socketKey)?.removeListener(listener)
    }
    fun removeListenerAll(){
        var socketMgrList = WebSocketHandler.getAllWebSocket()
        var listener: SocketListener? = null
            socketMgrList.forEach{
                when(it.key){
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
    }

    fun addListenerAll(){
        var socketMgrList = WebSocketHandler.getAllWebSocket()
        var listener: SocketListener? = null
        socketMgrList.forEach{
            when(it.key){
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
            it.value.addListener(listener)
        }
    }

    fun startListenKLine(){
        currentPair = SocketUtil.getCurrentPair(mCcontext!!)
        Log.d(tag, "startListenKline = $currentPair")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subKline")
            jsonObject.put("symbol", currentPair)
            jsonObject.put("type", kLineTimeStep)
            WebSocketHandler.getWebSocket(SocketUtil.WS_PAIR_KLINE).send(jsonObject.toString())
        }catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    fun startListenPair(pair:String?){
        Log.d(tag, "startListenPair = $pair")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subSymbol")
            jsonObject.put("symbol", pair)
            WebSocketHandler.getWebSocket(SocketUtil.WS_SUBSTATUS)?.send(jsonObject.toString())
        }catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }

    fun startListenUser(){
        Log.d(tag, "startListenUser")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subUser")
            jsonObject.put("token",HttpCookieUtil.getWsToken(mCcontext))
            WebSocketHandler.getWebSocket(SocketUtil.WS_USER)?.send(jsonObject.toString())
        }catch (e:Exception){
            FryingUtil.printError(e)
        }
    }

    fun startListenTickers(){
        Log.d(tag, "startListenTickers")
        try {
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subStats")
            WebSocketHandler.getWebSocket(SocketUtil.WS_TICKETS).send(jsonObject.toString())
        }catch (e:Exception){
            FryingUtil.printError(e)
        }
    }

    /**
     * 用户相关
     */
    inner class UserDataListener():SimpleListener(){
        override fun onConnected() {
            Log.d(tag, "UserDataListener onConnected")
            startListenUser()
        }
        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d(tag, "UserDataListener onMessage = $message")
            if(message.equals("succeed") || message.equals("invalid_ws_token")){
                return
            }
            var data:JSONObject? = null
            try {
                data = JSONObject(message)
                if(data != null){
                    var resType = data.getString("resType")
                    var resultData = data.getString("data")
                    when(resType){
                        //余额变更
                        "uBalance" ->{

                        }
                        //用户成交
                        "uTrade" ->{

                        }
                        //用户订单
                        "uOrder" ->{

                        }

                    }
                }
            }catch (e:JSONException){
                FryingUtil.printError(e)
            }
            if(data != null){
                Log.d(tag,"userData->resType = "+data.getString("resType"))
            }
        }
    }

    /**
     * 行情相关
     */
    inner class TickerStatusListener():SimpleListener(){
        override fun onConnected() {
            Log.d(tag, "tickerStatus onConnected")
            startListenTickers()
        }
        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d(tag, "tickerStatus->onMessage = $message")
            if(message.equals("succeed")){
                return
            }
            var data:JSONObject? = null
            try {
                data = JSONObject(message)
            }catch (e:JSONException){
                FryingUtil.printError(e)
            }
            if(data != null){
                Log.d(tag,"tickerStatus->resType = "+data.getString("resType"))
                Log.d(tag,"tickerStatus->data = "+data.getString("data"))
                var data = data.getString("data")
                val pairQuo:PairQuotation? = gson.fromJson<PairQuotation?>(data.toString(), object : TypeToken<PairQuotation?>() {}.type)
                if(pairQuo != null){
                    SocketDataContainer.getCurrentPairQuotation(mHandler,pairQuo)
                }
            }
        }
    }

    /**
     * k线相关
     */
    inner class PairKlineListener():SimpleListener(){
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
                var data:JSONObject? = null
                try {
                    data = JSONObject(message)
                }catch (e:JSONException){
                    FryingUtil.printError(e)
                }
                if(data != null){
                    Log.d(tag,"PairKline->resType = "+data.getString("resType"))
                    var resultData = data.getString("data")
                    val newData = gson.fromJson<KLineItem>(resultData.toString(), object : TypeToken<KLineItem?>() {}.type)
                    SocketDataContainer.addKLineData(currentPair, mHandler, kLineId, newData)
                }
            }
        }
    }

    /**
     * 交易对相关
     */
    inner class SubStatusDataListener():SimpleListener(){
        override fun onConnected() {
            Log.d(tag, "SubStatusDataListener onConnected")
            currentPair = SocketUtil.getCurrentPair(mCcontext!!)
            startListenPair(currentPair)
        }
        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d(tag, "subStatus->onMessage = $message")
            if(message.equals("succeed")){
                return
            }
            var data:JSONObject? = null
            try {
                data = JSONObject(message)
                if(data != null){
                    var resType = data.getString("resType")
                    var resultData = data.getString("data")
                    val jsonObject: JsonObject = JsonParser().parse(resultData) as JsonObject
                    when(resType){
                        //50挡深度
                        "qAllDepth" -> {
                            val allDepth:TradeOrderDepth? = gson.fromJson<TradeOrderDepth?>(jsonObject.toString(), object : TypeToken<TradeOrderDepth??>() {}.type)
                            SocketDataContainer.updateQuotationOrderNewDataFiex(mCcontext,mHandler,currentPair,allDepth,true)
                        }
                        "qDepth" ->{
                            val oneDepth:TradeOrderOneDepth? = gson.fromJson<TradeOrderOneDepth?>(jsonObject.toString(), object : TypeToken<TradeOrderOneDepth??>() {}.type)
                            var allDepthData = TradeOrderDepth()
                            var direction = oneDepth?.m
                            var desArray = arrayOf(oneDepth?.p?.toDouble(),oneDepth?.q?.toDouble())
                            if(direction.equals("1")){//BID
                                var bidArray = arrayOf(desArray)
                                allDepthData?.b = bidArray
                            }
                            if(direction.equals("2")){//ASK
                                var askArray = arrayOf(desArray)
                                allDepthData?.a = askArray
                            }
                            allDepthData.s = oneDepth?.s
//                            SocketDataContainer.updateQuotationOrderNewDataFiex(mCcontext,mHandler,currentPair,allDepthData,false)
                        }
                        //当前交易对成交数据
                        "qDeal" ->{
                            val pairDeal:PairDeal? = gson.fromJson<PairDeal?>(jsonObject.toString(), object : TypeToken<PairDeal??>() {}.type)
                            if(pairDeal != null){
                                SocketDataContainer.getCurrentPairDeal(mHandler,pairDeal)
                            }
                        }
                        //当前交易对24小时行情
                        "qStatus" ->{
                            val pairQuo:PairQuotation? = gson.fromJson<PairQuotation?>(jsonObject.toString(), object : TypeToken<PairQuotation?>() {}.type)
                            if(pairQuo != null){
                                SocketDataContainer.getCurrentPairQuotation(mHandler,pairQuo)
                            }
                        }
                        //当前交易对k线
                        "qKLine" ->{

                        }
                    }
                }
            }catch (e:JSONException){
                FryingUtil.printError(e)
            }
        }
    }
}