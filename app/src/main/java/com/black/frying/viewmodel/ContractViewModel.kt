package com.black.frying.viewmodel

import android.annotation.SuppressLint
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
import com.black.base.model.clutter.Kline
import com.black.base.model.future.*
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
import com.black.base.widget.AnalyticChart
import com.black.base.widget.AnalyticChart2
import com.black.frying.fragment.PLAN
import com.black.frying.service.FutureService
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
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class ContractViewModel(
    context: Context,
    private val onContractModelListener: OnContractModelListener?
) : BaseViewModel<Any?>(context) {
    companion object {
        var TAG = ContractViewModel::class.java.simpleName
    }


    private var tabType = ConstData.TAB_COIN
    private var currentPairStatus = PairStatus()
    private var currentOrderType: String? = null
    private var currentUnitType: String? = null
    private var currentTimeInForceType: String? = null
    private var coinType: String? = null
    private var pairSet: String? = null
    private var kLineId: String? = ""
    private var onKLineAllEnd = false
    var askMax = 10
    var bidMax = 10

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null

    //用户相关
    private var userBalanceObserver: Observer<UserBalance?>? = createUserBalanceObserver()
    private var userTradeOrderObserver: Observer<TradeOrderFiex?>? = createUserOrderObserver()

    //交易对相关
    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = createPairObserver()

    //深度变化
    private var depthObserver: Observer<Pair<String?, TradeOrderPairList?>>? = createDepthObserver()

    //行情涨跌幅以及价格变化
    private var pairQuotationObserver: Observer<PairQuotation?>? = createPairQuotationObserver()

    /**
     * markPrice更新
     */
    private var markPriceObserver: Observer<MarkPriceBean?>? = createMarkPriceObserver()

    /**
     * indexPrice更新
     */
    private var indexPriceObserver: Observer<IndexPriceBean?>? = createIndexPriceObserver()

    /**
     * 费率更新
     */
    private var fundRateObserver: Observer<FundRateBean?>? = createFundRateObserver()


    //当前交易对所有用户成交数据
    private var pairDealObserver: Observer<PairDeal?>? = createPairDealObserver()

    //用户相关
    private var userInfoObserver: Observer<String?>? = createUserInfoObserver()


    private var tradeOrderDepthPair: TradeOrderPairList? = null
    private var singleOrderDepthList: ArrayList<QuotationOrderNew?>? = null
    private var fkLineObserver: Observer<KLineItemListPair?>? = createKLineObserver()
    private var fkLineAddObserver: Observer<KLineItemPair?>? = createKLineAddObserver()
    private var fkLineAddMoreObserver: Observer<KLineItemListPair?>? = createKLineAddMoreObserver()

    //标记价格
    private var marketPrice: MarkPriceBean? = null

    //指数价格
    private var indexPrice: IndexPriceBean? = null

    //行情
    private var tickeBean: PairQuotation? = null

    //资金费率
    private var fundRate: FundingRateBean? = null

    //持仓列表
    private var positionList: ArrayList<PositionBean?>? = null

    var userStepRate: UserStepRate? = null//用户费率

    var balanceDetailBean: BalanceDetailBean? = null
    private var planUnionBean = PlanUnionBean()



    init {
        currentPairStatus.pair == (CookieUtil.getCurrentFutureUPair(context))
        initPairCoinSet()
    }

    override fun onResume() {
        super.onResume()
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread!!.start()
        socketHandler = Handler(handlerThread!!.looper)


        if (markPriceObserver == null) {
            markPriceObserver = createMarkPriceObserver()
        }
        SocketDataContainer.subscribeMarkPriceObservable(markPriceObserver)

        if (indexPriceObserver == null) {
            indexPriceObserver = createIndexPriceObserver()
        }
        SocketDataContainer.subscribeIndexPriceObservable(indexPriceObserver)

        if (fundRateObserver == null) {
            fundRateObserver = createFundRateObserver()
        }
        SocketDataContainer.subscribeFundRateObservable(fundRateObserver)

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
        SocketDataContainer.subscribeFuturePairDealObservable(pairDealObserver)

        if (depthObserver == null) {
            depthObserver = createDepthObserver()
        }
        SocketDataContainer.subscribeFutureDepthObservable(depthObserver)

        if (userInfoObserver == null) {
            userInfoObserver = createUserInfoObserver()
        }
        SocketDataContainer.subscribeUserInfoObservable(userInfoObserver)

        if (pairQuotationObserver != null) {
            pairQuotationObserver = createPairQuotationObserver()
        }
        SocketDataContainer.subscribeFuturePairQuotationObservable(pairQuotationObserver)

        if (fkLineObserver == null) {
            fkLineObserver = createKLineObserver()
        }
        SocketDataContainer.subscribeFKLineObservable(fkLineObserver)

        if (fkLineAddObserver == null) {
            fkLineAddObserver = createKLineAddObserver()
        }
        SocketDataContainer.subscribeFKLineAddObservable(fkLineAddObserver)

        if (fkLineAddMoreObserver == null) {
            fkLineAddMoreObserver = createKLineAddMoreObserver()
        }
        SocketDataContainer.subscribeFKLineAddMoreObservable(fkLineAddMoreObserver)

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
        if (LoginUtil.isFutureLogin(context)) {
                getPositionData()
                initBalanceByCoin(context)
            getProfitData(Constants.UNFINISHED)
            getPlanData(Constants.UNFINISHED)
        }
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

        if (markPriceObserver != null) {
            SocketDataContainer.removeMarkPriceObservable(markPriceObserver)
        }
        if (indexPriceObserver != null) {
            SocketDataContainer.removeIndexPriceObservable(indexPriceObserver)
        }

        if (depthObserver != null) {
            SocketDataContainer.removeFutureDepthObservable(depthObserver)
        }
        if (pairObserver != null) {
            SocketDataContainer.removeFuturePairDealObservable(pairDealObserver)
        }
        if (userInfoObserver != null) {
            SocketDataContainer.removeUserInfoObservable(userInfoObserver)
        }

        if (pairQuotationObserver != null) {
            SocketDataContainer.removeFuturePairQuotationObservable(pairQuotationObserver)
        }

        if (fundRateObserver != null) {
            SocketDataContainer.removeFundRateObservable(fundRateObserver)
        }

        if (fkLineObserver != null) {
            SocketDataContainer.removeFKLineObservable(fkLineObserver)
        }
        if (fkLineAddObserver != null) {
            SocketDataContainer.removeFKLineAddObservable(fkLineAddObserver)
        }
        if (fkLineAddMoreObserver != null) {
            SocketDataContainer.removeFKLineAddMoreObservable(fkLineAddMoreObserver)
        }

        if (socketHandler != null) {
            socketHandler!!.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread!!.quit()
        }
    }

    /**
     * 获取资产
     */
    fun initBalanceByCoin(context: Context?) {
        var coin: String = "usdt"
        if(currentPairStatus.pair != null){
            coin = currentPairStatus.pair.toString().split("_")[1]
        }
        FutureApiServiceHelper.getBalanceDetail(context, coin, FutureService.underlyingType, false,
            object : Callback<HttpRequestResultBean<BalanceDetailBean?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->getBalanceDetail", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<BalanceDetailBean?>?) {
                    Log.d("ttttttt-->getBalanceDetail", returnData?.result.toString())
                    if (returnData != null) {
                        balanceDetailBean = returnData.result!!
                        //onContractModelListener?.futureBalance(balanceDetailBean)
                    }
                }
            })
    }

    /**
     * 获取仓位列表
     */
    fun getPositionData() {
        FutureApiServiceHelper.getPositionList(context, null, false,
            object : Callback<HttpRequestResultBean<ArrayList<PositionBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<PositionBean?>?>?) {
                    if (returnData != null) {

                        var data: ArrayList<PositionBean?>? = returnData.result
                        positionList =
                            data?.filter { it?.positionSize!!.toInt() > 0  || it.availableCloseSize!!.toInt() > 0} as ArrayList<PositionBean?>?
                        if (positionList?.size!! > 0) {
                            onContractModelListener?.onPositionData(positionList)
                        }
                    }
                }
            })
    }

    /**
     * 获取止盈止损列表
     */
    /**
     * 获取当前持仓数据
     */
    fun getProfitData(state: String?) {
        var symbol:String? = currentPairStatus?.pair
        if(SharedPreferenceUtils.getData(Constants.PROFIT_ALL_CHECKED,false) as Boolean){
            symbol = null
        }
        FutureApiServiceHelper.getProfitList(context, symbol,state, false,
            object : Callback<HttpRequestResultBean<PagingData<ProfitsBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(returnData: HttpRequestResultBean<PagingData<ProfitsBean?>?>?) {
                    if (returnData != null) {
                        var data = returnData.result?.items
                        if (data?.size!! > 0) {
                            onContractModelListener?.onProfitData(data)
                        }

                    }
                }
            })
    }

    /**
     * 获取当前计划委托列表
     */
    fun getPlanData(state: String?) {
        var symbol:String? = currentPairStatus?.pair
        if(SharedPreferenceUtils.getData(Constants.PLAN_ALL_CHECKED,true) as Boolean){
            symbol = null
        }
        FutureApiServiceHelper.getPlanList(context,symbol, state, false,
            object : Callback<HttpRequestResultBean<PagingData<PlansBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(returnData: HttpRequestResultBean<PagingData<PlansBean?>?>?) {
                    if (returnData != null) {
                        var data = returnData.result?.items
                        planUnionBean?.planList = data
                        getLimitPricePlanData()
                    }
                }
            })
    }

    /**
     * 获取当前限价委托
     */
    fun getLimitPricePlanData() {
        var symbol:String? = currentPairStatus?.pair
        if(SharedPreferenceUtils.getData(Constants.PLAN_ALL_CHECKED,true) as Boolean){
            symbol = null
        }
        FutureApiServiceHelper.getOrderList(1, 10,symbol, Constants.UNFINISHED, context, false,
            object : Callback<HttpRequestResultBean<OrderBean>>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(returnData: HttpRequestResultBean<OrderBean>?) {
                    if (returnData != null) {
                        var orderData = returnData?.result
                        var orderList = orderData?.items
                        planUnionBean?.limitPriceList = orderList
                        onContractModelListener?.onPlanData(planUnionBean)
                    }
                }
            })
    }

    private fun createUserBalanceObserver(): Observer<UserBalance?> {
        return object : SuccessObserver<UserBalance?>() {
            override fun onSuccess(value: UserBalance?) {
                onContractModelListener?.run {
                    if (value != null) {
                        //onContractModelListener.onUserBalanceChanged(value)
                    }
                }
            }
        }
    }



    private fun createUserOrderObserver(): Observer<TradeOrderFiex?> {
        return object : SuccessObserver<TradeOrderFiex?>() {
            override fun onSuccess(value: TradeOrderFiex?) {
                onContractModelListener?.run {
                    if (value != null) {
                        onContractModelListener.onUserTradeOrderChanged(value)
                    }
                }
            }
        }
    }


    private fun createPairDealObserver(): Observer<PairDeal?> {
        return object : SuccessObserver<PairDeal?>() {
            override fun onSuccess(value: PairDeal?) {
                onContractModelListener?.run {
                    if (value != null && currentPairStatus.pair != null) {
                        onContractModelListener.onPairDeal(value)
                    }
                }
            }
        }
    }

    private fun createUserInfoObserver(): Observer<String?> {
        return object : SuccessObserver<String?>() {
            override fun onSuccess(value: String?) {
                onContractModelListener?.onUserInfoChanged()
            }
        }
    }

    //检查存在的杠杆交易对
    fun checkLeverPairConfig() {
        onContractModelListener?.let {
            CommonUtil.postHandleTask(socketHandler) {
                val callback: Callback<java.util.ArrayList<PairStatus?>?> =
                    object : Callback<java.util.ArrayList<PairStatus?>?>() {
                        override fun error(type: Int, error: Any) {

                        }

                        override fun callback(returnData: java.util.ArrayList<PairStatus?>?) {
                            //Log.e("checkLeverPairConfig", "returnData:" + returnData)
                            it.onLeverPairConfigCheck(returnData != null && returnData.isNotEmpty())
                        }
                    }
//                SocketDataContainer.getAllLeverPairStatus(context, callback)
            }
        }
    }


    private fun createPairObserver(): Observer<ArrayList<PairStatus?>?> {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
            override fun onSuccess(value: ArrayList<PairStatus?>?) {
                if (value == null || value.isEmpty()) {
                    return
                }
                if (onContractModelListener != null) {
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
                checkLeverPairConfig()
            }
        }
    }

    private fun createDepthObserver(): Observer<Pair<String?, TradeOrderPairList?>> {
        return object : SuccessObserver<Pair<String?, TradeOrderPairList?>>() {
            override fun onSuccess(value: Pair<String?, TradeOrderPairList?>) {
                Log.d("iiiiii", "createDepthObserver,pair = " + value.first)
                Log.d(
                    "iiiiii",
                    "createDepthObserver,askOrderList->size = " + value.second?.askOrderList?.size
                )
                Log.d(
                    "iiiiii",
                    "createDepthObserver,bidOrderList->size = " + value.second?.bidOrderList?.size
                )
                if (TextUtils.equals(
                        currentPairStatus.pair,
                        value.first
                    ) && value.second != null && onContractModelListener != null
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
                    onContractModelListener.run {
                        onContractModelListener?.onPairQuotation(value)
                        Log.d("ttt---->行情", value.toString())
                    }
                }
            }
        }
    }

    private fun createMarkPriceObserver(): Observer<MarkPriceBean?> {
        return object : SuccessObserver<MarkPriceBean?>() {
            override fun onSuccess(value: MarkPriceBean?) {
//                todo 计算总权益
//                Log.d("ttt------>markPirce", value.toString())
                var floatProfit: BigDecimal = BigDecimal.ZERO
                if (positionList != null) {
                    for (item in positionList!!) {
                        Log.d("tt1------>floatProfit", positionList!!.size.toString())
                        if (item?.symbol == value?.s) {
                            Log.d("tt1------>float1", positionList.toString())
                            val fp = FutureService.getFloatProfit(item!!, value!!)
                            floatProfit = floatProfit.add(fp)
                        }
                    }
                    var totalProfit: BigDecimal = BigDecimal.ZERO
                    var available: BigDecimal = BigDecimal.ZERO
//                    Log.d("ttt------>balanceAmount", balanceDetailBean?.walletBalance.toString())
                    if (balanceDetailBean != null) {
                        Log.d("ttt------>floatProfit", floatProfit.toString())
                        totalProfit = BigDecimal(balanceDetailBean?.walletBalance).add(floatProfit)
                        available = BigDecimal(balanceDetailBean?.availableBalance).add(floatProfit)
                        onContractModelListener?.updateTotalProfit(String.format("%.4f", totalProfit),String.format("%.4f", available))
                    }
//                    Log.d("ttt------>totalProfit", totalProfit.toString())
                }
                if (value?.s == currentPairStatus.pair) {
                    onContractModelListener?.onMarketPrice(value)
                }
            }
        }
    }

    private fun createIndexPriceObserver(): Observer<IndexPriceBean?>? {
        return object : SuccessObserver<IndexPriceBean?>() {
            override fun onSuccess(value: IndexPriceBean?) {
                onContractModelListener?.onIndexPirce(value)
            }
        }
    }

    private fun createFundRateObserver(): Observer<FundRateBean?>? {
        return object : SuccessObserver<FundRateBean?>() {
            override fun onSuccess(value: FundRateBean?) {
                if (value != null) {
                    var data = FundingRateBean(value.r, value.t.toLong(), value.s)
                    onContractModelListener?.onFundingRate(data)
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
            SocketDataContainer.getOrderList(
                context,
                ConstData.DEPTH_FUTURE_TYPE,
                object : Callback<TradeOrderPairList?>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(returnData: TradeOrderPairList?) {
                        sortTradeOrder(currentPairStatus.pair, returnData)
                    }
                })
        }
    }

    fun getAllDepthOrderFiex() {
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

    /**
     * 获取交易对杠杆分层
     */
    fun getLeverageBracketDetail() {
        FutureApiServiceHelper.getLeverageBracketDetail(context, getCurrentPair(), false,
            object : Callback<HttpRequestResultBean<LeverageBracketBean?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("iiiiii-->LeverageBracketDetail", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<LeverageBracketBean?>?) {
                    if (returnData != null) {
                        onContractModelListener?.onLeverageDetail(returnData.result)
                    }
                }
            })
    }

    /**
     * 获取交易当前成交
     */
    fun getCurrentDeal() {
        FutureApiServiceHelper.getDealList(context, getCurrentPair(), 1, false,
            object : Callback<HttpRequestResultBean<ArrayList<PairDeal>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("iiiiii-->getCurrentDeal", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<PairDeal>?>?) {
                    if (returnData != null) {
                        var dealList = returnData.result
                        var recentDeal = CommonUtil.getItemFromList(dealList, 0)
                        onContractModelListener?.run {
                            if (recentDeal != null && currentPairStatus.pair != null) {
                                onContractModelListener.onPairDeal(recentDeal)
                            }
                        }
                    }
                }
            })
    }


    /**
     * 获取交易对当前行情
     */
    fun getAggTicker() {
        FutureApiServiceHelper.getAggTicker(context, getCurrentPair(), false,
            object : Callback<HttpRequestResultBean<PairQuotation?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("iiiiii-->getAggTicker", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<PairQuotation?>?) {
                    if (returnData != null) {
                        var data = returnData.result
                        if (data != null) {
                            onContractModelListener?.onPairQuotation(data)
                        }
                    }
                }
            })
    }

    /**
     * 改变当前杠杆类型
     */
    fun changeType(positionSide: String? , positionType: String?) {
        FutureApiServiceHelper.changType(context, getCurrentPair(), positionSide, positionType,false,
            object : Callback<HttpRequestResultBean<String>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(returnData: HttpRequestResultBean<String>?) {
                    if (returnData != null) {
                        }
                    }

            })
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
                    onContractModelListener?.onTradeOrder(
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

    fun getCurrentPairOrderTypeList(): ArrayList<String?> {
        return currentPairStatus.getSupportOrderTypeList()
    }

    fun getCurrentUnitTypeList(): ArrayList<String?>? {
        var unitList: ArrayList<String?>? = ArrayList()
        unitList?.add(currentPairStatus.name!!.uppercase())
        unitList?.add("USDT")
        return unitList
    }

    fun getCurrentTimeInForceTypeList(): ArrayList<String?>? {
        return currentPairStatus.getSupportTimeInForceTypeList()
    }


    fun setCurrentPairOrderType(type: String?) {
        currentOrderType = type
    }

    fun setCurrentUnitType(type: String?) {
        currentUnitType = type
    }

    fun setCurrentTimeInForceType(type: String?) {
        currentTimeInForceType = type
    }

    fun getCurrentPairStatus(pair: String?) {
        currentPairStatus.pair = (pair)
        initPairCoinSet()
        val pairStatus: PairStatus? = SocketDataContainer.getPairStatusSync(
            context,
            ConstData.PairStatusType.FUTURE_ALL,
            pair
        )
        if (pairStatus != null) {
            currentPairStatus = pairStatus
            initPairCoinSet()
        } else {
//            SocketDataContainer.initAllFutureUsdtPairStatusData(context)
        }
        onContractModelListener?.onPairStatusInit(pairStatus)
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
//            SocketDataContainer.initAllFutureUsdtPairStatusData(context)
        } else {
            onResumeTodo.run()
        }
    }

    private fun getLastPair(): String? {
        var pair = CookieUtil.getCurrentFutureUPair(context)
        if (pair == null) {
            val allPair = SocketDataContainer.getAllPair(context, ConstData.PairStatusType.FUTURE_U)
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
        FutureApiServiceHelper.getDepthData(
            context,
            currentPairStatus.pair,
            level,
            false,
            object : Callback<HttpRequestResultBean<DepthBean?>?>() {
                override fun callback(returnData: HttpRequestResultBean<DepthBean?>?) {
                    if (returnData != null && returnData.returnCode == HttpRequestResult.SUCCESS) {
                        var fDepth = returnData.result
                        if (fDepth != null) {
                            var depth = TradeOrderDepth()
                            depth.s = fDepth.s
                            depth.a = fDepth.a
                            depth.b = fDepth.b
                            depth.t = fDepth.t
                            depth.u = fDepth.u
                            tradeOrderDepthPair = depth.let {
                                SocketDataContainer.parseOrderDepthData(
                                    context,
                                    ConstData.DEPTH_FUTURE_TYPE,
                                    it
                                )
                            }
                            singleOrderDepthList = tradeOrderDepthPair?.let {
                                SocketDataContainer.parseOrderDepthToList(it)
                            }!!
                            getAllDepthOrderFiex()
                        }

                    }
                }

                override fun error(type: Int, error: Any?) {
                }
            })
    }
    fun listenKLineData(analyticChart: AnalyticChart2) {
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
    }

    fun getKLineDataFiex(timeStep:String?,kLinePage: Int,startTime:Long,endTime:Long){
        currentPairStatus.pair?.let {
            if (timeStep != null) {
                FutureApiServiceHelper.getHistoryKline(
                    context,
                    it,
                    timeStep,
                    500,
                    true,
                    object : Callback<HttpRequestResultDataList<Kline?>?>() {
                        override fun error(type: Int, error: Any) {
                            if(kLinePage != 0){
                                onContractModelListener!!.onKLineLoadingMore()
                            }
                        }
                        override fun callback(returnData: HttpRequestResultDataList<Kline?>?) {
                            if (returnData != null) {
                                var items = returnData.result!!
                                onKLineAllEnd = true
                                if(items.size>0){
                                    var dataItem = ArrayList<KLineItem?>()
                                    for (i in items.lastIndex downTo 0){
                                        var klineItem = KLineItem()
                                        var temp = items[i]
                                        klineItem.a = temp?.a?.toDouble()!!
                                        klineItem.c = temp.c?.toDouble()!!
                                        klineItem.h = temp.h?.toDouble()!!
                                        klineItem.l = temp.l?.toDouble()!!
                                        klineItem.o = temp.o?.toDouble()!!
                                        klineItem.t = temp.t?.div(1000)
                                        klineItem.v = temp.v?.toDouble()!!
                                        dataItem.add(klineItem)
                                    }
                                    if(kLinePage == 0){
                                        onContractModelListener!!.onKLineDataAll(dataItem)
                                    }else{
                                        onContractModelListener!!.onKLineDataMore(kLinePage, dataItem)
                                    }
                                }
                            }else{
                                if(kLinePage != 0){
                                    onContractModelListener!!.onKLineLoadingMore()
                                }
                            }
                        }
                    })
            }
        }
    }

    private fun createKLineObserver(): Observer<KLineItemListPair?> {
        return object : SuccessObserver<KLineItemListPair?>() {
            @SuppressLint("CheckResult")
            override fun onSuccess(value: KLineItemListPair?) {
                if (value == null) {
                    return
                }
                onContractModelListener?.run {
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
                onContractModelListener?.run {
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
                onContractModelListener?.run {
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

    /**
     * 获取用户资产
     *
     */
    /*fun getCurrentUserBalance(balanceType: ConstData.BalanceType?) {
        onContractModelListener?.getUserBalanceCallback()?.let {
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
                                    balanceList = balances.spotBalance
                                }
                                ConstData.BalanceType.CONTRACT -> {
                                    balanceList = balances.tigerBalance
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

     */

    /*fun getCurrentWallet(tabType: Int) {
        onContractModelListener?.getWalletCallback()?.let {
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

     */

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

    /**
     * 获取标记价格
     */
    fun getMarketPrice(symbol: String?) {
        FutureApiServiceHelper.getSymbolMarkPrice(context, symbol, false,
            object : Callback<HttpRequestResultBean<MarkPriceBean?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(returnData: HttpRequestResultBean<MarkPriceBean?>?) {
                    if (returnData != null) {
                        marketPrice = returnData.result
                        onContractModelListener?.onMarketPrice(marketPrice)
                    }
                }
            })
    }

    /**
     * 获取指数价格
     */
    fun getIndexPrice(symbol: String?) {
        FutureApiServiceHelper.getSymbolIndexPrice(context, symbol, false,
            object : Callback<HttpRequestResultBean<IndexPriceBean?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(returnData: HttpRequestResultBean<IndexPriceBean?>?) {
                    if (returnData != null) {
                        indexPrice = returnData.result
                        onContractModelListener?.onIndexPirce(indexPrice)
                    }
                }
            })
    }

    /**
     * 获取单个交易对行情
     */
    fun getSymbolTicker(symbol: String?) {
        FutureApiServiceHelper.getAggTicker(context, symbol, false,
            object : Callback<HttpRequestResultBean<PairQuotation?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(returnData: HttpRequestResultBean<PairQuotation?>?) {
                    if (returnData != null) {
                        tickeBean = returnData.result
                        onContractModelListener?.onPairQuotation(tickeBean)
                    }
                }
            })
    }

    fun initUserStepRate() {
        FutureApiServiceHelper.getUserStepRate(context, false,
            object : Callback<HttpRequestResultBean<UserStepRate>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->initUserStepRate--error", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<UserStepRate>?) {
                    if (returnData != null) {
                        userStepRate = returnData.result
                        onContractModelListener?.onUserFundingRate(userStepRate)
                    }
                }

            })
    }

    /**
     * 获取资金费率
     */
    fun getFundRate(symbol: String?) {
        FutureApiServiceHelper.getFundingRate(symbol, context, false,
            object : Callback<HttpRequestResultBean<FundingRateBean?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(returnData: HttpRequestResultBean<FundingRateBean?>?) {
                    if (returnData != null) {
                        fundRate = returnData.result
                        onContractModelListener?.onFundingRate(fundRate)
                    }
                }
            })
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

    fun getContractSize(): String? {
        return currentPairStatus.contractSize
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

    fun finishListenKLine() {
        SocketUtil.sendSocketCommandBroadcast(context, SocketUtil.COMMAND_KTAB_CLOSE)
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

    interface OnContractModelListener {

        /**
         * 用户挂单数据变化
         */
        fun onUserTradeOrderChanged(userTradeOrder: TradeOrderFiex?)

        /**
         * 用户余额变化
         */
        //fun onUserBalanceChanged(userBalance: UserBalance?)

        /**
         * 交易对24小时行情变更
         */
        fun onPairQuotation(tickerBean: PairQuotation?)

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

        /**
         * 标记价格变化
         */
        fun onMarketPrice(marketPrice: MarkPriceBean?)

        /**
         * 指数价格变化
         */
        fun onIndexPirce(indexPrice: IndexPriceBean?)

        /**
         * 资金费率变化
         */
        fun onFundingRate(fundRate: FundingRateBean?)

        /**
         * 用户费率变化
         */
        fun onUserFundingRate(fundRate: UserStepRate?)

        /**
         * 杠杆分层
         */
        fun onLeverageDetail(leverageBracket: LeverageBracketBean?)

        //fun onWallet(observable: Observable<Pair<Wallet?, Wallet?>>?)
        //fun getWalletCallback(): Callback<Pair<Wallet?, Wallet?>>

        //fun onWalletLeverDetail(leverDetail: WalletLeverDetail?)
        fun onLeverPairConfigCheck(hasLeverConfig: Boolean)
        //fun onUserBanlance(userBalance: Observable<HttpRequestResultDataList<UserBalance?>?>?)

        //fun getUserBalanceCallback(): Callback<Pair<UserBalance?, UserBalance?>>

        /**
         * 更新总权益
         */
        fun updateTotalProfit(totalProfit: String,available:String)

        fun onKLineDataAll(items: ArrayList<KLineItem?>)
        fun onKLineDataAdd(item: KLineItem)
        fun onKLineDataMore(kLinePage: Int, items: ArrayList<KLineItem?>)
        fun onKLineLoadingMore()

        fun futureBalance(balanceDetailBean: BalanceDetailBean?)

        fun onPositionData(data: ArrayList<PositionBean?>?)
        fun onProfitData(data: ArrayList<ProfitsBean?>?)
        fun onPlanData(data: PlanUnionBean?)

    }
}
