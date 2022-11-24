package com.black.frying.viewmodel

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.TextUtils
import android.util.Log
import com.black.base.api.C2CApiServiceHelper
import com.black.base.api.CommonApiServiceHelper
import com.black.base.api.PairApiService
import com.black.base.lib.FryingSingleToast
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.SuccessObserver
import com.black.base.model.c2c.C2CPrice
import com.black.base.model.clutter.Kline
import com.black.base.model.socket.*
import com.black.base.util.*
import com.black.base.viewmodel.BaseViewModel
import com.black.base.widget.AnalyticChart
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import io.reactivex.Notification
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import java.util.*

class KLineFullViewModel(context: Context) : BaseViewModel<Any>(context) {

    private var currentPairStatus: PairStatus = PairStatus()
    private var coinType: String? = null
    private var pairSet: String? = null
    private var nullAmount: String? = null

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null

    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = createPairObserver()
    private var pairDealObserver:Observer<PairDeal?>? = createPairDealObserver()
    private var kLineObserver: Observer<KLineItemListPair?>? = createKLineObserver()
    private var kLineAddObserver: Observer<KLineItemPair?>? = createKLineAddObserver()
    private var kLineAddMoreObserver: Observer<KLineItemListPair?>? = createKLineAddMoreObserver()
    private var kLineId: String? = null
    private var onKLineAllEnd = false

    private var onKLineFullListener: OnKLineFullListener? = null

    constructor(context: Context, pair: String?, onKLineFullListener: OnKLineFullListener) : this(context) {
        this.onKLineFullListener = onKLineFullListener
        this.nullAmount = context.resources.getString(R.string.number_default)
        currentPairStatus.pair = pair
    }

