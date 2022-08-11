package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.SocketDataContainer
import com.black.base.util.UrlConfig
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import io.socket.engineio.client.transports.WebSocket
import org.json.JSONException
import org.json.JSONObject

class UserSocket(context: Context, handler: Handler) : FryingSocket(context, handler) {
    companion object {
        private const val TAG = "UserSocket"
        const val USER_COIN_BALANCE = "userCoinBalance"
        const val USER_FINISH_ORDER = "userFinishOrder"
        const val USER_LEVER_BALANCE = "userCoinBalanceIsolated"
        const val LEVER_DETAIL = "userCoinBalanceIsolatedAssets"
    }

    var leverPair: String? = null

    private val onUserConnectListener = Emitter.Listener {
        Log.d(TAG, "onUserConnectListener");
        for (event in emitterListenerMap.keys) {
            socket.on(event, emitterListenerMap[event])
        }
        //            startListenUserConnect();
        startListenUserNewConnect()
    }
    //余额变更
    private val onUserInfoNewListener = Emitter.Listener { args ->
        //通知当前委托订单更新
        //Log.e(TAG, "onUserInfoNewListener ==============================================\n obj：")
        SocketDataContainer.onUserInfoChanged()
        //            sendDataChangedBroadcast(ConstData.USER_INFO_CHANGED);
        for (`object` in args) {
            //Log.e(TAG, "onUserInfoNewListener ==============================================\n obj：" + `object`);
        }
    }
    //杠杆余额变更
    private val onUserLeverBalanceListener = Emitter.Listener { args ->
        //Log.e(TAG, "onUserLeverBalanceListener ==============================================\n obj：")
        //通知当前委托订单更新
        SocketDataContainer.onUserLeverChanged()
        //强行更新当前监听交易对对应杠杆详情
        if (leverPair != null) {
            //SocketDataContainer.updateWalletLeverDetail(context, handler, leverPair);
        }
        for (`object` in args) {
            //Log.e(TAG, "onUserLeverBalanceListener ==============================================\n obj：" + `object`);
        }
    }
    //用户挂单信息变化
    private val onUserOrderListener = Emitter.Listener { args ->
        //Log.e(TAG, "onUserOrderListener ==============================================\n obj：")
        //通知当前委托订单更新
        SocketDataContainer.onUserOrderChanged()
        //            sendDataChangedBroadcast(ConstData.USER_INFO_CHANGED);
        for (`object` in args) {
            //Log.e(TAG, "onUserOrderListener ==============================================\n obj：" + `object`);
        }
    }
    //用户杠杆资产详情变化
    private val onUserLeverDetailListener = Emitter.Listener { args ->
        for (`object` in args) {
            //Log.e(TAG, "onUserLeverDetailListener ==============================================\n obj：" + `object`);
            var data: JSONObject? = null
            if (`object` is String) {
                try {
                    data = JSONObject(`object`.toString())
                } catch (e: JSONException) {
                    FryingUtil.printError(e)
                }
            } else if (`object` is JSONObject) {
                data = `object`
            }
            if (data != null) {
                SocketDataContainer.onWalletLeverDetailUpdate(data, handler)
            }
        }
    }

    init {
        emitterListenerMap[USER_COIN_BALANCE] = onUserInfoNewListener
        emitterListenerMap[USER_FINISH_ORDER] = onUserOrderListener
        emitterListenerMap[USER_LEVER_BALANCE] = onUserLeverBalanceListener
        emitterListenerMap[LEVER_DETAIL] = onUserLeverDetailListener
    }

    override fun getTag(): String {
        return TAG
    }

    @Throws(Exception::class)
    override fun initSocket(): Socket {
        Log.d(TAG,"initSocket")
//        return IO.socket(UrlConfig.getSocketHost(context) + "/user", socketOptions)
        return IO.socket(UrlConfig.getSocketHost(context), socketOptions)
    }


    override fun start() {
        if (socket != null) {
            socket.on(Socket.EVENT_CONNECT, onUserConnectListener)
            socket.on(Socket.EVENT_DISCONNECT, onDisconnectListener)
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectErrorListener)
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectErrorListener)
            socket.connect()
        }
    }

    override fun stop() {
        if (socket != null) {
            socket.disconnect()
            socket.off(Socket.EVENT_CONNECT, onUserConnectListener)
            socket.off(Socket.EVENT_DISCONNECT, onDisconnectListener)
            socket.off(Socket.EVENT_CONNECT_ERROR, onConnectErrorListener)
            socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectErrorListener)
            for (event in emitterListenerMap.keys) {
                socket.off(event, emitterListenerMap[event])
            }
        }
    }

    //请求监听用户断开
    fun startListenUserDisconnect() {
        for (event in emitterListenerMap.keys) {
            socket.off(event, emitterListenerMap[event])
        }
    }

    //请求监听用户连接 new
    fun startListenUserNewConnect() {
        Log.d(TAG,"startListenUserNewConnect")
        val token = CookieUtil.getToken(context)
        Log.d(TAG,"token = "+token)
        ////Log.e(TAG, "startListenUserNewConnect ==========================currentPair：" + currentPair + ",:token:" + token);
        if (!TextUtils.isEmpty(token)) {
            try {
                val jsonObject = JSONObject()
                jsonObject.put("token", token)
                socket.emit("userConnect", jsonObject)
            } catch (e: Exception) {
                FryingUtil.printError(e)
            }
        }
    }

    //请求监听用户杠杆详情
    fun startListenLeverDetail(pair: String?) {
        if (TextUtils.equals(leverPair, pair)) {
            return
        }
        leverPair = pair
        val token = CookieUtil.getToken(context)
        ////Log.e(TAG, "startListenUserNewConnect ==========================currentPair：" + currentPair + ",:token:" + token);
        if (!TextUtils.isEmpty(token) && !TextUtils.isEmpty(pair)) {
            try {
                val jsonObject = JSONObject()
                jsonObject.put("pair", pair)
                jsonObject.put("token", token)
                socket.emit("userConnectLeverPair", jsonObject)
                openListenLeverDetail()
            } catch (e: Exception) {
                FryingUtil.printError(e)
            }
        }
    }

    fun openListenLeverDetail() {
        startListenSocket(LEVER_DETAIL)
    }

    fun finishListenLeverDetail() {
        leverPair = null
        finishListenSocket(LEVER_DETAIL)
    }
}