package com.black.base.api

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.QuotationSet
import com.black.base.model.c2c.C2CPrice
import com.black.base.model.clutter.HomeSymbolList
import com.black.base.model.clutter.HomeTickers
import com.black.base.model.clutter.HomeTickersKline
import com.black.base.model.socket.CoinOrder
import com.black.base.model.socket.PairStatus
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import io.reactivex.Observable
import java.math.BigDecimal

object PairApiServiceHelper {
    private var TAG = PairApiServiceHelper::class.java.simpleName
    private const val DATA_CACHE_OVER_TIME = 20 * 60 * 1000 //热门币种，请求缓存时间，20分钟
    private const val C2C_PRICE = 1
    private const val TRADE_SET = 2
    private const val TRADE_PAIR = 3
    private const val HOT_PAIR = 4
    private const val HOME_TICKER_LIST = 5

    //上次拉取数据时间，根据类型分类
    private val lastGetTimeMap = SparseArray<Long>()
    private var tradeSets: ArrayList<QuotationSet?>? = null
    private var hotPairCache: HttpRequestResultDataList<String?>? = null

    //所有交易对行情数据
    private var homeTickersPairStatus: ArrayList<PairStatus?>? = ArrayList()



    private fun getLastGetTime(type: Int): Long? {
        val lastGetTime = lastGetTimeMap[type]
        return lastGetTime ?: 0
    }

    private fun setLastGetTime(type: Int, time: Long) {
        lastGetTimeMap.put(type, time)
    }

    fun getSymboleListPairData(context: Context?): ArrayList<PairStatus?>? {
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
        SocketDataContainer.getAllPairStatus(context, callback)
        return symbolListPairData
    }