    override fun onResume() {
        super.onResume()
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
        initPairStatus()
        if (pairObserver == null) {
            pairObserver = createPairObserver()
        }
        if(pairDealObserver == null){
            pairDealObserver = createPairDealObserver()
        }
        SocketDataContainer.subscribePairDealObservable(pairDealObserver)

        if (kLineObserver == null) {
            kLineObserver = createKLineObserver()
        }
        SocketDataContainer.subscribeKLineObservable(kLineObserver)
        if (kLineAddObserver == null) {
            kLineAddObserver = createKLineAddObserver()
        }
        SocketDataContainer.subscribeKLineAddObservable(kLineAddObserver)
        if (kLineAddMoreObserver == null) {
            kLineAddMoreObserver = createKLineAddMoreObserver()
        }
        SocketDataContainer.subscribeKLineAddMoreObservable(kLineAddMoreObserver)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_QUOTA_OPEN)

    }

    override fun onStop() {
        super.onStop()
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_QUOTA_CLOSE)
//        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_KTAB_CLOSE)
        if (socketHandler != null) {
            socketHandler?.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
        }
        if (pairObserver != null) {
            SocketDataContainer.removePairObservable(pairObserver)
        }
        if(pairDealObserver != null){
            SocketDataContainer.removePairDealObservable(pairDealObserver)
        }
        if (kLineObserver != null) {
            SocketDataContainer.removeKLineObservable(kLineObserver)
        }
        if (kLineAddObserver != null) {
            SocketDataContainer.removeKLineAddObservable(kLineAddObserver)
        }
        if (kLineAddMoreObserver != null) {
            SocketDataContainer.removeKLineAddMoreObservable(kLineAddMoreObserver)
        }
    }

    private fun createPairDealObserver(): Observer<PairDeal?> {
        return object : SuccessObserver<PairDeal?>() {
            override fun onSuccess(value: PairDeal?) {
                onKLineFullListener?.run {
                    if (value != null && currentPairStatus.pair != null) {
                        var quotationDealNew = QuotationDealNew()
                        quotationDealNew.pair = value.s
                        quotationDealNew.p = value.p
                        quotationDealNew.a = value?.a?.toDouble()!!
                        quotationDealNew.d = value.m
                        quotationDealNew.t = value.t!!
                        var list = ArrayList<QuotationDealNew?>()
                        list.add(quotationDealNew)
                        SocketDataContainer.updateQuotationDealNewData(socketHandler,currentPairStatus.pair,list,false)
                    }
                }
            }
        }
    }

    private fun createPairObserver(): Observer<ArrayList<PairStatus?>?>? {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
            override fun onSuccess(value: ArrayList<PairStatus?>?) {
                onKLineFullListener?.run {
                    if (value != null && currentPairStatus.pair != null && onKLineFullListener != null) {
                        val pairStatus = CommonUtil.findItemFromList(value, currentPairStatus.pair)
                        refreshPairStatus(pairStatus!!)
                    }
                }
            }
        }
    }

    private fun createKLineObserver(): Observer<KLineItemListPair?>? {
        return object : SuccessObserver<KLineItemListPair?>() {
            override fun onSuccess(value: KLineItemListPair?) {
                if (value == null) {
                    return
                }
                onKLineFullListener?.run {
                    if (TextUtils.equals(currentPairStatus.pair, value.pair) && TextUtils.equals(kLineId, value.kLineId) && value.items != null) {
                        FryingUtil.observableWithHandler(socketHandler, value.items!!)
                            ?.subscribe {
                                onKLineAllEnd = true
                                onKLineDataAll(it)
                            }
                    }
                }
            }
        }
    }

    private fun createKLineAddObserver(): Observer<KLineItemPair?>? {
        return object : SuccessObserver<KLineItemPair?>() {
            override fun onSuccess(value: KLineItemPair?) {
                if (value == null) {
                    return
                }
                onKLineFullListener?.run {
                    if (TextUtils.equals(currentPairStatus.pair, value.pair) && TextUtils.equals(kLineId, value.kLineId) && value.item != null) {
                        FryingUtil.observableWithHandler(socketHandler, value.item!!)
                                ?.subscribe {
                                    onKLineDataAdd(it)
                                }
                    }
                }
            }
        }
    }

    private fun createKLineAddMoreObserver(): Observer<KLineItemListPair?>? {
        return object : SuccessObserver<KLineItemListPair?>() {
            override fun onSuccess(value: KLineItemListPair?) {
                if (value == null) {
                    return
                }
                onKLineFullListener?.run {
                    val returnKLineIdPair = value.kLineId ?: return
                    val arr = returnKLineIdPair.split("_").toTypedArray()
                    if (arr.size != 2) {
                        return
                    }
                    val returnKlineId = arr[0]
                    val kLinePage = arr[1]
                    val kLinePageInt = CommonUtil.parseInt(kLinePage) ?: return
                    if (TextUtils.equals(currentPairStatus.pair, value.pair) && TextUtils.equals(returnKlineId, kLineId) && value.items != null) {
                        FryingUtil.observableWithHandler(socketHandler, value.items!!)
                                ?.subscribe {
                                    onKLineDataMore(kLinePageInt, it)
                                }
                    }
                }
            }
        }
    }

    fun getCurrentPair(): String? {
        return currentPairStatus.pair
    }


    fun getKLineDataFiex(timeStep:String?,kLinePage: Int,startTime:Long,endTime:Long){
        currentPairStatus.pair?.let {
            if (timeStep != null) {
                CommonApiServiceHelper.getHistoryKline(
                    context,
                    it,
                    timeStep,
                    1500,
                    true,
                    startTime,
                    endTime,
                    object : Callback<HttpRequestResultDataList<Kline?>?>() {
                        override fun error(type: Int, error: Any) {
                            if(kLinePage != 0){
                                onKLineFullListener!!.onKLineLoadingMore()
                            }
                        }
                        override fun callback(returnData: HttpRequestResultDataList<Kline?>?) {
                            if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                                var items = returnData.data!!
                                onKLineAllEnd = true
                                if(items != null && items.size>0){
                                    var dataItem = ArrayList<KLineItem?>()
                                    for (i in items.indices){
                                        var klineItem = KLineItem()
                                        var temp = items[i]
                                        klineItem.a = temp?.a?.toDouble()!!
                                        klineItem.c = temp?.c?.toDouble()!!
                                        klineItem.h = temp?.h?.toDouble()!!
                                        klineItem.l = temp?.l?.toDouble()!!
                                        klineItem.o = temp?.o?.toDouble()!!
                                        klineItem.t = temp?.t?.div(1000)
                                        klineItem.v = temp?.v?.toDouble()!!
                                        dataItem?.add(klineItem)
                                    }
                                    if(kLinePage == 0){
                                        onKLineFullListener!!.onKLineDataAll(dataItem)
                                    }else{
                                        onKLineFullListener!!.onKLineDataMore(kLinePage, dataItem)
                                    }
                                }
                            }else{
                                if(kLinePage != 0){
                                    onKLineFullListener!!.onKLineLoadingMore()
                                }
                            }
                        }
                    })
            }
        }
    }

    private fun initPairStatus() {
        if (TextUtils.isEmpty(currentPairStatus.pair)) {
            currentPairStatus.pair = CookieUtil.getCurrentPair(context)
        }
        onKLineFullListener?.onPairChanged(currentPairStatus.pair)
        onKLineFullListener?.onPairStatusDataChanged(if (currentPairStatus.pair == null) null else currentPairStatus)
        currentPairStatus.pair?.let {
            val arr: Array<String> = it.split("_").toTypedArray()
            if (arr.size > 1) {
                coinType = arr[0]
                pairSet = arr[1]
            }
//            getTradePairInfo()
            getPairStatus()
        }
    }

    //获取当前交易对深度
    fun getTradePairInfo() {
        SocketDataContainer.getPairStatus(context, ConstData.PairStatusType.SPOT,currentPairStatus.pair, object : Callback<PairStatus?>() {
            override fun error(type: Int, error: Any) {
                FryingUtil.showToast(context, context.getString(R.string.pair_error), FryingSingleToast.ERROR)
            }

            override fun callback(returnData: PairStatus?) {
                if (returnData == null) {
                    FryingUtil.showToast(context, context.getString(R.string.pair_error), FryingSingleToast.ERROR)
                } else {
                    currentPairStatus = returnData
                    onKLineFullListener?.run {
                        onPairStatusPrecision(currentPairStatus.precision)
                    }
                }
            }
        })
    }

    fun changePairStatus(pairStatus: PairStatus) {
        CookieUtil.setCurrentPair(context, pairStatus.pair)
        currentPairStatus.pair = pairStatus.pair
        onKLineFullListener?.onPairChanged(currentPairStatus.pair)
        currentPairStatus.pair?.let {
            val arr: Array<String> = it.split("_").toTypedArray()
            if (arr.size > 1) {
                coinType = arr[0]
                pairSet = arr[1]
            }
            getTradePairInfo()
            getPairStatus()
        }
    }

    //根据当前交易对状态，刷新所有数据
    private fun getPairStatus() {
        SocketDataContainer.getPairStatusObservable(context,ConstData.PairStatusType.SPOT, currentPairStatus.pair)?.run {
            subscribeOn(AndroidSchedulers.from(socketHandler?.looper))
                    .observeOn(AndroidSchedulers.from(socketHandler?.looper))
                    .subscribe {
                        refreshPairStatus(it!!)
                        onKLineFullListener?.run {
                            onPairStatusPrecision(currentPairStatus.precision)
                        }
                    }
                    .run { }
        }
    }

    private fun refreshPairStatus(pairStatus: PairStatus) {
        if (pairStatus.pair == null) {
            onKLineFullListener?.onPairStatusDataChanged(null)
        } else {
            onKLineFullListener?.run {
                Observable.create { it: ObservableEmitter<PairStatus> ->
                    currentPairStatus.currentPrice = pairStatus.currentPrice
                    currentPairStatus.firstPriceToday = pairStatus.firstPriceToday
                    currentPairStatus.lastPrice = pairStatus.lastPrice
                    currentPairStatus.maxPrice = pairStatus.maxPrice
                    currentPairStatus.minPrice = pairStatus.minPrice
                    currentPairStatus.priceChangeSinceToday = pairStatus.priceChangeSinceToday
                    currentPairStatus.statDate = pairStatus.statDate
                    currentPairStatus.totalAmount = pairStatus.totalAmount
                    if (pairStatus.pair != null) {
                        currentPairStatus.pair = pairStatus.pair
                    }
                    it.onNext(currentPairStatus)
                    it.onComplete()
                }
                        .flatMap { pair ->
                            C2CApiServiceHelper.getC2CPrice(context)
                                    ?.materialize()
                                    ?.flatMap(Function<Notification<C2CPrice?>, Observable<PairStatus>> { notify: Notification<C2CPrice?> ->
                                        if (notify.isOnNext) {
                                            pair.setCurrentPriceCNY(SocketDataContainer.computeCoinPriceCNY(pairStatus, notify.value), nullAmount)
                                            return@Function Observable.just(pair)
                                        }
                                        if (notify.isOnError) {
                                            return@Function Observable.just(pair)
                                        }
                                        Observable.empty()
                                    })
                        }
                        .subscribeOn(AndroidSchedulers.from(socketHandler?.looper))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe { pair ->
                            onKLineFullListener?.onPairStatusDataChanged(pair)
                        }
                        .run { }
            }
        }
    }

    fun listenKLineData(analyticChart: AnalyticChart) {
        kLineId = System.nanoTime().toString()
        onKLineAllEnd = false
        val bundle = Bundle()
        bundle.putString("timeStep", analyticChart.getTimeStepRequestStr())
        bundle.putString("kLineId", kLineId)
        //进行延时请求，数据无法及时返回
        socketHandler?.run {
            post {
                SocketUtil.sendSocketCommandBroadcast(context!!, SocketUtil.COMMAND_KTAB_CHANGED, bundle)
            }
        }
//        CommonUtil.postHandleTask(socketHandler!!, {
//            SocketUtil.sendSocketCommandBroadcast(context!!, SocketUtil.COMMAND_KTAB_CHANGED, bundle)
//        }, 2000)
    }

    //K线加载更多数据
    fun listenKLineDataMore(page: Int) {
        val bundle = Bundle()
        bundle.putInt("kLinePage", page)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_K_LOAD_MORE, bundle)
    }

    fun finishListenKLine() {
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_KTAB_CLOSE)
    }

    interface OnKLineFullListener {
        fun onPairChanged(pair: String?)
        fun onPairStatusPrecision(precision: Int)
        fun onPairStatusDataChanged(pairStatus: PairStatus?)
        fun onKLineDataAll(items: ArrayList<KLineItem?>)
        fun onKLineDataAdd(item: KLineItem)
        fun onKLineDataMore(kLinePage: Int, items: ArrayList<KLineItem?>)
        fun onKLineLoadingMore()//判断是否正在加载更多
    }
}