package com.black.base.api

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import com.black.base.R
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.c2c.C2CPrice
import com.black.base.model.clutter.HomeSymbolList
import com.black.base.model.clutter.HomeTickers
import com.black.base.model.future.*
import com.black.base.model.socket.PairStatus
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import io.reactivex.Observable
import java.math.BigDecimal

object FutureApiServiceHelperWrapper {
    private const val DATA_CACHE_OVER_TIME = 0.5 * 60 * 1000 //20分钟
    private const val FUTURE_SYMBOL_U_LIST = 0
    private const val FUTURE_TICKERS_LIST = 1

    //u本位交易对列表
    var futureSymbolPairList: ArrayList<PairStatus>? = null

    //u本位交易对行情数据
    private var futureUbaseTickersPairStatus: ArrayList<PairStatus?>? = ArrayList()
    //币本位交易对行情数据
    private var futureCoinTickersPairStatus: ArrayList<PairStatus?>? = ArrayList()
    var markPriceBeanList: ArrayList<MarkPriceBean>? = null
    var markPrice: MarkPriceBean? = null

    //上次拉取数据时间，根据类型分类
    private val lastGetTimeMap = SparseArray<Long>()

    private fun getLastGetTime(type: Int): Long? {
        val lastGetTime = lastGetTimeMap[type]
        return lastGetTime ?: 0
    }

    private fun setLastGetTime(type: Int, time: Long) {
        lastGetTimeMap.put(type, time)
    }

    /**
     * 从本地缓存获取u本位交易对
     */
    fun getFuturesSymbolListLocal(
        context: Context?,
        type:String,
        isShowLoading: Boolean,
        callback: Callback<ArrayList<PairStatus>?>?
    ) {
        if (futureSymbolPairList != null && futureSymbolPairList!!.isNotEmpty() && System.currentTimeMillis() - (getLastGetTime(
                FUTURE_SYMBOL_U_LIST
            )
                ?: 0) < DATA_CACHE_OVER_TIME
        ) {
            callback?.callback(futureSymbolPairList)
        } else {
            getFutureSymbolPairListCallBack(context, type,isShowLoading, callback)
        }
    }

