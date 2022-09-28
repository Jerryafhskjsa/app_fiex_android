package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.util.Log
import com.black.base.model.socket.PairDeal
import com.black.base.model.socket.PairQuotation
import com.black.base.model.trade.TradeOrderDepth
import com.black.base.model.trade.TradeOrderOneDepth
import com.black.base.util.*
import com.black.net.HttpCookieUtil
import com.black.net.websocket.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.channels.ticker
import org.json.JSONException
import org.json.JSONObject

class FiexSocketManager(context: Context, handler: Handler){

    private var gson: Gson = Gson()
        get() {
            if (field == null) {
                field = Gson()
            }
            return field
        }
    private var mCcontext:Context? = null
    private var mHandler:Handler? = null
    private var currentPair:String? = null
    private var userSocketMgr:WebSocketManager
    private var tickerSocketMgr:WebSocketManager
    private var subStatusSocketMgr:WebSocketManager
    private var pairKlineMgr:WebSocketManager

    private var socketSetting:WebSocketSetting
    //用户数据相关
    private var userDataListener:SocketListener = UserDataListener(userKeyS)
    //所有币种行情相关
    private var tickerDataListener:SocketListener = TickerStatusListener(tickerKeyS)
    //交易对行情相关
    private var subStatusSocketListener:SocketListener = SubStatusDataListener(subStatusKeys)
    //交易对k线相关
    private var pariKlineSocketListener:SocketListener = PairKlineListener(pairKlineKeys)

    private var listenerMap:HashMap<String,SocketListener> = HashMap()

    companion object{
        private const val userKeyS = "userStatus"
        private const val tickerKeyS = "tickerStatus"
        private const val subStatusKeys = "subStatus"
        private const val pairKlineKeys = "pairKline"
    }
    init {
        mCcontext = context
        mHandler = handler
        currentPair = SocketUtil.getCurrentPair(mCcontext!!)
        var socketUrl = UrlConfig.getSocketHostFiex(context)+"/websocket"
        socketSetting = WebSocketSetting()
        socketSetting.connectUrl = socketUrl
        socketSetting.connectionLostTimeout = 5
        userSocketMgr = WebSocketHandler.initGeneralWebSocket("wsUser",socketSetting)
        subStatusSocketMgr = WebSocketHandler.initGeneralWebSocket("wsSubStatus",socketSetting)
        pairKlineMgr = WebSocketHandler.initGeneralWebSocket("wsPairKline",socketSetting)
        tickerSocketMgr = WebSocketHandler.initGeneralWebSocket("wsTickets",socketSetting)
        addListener(userSocketMgr, userKeyS, userDataListener as SimpleListener)
        addListener(subStatusSocketMgr, subStatusKeys,subStatusSocketListener as SimpleListener)
//        addListener(pairKlineMgr, pairKlineKeys,pariKlineSocketListener as SimpleListener)
        addListener(tickerSocketMgr, tickerKeyS,tickerDataListener as SimpleListener)
    }


    fun startConnect(){
        userSocketMgr.start()
        subStatusSocketMgr.start()
//        pairKlineMgr.start()
        tickerSocketMgr.start()
    }

    fun stopConnect(){
        userSocketMgr.disConnect()
        subStatusSocketMgr.disConnect()
        pairKlineMgr.disConnect()
        tickerSocketMgr.disConnect()
    }

    fun destorySocket(){
        userSocketMgr.destroy()
        subStatusSocketMgr.destroy()
        pairKlineMgr.destroy()
        tickerSocketMgr.destroy()
    }

    private fun addListener(socketMgr:WebSocketManager,keyListener:String, listener: SimpleListener){
        if(listenerMap.containsKey(keyListener)){
            return
        }
        socketMgr.addListener(listener)
        listenerMap[keyListener] = listener
    }

    fun removeListener(socketMgr:WebSocketManager,keyListener: String){
        var listener: SocketListener? = listenerMap[keyListener]
        socketMgr.removeListener(listener)
    }

    /**
     * 用户相关
     */
    inner class UserDataListener(keyListener: String):SimpleListener(){
        private var key = keyListener
        override fun onConnected() {
            Log.d("userData", "$key onConnected")
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subUser")
            jsonObject.put("token",HttpCookieUtil.getWsToken(mCcontext))
            userSocketMgr.send(jsonObject.toString())
        }
        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d("userData", "$key onMessage = $message")
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
                Log.d("userData","resType = "+data.getString("resType"))
            }
        }
    }

    /**
     * 行情相关
     */
    inner class TickerStatusListener(keyListener: String):SimpleListener(){
        private var key = keyListener
        override fun onConnected() {
            Log.d("tickerData", "$key onConnected")
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subStats")
            tickerSocketMgr.send(jsonObject.toString())
        }
        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d("tickerData", "$key onMessage = $message")
            var data:JSONObject? = null
            try {
                data = JSONObject(message)
            }catch (e:JSONException){
                FryingUtil.printError(e)
            }
            if(data != null){
                Log.d("tickerData","resType = "+data.getString("resType"))
            }
        }
    }

    /**
     * k线相关
     */
    inner class PairKlineListener(keyListener: String):SimpleListener(){
        private var key = keyListener
        override fun onConnected() {
            Log.d("kline", "$key onConnected")
            val jsonObject = JSONObject()
            jsonObject.put("sub", "subKline")
            jsonObject.put("symbol", currentPair)
            jsonObject.put("type", "1m")
            pairKlineMgr.send(jsonObject.toString())
        }
        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d("kLine", "$key onMessage = $message")
//            var data:JSONObject? = null
//            try {
//                data = JSONObject(message)
//            }catch (e:JSONException){
//                FryingUtil.printError(e)
//            }
//            if(data != null){
//                Log.d("kLine","resType = "+data.getString("resType"))
//            }
        }
    }

    /**
     * 交易对相关
     */
    inner class SubStatusDataListener(keyListener: String):SimpleListener(){
        private var key = keyListener
        override fun onConnected() {
            currentPair?.let { setPair() }
        }
        private fun setPair() {
            currentPair
            Log.d("subStatus", "$key onConnected")
            val jsonObject2 = JSONObject()
            jsonObject2.put("sub", "subSymbol")
            jsonObject2.put("symbol", currentPair)
            subStatusSocketMgr.send(jsonObject2.toString())
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d("subStatus", "$key onMessage = $message")
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
                            val pairQuo:PairQuotation? = gson.fromJson<PairQuotation?>(jsonObject.toString(), object : TypeToken<PairQuotation??>() {}.type)
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