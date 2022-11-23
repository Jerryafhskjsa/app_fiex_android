package com.black.base.api

import android.content.Context
import android.text.TextUtils
import android.util.SparseArray
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.clutter.HomeSymbolList
import com.black.base.model.future.*
import com.black.base.model.socket.PairStatus
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil
import io.reactivex.Observable

object FutureApiServiceHelperWrapper {
    private const val DATA_CACHE_OVER_TIME = 20 * 60 * 1000 //20分钟
    private const val FUTURE_SYMBOL_U_LIST = 0
    var futureSymbolPairList: ArrayList<PairStatus>? = null
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
            getFutureSymbolPairListCallBack(context, isShowLoading, callback)
        }
    }

    /**
     * 获取u本位合约币种列表
     */
    fun getFutureSymbolPairListCallBack(
        context: Context?,
        isShowLoading: Boolean,
        callback: Callback<ArrayList<PairStatus>?>?
    ) {
        if (context == null || callback == null) {
            return
        }
        val observable = getFutureSymbolPairListObservable(context)
        observable?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(context, isShowLoading, callback))
    }

    /**
     * 获取u本位合约币种列表
     */
    fun getFutureSymbolPairListObservable(context: Context?): Observable<ArrayList<PairStatus>?>? {
        if (context == null) {
            return null
        }
        val futureApiService = ApiManager.build(context, UrlConfig.ApiType.URL_FUT_F)
            .getService(FutureApiService::class.java)
        return futureApiService?.getSymbolList()
            ?.flatMap { pairInfoData: HttpRequestResultBean<ArrayList<SymbolBean>?>? ->
                val pairStatuses = ArrayList<PairStatus>()
                val allPair = ArrayList<String?>()
                var resultDataList = pairInfoData!!.result
                if (resultDataList != null && pairInfoData.code == HttpRequestResult.SUCCESS) {
                    setLastGetTime(FUTURE_SYMBOL_U_LIST, System.currentTimeMillis())
                    var pairStatusList: ArrayList<PairStatus>? = ArrayList()
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
                        pairStatusList?.add(pairStatus!!)
                        allPair.add(pairStatus?.pair)
                        futureSymbolPairList = pairStatusList
                    }
                }
                var currentPair = CookieUtil.getCurrentFutureUPair(context)
                if (allPair.isNotEmpty()
                    && (TextUtils.isEmpty(currentPair)
                            || !allPair.contains(currentPair))
                ) {
                    currentPair = CommonUtil.getItemFromList(allPair, 0)
                    if (currentPair != null) {
                        CookieUtil.setCurrentFutureUPair(context, currentPair)
                        SocketUtil.notifyFutureUPairChanged(context)
                    }
                }
                Observable.just(pairStatuses)
            }
    }

}