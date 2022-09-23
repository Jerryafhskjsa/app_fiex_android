package com.black.frying.viewmodel

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.TextUtils
import android.util.Pair
import com.black.base.api.*
import com.black.base.manager.ApiManager
import com.black.base.model.*
import com.black.base.model.c2c.C2CPrice
import com.black.base.model.clutter.Kline
import com.black.base.model.socket.*
import com.black.base.model.wallet.CoinInfo
import com.black.base.service.DearPairService
import com.black.base.util.*
import com.black.base.viewmodel.BaseViewModel
import com.black.base.widget.AnalyticChart
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CallbackObject
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import io.reactivex.*
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

//K线竖屏数据模型
class QuotationDetailViewModel(context: Context, private val pair: String?, private val onKLineModelListener: OnKLineModelListener?) : BaseViewModel<Any?>(context) {
    private val currentPairStatus: PairStatus = PairStatus()
    private var coinType: String? = null
    private var pairSet: String? = null
    private var nullAmount: String? = null

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null

    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = createPairObserver()
    private var orderObserver: Observer<Pair<String?, TradeOrderPairList?>>? = createOrderObserver()
    private var dealObserver: Observer<Pair<String?, ArrayList<TradeOrder?>?>>? = createDealObserver()

    private var kLineObserver: Observer<KLineItemListPair?>? = createKLineObserver()
    private var kLineAddObserver: Observer<KLineItemPair?>? = createKLineAddObserver()
    private var kLineAddMoreObserver: Observer<KLineItemListPair?>? = createKLineAddMoreObserver()

    private var kLineId: String? = null
    private var onKLineAllEnd = false
    var gotoLarge = false

    init {
        currentPairStatus.pair = (pair)
        this.nullAmount = context?.resources?.getString(R.string.number_default)
    }

