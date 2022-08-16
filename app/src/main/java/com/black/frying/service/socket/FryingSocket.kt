package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.util.Log
import com.black.net.websocket.SimpleListener
import com.black.net.websocket.WebSocketManager
import com.google.gson.Gson
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import java.util.*
import kotlin.collections.HashMap

abstract class FryingSocket(protected val context: Context, protected val handler: Handler) {
    private val TAG: String? = "FryingSocket"
    protected val gson = Gson()



    protected val socket: Socket
    protected var emitterListenerMap = HashMap<String, Emitter.Listener>() //消息监听

    protected var onConnectListener = Emitter.Listener {
        Log.e(TAG, "onConnectListener ==============================================\nonConnectListener obj：")
    }

    protected var onDisconnectListener = Emitter.Listener { args: Array<Any?> ->
        Log.e(TAG, "onDisconnectListener ==============================================\nonuConnectListener obj：")
        for (`object` in args) {
//            Log.e(TAG, "onDisconnectListener ==============================================\n obj：" + `object`);
        }
    }
    protected var onConnectErrorListener = Emitter.Listener { args: Array<Any?> ->
        Log.e(TAG, "onConnectErrorListener ==============================================\n obj："+args.contentToString())
        for (`object` in args) {
            //Log.e(TAG, "onConnectErrorListener ==============================================\n obj：" + object);
        }
    }

    protected val socketOptions: IO.Options
        get() {
            val options = IO.Options()
            options.reconnectionDelay = 5000
//            options.transports = arrayOf("websocket")
            options.transports = arrayOf(WebSocket.NAME)
            options.path = "/websocket"
            return options
        }

    init {
        socket = initSocket()
    }

    @Throws(Exception::class)
    protected abstract fun initSocket(): Socket


    open protected fun getTag(): String {
        return ""
    }

    open fun start() {
        if (socket != null) {
            socket.on(Socket.EVENT_CONNECT, onConnectListener)
            socket.on(Socket.EVENT_DISCONNECT, onDisconnectListener)
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectErrorListener)
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectErrorListener)
            socket.connect()
        }
    }

    open fun stop() {
        if (socket != null) {
            socket.disconnect()
            socket.off(Socket.EVENT_CONNECT, onConnectListener)
            socket.off(Socket.EVENT_DISCONNECT, onDisconnectListener)
            socket.off(Socket.EVENT_CONNECT_ERROR, onConnectErrorListener)
            socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectErrorListener)
            for (event in emitterListenerMap.keys) {
                socket.off(event, emitterListenerMap[event])
            }
        }
    }

    fun startListenSocket(key: String?) {
        if (socket == null) {
            return
        }
        val listener = emitterListenerMap[key]
        if (listener != null) {
            socket.on(key, listener)
        }
    }

    fun finishListenSocket(key: String?) {
        if (socket == null) {
            return
        }
        val listener = emitterListenerMap[key]
        if (listener != null) {
            socket.off(key, listener)
        }
    }
}