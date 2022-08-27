package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.util.Log
import com.black.base.model.trade.TradeOrderDepth
import com.black.base.model.trade.TradeOrderOneDepth
import com.black.base.util.FryingUtil
import com.black.base.util.SocketDataContainer
import com.black.base.util.SocketUtil
import com.black.base.util.UrlConfig
import com.black.net.websocket.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
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
    private var subStatusSocketMgr:WebSocketManager
    private var socketSetting:WebSocketSetting
    private var userDataListener:SocketListener = UserDataListener(userKeyS)
    //交易对行情相关
    private var subStatusSocketListener:SocketListener = SubStatusDataListener(subStatusKeys)

    private var listenerMap:HashMap<String,SocketListener> = HashMap()

    companion object{
        private const val userKeyS = "userStatus"
        private const val subStatusKeys = "subStatus"
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
        addListener(userSocketMgr, userKeyS, userDataListener as SimpleListener)
        addListener(subStatusSocketMgr, subStatusKeys,subStatusSocketListener as SimpleListener)
    }


    fun startConnect(){
//        userSocketMgr.start()
        subStatusSocketMgr.start()
    }

    fun stopConnect(){
//        userSocketMgr.disConnect()
        subStatusSocketMgr.disConnect()
    }

    fun destorySocket(){
//        userSocketMgr.destroy()
        subStatusSocketMgr.destroy()
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


    inner class UserDataListener(keyListener: String):SimpleListener(){
        private var key = keyListener
        override fun onConnected() {
            Log.d("11111", "$key onConnected")
            val jsonObject1 = JSONObject()
            jsonObject1.put("sub", "subStats")
            userSocketMgr.send(jsonObject1.toString())
        }
        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d("11111", "$key onMessage = $message")
            var data:JSONObject? = null
            try {
                data = JSONObject(message)
            }catch (e:JSONException){
                FryingUtil.printError(e)
            }
            if(data != null){
                Log.d("11111","resType = "+data.getString("resType"))
            }
        }
    }

    inner class SubStatusDataListener(keyListener: String):SimpleListener(){
        private var key = keyListener
        override fun onConnected() {
            Log.d("11111", "$key onConnected")
            val jsonObject2 = JSONObject()
            jsonObject2.put("sub", "subSymbol")
            jsonObject2.put("symbol", "BTC_USDT")
            subStatusSocketMgr.send(jsonObject2.toString())
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            var data:JSONObject? = null
            try {
                data = JSONObject(message)
                if(data != null){
                    var resType = data.getString("resType")
                    var resultData = data.getString("data")
                    val jsonObject: JsonObject = JsonParser().parse(resultData) as JsonObject
                    when(resType){
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
                    }
                }
            }catch (e:JSONException){
                FryingUtil.printError(e)
            }
        }
    }


}