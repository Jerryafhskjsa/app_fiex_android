package com.black.frying.viewmodel

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import com.black.base.api.*
import com.black.base.manager.ApiManager
import com.black.base.model.*
import com.black.base.model.socket.*
import com.black.base.model.trade.TradeOrderDepth
import com.black.base.model.trade.TradeOrderResult
import com.black.base.model.user.UserBalance
import com.black.base.model.user.UserBalanceWarpper
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletLever
import com.black.base.model.wallet.WalletLeverDetail
import com.black.base.net.HttpCallbackSimple
import com.black.base.service.DearPairService
import com.black.base.util.*
import com.black.base.viewmodel.BaseViewModel
import com.black.frying.service.socket.FiexSocketManager
import com.black.net.HttpRequestResult
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Observer
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import java.util.*
import kotlin.collections.ArrayList

class TransactionViewModel(
    context: Context,
    private val onTransactionModelListener: OnTransactionModelListener?
) : BaseViewModel<Any?>(context) {
    companion object {
        var tag = TransactionViewModel::class.java.simpleName
        const val LEVER_TYPE_COIN = "PHYSICAL" // 现货
        const val LEVER_TYPE_LEVER = "ISOLATED" //逐仓杠杆
    }


    private var tabType = ConstData.TAB_COIN
    private var currentPairStatus = PairStatus()
    private var currentOrderType: String? = null
    private var coinType: String? = null
    private var pairSet: String? = null
    var askMax = 5
    var bidMax = 5

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null

    //用户相关
    private var userBalanceObserver: Observer<UserBalance?>? = createUserBalanceObserver()
    private var userTradeOrderObserver: Observer<TradeOrderFiex?>? = createUserOrderObserver()

    //交易对相关
    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = createPairObserver()

    //深度变化
    private var orderObserver: Observer<Pair<String?, TradeOrderPairList?>>? = createOrderObserver()

    //行情涨跌幅以及价格变化
    private var pairQuotationObserver: Observer<PairQuotation?>? = createPairQuotationObserver()

    //当前交易对所有用户成交数据
    private var pairDealObserver: Observer<PairDeal?>? = createPairDealObserver()

    //用户相关
    private var userInfoObserver: Observer<String?>? = createUserInfoObserver()


    private var tradeOrderDepthPair: TradeOrderPairList? = null
    private var singleOrderDepthList: ArrayList<QuotationOrderNew?>? = null

    init {
        currentPairStatus.pair == (CookieUtil.getCurrentPair(context))
        initPairCoinSet()
    }

    override fun onResume() {
        super.onResume()
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread!!.start()
        socketHandler = Handler(handlerThread!!.looper)

        if (userBalanceObserver == null) {
            userBalanceObserver = createUserBalanceObserver()
        }
        SocketDataContainer.subscribeUserBalanceObservable(userBalanceObserver)

        if (userTradeOrderObserver == null) {
            userTradeOrderObserver = createUserOrderObserver()
        }
        SocketDataContainer.subscribeUserOrderObservable(userTradeOrderObserver)

        if (pairDealObserver == null) {
            pairDealObserver = createPairDealObserver()
        }
        SocketDataContainer.subscribePairDealObservable(pairDealObserver)

        if (orderObserver == null) {
            orderObserver = createOrderObserver()
        }
        SocketDataContainer.subscribeOrderObservable(orderObserver)

        if (pairObserver == null) {
            pairObserver = createPairObserver()
        }
        SocketDataContainer.subscribePairObservable(pairObserver)

        if (userInfoObserver == null) {
            userInfoObserver = createUserInfoObserver()
        }
        SocketDataContainer.subscribeUserInfoObservable(userInfoObserver)

        if (pairQuotationObserver != null) {
            pairQuotationObserver = createPairQuotationObserver()
        }
        SocketDataContainer.subscribePairQuotationObservable(pairQuotationObserver)

        val bundle = Bundle()
        bundle.putString(SocketUtil.WS_TYPE, SocketUtil.WS_USER)
        SocketUtil.sendSocketCommandBroadcast(
            context,
            SocketUtil.COMMAND_ADD_SOCKET_LISTENER,
            bundle
        )
        val bundle1 = Bundle()
        bundle1.putString(SocketUtil.WS_TYPE, SocketUtil.WS_TICKETS)
        SocketUtil.sendSocketCommandBroadcast(
            context,
            SocketUtil.COMMAND_ADD_SOCKET_LISTENER,
            bundle1
        )
        val bundle2 = Bundle()
        bundle2.putString(SocketUtil.WS_TYPE, SocketUtil.WS_SUBSTATUS)
        SocketUtil.sendSocketCommandBroadcast(
            context,
            SocketUtil.COMMAND_ADD_SOCKET_LISTENER,
            bundle2
        )
        initPairStatus()
        changePairSocket()
    }

    override fun onStop() {
        super.onStop()
        val bundle = Bundle()
        bundle.putString(SocketUtil.WS_TYPE, SocketUtil.WS_USER)
        SocketUtil.sendSocketCommandBroadcast(
            context,
            SocketUtil.COMMAND_REMOVE_SOCKET_LISTENER,
            bundle
        )
        val bundle1 = Bundle()
        bundle1.putString(SocketUtil.WS_TYPE, SocketUtil.WS_TICKETS)
        SocketUtil.sendSocketCommandBroadcast(
            context,
            SocketUtil.COMMAND_REMOVE_SOCKET_LISTENER,
            bundle1
        )
        val bundle2 = Bundle()
        bundle2.putString(SocketUtil.WS_TYPE, SocketUtil.WS_SUBSTATUS)
        SocketUtil.sendSocketCommandBroadcast(
            context,
            SocketUtil.COMMAND_REMOVE_SOCKET_LISTENER,
            bundle2
        )
        if (userBalanceObserver != null) {
            SocketDataContainer.removeUserBalanceObservable(userBalanceObserver)
        }

        if (userTradeOrderObserver != null) {
            SocketDataContainer.removeUserOrderObservable(userTradeOrderObserver)
        }

        if (orderObserver != null) {
            SocketDataContainer.removeOrderObservable(orderObserver)
        }
        if (pairObserver != null) {
            SocketDataContainer.removePairObservable(pairObserver)
        }
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }

        if (pairQuotationObserver != null) {
            SocketDataContainer.removePairQuotationObservable(pairQuotationObserver)
        }
        if (pairDealObserver != null) {
            SocketDataContainer.removePairDealObservable(pairDealObserver)
        }

        if (socketHandler != null) {
            socketHandler!!.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread!!.quit()
        }
    }

    private fun createUserBalanceObserver(): Observer<UserBalance?> {
        return object : SuccessObserver<UserBalance?>() {
            override fun onSuccess(value: UserBalance?) {
                onTransactionModelListener?.run {
                    if (value != null) {
                        onTransactionModelListener?.onUserBalanceChanged(value)
                    }
                }
            }
        }
    }

    private fun createUserOrderObserver(): Observer<TradeOrderFiex?> {
        return object : SuccessObserver<TradeOrderFiex?>() {
            override fun onSuccess(value: TradeOrderFiex?) {
                onTransactionModelListener?.run {
                    if (value != null) {
                        onTransactionModelListener?.onUserTradeOrderChanged(value)
                    }
                }
            }
        }
    }


    private fun createPairDealObserver(): Observer<PairDeal?> {
        return object : SuccessObserver<PairDeal?>() {
            override fun onSuccess(value: PairDeal?) {
                onTransactionModelListener?.run {
                    if (value != null && currentPairStatus.pair != null) {
                        onTransactionModelListener?.onPairDeal(value)
                    }
                }
            }
        }
    }

    private fun createUserInfoObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onTransactionModelListener?.onUserInfoChanged()
            }
        }
    }

    private fun createUserLeverObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onTransactionModelListener?.onUserInfoChanged()
            }
        }
    }

    private fun createPairObserver(): Observer<ArrayList<PairStatus?>?> {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
            override fun onSuccess(value: ArrayList<PairStatus?>?) {
                if (value == null || value.isEmpty()) {
                    return
                }
                if (onTransactionModelListener != null) {
                    if (currentPairStatus.pair == null) {
                        if (tabType == ConstData.TAB_COIN) {
                            val pairStatus = CommonUtil.getItemFromList(value, 0)
                            if (pairStatus != null) {
                                CookieUtil.setCurrentPair(context, pairStatus.pair)
                                initPairStatus()
                            }
                        } else {
                            for (i in 0..value.size) {
                                val pairStatus = value[i]
                                if (pairStatus?.isLever!!) {
                                    CookieUtil.setCurrentPairLever(context, pairStatus.pair)
                                    initPairStatus()
                                }
                            }
                        }
                    } else {
                        resetPairStatus(CommonUtil.findItemFromList(value, currentPairStatus.pair))
                    }
                }
            }
        }
    }

    private fun createOrderObserver(): Observer<Pair<String?, TradeOrderPairList?>> {
        return object : SuccessObserver<Pair<String?, TradeOrderPairList?>>() {
            override fun onSuccess(value: Pair<String?, TradeOrderPairList?>) {
                if (TextUtils.equals(
                        currentPairStatus.pair,
                        value.first
                    ) && value.second != null && onTransactionModelListener != null
                ) {
                    sortTradeOrder(value.first, value.second)
                }
            }
        }
    }

    private fun createPairQuotationObserver(): Observer<PairQuotation?>? {
        return object : SuccessObserver<PairQuotation?>() {
            override fun onSuccess(value: PairQuotation?) {
                if (value != null && TextUtils.equals(value.s, currentPairStatus.pair)) {
                    onTransactionModelListener.run {
                        onTransactionModelListener?.onPairQuotation(value)
                    }
                }
            }
        }
    }

    /**
     * btc/usdt-->btc,usdt
     */
    private fun initPairCoinSet() {
        currentPairStatus.pair?.run {
            val arr: Array<String>? = currentPairStatus.pair?.split("_")?.toTypedArray()
            if (arr != null && arr.size > 1) {
                coinType = arr[0]
                pairSet = arr[1]
            }
        }
    }

    fun getAllOrder() {
        CommonUtil.postHandleTask(socketHandler) {
            SocketDataContainer.getOrderList(context, object : Callback<TradeOrderPairList?>() {
                override fun error(type: Int, error: Any) {}
                override fun callback(returnData: TradeOrderPairList?) {
                    sortTradeOrder(currentPairStatus.pair, returnData)
                }
            })
        }
    }

    fun getAllOrderFiex() {
        CommonUtil.postHandleTask(socketHandler) {
            SocketDataContainer.getOrderListFiex(
                context,
                singleOrderDepthList,
                object : Callback<TradeOrderPairList?>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(returnData: TradeOrderPairList?) {
                        sortTradeOrder(currentPairStatus.pair, returnData)
                    }
                })
        }
    }

    private fun sortTradeOrder(pair: String?, orderPairList: TradeOrderPairList?) {
        Observable.just(orderPairList)
            .flatMap(object : Function<TradeOrderPairList?, ObservableSource<Void>> {
                @Throws(Exception::class)
                override fun apply(orders: TradeOrderPairList): ObservableSource<Void> {
                    var tradeOrders = orders
//                        tradeOrders = tradeOrders ?: TradeOrderPairList()
                    tradeOrders.bidOrderList =
                        if (tradeOrders.bidOrderList == null) ArrayList() else tradeOrders.bidOrderList
                    tradeOrders.askOrderList =
                        if (tradeOrders.askOrderList == null) ArrayList() else tradeOrders.askOrderList
                    var bidTradeOrderList: List<TradeOrder?>? = tradeOrders.bidOrderList
                    if (bidTradeOrderList != null && bidTradeOrderList.isNotEmpty()) {
                        val bidTradeOrderListAfterFilter = ArrayList<TradeOrder>()
                        for (tradeOrder in bidTradeOrderList) {
                            //过滤出委托量>0的数据
                            if (tradeOrder != null && tradeOrder.exchangeAmount > 0 && TextUtils.equals(
                                    currentPairStatus.pair,
                                    tradeOrder.pair
                                )
                            ) {
                                bidTradeOrderListAfterFilter.add(tradeOrder)
                            }
                        }
                        //按照价格，精度和最大数量进行合并,
                        bidTradeOrderList = SocketUtil.mergeQuotationOrder(
                            bidTradeOrderListAfterFilter,
                            currentPairStatus.pair,
                            "BID",
                            currentPairStatus.precision,
                            bidMax
                        )
                        bidTradeOrderList = bidTradeOrderList ?: ArrayList()
                        Collections.sort(bidTradeOrderList, TradeOrder.COMPARATOR_DOWN)
                    }
                    var askTradeOrderList: List<TradeOrder?>? = tradeOrders.askOrderList
                    if (askTradeOrderList != null && askTradeOrderList.isNotEmpty()) {
                        val askTradeOrderListAfterFilter = ArrayList<TradeOrder>()
                        for (tradeOrder in askTradeOrderList) {
                            if (tradeOrder != null && tradeOrder.exchangeAmount > 0 && TextUtils.equals(
                                    currentPairStatus.pair,
                                    tradeOrder.pair
                                )
                            ) {
                                askTradeOrderListAfterFilter.add(tradeOrder)
                            }
                        }
                        askTradeOrderList = SocketUtil.mergeQuotationOrder(
                            askTradeOrderListAfterFilter,
                            currentPairStatus.pair,
                            "ASK",
                            currentPairStatus.precision,
                            askMax
                        )
                        askTradeOrderList = askTradeOrderList ?: ArrayList()
                        Collections.sort(askTradeOrderList, TradeOrder.COMPARATOR_UP)
                    }
                    val firstBidTrad = CommonUtil.getItemFromList(bidTradeOrderList, 0)
                    val firstBidTradPrice =
                        if (firstBidTrad == null) null else CommonUtil.parseBigDecimal(firstBidTrad.priceString)
                    val firstAskTrad = CommonUtil.getItemFromList(askTradeOrderList, 0)
                    val firstAskTradPrice =
                        if (firstAskTrad == null) null else CommonUtil.parseBigDecimal(firstAskTrad.priceString)
                    //如果买单价格大于等于卖单价格，说明数据异常，重新订阅socket挂单
                    val isError =
                        firstBidTradPrice != null && firstAskTradPrice != null && firstBidTradPrice >= firstAskTradPrice
                    if (isError) {
                        SocketUtil.sendSocketCommandBroadcast(
                            context,
                            SocketUtil.COMMAND_ORDER_RELOAD
                        )
                        return Observable.empty()
                    }
                    onTransactionModelListener?.onTradeOrder(
                        pair,
                        bidTradeOrderList,
                        askTradeOrderList
                    )
                    return Observable.empty()
                }

            })
            .observeOn(Schedulers.io())
            .subscribeOn(Schedulers.io())
            .subscribe()
    }

    fun getCurrentPairOrderTypeList(): ArrayList<String?>? {
        return currentPairStatus.getSupportOrderTypeList()
    }

    fun getCurrentPairOrderType(): String? {
        if (currentPairStatus == null) {
            return currentPairStatus.getSupportOrderTypeList()?.get(0)
        } else {
            return currentOrderType
        }
    }

    fun setCurrentPairorderType(type: String?) {
        currentOrderType = type
    }

    fun getCurrentPairStatus(pair: String?) {
        currentPairStatus.pair = (pair)
        initPairCoinSet()
        val pairStatus: PairStatus? =
            SocketDataContainer.getPairStatusSync(context, ConstData.PairStatusType.SPOT, pair)
        if (pairStatus != null) {
            currentPairStatus = pairStatus
            initPairCoinSet()
        } else {
            SocketDataContainer.initAllPairStatusData(context)
        }
        onTransactionModelListener?.onPairStatusInit(pairStatus)
        resetPairStatus(pairStatus)
    }

    //初始化交易对
    private fun initPairStatus() {
        val onResumeTodo = Runnable {
            val currentPair = getLastPair()
            if (!TextUtils.equals(currentPair, currentPairStatus.pair)) {
                currentPairStatus = PairStatus()
                currentPairStatus.pair = (currentPair)
                initPairCoinSet()
                currentPairStatus.supportingPrecisionList = null
                currentPairStatus.precision = ConstData.DEFAULT_PRECISION
            }
            getCurrentPairStatus(currentPairStatus.pair)
        }
        currentPairStatus.pair = (getLastPair())
        initPairCoinSet()
        currentPairStatus.supportingPrecisionList = null
        currentPairStatus.precision = ConstData.DEFAULT_PRECISION
        if (currentPairStatus.pair == null) {
            SocketDataContainer.initAllPairStatusData(context)
        } else {
            onResumeTodo.run()
        }
    }

    private fun getLastPair(): String? {
        var pair = CookieUtil.getCurrentPair(context)
        if (pair == null) {
            val allPair = SocketDataContainer.getAllPair(context, ConstData.PairStatusType.SPOT)
            if (allPair != null) {
                pair = CommonUtil.getItemFromList(allPair, 0)
            }
        }
        return pair
    }

    fun setTabType(tabType: Int) {
        this.tabType = tabType
        initPairStatus()
    }

    private fun resetPairStatus(pairStatus: PairStatus?) {
        if (pairStatus == null) {
            return
        }
        currentPairStatus.currentPrice = (pairStatus.currentPrice)
        currentPairStatus.setCurrentPriceCNY(
            pairStatus.currentPriceCNY,
            context.getString(R.string.number_default)
        )
        currentPairStatus.maxPrice = (pairStatus.maxPrice)
        currentPairStatus.minPrice = (pairStatus.minPrice)
        currentPairStatus.priceChangeSinceToday = (pairStatus.priceChangeSinceToday)
        currentPairStatus.totalAmount = (pairStatus.totalAmount)
        if (pairStatus.pair != null) {
            currentPairStatus.pair = (pairStatus.pair)
        }
    }

    /**
     * 获取当前交易对深度
     */
    fun getCurrentPairDepth(level: Int) {
        TradeApiServiceHelper.getTradeOrderDepth(
            context,
            level,
            currentPairStatus.pair,
            false,
            object : Callback<HttpRequestResultData<TradeOrderDepth?>?>() {
                override fun callback(returnData: HttpRequestResultData<TradeOrderDepth?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        tradeOrderDepthPair = returnData.data?.let {
                            SocketDataContainer.parseOrderDepthData(
                                context,
                                ConstData.DEPTH_SPOT_TYPE,
                                it
                            )
                        }
                        singleOrderDepthList =
                            tradeOrderDepthPair?.let { SocketDataContainer.parseOrderDepthToList(it) }!!
                        getAllOrderFiex()
                    }
                }

                override fun error(type: Int, error: Any?) {
                }
            })
    }

    /**
     * 获取当前交易对成交列表
     */
    fun getCurrentPairDeal(level: Int) {
        TradeApiServiceHelper.getTradeOrderDeal(
            context,
            level,
            currentPairStatus.pair,
            false,
            object : Callback<HttpRequestResultDataList<PairDeal?>?>() {
                override fun callback(returnData: HttpRequestResultDataList<PairDeal?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        var dealList = returnData.data
                        var recentDeal = CommonUtil.getItemFromList(dealList, 0)
                        onTransactionModelListener?.run {
                            if (recentDeal != null && currentPairStatus.pair != null) {
                                onTransactionModelListener?.onPairDeal(recentDeal)
                            }
                        }
                    }
                }

                override fun error(type: Int, error: Any?) {
                }
            })
    }


    /**
     * 获取用户资产
     *
     */
    fun getCurrentUserBalance(balanceType: ConstData.BalanceType?) {
        onTransactionModelListener?.getUserBalanceCallback()?.let {
            WalletApiServiceHelper.getUserBalanceReal(
                context,
                false,
                object : Callback<UserBalanceWarpper?>() {
                    override fun callback(balances: UserBalanceWarpper?) {
                        var buyBalance: UserBalance? = null
                        var sellBalance: UserBalance? = null
                        if (balances != null) {
                            var balanceList: ArrayList<UserBalance?>? = null
                            when (balanceType) {
                                ConstData.BalanceType.SPOT -> {
                                    balanceList = balances?.spotBalance
                                }
                                ConstData.BalanceType.CONTRACT -> {
                                    balanceList = balances?.tigerBalance
                                }
                            }
                            if (balanceList != null) {
                                for (balance in balanceList) {
                                    val pairCoinType = currentPairStatus.name
                                    val pairEstimatedCoinType = currentPairStatus.setName
                                    if (TextUtils.equals(pairCoinType, balance?.coin)) {
                                        buyBalance = balance
                                    }
                                    if (TextUtils.equals(pairEstimatedCoinType, balance?.coin)) {
                                        sellBalance = balance
                                    }
                                    if (buyBalance != null && sellBalance != null) {
                                        break
                                    }
                                }
                            }
                        }
                        it.callback(Pair(buyBalance, sellBalance))
                    }

                    override fun error(type: Int, error: Any?) {

                    }
                },
                object : Callback<Any?>() {
                    override fun error(type: Int, error: Any?) {
                        it.error(type, error)
                    }

                    override fun callback(returnData: Any?) {
                    }
                })
        }
    }

    fun getCurrentWallet(tabType: Int) {
        onTransactionModelListener?.getWalletCallback()?.let {
            if (tabType == ConstData.TAB_LEVER) {
                WalletApiServiceHelper.getWalletLeverList(
                    context,
                    false,
                    object : Callback<ArrayList<WalletLever?>?>() {
                        override fun callback(wallets: ArrayList<WalletLever?>?) {
                            var coinWallet: Wallet? = null
                            var setWallet: Wallet? = null
                            if (wallets != null) {
                                val pair = currentPairStatus.pair
                                for (wallet in wallets) {
                                    if (TextUtils.equals(pair, wallet?.pair)) {
                                        coinWallet = wallet?.createCoinWallet()
                                        setWallet = wallet?.createSetWallet()
                                    }
                                    if (coinWallet != null && setWallet != null) {
                                        break
                                    }
                                }
                            }
                            it.callback(Pair(coinWallet, setWallet))
                        }

                        override fun error(type: Int, error: Any?) {
                            it.error(type, error)
                        }

                    })
            } else {
                WalletApiServiceHelper.getWalletList(
                    context,
                    false,
                    object : Callback<ArrayList<Wallet?>?>() {
                        override fun callback(wallets: ArrayList<Wallet?>?) {
                            var coinWallet: Wallet? = null
                            var setWallet: Wallet? = null
                            if (wallets != null) {
                                val pairCoinType = currentPairStatus.name
                                val pairEstimatedCoinType = currentPairStatus.setName
                                for (wallet in wallets) {
                                    if (TextUtils.equals(pairCoinType, wallet?.coinType)) {
                                        coinWallet = wallet
                                    }
                                    if (TextUtils.equals(pairEstimatedCoinType, wallet?.coinType)) {
                                        setWallet = wallet
                                    }
                                    if (coinWallet != null && setWallet != null) {
                                        break
                                    }
                                }
                            }
                            it.callback(Pair(coinWallet, setWallet))
                        }

                        override fun error(type: Int, error: Any?) {
                            it.error(type, error)
                        }

                    })
            }
        }
    }

    fun checkDearPair(): Observable<Boolean>? {
        return DearPairService.isDearPair(context, socketHandler, currentPairStatus.pair)
            ?.compose(RxJavaHelper.observeOnMainThread())
    }

    fun toggleDearPair(isDearPair: Boolean): Observable<HttpRequestResultString?>? {
        return if (currentPairStatus.pair == null) null else {
            if (isDearPair) {
                DearPairService.removeDearPair(context, socketHandler, currentPairStatus.pair!!)
            } else {
                DearPairService.insertDearPair(context, socketHandler, currentPairStatus.pair!!)
            }
        }
    }

    fun checkIntoChatRoom(): Observable<HttpRequestResultString?>? {
        return ApiManager.build(context).getService(UserApiService::class.java)
            ?.checkChatEnable(currentPairStatus.name)
            ?.compose(RxJavaHelper.observeOnMainThread())
    }

    fun getAmountLength(): Int {
        return currentPairStatus.amountPrecision ?: 4
    }

    fun getCurrentPair(): String? {
        return currentPairStatus.pair
    }

    fun getPrecisionList(): ArrayList<Deep>? {
        return currentPairStatus.supportingPrecisionList
    }

    fun getCoinType(): String? {
        return coinType
    }

    fun getSetName(): String? {
        return pairSet
    }

    fun setPrecision(precision: Int) {
        currentPairStatus.precision = precision
    }

    fun getPrecision(): Int? {
        return currentPairStatus.precision
    }

    fun getPrecisionDeep(precision: Int?): Deep? {
        var deepList = currentPairStatus.supportingPrecisionList
        if (deepList != null) {
            for (deep in deepList) {
                if (precision == deep.precision) {
                    return deep
                }
            }
        }
        return null
    }

    fun getCurrentPriceCNY(): Double? {
        return currentPairStatus.currentPriceCNY
    }

    fun getCurrentPrice(): Double {
        return currentPairStatus.currentPrice
    }

    fun changePairSocket() {
        val bundle = Bundle();
        bundle.putString(ConstData.PAIR, currentPairStatus.pair)
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_PAIR_CHANGED, bundle)
    }

    interface OnTransactionModelListener {

        /**
         * 用户挂单数据变化
         */
        fun onUserTradeOrderChanged(userTradeOrder: TradeOrderFiex?)

        /**
         * 用户余额变化
         */
        fun onUserBalanceChanged(userBalance: UserBalance?)

        /**
         * 交易对24小时行情变更
         */
        fun onPairQuotation(pairQuo: PairQuotation)

        /**
         * 交易对初始化
         */
        fun onPairStatusInit(pairStatus: PairStatus?)

        /**
         * 用户信息数据变更
         */
        fun onUserInfoChanged()

        /**
         * 挂单数据回调
         */
        fun onTradeOrder(
            pair: String?,
            bidOrderList: List<TradeOrder?>?,
            askOrderList: List<TradeOrder?>?
        )

        /**
         * 当前交易对成交
         */
        fun onPairDeal(value: PairDeal)

        fun onTradePairInfo(pairStatus: PairStatus?)

        fun onWallet(observable: Observable<Pair<Wallet?, Wallet?>>?)
        fun getWalletCallback(): Callback<Pair<Wallet?, Wallet?>>

        fun onWalletLeverDetail(leverDetail: WalletLeverDetail?)
        fun onLeverPairConfigCheck(hasLeverConfig: Boolean)
        fun onUserBanlance(userBalance: Observable<HttpRequestResultDataList<UserBalance?>?>?)

        fun getUserBalanceCallback(): Callback<Pair<UserBalance?, UserBalance?>>


    }
}