    fun getTradeSetsLocal(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<ArrayList<QuotationSet?>?>?
    ) {
        if (tradeSets != null && tradeSets!!.isNotEmpty() && System.currentTimeMillis() - (getLastGetTime(
                TRADE_SET
            )
                ?: 0) < DATA_CACHE_OVER_TIME
        ) {
            callback?.callback(tradeSets)
        } else {
            getTradeSets(
                context,
                isShowLoading,
                object : Callback<HttpRequestResultDataList<QuotationSet?>?>() {
                    override fun error(type: Int, error: Any) {
                        tradeSets = null
                        callback?.error(type, error)
                    }

                    override fun callback(returnData: HttpRequestResultDataList<QuotationSet?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            setLastGetTime(TRADE_SET, System.currentTimeMillis())
                            tradeSets = ArrayList()
                            returnData.data?.let {
                                for (item in it) {
                                    item?.let {
                                        tradeSets?.add(item)
                                    }
                                }
                            }
                            //将币种列表保存到本地
                            callback?.callback(tradeSets)
                        }
                    }
                })
        }
    }

    fun getTradeSets(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<HttpRequestResultDataList<QuotationSet?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URL_PRO).getService(PairApiService::class.java)
            ?.getTradeSetsFiex()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun getTradePairInfo(
        context: Context?,
        pair: String?,
        callback: Callback<HttpRequestResultDataList<PairStatus?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(PairApiService::class.java)
            ?.getTradePairInfo(pair)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getTradePairList(
        context: Context?,
        callback: Callback<HttpRequestResultDataList<String?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(PairApiService::class.java)
            ?.getOrderedPairs(null)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }


    /**
     * 本地获取所有交易对的行情信息
     */
    fun getHomeTickersLocal(context: Context?): Observable<ArrayList<PairStatus?>?>? {
        if (context == null) {
            return Observable.empty()
        }
        return if (homeTickersPairStatus != null
            && homeTickersPairStatus!!.isNotEmpty()
            && System.currentTimeMillis() - (getLastGetTime(HOME_TICKER_LIST)
                ?: 0) < DATA_CACHE_OVER_TIME
        ) {
            Observable.just(homeTickersPairStatus)
        } else {
            getHomeTickers(context)
        }
    }


    /**
     * 获取所有交易对的行情信息
     */
    fun getHomeTickers(context: Context?): Observable<ArrayList<PairStatus?>?>? {
        if (context == null) {
            return Observable.empty()
        }
        return ApiManager.build(context, false, UrlConfig.ApiType.URL_PRO)
            .getService(PairApiService::class.java)
            ?.getHomeTickersList()
            ?.flatMap { result: HttpRequestResultDataList<HomeTickers?>? ->
                var data = result?.data!!
                if (data.isNotEmpty()) {
                    setLastGetTime(HOME_TICKER_LIST, System.currentTimeMillis())
                    var c2CPrice:C2CPrice? = null
                    //获取c2c usdt价格
                    C2CApiServiceHelper.getC2CPrice(context,object :Callback<C2CPrice?>(){
                        override fun callback(returnData: C2CPrice?) {
                            c2CPrice = returnData
                        }

                        override fun error(type: Int, error: Any?) {
                        }

                    })
                    var pairListData = getSymboleListPairData(context)
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
                                if(c2CPrice != null){
                                    var price = BigDecimal(temp!!.currentPrice)
                                    var usdt = BigDecimal(c2CPrice?.buy!!)
                                    var priceCny = price.times(usdt)
                                    var priceCnyStr =  NumberUtil.formatNumberNoGroup(priceCny, 4, 4)
                                    temp?.setCurrentPriceCNY(priceCnyStr.toDouble(),"0.0000")
                                }
                            }
                            pairStatusMap[temp?.pair] = temp
                        }
                    }
                    if (homeTickersPairStatus?.isNotEmpty() == true) {
                        homeTickersPairStatus?.clear()
                    }
                    for ((key,value) in pairStatusMap){
                        newData.add(value)
                    }
                    homeTickersPairStatus?.addAll(newData)
                    Observable.just(newData)
                } else {
                    Observable.empty()
                }
            }
            ?.compose(RxJavaHelper.observeOnMainThread())
    }

    /**
     * 获取交易对全部配置信息
     */
    fun getFullPairStatusObservable(context: Context?): Observable<ArrayList<PairStatus>?>? {
        if (context == null) {
            return null
        }
        val pairApiService = ApiManager.build(context, UrlConfig.ApiType.URL_PRO)
            .getService(PairApiService::class.java)
        return pairApiService!!.getHomeSymbolList()
            ?.flatMap { pairInfoData: HttpRequestResultDataList<HomeSymbolList?>? ->
                val pairStatuses = ArrayList<PairStatus>()
                val allPair = ArrayList<String?>()
                val allLeverPair = ArrayList<String?>()
                if (pairInfoData!!.data != null && pairInfoData.code == HttpRequestResult.SUCCESS) {
                    for (i in pairInfoData.data!!.indices) {
                        val symbol = pairInfoData.data!![i]
                        var pairStatus: PairStatus? = PairStatus()
                        var pair = symbol?.symbol//交易对名
                        pairStatus?.pair = pair
                        pairStatus?.hot = symbol?.hot
                        pairStatus?.setType = symbol?.setType
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
                        if (pairStatus != null) {
                            pairStatuses.add(pairStatus)
                        }
                        allPair.add(pairStatus?.pair)
                        if (pairStatus?.isLever == true) {
                            allLeverPair.add(pairStatus.pair)
                        }
                    }
                }
                var currentPair = CookieUtil.getCurrentPair(context)
                if (allPair.isNotEmpty() && (TextUtils.isEmpty(currentPair) || !allPair.contains(
                        currentPair
                    ))
                ) {
                    currentPair = CommonUtil.getItemFromList(allPair, 0)
                    if (currentPair != null) {
                        CookieUtil.setCurrentPair(context, currentPair)
                        SocketUtil.notifyPairChanged(context)
                    }
                }
                val currentLeverPair = CookieUtil.getCurrentPairLever(context)
                if (allLeverPair.isNotEmpty() && (TextUtils.isEmpty(currentLeverPair) || !allLeverPair.contains(
                        currentLeverPair
                    ))
                ) {
                    currentPair = CommonUtil.getItemFromList(allLeverPair, 0)
                    if (currentPair != null) {
                        CookieUtil.setCurrentPairLever(context, currentPair)
                    }
                }
                Observable.just(pairStatuses)
            }
    }

    /**
     * 获取交易对全部信息，包括 ST 默认排序 交易深度
     */
    fun getFullPairStatus(context: Context?, callback: Callback<ArrayList<PairStatus>?>?) {
        if (context == null || callback == null) {
            return
        }
        val observable = getFullPairStatusObservable(context)
        observable?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    /**
     * 获取首页kline
     */
    fun getHomeKline(
        context: Context?,
        tickers: ArrayList<PairStatus?>?
    ): Observable<ArrayList<PairStatus?>?>? {
        if (context == null) {
            return null
        }
        return ApiManager.build(context, false, UrlConfig.ApiType.URL_PRO)
            .getService(PairApiService::class.java)
            ?.getHomeKLine()
            ?.flatMap { result: HttpRequestResultDataList<HomeTickersKline?>? ->
                var data = result?.data!!
                for (i in data.indices) {
                    for (j in tickers!!.indices) {
                        if (tickers!![j]?.pair == data[i]?.s) {
                            //赋k线数值
                            tickers!![j]?.kLineData = data[i]
                        }
                    }
                }
                Observable.just(tickers)
            }
            ?.compose(RxJavaHelper.observeOnMainThread())
    }

    /**
     * 热门交易对
     */
    fun getHotPair(context: Context?): Observable<HttpRequestResultDataList<String?>?>? {
        if (context == null) {
            return null
        }
        val lastTime = getLastGetTime(HOT_PAIR)
        return if (hotPairCache != null && hotPairCache!!.code != null && hotPairCache!!.code == HttpRequestResult.SUCCESS && lastTime != null && System.currentTimeMillis() - lastTime < DATA_CACHE_OVER_TIME) {
            Observable.just(hotPairCache)
        } else {
            ApiManager.build(context).getService(PairApiService::class.java)
                ?.getHotPair()
                ?.flatMap { result: HttpRequestResultDataList<String?>? ->
                    if (result != null && result.code == HttpRequestResult.SUCCESS) {
                        setLastGetTime(HOT_PAIR, System.currentTimeMillis())
                        hotPairCache = result
                    }
                    Observable.just(result)
                }
                ?.compose(RxJavaHelper.observeOnMainThread())
        }
    }

    /**
     * 热门交易对
     */
    fun getHotPair(context: Context?, callback: Callback<HttpRequestResultDataList<String?>?>?) {
        if (context == null || callback == null) {
            return
        }
        val lastTime = getLastGetTime(HOT_PAIR)
        if (hotPairCache != null && hotPairCache!!.code != null && hotPairCache!!.code == HttpRequestResult.SUCCESS && lastTime != null && System.currentTimeMillis() - lastTime < DATA_CACHE_OVER_TIME) {
            callback.callback(hotPairCache)
        } else {
            getHotPairAndCache(context, callback)
        }
    }

    fun getHotPairAndCache(
        context: Context?,
        callback: Callback<HttpRequestResultDataList<String?>?>?
    ) {
        if (context == null) {
            return
        }
        getHotPairFromServer(context, object : Callback<HttpRequestResultDataList<String?>?>() {
            override fun error(type: Int, error: Any) {
                callback?.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultDataList<String?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    setLastGetTime(HOT_PAIR, System.currentTimeMillis())
                    hotPairCache = returnData
                }
                callback?.callback(returnData)
            }
        })
    }

    private fun getHotPairFromServer(
        context: Context?,
        callback: Callback<HttpRequestResultDataList<String?>?>
    ) {
        if (context == null) {
            return
        }
        ApiManager.build(context).getService(PairApiService::class.java)
            ?.getHotPair()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    /**
     * 收藏交易对
     */
    fun pairCollect(
        context: Context?,
        pair: String?,
        callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URl_UC).getService(PairApiService::class.java)
            ?.pairCollect(pair)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    /**
     * 取消收藏交易对
     */
    fun pairCollectCancel(
        context: Context?,
        pair: String?,
        callback: Callback<HttpRequestResultString?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URl_UC).getService(PairApiService::class.java)
            ?.pairCollectCancel(pair)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    /**
     * 查询收藏交易对
     */
    fun getCollectPairs(
        context: Context?,
        callback: Callback<HttpRequestResultDataList<String?>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context, UrlConfig.ApiType.URl_UC).getService(PairApiService::class.java)
            ?.getCollectPairs()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    /**
     * 查询币种序号
     */
    fun getCoinOrders(context: Context?, callback: Callback<HttpRequestResultData<CoinOrder?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(PairApiService::class.java)
            ?.getCoinOrders()
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, false, callback))
    }
}