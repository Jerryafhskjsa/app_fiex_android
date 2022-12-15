package com.black.base.api

import android.content.Context
import android.util.Log
import android.util.SparseArray
import com.black.base.R
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.c2c.C2CPrice
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
    private const val FUTURE_SYMBOL_COIN_LIST = 2
    private const val FUTURE_TICKERS_LIST = 1

    //合约所有交易对列表
    var futureAllSymbolPairList: ArrayList<PairStatus>? = ArrayList()
    //合约U本位交易对列表
    var futureUbaseSymbolPairList: ArrayList<PairStatus>? = null
    //合约币本位交易对列表
    var futureCoinBaseSymbolPairList: ArrayList<PairStatus>? = null

    //u本位交易对行情数据
    private var futureUbaseTickersPairStatus: ArrayList<PairStatus?>? = ArrayList()

    //币本位交易对行情数据
    private var futureCoinTickersPairStatus: ArrayList<PairStatus?>? = ArrayList()

    //所有合约交易对行情数据
    private var futureAllPairTickersStatus: ArrayList<PairStatus?>? = ArrayList()

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
     * 从本地缓存获取合约交易对列表
     */
    fun getFuturesSymbolListLocal(
        context: Context?,
        type: ConstData.PairStatusType,
        isShowLoading: Boolean,
        callback: Callback<ArrayList<PairStatus>?>?
    ) {
        when(type){
            ConstData.PairStatusType.FUTURE_U ->{
                if (futureUbaseSymbolPairList != null && futureUbaseSymbolPairList!!.isNotEmpty() && System.currentTimeMillis() - (getLastGetTime(
                        FUTURE_SYMBOL_U_LIST
                    )
                        ?: 0) < DATA_CACHE_OVER_TIME
                ) {
                    callback?.callback(futureUbaseSymbolPairList)
                } else {
                    getFutureSymbolPairListCallBack(context, type, isShowLoading, callback)
                }
            }
            ConstData.PairStatusType.FUTURE_COIN ->{
                if (futureCoinBaseSymbolPairList != null && futureCoinBaseSymbolPairList!!.isNotEmpty() && System.currentTimeMillis() - (getLastGetTime(
                        FUTURE_SYMBOL_COIN_LIST
                    )
                        ?: 0) < DATA_CACHE_OVER_TIME
                ) {
                    callback?.callback(futureCoinBaseSymbolPairList)
                } else {
                    getFutureSymbolPairListCallBack(context, type, isShowLoading, callback)
                }
            }
            ConstData.PairStatusType.FUTURE_ALL ->{
                getFutureSymbolPairListCallBack(context, type, isShowLoading, callback)
            }
        }
    }

    /**
     * 获取合约交易对列表
     */
    private fun getFutureSymbolPairListCallBack(
        context: Context?,
        type: ConstData.PairStatusType?,
        isShowLoading: Boolean,
        callback: Callback<ArrayList<PairStatus>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        val observable = getFutureSymbolPairListObservable(context, type)
        observable?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取合约交易对列表
     * 并且把数据组装到PairStatus
     */
    private fun getFutureSymbolPairListObservable(
        context: Context?,
        type: ConstData.PairStatusType?
    ): Observable<ArrayList<PairStatus>?>? {
        if (context == null) {
            return null
        }
        var apiManager: ApiManager? = null
        when (type) {
            ConstData.PairStatusType.FUTURE_U -> apiManager =
                ApiManager.build(context, UrlConfig.ApiType.URL_FUT_F)
            ConstData.PairStatusType.FUTURE_COIN -> apiManager =
                ApiManager.build(context, UrlConfig.ApiType.URL_FUT_D)
            ConstData.PairStatusType.FUTURE_ALL ->{
                futureAllSymbolPairList?.clear()
                futureAllSymbolPairList?.addAll(futureUbaseSymbolPairList!!)
                futureAllSymbolPairList?.addAll(futureCoinBaseSymbolPairList!!)
                return Observable.just(futureAllSymbolPairList)
            }
        }
        val futureApiService = apiManager?.getService(FutureApiService::class.java)
        return futureApiService?.getSymbolList()
            ?.flatMap { pairInfoData: HttpRequestResultBean<ArrayList<SymbolBean>?>? ->
                val pairStatuses = ArrayList<PairStatus>()
                var resultDataList = pairInfoData!!.result
                if (resultDataList != null && pairInfoData.returnCode == HttpRequestResult.SUCCESS) {
                    if(type == ConstData.PairStatusType.FUTURE_U){
                        setLastGetTime(FUTURE_SYMBOL_U_LIST, System.currentTimeMillis())
                    }
                    if(type == ConstData.PairStatusType.FUTURE_COIN){
                        setLastGetTime(FUTURE_SYMBOL_COIN_LIST, System.currentTimeMillis())
                    }
                    for (i in resultDataList!!.indices) {
                        val symbol = resultDataList[i]
                        var pairStatus: PairStatus? = PairStatus()
                        var pair = symbol?.symbol//交易对名
                        pairStatus?.pair = pair
//                            pairStatus?.hot = symbol?.hot
                        pairStatus?.contractSize = symbol?.contractSize//合约乘数（面值）
                        pairStatus?.underlyingType = symbol?.underlyingType//标的类型，币本位(C_BASED)，u本位(U_BASED)
                        pairStatus?.initLeverage = symbol?.initLeverage//初始杠杆倍数
                        pairStatus?.supportOrderType = symbol?.supportOrderType//支持的下单类型
                        pairStatus?.supportEntrustType = symbol?.supportEntrustType//支持计划委托类型
                        pairStatus?.supportTimeInForce = symbol?.supportTimeInForce//支持有效方式
                        pairStatus?.order_no = i
                        var maxPrecision = symbol?.pricePrecision
                        maxPrecision =
                            if (maxPrecision == null || maxPrecision == 0) ConstData.DEFAULT_PRECISION else maxPrecision
                        pairStatus?.precision = maxPrecision//价格精度
                        pairStatus?.amountPrecision = symbol?.quoteCoinDisplayPrecision//报价币种显示精度
                        pairStatus?.supportingPrecisionList =
                            pairStatus?.setMaxSupportPrecisionList(
                                maxPrecision.toString(),
                                symbol?.depthPrecisionMerge//盘口精度合并
                            )
                        pairStatuses?.add(pairStatus!!)
                        when(type){
                            ConstData.PairStatusType.FUTURE_U ->{
                                futureUbaseSymbolPairList = pairStatuses
                            }
                            ConstData.PairStatusType.FUTURE_COIN ->{
                                futureCoinBaseSymbolPairList = pairStatuses
                            }
                        }
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
                        CommonUtil.getItemFromList(futureUbaseSymbolPairList, 0)
                    )
                }
                Observable.just(pairStatuses)
            }
    }

    /**
     * 本地获取合约交易对的行情信息
     */
    fun getFutureTickersLocal(
        context: Context?,
        type: ConstData.PairStatusType
    ): Observable<ArrayList<PairStatus?>?>? {
        if (context == null) {
            return Observable.empty()
        }
        var futureList: ArrayList<PairStatus?>? = null
        when (type) {
            ConstData.PairStatusType.FUTURE_U -> {
                futureList = futureUbaseTickersPairStatus
            }
            ConstData.PairStatusType.FUTURE_COIN -> {
                futureList = futureCoinTickersPairStatus
            }
            ConstData.PairStatusType.FUTURE_ALL -> {
                futureList = futureAllPairTickersStatus
            }
        }
        if (futureList != null
            && futureList!!.isNotEmpty()
        ) {
            return if (type == ConstData.PairStatusType.FUTURE_ALL) {
                Observable.just(futureList)
            } else {
                if (System.currentTimeMillis() - (getLastGetTime(FUTURE_TICKERS_LIST)
                        ?: 0) < DATA_CACHE_OVER_TIME
                ) {
                    Observable.just(futureList)
                } else {
                    getFutureTickers(context, type)
                }
            }
        } else {
            return getFutureTickers(context, type)
        }
    }


    fun getFutureSymboleListPairData(context: Context?, type: ConstData.PairStatusType?): ArrayList<PairStatus?>? {
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
        SocketDataContainer.getAllFuturePairStatus(context, type, callback)
        return symbolListPairData
    }


    /**
     * 获取合约交易对的行情信息
     */
    fun getFutureTickers(context: Context?, type: ConstData.PairStatusType): Observable<ArrayList<PairStatus?>?>? {
        if (context == null) {
            return Observable.empty()
        }
        var apiManager: ApiManager? = null
        when (type) {
            ConstData.PairStatusType.FUTURE_U -> apiManager =
                ApiManager.build(context, false, UrlConfig.ApiType.URL_FUT_F)
            ConstData.PairStatusType.FUTURE_COIN -> apiManager =
                ApiManager.build(context, false, UrlConfig.ApiType.URL_FUT_D)
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
                    var pairListData:ArrayList<PairStatus>? = null
                    when(type){
                        ConstData.PairStatusType.FUTURE_U -> pairListData = futureUbaseSymbolPairList
                        ConstData.PairStatusType.FUTURE_COIN -> pairListData = futureCoinBaseSymbolPairList
                        ConstData.PairStatusType.FUTURE_ALL -> pairListData = futureAllSymbolPairList
                    }
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
                    for ((key, value) in pairStatusMap) {
                        newData.add(value)
                    }
                    if (type == ConstData.PairStatusType.FUTURE_U) {
                        if (futureUbaseTickersPairStatus?.isNotEmpty() == true) {
                            futureUbaseTickersPairStatus?.clear()
                        }
                        futureUbaseTickersPairStatus?.addAll(newData)
                        if (futureAllPairTickersStatus?.size!! < futureUbaseTickersPairStatus?.size?.plus(
                                futureCoinTickersPairStatus?.size!!
                            )!!
                        ) {
                            futureAllPairTickersStatus?.addAll(futureUbaseTickersPairStatus!!)
                        }
                    }
                    if (type == ConstData.PairStatusType.FUTURE_COIN) {
                        if (futureCoinTickersPairStatus?.isNotEmpty() == true) {
                            futureCoinTickersPairStatus?.clear()
                        }
                        futureCoinTickersPairStatus?.addAll(newData)
                        if (futureAllPairTickersStatus?.size!! < futureUbaseTickersPairStatus?.size?.plus(
                                futureCoinTickersPairStatus?.size!!
                            )!!
                        ) {
                            futureAllPairTickersStatus?.addAll(futureCoinTickersPairStatus!!)
                        }
                    }
                    Observable.just(newData)
                } else {
                    Observable.empty()
                }
            }
            ?.compose(RxJavaHelper.observeOnMainThread())
    }

}