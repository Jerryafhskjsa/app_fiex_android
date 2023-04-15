package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import com.black.base.util.UrlConfig
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter

//封装push相关的socket
class PushSocket(context: Context, handler: Handler) : FryingSocket(context, handler) {
    companion object {
        private val TAG = PushSocket::class.java.simpleName
        const val COIN_CONFIG = "coinConfig"
        const val COIN_PRICE = "coinPrice"
        const val PAIR_CONFIG = "pairConfig"
        const val HOT_PAIR = "hotPair"
        const val C2C_COIN_TYPE = "c2cCoinType"
    }

    private val onPushConnectListener = Emitter.Listener {
        ////Log.e(TAG, "onuConnectListener ==============================================\nonuConnectListener obj：");
        for (event in emitterListenerMap.keys) {
            socket.on(event, emitterListenerMap[event])
        }
    }

    //币种配置
    private val onCoinConfigListener = Emitter.Listener {
        //Log.e(TAG, "===onCoinConfigListener===");
//        SocketDataContainer.onCoinInfoUpdate(context)
    }
    //币种价格
    private val onCoinPriceListener = Emitter.Listener {
        //Log.e(TAG, "===onCoinPriceListener===");
//        SocketDataContainer.onCoinPriceUpdate(context)
    }
    //交易对配置
    private val onPairConfigListener = Emitter.Listener {
        //Log.e(TAG, "===onPairConfigListener===");
//        SocketDataContainer.onPairUpdate(context)
    }
    //热门币种
    private val onHotPairListener = Emitter.Listener {
        //Log.e(TAG, "===onHotPairListener===");
//        SocketDataContainer.onHotPairUpdate(context)
    }
    //C2C交易币种
    private val onC2cCoinTypeListener = Emitter.Listener {
        //Log.e(TAG, "===onC2cCoinTypeListener===");
    }

    init {
        emitterListenerMap[COIN_CONFIG] = onCoinConfigListener
        emitterListenerMap[COIN_PRICE] = onCoinPriceListener
        emitterListenerMap[PAIR_CONFIG] = onPairConfigListener
        emitterListenerMap[HOT_PAIR] = onHotPairListener
        emitterListenerMap[C2C_COIN_TYPE] = onC2cCoinTypeListener
    }

    override fun getTag(): String {
        return TAG
    }

    @Throws(Exception::class)
    override fun initSocket(): Socket {
        return IO.socket(UrlConfig.getSocketHost() + "/push", socketOptions)
    }


    override fun start() {
        if (socket != null) {
            socket.on(Socket.EVENT_CONNECT, onPushConnectListener)
            socket.on(Socket.EVENT_DISCONNECT, onDisconnectListener)
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectErrorListener)
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectErrorListener)
            socket.connect()
        }
    }

    override fun stop() {
        if (socket != null) {
            socket.disconnect()
            socket.off(Socket.EVENT_CONNECT, onPushConnectListener)
            socket.off(Socket.EVENT_DISCONNECT, onDisconnectListener)
            socket.off(Socket.EVENT_CONNECT_ERROR, onConnectErrorListener)
            socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectErrorListener)
            for (event in emitterListenerMap.keys) {
                socket.off(event, emitterListenerMap[event])
            }
        }
    }

    fun startListenCoinConfig() {
        startListenSocket(COIN_CONFIG)
    }

    fun finishListenCoinConfig() {
        finishListenSocket(COIN_CONFIG)
    }

    fun startListenCoinPrice() {
        startListenSocket(COIN_PRICE)
    }

    fun finishListenCoinPrice() {
        finishListenSocket(COIN_PRICE)
    }

    fun startListenPairConfig() {
        startListenSocket(PAIR_CONFIG)
    }

    fun finishListenPairConfig() {
        finishListenSocket(PAIR_CONFIG)
    }

    fun startListenHotPair() {
        startListenSocket(HOT_PAIR)
    }

    fun finishListenHotPair() {
        finishListenSocket(HOT_PAIR)
    }

    fun startListenC2CCoinType() {
        startListenSocket(C2C_COIN_TYPE)
    }

    fun finishListenC2CCoinType() {
        finishListenSocket(C2C_COIN_TYPE)
    }

}