    /**
     * 获取u本位合约币种列表
     */
    fun getFutureSymbolPairListCallBack(
        context: Context?,
        type:String?,
        isShowLoading: Boolean,
        callback: Callback<ArrayList<PairStatus>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        val observable = getFutureSymbolPairListObservable(context,type)
        observable?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取u本位合约币种列表
     */
    fun getFutureSymbolPairListObservable(context: Context?,type: String?): Observable<ArrayList<PairStatus>?>? {
        if (context == null) {
            return null
        }
        var apiManager:ApiManager? = null
        when(type){
            context.getString(R.string.usdt_base) -> apiManager = ApiManager.build(context, UrlConfig.ApiType.URL_FUT_F)
            context.getString(R.string.coin_base) -> apiManager = ApiManager.build(context, UrlConfig.ApiType.URL_FUT_D)
        }
        val futureApiService = apiManager?.getService(FutureApiService::class.java)
        return futureApiService?.getSymbolList()
            ?.flatMap { pairInfoData: HttpRequestResultBean<ArrayList<SymbolBean>?>? ->
                val pairStatuses = ArrayList<PairStatus>()
                var resultDataList = pairInfoData!!.result
                if (resultDataList != null && pairInfoData.returnCode == HttpRequestResult.SUCCESS) {
                    setLastGetTime(FUTURE_SYMBOL_U_LIST, System.currentTimeMillis())
                    for (i in resultDataList!!.indices) {
                        val symbol = resultDataList[i]
                        var pairStatus: PairStatus? = PairStatus()
                        var pair = symbol?.symbol//交易对名
                        pairStatus?.pair = pair
//                            pairStatus?.hot = symbol?.hot
                        pairStatus?.contractSize = symbol?.contractSize
                        pairStatus?.supportOrderType = symbol?.supportOrderType//支持的下单类型
                        pairStatus?.order_no = i
                        var maxPrecision = symbol?.pricePrecision
                        maxPrecision =
                            if (maxPrecision == null || maxPrecision == 0) ConstData.DEFAULT_PRECISION else maxPrecision
                        pairStatus?.precision = maxPrecision//价格精度
                        pairStatus?.amountPrecision = symbol?.quantityPrecision//数量精度
                        pairStatus?.supportingPrecisionList =
                            pairStatus?.setMaxSupportPrecisionList(
                                maxPrecision.toString(),
                                symbol?.depthPrecisionMerge
                            )
                        pairStatuses?.add(pairStatus!!)
                        futureSymbolPairList = pairStatuses
                    }
                }
                var currentPair = CookieUtil.getCurrentFutureUPair(context)
                var currentPairStatus = CookieUtil.getCurrentFutureUPairObjrInfo(context)
                if (currentPair == null) {
                    CookieUtil.setCurrentFutureUPair(context, currentPair)
                }
                if (currentPairStatus == null) {
                    CookieUtil.setCurrentFutureUPairObjrInfo(
                        context,
                        CommonUtil.getItemFromList(futureSymbolPairList, 0)
                    )
                }
                Observable.just(pairStatuses)
            }
    }

    /**
     * 本地获取u本位交易对的行情信息
     */
    fun getFutureTickersLocal(context: Context?,type:String): Observable<ArrayList<PairStatus?>?>? {
        if (context == null) {
            return Observable.empty()
        }
        var futureList:ArrayList<PairStatus?>? = null
        when(type){
            context.getString(R.string.usdt_base) -> {
                futureList = futureUbaseTickersPairStatus
            }
            context.getString(R.string.coin_base) -> {
                futureList = futureCoinTickersPairStatus
            }
        }
        return if (futureList != null
            && futureList!!.isNotEmpty()
            && System.currentTimeMillis() - (getLastGetTime(
                FUTURE_TICKERS_LIST
            )
                ?: 0) < DATA_CACHE_OVER_TIME
        ) {
            Observable.just(futureList)
        } else {
            getFutureTickers(context,type)
        }
    }


    fun getFutureSymboleListPairData(context: Context?,type:String?): ArrayList<PairStatus?>? {
        var symbolListPairData: ArrayList<PairStatus?>? = ArrayList()
        val callback: Callback<ArrayList<PairStatus?>?> =
            object : Callback<ArrayList<PairStatus?>?>() {
                override fun callback(returnData: ArrayList<PairStatus?>?) {
                    if (returnData != null) {
                        symbolListPairData = returnData
                    }
                }

                override fun error(type: Int, error: Any?) {
                }
            }
        SocketDataContainer.getAllFuturePairStatus(context,type, callback)
        return symbolListPairData
    }


    /**
     * 获取合约交易对的行情信息
     */
    fun getFutureTickers(context: Context?,type:String): Observable<ArrayList<PairStatus?>?>? {
        if (context == null) {
            return Observable.empty()
        }
        var apiManager:ApiManager? = null
        when(type){
            context.getString(R.string.usdt_base) -> apiManager = ApiManager.build(context, false, UrlConfig.ApiType.URL_FUT_F)
            context.getString(R.string.coin_base) -> apiManager = ApiManager.build(context, false, UrlConfig.ApiType.URL_FUT_D)
        }
        return apiManager?.getService(FutureApiService::class.java)
            ?.getTickers()
            ?.flatMap { result: HttpRequestResultBean<List<TickerBean?>?>? ->
                var data = result?.result!!
                if (data.isNotEmpty()) {
                    setLastGetTime(FUTURE_TICKERS_LIST, System.currentTimeMillis())
                    var c2CPrice: C2CPrice? = null
                    //获取c2c usdt价格
                    C2CApiServiceHelper.getC2CPrice(context, object : Callback<C2CPrice?>() {
                        override fun callback(returnData: C2CPrice?) {
                            c2CPrice = returnData
                        }

                        override fun error(type: Int, error: Any?) {
                        }

                    })
                    var pairListData = getFutureSymboleListPairData(context,type)
                    var newData = ArrayList<PairStatus?>()
                    var pairStatusMap: MutableMap<String?, PairStatus?> = HashMap()
                    for (j in pairListData!!.indices) {
                        for (i in data.indices) {
                            var temp = pairListData!![j]
                            if (pairListData!![j]?.pair == data[i]?.s) {
                                var temp = pairListData!![j]
                                //现价
                                temp?.currentPrice = data[i]?.c?.toDouble()!!
                                //交易量
                                temp?.tradeVolume = data[i]?.v?.toDouble()!!
                                //交易额
                                temp?.tradeAmount = data[i]?.a?.toDouble()!!
                                temp?.totalAmount = data[i]?.a?.toDouble()!!
                                //涨跌幅
                                temp?.priceChangeSinceToday = data[i]?.r?.toDouble()!!
                                if (c2CPrice != null) {
                                    var price = BigDecimal(temp!!.currentPrice)
                                    var usdt = BigDecimal(c2CPrice?.buy!!)
                                    var priceCny = price.times(usdt)
                                    var priceCnyStr = NumberUtil.formatNumberNoGroup(priceCny, 4, 4)
                                    temp?.setCurrentPriceCNY(priceCnyStr.toDouble(), "0.0000")
                                }
                            }
                            pairStatusMap[temp?.pair] = temp
                        }
                    }
                    if(type == context.getString(R.string.usdt_base)){
                        if (futureUbaseTickersPairStatus?.isNotEmpty() == true) {
                            futureUbaseTickersPairStatus?.clear()
                        }
                        futureUbaseTickersPairStatus?.addAll(newData)
                    }
                    if(type == context.getString(R.string.coin_base)){
                        if (futureCoinTickersPairStatus?.isNotEmpty() == true) {
                            futureCoinTickersPairStatus?.clear()
                        }
                        futureCoinTickersPairStatus?.addAll(newData)
                    }
                    for ((key, value) in pairStatusMap) {
                        newData.add(value)
                    }
                    Observable.just(newData)
                } else {
                    Observable.empty()
                }
            }
            ?.compose(RxJavaHelper.observeOnMainThread())
    }


}