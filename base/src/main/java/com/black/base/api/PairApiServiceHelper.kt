package com.black.base.api

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.clutter.HomeSymbolList
import com.black.base.model.clutter.HomeTickers
import com.black.base.model.clutter.HomeTickersKline
import com.black.base.model.socket.CoinOrder
import com.black.base.model.socket.PairStatus
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.CookieUtil
import com.black.base.util.RxJavaHelper
import com.black.base.util.SocketUtil
import com.black.base.util.UrlConfig
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import io.reactivex.Observable

object PairApiServiceHelper {
    private const val DATA_CACHE_OVER_TIME = 0.5 * 60 * 1000 //热门币种，请求缓存时间，20分钟
            .toLong()
    private const val C2C_PRICE = 1
    private const val TRADE_SET = 2
    private const val TRADE_PAIR = 3
    private const val HOT_PAIR = 4
    //上次拉取数据时间，根据类型分类
    private val lastGetTimeMap = SparseArray<Long>()
    private var tradeSets: ArrayList<String?>? = null
    var hotPairCache: HttpRequestResultDataList<String?>? = null

    private var symbolListData: ArrayList<HomeSymbolList?>? = null
    private var tickersData:ArrayList<HomeTickers?>? = null
    private var tickersKline:ArrayList<HomeTickersKline?>? = null

    //首页数据
    private var homePagePairData:ArrayList<PairStatus?>? = ArrayList()

    private fun getLastGetTime(type: Int): Long? {
        val lastGetTime = lastGetTimeMap[type]
        return lastGetTime ?: 0
    }

    private fun setLastGetTime(type: Int, time: Long) {
        lastGetTimeMap.put(type, time)
    }

    fun getHomePagePairData():ArrayList<PairStatus?>?{
        return homePagePairData
    }

