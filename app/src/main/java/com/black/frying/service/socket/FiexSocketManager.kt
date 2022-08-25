package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.util.Log
import com.black.base.util.FryingUtil
import com.black.base.util.UrlConfig
import com.black.net.websocket.*
import org.json.JSONException
import org.json.JSONObject

class FiexSocketManager(context: Context, handler: Handler){
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
        var socketUrl = UrlConfig.getSocketHostFiex(context)+"/websocket"
        Log.d("11111", "socketUrl = $socketUrl")
        socketSetting = WebSocketSetting()
        socketSetting.connectUrl = socketUrl
        socketSetting.connectionLostTimeout = 5
        userSocketMgr = WebSocketHandler.initGeneralWebSocket("wsUser",socketSetting)
        subStatusSocketMgr = WebSocketHandler.initGeneralWebSocket("wsSubStatus",socketSetting)
        addListener(userSocketMgr, userKeyS, userDataListener as SimpleListener)
        addListener(subStatusSocketMgr, subStatusKeys,subStatusSocketListener as SimpleListener)
    }

    fun startConnect(){
        userSocketMgr.start()
        subStatusSocketMgr.start()
    }

    fun stopConnect(){
        userSocketMgr.disConnect()
        subStatusSocketMgr.disConnect()
    }

    fun destorySocket(){
        userSocketMgr.destroy()
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


}