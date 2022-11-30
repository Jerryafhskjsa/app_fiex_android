package com.black.frying.service.socket

import android.content.Context
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.black.base.model.socket.KLineItem
import com.black.base.util.FryingUtil
import com.black.base.util.SocketDataContainer
import com.black.base.util.SocketUtil
import com.black.base.util.UrlConfig
import com.black.util.CommonUtil
import com.google.gson.reflect.TypeToken
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

class QuotationSocket(context: Context, handler: Handler) : FryingSocket(context, handler) {
    companion object {
        val TAG = QuotationSocket::class.java.simpleName
        const val ALL_DEAL = "quotationAllDeal"
        const val NEW_DEAL = "quotationListDeal"
        const val ALL_ORDER = "quotationAllOrder"
        const val NEW_ORDER = "quotationIncrementOrder"
        const val ALL_K_LINE = "qPairsAllKLine"
        const val NEW_K_LINE = "qPairsKLine"
        const val PAIR_STATUS = "qPairsStats"
    }

    var currentPair: String? = null
    var kLineTimeStep: String? = null
    var kLineTimeStepSecond: Long = 0
    var kLineId: String? = null
    private var kLinePage = 1
    var listenDeal = false

    private var onQuotationConnectListener = Emitter.Listener {
//        Log.e(TAG, "onConnectListener ==============================================\nonConnectListener obj：");
//            for (String event : quotationListenerMap.keySet()) {
//                qSocket.on(event, quotationListenerMap.get(event));
//            }
        socket.on("quotationAllOrder", onQuotationAllOrderNewListener)
        socket.on("quotationIncrementOrder", onQuotationOrderNewListener)
        socket.on("qPairsStats", onQuotationAllPairNewListener)
        //获取所有交易对信息，行情列表需要
        startListenQuotationAllConnect()
        //            startListenQuotationAllNewConnect();
//获取当前交易对信息， 成交量，最高值 最低值等
//startListenQuotationConnect();
        startListenQuotationNewConnect(currentPair)
        startListenKLine()
        //            startListenDeal();
    }
    //成交全量数据
    var onQuotationDealAllNewListener = Emitter.Listener { args ->
        CommonUtil.postHandleTask(handler) {
            for (`object` in args) {
                ////Log.e(TAG, "qSocket:" + socket + ",onQuotationDealAllNewListener ==============================================\n obj：" + object);
                val data = CommonUtil.parseJSONArray(`object`)
                if (data != null) {
//                    SocketDataContainer.updateQuotationDealNewData(context, handler, currentPair, data, true)
                }
            }
        }
    }
    var onQuotationDealNewListener = Emitter.Listener { args ->
        CommonUtil.postHandleTask(handler) {
            for (`object` in args) {
                ////Log.e(TAG, "qSocket:" + socket + ",onQuotationDealNewListener ==============================================\n obj：" + object);
                var data: JSONArray? = null
                if (`object` is String) {
                    try {
                        data = JSONArray(`object`.toString())
                    } catch (e: JSONException) {
                        FryingUtil.printError(e)
                    }
                } else if (`object` is JSONArray) {
                    data = `object`
                }
                if (data != null) {
//                    SocketDataContainer.updateQuotationDealNewData(context, handler, currentPair, data, false)
                }
            }
        }
    }
    var onQuotationAllOrderNewListener = Emitter.Listener { args ->
        CommonUtil.postHandleTask(handler) {
            for (`object` in args) {
                //Log.e(TAG, "onQuotationAllOrderNewListener ==============================================\n obj：" + object);
                var data: JSONArray? = null
                if (`object` is String) {
                    try {
                        data = JSONArray(`object`.toString())
                    } catch (e: JSONException) {
                        FryingUtil.printError(e)
                    }
                } else if (`object` is JSONArray) {
                    data = `object`
                }
                if (data != null) {
                    SocketDataContainer.updateQuotationOrderNewData(context, handler, currentPair, data, true)
                }
            }
        }
    }
    var onQuotationOrderNewListener = Emitter.Listener { args ->
        CommonUtil.postHandleTask(handler) {
            //请求使用全量数据
            //                    startListenQuotationOrderConnect();
            for (`object` in args) {
                //Log.e(TAG, "onQuotationOrderNewListener ==============================================\n obj：" + object);
                var data: JSONArray? = null
                if (`object` is String) {
                    try {
                        data = JSONArray(`object`.toString())
                    } catch (e: JSONException) {
                        FryingUtil.printError(context, e)
                    }
                } else if (`object` is JSONArray) {
                    data = `object`
                }
                if (data != null) {
                    SocketDataContainer.updateQuotationOrderNewData(context, handler, currentPair, data, false)
                }
            }
        }
    }
    var onQuotationAllPairNewListener = Emitter.Listener { args ->
        CommonUtil.postHandleTask(handler) {
            for (`object` in args) {
                //Log.e(TAG, "onQuotationAllPairNewListener ==============================================\n obj：" + object);
                var data: JSONArray? = null
                if (`object` is String) {
                    try {
                        data = JSONArray(`object`.toString())
                    } catch (e: JSONException) {
                        FryingUtil.printError(e)
                    }
                } else if (`object` is JSONArray) {
                    data = `object`
                }
                if (data != null) {
//                    SocketDataContainer.updatePairStatusData(context, handler, data, true)
                }
            }
        }
    }
    var onKLineAllListener = Emitter.Listener { args ->
        //Log.e(TAG, "onKLineAllListener==========================:" + kLineTimeStep);
        CommonUtil.postHandleTask(handler) {
            for (`object` in args) {
                ////Log.e(TAG, "kLineTimeStep:" + kLineTimeStep);
                //Log.e(TAG, "onKLineAllListener ==============================================\n obj：" + `object`);
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
                    if (kLinePage == 1) {
                        //保存KLineData
//                        SocketDataContainer.saveKLineDataAll(currentPair, handler, data)
//                        startListenSocket(NEW_K_LINE)
                    } else {
//                        SocketDataContainer.addKLineDataList(currentPair, handler, data)
                    }
                }
            }
        }
    }
    var onKLineNewListener = Emitter.Listener { args ->
        CommonUtil.postHandleTask(handler) {
            for (`object` in args) {
                //Log.e(TAG, "onKLineNewListener ==============================================\n obj：" + object);
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
                    val newData = gson.fromJson<KLineItem>(data.toString(), object : TypeToken<KLineItem?>() {}.type)
                    SocketDataContainer.addKLineData(currentPair, handler, kLineId, newData)
                }
            }
        }
    }

    init {
        emitterListenerMap[ALL_DEAL] = onQuotationDealAllNewListener
        emitterListenerMap[NEW_DEAL] = onQuotationDealNewListener
        emitterListenerMap[ALL_ORDER] = onQuotationAllOrderNewListener
        emitterListenerMap[NEW_ORDER] = onQuotationOrderNewListener
        emitterListenerMap[ALL_K_LINE] = onKLineAllListener
        emitterListenerMap[NEW_K_LINE] = onKLineNewListener
        emitterListenerMap[PAIR_STATUS] = onQuotationAllPairNewListener
    }

    override fun getTag(): String {
        return TAG
    }

    @Throws(Exception::class)
    override fun initSocket(): Socket {
//        val url = UrlConfig.getSocketHost(context) + "/quotation"
//        Log.e(TAG, "initSocket url:$url")
        return IO.socket(UrlConfig.getSocketHost(context) + "/quotation", socketOptions)
    }

    override fun start() {
        if (socket != null) {
            socket.on(Socket.EVENT_CONNECT, onQuotationConnectListener)
            socket.on(Socket.EVENT_DISCONNECT, onDisconnectListener)
            socket.on(Socket.EVENT_CONNECT_ERROR, onConnectErrorListener)
            socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectErrorListener)
            socket.connect()
        }
    }

    override fun stop() {
        if (socket != null) {
            socket.disconnect()
            socket.off(Socket.EVENT_CONNECT, onQuotationConnectListener)
            socket.off(Socket.EVENT_DISCONNECT, onDisconnectListener)
            socket.off(Socket.EVENT_CONNECT_ERROR, onConnectErrorListener)
            socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectErrorListener)
            for (event in emitterListenerMap.keys) {
                socket.off(event, emitterListenerMap[event])
            }
        }
    }

    //获取所有交易对信息，行情列表需要
    fun startListenQuotationAllConnect() {
        //////Log.e(TAG, "startListenQuotationAllConnect ==========================");
        val pairAll = JSONObject()
        try {
            pairAll.put("pair", "*")
        } catch (e: JSONException) {
            FryingUtil.printError(e)
        }
        socket.emit("qAllConnect", pairAll)
    }

    //请求监听交易对
    fun startListenQuotationConnect() {
        currentPair = SocketUtil.getCurrentPair(context)
        //////Log.e(TAG, "startListenQuotationConnect ==========================currentPair：" + currentPair);
        if (!TextUtils.isEmpty(currentPair)) {
            val obj = JSONObject()
            try {
                obj.put("pair", currentPair)
            } catch (e: JSONException) {
                FryingUtil.printError(e)
            }
            socket.emit("qConnect", obj)
        }
    }

    //请求监听新的连接 交易对相关
    fun startListenQuotationNewConnect(pair: String?) {
        currentPair = pair ?: SocketUtil.getCurrentPair(context)
        //////Log.e(TAG, "startListenQuotationNewConnect ==========================currentPair：" + currentPair);
        if (!TextUtils.isEmpty(currentPair)) {
            var jsonObject = JSONObject()
            //最新成交
            if (!TextUtils.isEmpty(currentPair) && listenDeal) {
                //最新成交
                try {
                    jsonObject = JSONObject()
                    jsonObject.put("pair", currentPair)
                    jsonObject.put("number", 100)
                    socket.emit("quotationDealConnect", jsonObject)
                } catch (e: Exception) {
                    FryingUtil.printError(e)
                }
            }
            //深度  { "size": 30, "pair": "BTC_USDt", "direction": "ALL" }]
            try {
                jsonObject = JSONObject()
                jsonObject.put("pair", currentPair)
                jsonObject.put("number", 100)
                //                jsonObject.put("direction", "ALL");
                socket.emit("quotationOrderConnect", jsonObject)
            } catch (e: JSONException) {
                FryingUtil.printError(e)
            }
            //K线增量
            if (!TextUtils.isEmpty(currentPair) && kLineTimeStep != null) {
                //K线增量
                try {
                    jsonObject = JSONObject()
                    jsonObject.put("pair", currentPair)
                    jsonObject.put("type", kLineTimeStep)
                    socket.emit("quotationConnect", jsonObject)
                } catch (e: Exception) {
                    FryingUtil.printError(e)
                }
            }
        }
    }

    fun startListenQuotationOrderConnect() {
        currentPair = SocketUtil.getCurrentPair(context)
        if (!TextUtils.isEmpty(currentPair)) {
            var jsonObject = JSONObject()
            try {
                jsonObject = JSONObject()
                jsonObject.put("pair", currentPair)
                jsonObject.put("number", 100)
                //                jsonObject.put("direction", "ALL");
                socket.emit("quotationOrderConnect", jsonObject)
            } catch (e: JSONException) {
                FryingUtil.printError(e)
            }
        }
    }

    fun startListenOrder() {
        startListenSocket(ALL_ORDER)
        startListenSocket(NEW_ORDER)
    }

    fun finishListenOrder() {
        finishListenSocket(ALL_ORDER)
        finishListenSocket(NEW_ORDER)
    }

    fun startListenQuota() {
        startListenSocket(PAIR_STATUS)
    }

    fun finishListenQuota() {
        finishListenSocket(PAIR_STATUS)
    }

    fun startListenKLine() {
        kLinePage = 1
        currentPair = SocketUtil.getCurrentPair(context)
        //Log.e(TAG, "startListenKLine ==========================currentPair：" + currentPair + ",kLineTimeStep:" + kLineTimeStep + ":kLineTimeStepSecond：" + kLineTimeStepSecond + ",kLineId:" + kLineId);
        if (!TextUtils.isEmpty(currentPair) && kLineTimeStep != null && !TextUtils.isEmpty(kLineId)) {
            var jsonObject = JSONObject()
            //K线增量
            try {
                jsonObject = JSONObject()
                jsonObject.put("pair", currentPair)
                jsonObject.put("type", kLineTimeStep)
                if (kLineTimeStepSecond > 0) {
                    val to = System.currentTimeMillis() / 1000
                    var from = to - kLineTimeStepSecond * 100
                    from = Math.max(from, 1567296000)
                    jsonObject.put("from", from)
                    jsonObject.put("to", to)
                    jsonObject.put("no", kLineId)
                }
                socket.emit("quotationConnect", jsonObject)
                startListenSocket(ALL_K_LINE)
                //Log.e(TAG, "startListenSocket==========================:" + kLineTimeStep);
            } catch (e: Exception) {
                FryingUtil.printError(e)
            }
        }
    }

    fun loadMoreKLine(kLinePage: Int) {
        if (kLinePage <= 0 || this.kLinePage == kLinePage) {
            return
        }
        currentPair = SocketUtil.getCurrentPair(context)
        //Log.e(TAG, "loadMoreKLine ==========================currentPair：" + currentPair + ",kLineTimeStep:" + kLineTimeStep + ":kLineTimeStepSecond：" + kLineTimeStepSecond + ",kLinePage:" + kLinePage);
        if (!TextUtils.isEmpty(currentPair) && kLineTimeStep != null && !TextUtils.isEmpty(kLineId)) {
            var jsonObject = JSONObject()
            //K线增量
            try {
                jsonObject = JSONObject()
                jsonObject.put("pair", currentPair)
                jsonObject.put("type", kLineTimeStep)
                if (kLineTimeStepSecond > 0) {
                    var to = System.currentTimeMillis() / 1000 - kLineTimeStepSecond * 100 * (kLinePage - 1)
                    var from = to - kLineTimeStepSecond * 100
                    from = Math.max(from, 1567296000)
                    to = Math.max(to, 1567296000)
                    jsonObject.put("from", from)
                    jsonObject.put("to", to)
                    jsonObject.put("no", kLineId + "_" + kLinePage)
                }
                socket.emit("quotationConnect", jsonObject)
                this.kLinePage = kLinePage
            } catch (e: Exception) {
                FryingUtil.printError(e)
            }
        }
    }

    fun finishListenKLine() {
        finishListenSocket(ALL_K_LINE)
        finishListenSocket(NEW_K_LINE)
        //Log.e(TAG, "finishListenKLine==========================:" + kLineTimeStep);
    }

    fun startListenDeal() {
        currentPair = SocketUtil.getCurrentPair(context)
        ////Log.e(TAG, "startListenDeal ==========================currentPair：" + currentPair + ",listenDeal:" + listenDeal);
        if (!TextUtils.isEmpty(currentPair) && listenDeal) {
            var jsonObject = JSONObject()
            //最新成交
            try {
                jsonObject = JSONObject()
                jsonObject.put("pair", currentPair)
                jsonObject.put("number", 100)
                socket.emit("quotationDealConnect", jsonObject)
            } catch (e: Exception) {
                FryingUtil.printError(e)
            }
            startListenSocket(ALL_DEAL)
            startListenSocket(NEW_DEAL)
        }
    }

    fun finishListenDeal() {
        finishListenSocket(ALL_DEAL)
        finishListenSocket(NEW_DEAL)
    }
}