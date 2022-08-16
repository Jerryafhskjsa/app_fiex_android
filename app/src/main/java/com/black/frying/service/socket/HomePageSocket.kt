package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.util.Log
import com.black.net.websocket.*
import org.json.JSONObject

class HomePageSocket(context: Context,handler: Handler){
    private var homeSocketMgr:WebSocketManager
    private var userSocketMgr:WebSocketManager
    private var socketSetting:WebSocketSetting
    private var homeDataListener:SocketListener = HomeDataListener(homeKeyS)
    private var userSocketListener:SocketListener = UserDataListener(userKeys)

    private var listenerMap:HashMap<String,SocketListener> = HashMap()

    companion object{
        private const val homeKeyS = "homeKeys"
        private const val userKeys = "userKeys"
    }
    init {
        var socketUrl = "ws://112.194.204.105:8081/websocket"
        socketSetting = WebSocketSetting()
        socketSetting.connectUrl = socketUrl
        socketSetting.connectionLostTimeout = 5
        homeSocketMgr = WebSocketHandler.initGeneralWebSocket("wsHome",socketSetting)
        userSocketMgr = WebSocketHandler.initGeneralWebSocket("wsUser",socketSetting)
        addListener(homeSocketMgr, homeKeyS, homeDataListener as SimpleListener)
        addListener(userSocketMgr,userKeys,userSocketListener as SimpleListener)
    }

    fun startConnect(){
        homeSocketMgr.start()
        userSocketMgr.start()
    }

    fun stopConnect(){
        homeSocketMgr.disConnect()
        userSocketMgr.disConnect()
    }

    fun destorySocket(){
        homeSocketMgr.destroy()
        userSocketMgr.destroy()
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


    inner class HomeDataListener(keyListener: String):SimpleListener(){
        private var key = keyListener
        override fun onConnected() {
            Log.d("11111", "$key onConnected")
            val jsonObject1 = JSONObject()
            jsonObject1.put("sub", "subStats")
            homeSocketMgr.send(jsonObject1.toString())
        }
        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d("11111", "$key onMessage = $message")
            when(key){
                homeKeyS ->{
                    Log.d("11111", "22222")
                }
                userKeys ->{
                    Log.d("11111", "33333")
                }
            }
        }
    }

    inner class UserDataListener(keyListener: String):SimpleListener(){
        private var key = keyListener
        override fun onConnected() {
            Log.d("11111", "$key onConnected")
            val jsonObject2 = JSONObject()
            jsonObject2.put("sub", "subSymbol")
            jsonObject2.put("symbol", "BTC_USDT")
            userSocketMgr.send(jsonObject2.toString())
        }

        override fun <T : Any?> onMessage(message: String?, data: T) {
            Log.d("11111", "$key onMessage = $message")
            when(key){
                homeKeyS ->{
                    Log.d("11111", "22222")
                }
                userKeys ->{
                    Log.d("11111", "33333")
                }
            }
        }
    }


}