    fun getTradeSetsLocal(context: Context?, isShowLoading: Boolean, callback: Callback<ArrayList<String?>?>?) {
        if (tradeSets != null && tradeSets!!.isNotEmpty() && System.currentTimeMillis() - (getLastGetTime(TRADE_SET)
                        ?: 0) < DATA_CACHE_OVER_TIME) {
            callback?.callback(tradeSets)
        } else {
            getTradeSets(context, isShowLoading, object : Callback<HttpRequestResultDataList<String?>?>() {
                override fun error(type: Int, error: Any) {
                    tradeSets = null
                    callback?.error(type, error)
                }

                override fun callback(returnData: HttpRequestResultDataList<String?>?) {
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
                        //将交易对保存到本地
                        callback?.callback(tradeSets)
                    }
                }
            })
        }
    }

    fun getTradeSets(context: Context?, isShowLoading: Boolean, callback: Callback<HttpRequestResultDataList<String?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(PairApiService::class.java)
                ?.getTradeSets()
                ?.flatMap {
                    val setList = ArrayList<String?>()
                    if (it.code == HttpRequestResult.SUCCESS && it.data != null) {
                        for (tradeSet in it.data!!) {
                            if (tradeSet?.coinType != null) {
                                setList.add(tradeSet.coinType)
                            }
                        }
                    }
                    val result = HttpRequestResultDataList<String?>()
                    result.code = it.code
                    result.msg = it.msg
                    result.message = it.message
                    result.data = setList
                    Observable.just(result)
                }
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    fun getTradePairInfo(context: Context?, pair: String?, callback: Callback<HttpRequestResultDataList<PairStatus?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(PairApiService::class.java)
                ?.getTradePairInfo(pair)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
    }

    fun getTradePairList(context: Context?, callback: Callback<HttpRequestResultDataList<String?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(PairApiService::class.java)
                ?.getOrderedPairs(null)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, false, callback))
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
     * 获取首页tickers
     */
    fun getHomeTickers(context: Context?): Observable<HttpRequestResultDataList<HomeTickers?>?>? {
        if (context == null) {
            return null
        }
        return ApiManager.build(context,false,UrlConfig.ApiType.URL_PRO).getService(PairApiService::class.java)
                ?.getHomeTickersList()
                ?.flatMap { result: HttpRequestResultDataList<HomeTickers?>? ->
                    var data = result?.data!!
                    for(i in data.indices){
                        for(j in homePagePairData!!.indices){
                            if(homePagePairData!![j]?.pair == data[i]?.s){
                                //现价
                                homePagePairData!![j]?.currentPrice = data[i]?.c?.toDoubleOrNull()!!//这个地方小数点位数可能会有问题
                                //交易量
                                homePagePairData!![j]?.tradeVolume = data[i]?.v
                                //涨跌幅
                                homePagePairData!![j]?.priceChangeSinceToday = data[i]?.r?.toDoubleOrNull()!!
                            }
                        }
                    }
                    Observable.just(result)
                }
                ?.compose(RxJavaHelper.observeOnMainThread())
    }

    /**
     * 获取首页symbolList
     */
    fun getSymbolList(context: Context?): Observable<HttpRequestResultDataList<HomeSymbolList?>?>? {
        if (context == null) {
            return null
        }
        return ApiManager.build(context,false,UrlConfig.ApiType.URL_PRO).getService(PairApiService::class.java)
            ?.getHomeSymbolList()
            ?.flatMap { result: HttpRequestResultDataList<HomeSymbolList?>? ->
                var data = result?.data!!
                homePagePairData?.clear()//清除数据
                for(i in data.indices){
                    var pair = data[i]?.symbol//交易对名
                    var pairStatus:PairStatus? = PairStatus()
                    pairStatus?.pair = pair
                    pairStatus?.hot = data[i]?.hot
                    homePagePairData?.add(pairStatus)
                }
                Observable.just(result)
            }
            ?.compose(RxJavaHelper.observeOnMainThread())
    }

    /**
     * 获取首页kline
     */
    fun getHomeKline(context: Context?): Observable<HttpRequestResultDataList<HomeTickersKline?>?>? {
        if (context == null) {
            return null
        }
        return ApiManager.build(context,false,UrlConfig.ApiType.URL_PRO).getService(PairApiService::class.java)
            ?.getHomeKLine()
            ?.flatMap { result: HttpRequestResultDataList<HomeTickersKline?>? ->
                var data = result?.data!!
                for(i in data.indices){
                    for(j in homePagePairData!!.indices){
                        if(homePagePairData!![j]?.pair == data[i]?.s){
                            //赋k线数值
                            homePagePairData!![j]?.kLineDate = data[i]
                        }
                    }
                }
                Observable.just(result)
            }
            ?.compose(RxJavaHelper.observeOnMainThread())
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

    fun getHotPairAndCache(context: Context?, callback: Callback<HttpRequestResultDataList<String?>?>?) {
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

    private fun getHotPairFromServer(context: Context?, callback: Callback<HttpRequestResultDataList<String?>?>) {
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
    fun pairCollect(context: Context?, pair: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(PairApiService::class.java)
                ?.pairCollect(pair)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    /**
     * 取消收藏交易对
     */
    fun pairCollectCancel(context: Context?, pair: String?, callback: Callback<HttpRequestResultString?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(PairApiService::class.java)
                ?.pairCollectCancel(pair)
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(context, true, callback))
    }

    /**
     * 查询收藏交易对
     */
    fun getCollectPairs(context: Context?, callback: Callback<HttpRequestResultDataList<String?>?>?) {
        if (context == null || callback == null) {
            return
        }
        ApiManager.build(context).getService(PairApiService::class.java)
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

    /**
     * 获取交易对全部信息，包括 ST 默认排序 交易深度
     */
    fun getFullPairStatusObservable(context: Context?): Observable<ArrayList<PairStatus>?>? {
        if (context == null) {
            return null
        }
        val pairApiService = ApiManager.build(context).getService(PairApiService::class.java)
        return pairApiService!!.getTradePairInfo(null)
                ?.flatMap { pairInfoData: HttpRequestResultDataList<PairStatus?>? ->
                    val pairStatuses = ArrayList<PairStatus>()
                    val allPair = ArrayList<String?>()
                    val allLeverPair = ArrayList<String?>()
                    if (pairInfoData!!.data != null && pairInfoData.code == HttpRequestResult.SUCCESS) {
                        for (i in pairInfoData.data!!.indices) {
                            val pairStatus = pairInfoData.data!![i]
                            if (pairStatus != null) {
                                pairStatus.pair = pairStatus.pairName
                                pairStatus.order_no = i
                                var maxPrecision = CommonUtil.getMax(pairStatus.supportingPrecisionList)
                                maxPrecision = if (maxPrecision == null || maxPrecision == 0) 6 else maxPrecision
                                pairStatus.precision = maxPrecision
                                pairStatus.isHighRisk = if (pairStatus.isHighRisk == null) false else pairStatus.isHighRisk
                                pairStatuses.add(pairStatus)
                                allPair.add(pairStatus.pair)
                                if (pairStatus.isLever) {
                                    allLeverPair.add(pairStatus.pair)
                                }
                            }
                        }
                    }
                    var currentPair = CookieUtil.getCurrentPair(context)
                    if (allPair.isNotEmpty() && (TextUtils.isEmpty(currentPair) || !allPair.contains(currentPair))) {
                        currentPair = CommonUtil.getItemFromList(allPair, 0)
                        if (currentPair != null) {
                            CookieUtil.setCurrentPair(context, currentPair)
                            SocketUtil.notifyPairChanged(context)
                        }
                    }
                    val currentLeverPair = CookieUtil.getCurrentPairLever(context)
                    if (allLeverPair.isNotEmpty() && (TextUtils.isEmpty(currentLeverPair) || !allLeverPair.contains(currentLeverPair))) {
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
        observable?.compose(RxJavaHelper.observeOnMainThread())?.subscribe(HttpCallbackSimple(context, false, callback))
    }
}