    override fun onResume() {
        super.onResume()
        gotoLarge = false
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
        initPairStatus()
        if (orderObserver == null) {
            orderObserver = createOrderObserver()
        }
        SocketDataContainer.subscribeOrderObservable(orderObserver)
        if (pairObserver == null) {
            pairObserver = createPairObserver()
        }
        SocketDataContainer.subscribePairObservable(pairObserver)
        if (dealObserver == null) {
            dealObserver = createDealObserver()
        }
        SocketDataContainer.subscribeDealObservable(dealObserver)

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

        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_ORDER_RELOAD)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_QUOTA_OPEN)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_ORDER_OPEN)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_DEAL_OPEN)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_QUOTA_CLOSE)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_ORDER_CLOSE)
        if (!gotoLarge) {
            SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_KTAB_CLOSE)
        }
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_DEAL_CLOSE)
        if (socketHandler != null) {
            socketHandler?.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
        }
        if (orderObserver != null) {
            SocketDataContainer.removeOrderObservable(orderObserver)
        }
        if (pairObserver != null) {
            SocketDataContainer.removePairObservable(pairObserver)
        }
        if (dealObserver != null) {
            SocketDataContainer.removeDealObservable(dealObserver)
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

    private fun initPairStatus() {
        if (TextUtils.isEmpty(currentPairStatus.pair)) {
            currentPairStatus.pair = CookieUtil.getCurrentPair(context)
        }
        onKLineModelListener?.onPairChanged(currentPairStatus.pair)
        onKLineModelListener?.onPairStatusDataChanged(if (currentPairStatus.pair == null) null else currentPairStatus)
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
        SocketDataContainer.getPairStatusObservable(context!!, currentPairStatus.pair)?.run {
            subscribeOn(AndroidSchedulers.from(socketHandler?.looper))
                    .observeOn(AndroidSchedulers.from(socketHandler?.looper))
                    .subscribe {
                        refreshPairStatus(it)
                    }
                    .run { }
        }

    }

    private fun refreshPairStatus(pairStatus: PairStatus) {
        if (pairStatus.pair == null) {
            onKLineModelListener?.onPairStatusDataChanged(null)
        } else {
            onKLineModelListener?.run {
                Observable.create { it: ObservableEmitter<PairStatus> ->
                    currentPairStatus.currentPrice = (pairStatus.currentPrice)
                    currentPairStatus.firstPriceToday = pairStatus.firstPriceToday
                    currentPairStatus.lastPrice = pairStatus.lastPrice
                    currentPairStatus.maxPrice = (pairStatus.maxPrice)
                    currentPairStatus.minPrice = (pairStatus.minPrice)
                    currentPairStatus.priceChangeSinceToday = (pairStatus.priceChangeSinceToday)
                    currentPairStatus.statDate = pairStatus.statDate
                    currentPairStatus.totalAmount = (pairStatus.totalAmount)
                    if (pairStatus.pair != null) {
                        currentPairStatus.pair = (pairStatus.pair)
                    }
                    it.onNext(currentPairStatus)
                    it.onComplete()
                }
                        .flatMap { pair ->
                            C2CApiServiceHelper.getC2CPrice(context!!)
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
                            onPairStatusDataChanged(pair)
                        }
            }
        }
    }

    fun changePairStatus(pairStatus: PairStatus) {
        CookieUtil.setCurrentPair(context!!, pairStatus.pair)
        currentPairStatus.pair = pairStatus.pair
        onKLineModelListener?.onPairChanged(currentPairStatus.pair)
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

    private fun createPairObserver(): Observer<ArrayList<PairStatus?>?> {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
            override fun onSuccess(value: ArrayList<PairStatus?>?) {
                onKLineModelListener?.run {
                    if (value != null && currentPairStatus.pair != null) {
                        CommonUtil.findItemFromList(value, currentPairStatus.pair)?.let{
                            refreshPairStatus(it)
                        }
                    }
                }
            }
        }
    }

    private fun createOrderObserver(): Observer<Pair<String?, TradeOrderPairList?>> {
        return object : SuccessObserver<Pair<String?, TradeOrderPairList?>>() {
            override fun onSuccess(value: Pair<String?, TradeOrderPairList?>) {
                onKLineModelListener?.run {
                    if (TextUtils.equals(currentPairStatus.pair, value.first) && value.second != null) {
                        sortTradeOrder(value.second)
                    }
                }
            }
        }
    }

    private fun createDealObserver(): Observer<Pair<String?, ArrayList<TradeOrder?>?>> {
        return object : SuccessObserver<Pair<String?, ArrayList<TradeOrder?>?>>() {
            override fun onSuccess(value: Pair<String?, ArrayList<TradeOrder?>?>) {
                onKLineModelListener?.let {
                    if (TextUtils.equals(currentPairStatus.pair, value.first) && value.second != null) {
                        FryingUtil.observableWithHandler(socketHandler, value.second)
                                ?.subscribe {
                                    onKLineModelListener.onDeal(it)
                                }
                    }
                }
            }
        }
    }

    private fun createKLineObserver(): Observer<KLineItemListPair?> {
        return object : SuccessObserver<KLineItemListPair?>() {
            override fun onSuccess(value: KLineItemListPair?) {
                if (value == null) {
                    return
                }
                onKLineModelListener?.run {
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

    private fun createKLineAddObserver(): Observer<KLineItemPair?> {
        return object : SuccessObserver<KLineItemPair?>() {
            override fun onSuccess(value: KLineItemPair?) {
                if (value == null) {
                    return
                }
                onKLineModelListener?.run {
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

    private fun createKLineAddMoreObserver(): Observer<KLineItemListPair?> {
        return object : SuccessObserver<KLineItemListPair?>() {
            override fun onSuccess(value: KLineItemListPair?) {
                value ?: return
                onKLineModelListener?.run {
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

    fun listenKLineData(timeStep: AnalyticChart.TimeStep) {
        kLineId = System.nanoTime().toString()
        onKLineAllEnd = false
        val bundle = Bundle()
        bundle.putString("timeStep", timeStep.apiText)
        bundle.putLong("timeStepSecond", timeStep.value)
        bundle.putString("kLineId", kLineId)
        //进行延时请求，数据无法及时返回
        socketHandler?.run {
            post {
                SocketUtil.sendSocketCommandBroadcast(context!!, SocketUtil.COMMAND_KTAB_CHANGED, bundle)
            }
        }
    }

    fun listenKLineDataResume() {
        SocketUtil.sendSocketCommandBroadcast(context!!, SocketUtil.COMMAND_KTAB_CHANGED)
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

    //获取挂单数据
    fun getAllOrder() {
        if (onKLineModelListener == null) {
            return
        }
        CommonUtil.postHandleTask(socketHandler) {
            SocketDataContainer.getOrderList(context, object : NormalCallback<TradeOrderPairList?>(context) {
                override fun callback(returnData: TradeOrderPairList?) {
                    sortTradeOrder(returnData)
                }
            })
        }
    }

    //获取当前成交数据
    fun getQuotationDeals() {
        onKLineModelListener?.run {
            CommonUtil.postHandleTask(socketHandler) {
                SocketDataContainer.getAllQuotationDeal(context, currentPairStatus.pair, object : Callback<ArrayList<TradeOrder?>?>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(dealOrders: ArrayList<TradeOrder?>?) {
                        FryingUtil.observableWithHandler(socketHandler, dealOrders)
                                ?.subscribe(object : SuccessObserver<ArrayList<TradeOrder?>?>() {
                                    override fun onSuccess(t: ArrayList<TradeOrder?>?) {
                                        onDeal(t)
                                    }
                                })
                    }
                })
            }
        }
    }

    fun getPairDescription() {
        if (onKLineModelListener == null) {
            return
        }
        val language = LanguageUtil.getLanguageSetting(context)
        onKLineModelListener.onPairDescription(ApiManager.build(context).getService(CommonApiService::class.java)
                ?.getPairDescription(coinType, if (language != null && language.languageCode == 4) "4" else "1")
                ?.compose(RxJavaHelper.observeOnMainThread()))
    }

    //获取当前交易对深度
    fun getTradePairInfo() {
        onKLineModelListener?.run {
            ApiManager.build(context).getService(PairApiService::class.java)
                    ?.getTradePairInfo(currentPairStatus.pair!!)
                    ?.materialize()
                    ?.flatMap { notify: Notification<HttpRequestResultDataList<PairStatus?>?> ->
                        if (notify.isOnComplete) {
                            Observable.empty<Int>()
                        } else {
                            var precision = 6
                            if (notify.isOnNext) {
                                val returnData: HttpRequestResultDataList<PairStatus?>? = notify.value
                                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                                    for (pairStatus in returnData.data!!) {
                                        if (TextUtils.equals(currentPairStatus.pair, pairStatus?.pairName)) {
                                            val supportingPrecisionList = pairStatus?.supportingPrecisionList
                                            var maxPrecision = CommonUtil.getMax(supportingPrecisionList)
                                            maxPrecision = if (maxPrecision == null || maxPrecision == 0) 6 else maxPrecision
                                            currentPairStatus.amountPrecision = if (pairStatus?.amountPrecision == null || pairStatus?.amountPrecision == 0) 5 else pairStatus?.amountPrecision
                                            currentPairStatus.precision = maxPrecision
                                            precision = maxPrecision
                                            break
                                        }
                                    }
                                }
                            }
                            Observable.just(precision)
                        }
                    }
                    ?.compose(RxJavaHelper.observeOnMainThread())
                    ?.subscribe {
                        onPairStatusPrecision(currentPairStatus.precision)
                        onPairStatusAmountPrecision(getAmountLength())
                        getAllOrder()
                        getQuotationDeals()
                    }
        }
    }

    fun getAmountLength(): Int {
        return currentPairStatus.amountPrecision ?: 5
    }

    fun getChatRoomId() {
        if (coinType == null) {
            return
        }
//        WalletApiServiceHelper.getCoinInfo(context, coinType, object : Callback<CoinInfo?>() {
//            override fun error(type: Int, error: Any) {
//                onKLineModelListener?.onChatRoomId(null)
//            }
//
//            override fun callback(coinInfo: CoinInfo?) {
//                onKLineModelListener?.onChatRoomId(coinInfo?.groupId)
//            }
//        })
    }

    fun checkDearPair() {
        if (onKLineModelListener == null) {
            return
        }
        DearPairService.isDearPair(context, socketHandler, currentPairStatus.pair, object : CallbackObject<Boolean?>() {
            override fun callback(returnData: Boolean?) {
                onKLineModelListener.onCheckDearPair(returnData)
            }
        })
    }

    fun getKLineDataFiex(){
        var kLineData = ArrayList<Kline?>()
        CommonApiServiceHelper.getHistoryKline(
            context,
            "BTC_USDT",
            "15m",
            1500,
            false,
            object : Callback<HttpRequestResultDataList<Kline?>?>() {
                override fun error(type: Int, error: Any) {
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
                            onKLineModelListener!!.onKLineDataAll(dataItem)
                        }
                    } else {

                    }
                }
            })
    }

    fun toggleDearPair(isDearPair: Boolean) {
        if (onKLineModelListener == null) {
            return
        }
        val callback: CallbackObject<Boolean> = object : CallbackObject<Boolean>() {
            override fun callback(returnData: Boolean) {
                onKLineModelListener.onToggleDearPair(returnData)
            }
        }
        if (isDearPair) {
            DearPairService.removeDearPair(context, socketHandler, currentPairStatus.pair, callback)
        } else {
            DearPairService.insertDearPair(context, socketHandler, currentPairStatus.pair, callback)
        }
    }

    fun checkIntoChatRoom() {
        if (onKLineModelListener == null) {
            return
        }
        onKLineModelListener.onCheckIntoChatRoom(ApiManager.build(context).getService(UserApiService::class.java)
                ?.checkChatEnable(coinType)
                ?.compose(RxJavaHelper.observeOnMainThread()))
    }

    private fun sortTradeOrder(orderPairList: TradeOrderPairList?) {
        Observable.just(orderPairList)
                .flatMap(Function<TradeOrderPairList?, ObservableSource<Void>> { orders: TradeOrderPairList? ->
                    var tradeOrders = orders
                    tradeOrders = tradeOrders ?: TradeOrderPairList()
                    var bidTradeOrderList = tradeOrders.bidOrderList ?: ArrayList()
                    var askTradeOrderList = tradeOrders.askOrderList ?: ArrayList()
                    val amountLength = getAmountLength()
                    if (bidTradeOrderList.isNotEmpty()) {
                        val bidTradeOrderListAfterFilter = ArrayList<TradeOrder?>()
                        for (tradeOrder in bidTradeOrderList) {
                            if (tradeOrder != null && tradeOrder.exchangeAmount > 0 && TextUtils.equals(currentPairStatus.pair, tradeOrder.pair)) {
                                var price = CommonUtil.parseBigDecimal(tradeOrder.priceString)
                                price = price?.setScale(currentPairStatus.precision, BigDecimal.ROUND_DOWN)
                                tradeOrder.formattedPrice = NumberUtil.formatNumberNoGroup(price, currentPairStatus.precision, currentPairStatus.precision)
                                tradeOrder.exchangeAmountFormat = NumberUtil.formatNumberNoGroup(tradeOrder.exchangeAmount, amountLength, amountLength)
                                bidTradeOrderListAfterFilter.add(tradeOrder)
                            }
                        }
                        bidTradeOrderList = bidTradeOrderListAfterFilter
                        Collections.sort(bidTradeOrderList, TradeOrder.COMPARATOR_DOWN)
                    }
                    if (askTradeOrderList.isNotEmpty()) {
                        val askTradeOrderListAfterFilter = ArrayList<TradeOrder?>()
                        for (tradeOrder in askTradeOrderList) {
                            if (tradeOrder != null && tradeOrder.exchangeAmount > 0 && TextUtils.equals(currentPairStatus.pair, tradeOrder.pair)) {
                                var price = CommonUtil.parseBigDecimal(tradeOrder.priceString)
                                price = price?.setScale(currentPairStatus.precision, BigDecimal.ROUND_UP)
                                tradeOrder.formattedPrice = NumberUtil.formatNumberNoGroup(price, currentPairStatus.precision, currentPairStatus.precision)
                                tradeOrder.exchangeAmountFormat = NumberUtil.formatNumberNoGroup(tradeOrder.exchangeAmount, amountLength, amountLength)
                                askTradeOrderListAfterFilter.add(tradeOrder)
                            }
                        }
                        askTradeOrderList = askTradeOrderListAfterFilter
                        Collections.sort(askTradeOrderList, TradeOrder.COMPARATOR_UP)
                    }
                    val firstBidTrad = CommonUtil.getItemFromList(bidTradeOrderList, 0)
                    val firstBidTradPrice = if (firstBidTrad == null) null else CommonUtil.parseBigDecimal(firstBidTrad.priceString)
                    val firstAskTrad = CommonUtil.getItemFromList(askTradeOrderList, 0)
                    val firstAskTradPrice = if (firstAskTrad == null) null else CommonUtil.parseBigDecimal(firstAskTrad.priceString)
                    //如果买单价格大于等于卖单价格，说明数据异常，重新订阅socket挂单
                    val isError = firstBidTradPrice != null && firstAskTradPrice != null && firstBidTradPrice.compareTo(firstAskTradPrice) >= 0
                    if (isError) {
                        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_ORDER_RELOAD)
                        return@Function Observable.empty()
                    }
                    FryingUtil.observableWithHandler(socketHandler, 1)
                            ?.subscribe(object : SuccessObserver<Int?>() {
                                override fun onSuccess(value: Int?) {
                                    onKLineModelListener?.onTradeOrder(currentPairStatus.currentPrice, bidTradeOrderList, askTradeOrderList)
                                }
                            })
                    Observable.empty()
                })
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe()
    }

    interface OnKLineModelListener {
        fun onPairChanged(pair: String?)
        fun onPairStatusPrecision(precision: Int)
        fun onPairStatusAmountPrecision(amountPrecision: Int)
        fun onPairStatusDataChanged(pairStatus: PairStatus?)

        fun onKLineDataAll(items: ArrayList<KLineItem?>)
        fun onKLineDataAdd(item: KLineItem)
        fun onKLineDataMore(kLinePage: Int, items: ArrayList<KLineItem?>)

        fun onTradeOrder(currentPrice: Double, bidOrderList: ArrayList<TradeOrder?>, askOrderList: ArrayList<TradeOrder?>)
        fun onDeal(dealData: ArrayList<TradeOrder?>?)
        fun onPairDescription(observable: Observable<HttpRequestResultData<PairDescription?>?>?)
        fun onChatRoomId(chatRoomId: String?)
        fun onCheckIntoChatRoom(observable: Observable<HttpRequestResultString>?)
        fun onCheckDearPair(isDearPair: Boolean?)
        fun onToggleDearPair(isSuccess: Boolean?)
    }

}
