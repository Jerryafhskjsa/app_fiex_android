package com.black.frying.viewmodel

import android.content.Context
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.util.Log
import com.black.base.api.*
import com.black.base.model.*
import com.black.base.model.future.*
import com.black.base.model.socket.*
import com.black.base.util.*
import com.black.base.viewmodel.BaseViewModel
import com.black.frying.service.FutureService
import com.black.util.Callback
import io.reactivex.Observer
import java.math.BigDecimal
import kotlin.collections.ArrayList

class ContractPositionViewModel(
    context: Context,
    private val onContractPositionModelListener: OnContractPositionModelListener?
) : BaseViewModel<Any?>(context) {
    companion object {
        var TAG = ContractPositionViewModel::class.java.simpleName
    }

    private var currentPairStatus: PairStatus? = null
    private var fundRate: FundingRateBean? = null//资金费率
    private var marketPrice: MarkPriceBean? = null//标记价格
    private var positionList: ArrayList<PositionBean?>? = null//持仓的订单
    private var leverageBracket: LeverageBracketBean? = null//交易对杠杆分层
    private var adlList: ArrayList<ADLBean?>? = null
    private var positionObservers : Observer<UserPositionBean?>? = createPositionObserver()
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null

    init {
        val pairStatus: PairStatus? = SocketDataContainer.getPairStatusSync(
            context,
            ConstData.PairStatusType.FUTURE_ALL,
            CookieUtil.getCurrentFutureUPair(context)
        )
        if (pairStatus != null) {
            currentPairStatus = pairStatus
        }
    }

    override fun onResume() {
        super.onResume()
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
        if (positionObservers == null) {
            positionObservers = createPositionObserver()
        }
        SocketDataContainer.subscribePositionObservable(positionObservers)
    }

    override fun onStop() {
        super.onStop()
        if (positionObservers != null) {
            SocketDataContainer.removePositionObservable(positionObservers)
            positionObservers = null
        }
        if (socketHandler != null) {
            socketHandler?.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
        }
    }


    /**
     * 获取当前持仓数据
     */
    fun getPositionData(all:Boolean?) {
        var pair: String? = null
        if (all == true){
            pair = CookieUtil.getCurrentFutureUPair(context)
        }
        Log.d("12123",pair.toString())
        FutureApiServiceHelper.getPositionList(context, pair, true,
            object : Callback<HttpRequestResultBean<ArrayList<PositionBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("iiiiii-->positionData--error", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<PositionBean?>?>?) {
                    if (returnData != null) {
                        var data: ArrayList<PositionBean?>? = returnData.result
                        positionList = data?.filter { it?.positionSize!!.toInt() > 0 && it.availableCloseSize!!.toInt() > 0} as ArrayList<PositionBean?>?
                        Log.d("1221123", positionList.toString())
                    }
                    else{
                        positionList = null
                    }
                }
            })
    }

    fun doUpdate(context: Context?,flagPrice: String?,symbol: String?,isSocket: Boolean?){
        updateCurrentPosition(context,flagPrice,symbol,isSocket)
        onContractPositionModelListener?.onGetPositionData(positionList)
    }

    /**
     * 持仓接口:/futures/fapi/user/v1/position/list
     * 持仓/可平：positionSize/availableCloseSize 单位:张；
     *仓位保证金：逐仓（isolatedMargin）,全仓（根据标记价格实时计算）;
     *开仓均价：entryPrice;
     *浮动盈亏/收益率：根据标记价格实时计算；
     *已实现盈亏:realizedProfit
     *自动减仓：调用接口/futures/fapi/user/v1/position/adl  开多 longQuantile 一共5个格
     *
     * 全仓时:
    多仓强平价格 = 数量 * 面值 * 开仓均价 / (数量 * 面值 + 开仓均价 * dex)
    空仓强平价格 = 数量 * 面值 * 开仓均价 / (数量 * 面值 - 开仓均价 * dex)

    dex（共享保证金） = 钱包余额 - ∑逐仓仓位保证金 - ∑全仓维持保证金 - ∑委托保证金 + ∑除本仓位其他全仓仓位未实现盈亏

    逐仓时:
    多仓强平价格 = 开仓均价 * 数量 * 面值 / (数量 * 面值 + 开仓均价 * (仓位保证金 - 维持保证金))
    空仓平价格 = 开仓均价 * 数量 * 面值 / (数量 * 面值 + 开仓均价 * (维持保证金 - 仓位保证金))
     *
     * isSocket = true更新socket推的数据
     * isSocket = false更新http请求的数据
     */
    private fun createPositionObserver(): Observer<UserPositionBean?> {
        return object : SuccessObserver<UserPositionBean?>() {
            override fun onSuccess(value:UserPositionBean?) {
                Log.d("ahsdjkhak", value.toString())
                onContractPositionModelListener?.onPosition(value)
            }
        }
    }
    private fun updateCurrentPosition(context: Context?,flagPrice:String?,symbol: String?,isSocket:Boolean?) {
        Log.d("ttttttt--->222", symbol.toString())
        for (positionBean in positionList!!) {
            Log.d("ttttttt--->333", positionBean?.symbol.toString())
            if (positionBean?.symbol == symbol) {
                if (positionBean?.positionSize.equals("0")) {
                    return
                }
                if (flagPrice == null){
                    return
                }
                        positionBean?.flagPrice = flagPrice

                Log.d("66666", positionBean?.flagPrice)
                Log.d("ttttttt--->1111", positionBean?.flagPrice.toString())
                val contractSize = FutureService.getContractSize(symbol)
                //仓位价值=开仓均价 * 数量 * 面值
                val positionValue = BigDecimal(positionBean?.positionSize)
                    .multiply(BigDecimal(positionBean?.entryPrice))
                    .multiply(BigDecimal(contractSize.toString()))
                //获取维持保证金率
                val maintMarginRate = getMaintMarginRate(positionValue.toString())
                //维持保证金 = 开仓均价 * 数量 * 面值 * 维持保证金率
                val maintMargin = BigDecimal(positionBean?.positionSize)
                    .multiply(BigDecimal(contractSize.toString()))
                    .multiply(BigDecimal(positionBean?.entryPrice.toString()))
                    .multiply(BigDecimal(maintMarginRate))
                Log.d("ttttttt--->maintMargin", maintMargin.toString())
                val adlBean = getAdlBean(positionBean?.symbol!!)
                Log.d("ttttttt--->adlBean", adlBean.toString())
                if (positionBean.positionSide.equals(Constants.LONG)) {
                    positionBean.adl = adlBean?.longQuantile
                } else {
                    positionBean.adl = adlBean?.shortQuantile
                }
                var liquidationPrice: BigDecimal? = null//强平价格
                var floatProfit: BigDecimal? = null//未实现盈亏
                var floatProfitRate: BigDecimal? = null//未实现盈亏收益率
                if (positionBean.positionType.equals("CROSSED")) { //全仓
                    val positionSide = positionBean.positionSide
                    positionBean.bondAmount = String.format("%.4f",maintMargin)
                    //多仓强平价格 = (开仓均价 * 数量 * 面值 - dex) / (数量 * 面值)
                    //空仓强平价格 = (开仓均价 * 数量 * 面值 + dex) / (数量 * 面值)
                    //dex（共享保证金） = 钱包余额 - ∑逐仓仓位保证金 - ∑全仓维持保证金 - ∑委托保证金 + ∑除本仓位其他全仓仓位未实现盈亏
                    if (positionBean.positionSide.equals("LONG")) { //做多
                        liquidationPrice = BigDecimal(positionValue.toString())
                            .subtract(FutureService.getDex(positionBean, positionSide!!,flagPrice))
                            .divide(
                                BigDecimal(positionBean.positionSize)
                                    .multiply(BigDecimal(contractSize.toString())),
                                4,
                                BigDecimal.ROUND_HALF_UP
                            )
                        Log.d("ttttttt-->全仓做多--强平价格", liquidationPrice.toString())
                        floatProfit = FutureService.getFloatProfit(positionBean ,flagPrice)
                        Log.d("ttttttt-->全仓做多--浮动盈亏", floatProfit.toString())
                        floatProfitRate = floatProfit
                            .divide(
                                BigDecimal(positionBean.isolatedMargin),
                                4,
                                BigDecimal.ROUND_HALF_UP
                            )
                            .multiply(BigDecimal("100"))
                        Log.d("ttttttt-->全仓做多--浮动盈亏收益率", floatProfitRate.toString())
                    } else if (positionBean.positionSide.equals("SHORT")) { //做空
                        liquidationPrice = BigDecimal(positionValue.toString())
                            .add(FutureService.getDex(positionBean, positionSide!!,flagPrice!!))
                            .divide(
                                BigDecimal(positionBean.positionSize)
                                    .multiply(BigDecimal(contractSize.toString())),
                                4,
                                BigDecimal.ROUND_HALF_UP
                            )
                        Log.d("ttttttt-->全仓做空--强平价格", liquidationPrice.toString())
                        floatProfit = FutureService.getFloatProfit(positionBean,flagPrice)
                        Log.d("ttttttt-->全仓做空--浮动盈亏", floatProfit.toString())
                        floatProfitRate = floatProfit
                            .divide(
                                BigDecimal(positionBean.isolatedMargin),
                                4,
                                BigDecimal.ROUND_HALF_UP
                            )
                            .multiply(BigDecimal("100"))
                        Log.d("ttttttt-->全仓做空--浮动盈亏收益率", floatProfitRate.toString())
                    }
                } else if (positionBean.positionType.equals("ISOLATED")) { //逐仓订单
                    positionBean.bondAmount = positionBean.isolatedMargin
                    //多仓强平价格 = (开仓均价 * 数量 * 面值 + 维持保证金 - 仓位保证金) / (数量 * 面值)
                    if (positionBean.positionSide.equals("LONG")) { //做多
                        liquidationPrice = BigDecimal(positionBean.entryPrice)
                            .multiply(BigDecimal(positionBean.positionSize))
                            .multiply(BigDecimal(contractSize.toString()))
                            .add(maintMargin)
                            .subtract(BigDecimal(positionBean.isolatedMargin))
                            .divide(
                                BigDecimal(positionBean.positionSize)
                                    .multiply(BigDecimal(contractSize.toString())),
                                4,
                                BigDecimal.ROUND_HALF_UP
                            )
                        Log.d("ttttttt-->逐仓做多--强平价格", liquidationPrice.toString())
                        floatProfit = FutureService.getFloatProfit(positionBean,flagPrice)
                        Log.d("ttttttt-->逐仓做多--浮动盈亏", floatProfit.toString())
                        //收益率=收益/isolatedMargin*100
                        floatProfitRate = floatProfit
                            .divide(
                                BigDecimal(positionBean.isolatedMargin),
                                4,
                                BigDecimal.ROUND_HALF_UP
                            )
                            .multiply(BigDecimal("100"))
                        Log.d("ttttttt-->逐仓做多--浮动盈亏收益率", floatProfitRate.toString())
                    } else if (positionBean.positionSide.equals("SHORT")) {  //做空
                        //空仓[强平价格 = (开仓均价 * 数量 * 面值 - 维持保证金 + 仓位保证金) / (数量 * 面值)
                        liquidationPrice = BigDecimal(positionBean.entryPrice)
                            .multiply(BigDecimal(positionBean.positionSize))
                            .multiply(BigDecimal(contractSize.toString()))
                            .add(BigDecimal(positionBean.isolatedMargin))
                            .subtract(maintMargin)
                            .divide(
                                BigDecimal(positionBean.positionSize)
                                    .multiply(BigDecimal(contractSize.toString())),
                                4,
                                BigDecimal.ROUND_HALF_UP
                            )
                        Log.d("ttttttt-->逐仓做空--强平价格", liquidationPrice.toString())
                        floatProfit = FutureService.getFloatProfit(positionBean,flagPrice)
                        Log.d("ttttttt-->逐仓做空--浮动盈亏", floatProfit.toString())
                        //收益率=收益/isolatedMargin*100
                        floatProfitRate = floatProfit
                            .divide(
                                BigDecimal(positionBean.isolatedMargin),
                                4,
                                BigDecimal.ROUND_HALF_UP
                            )
                            .multiply(BigDecimal("100"))
                        Log.d("ttttttt-->逐仓做多--浮动盈亏收益率", floatProfitRate.toString())
                    }

                }
                Log.d("ttttttt-->维持保证金率maintMarginRate", maintMarginRate)
                positionBean.forceStopPrice = String.format("%.2f", liquidationPrice)
                positionBean.unRealizedProfit = String.format("%.4f", floatProfit)
                positionBean.profitRate = String.format("%.2f", floatProfitRate) + "%"
                positionBean.price = String.format("%.4f", positionValue.add(floatProfit))
                Log.d("ttttttt---2222", positionBean.toString())
                //计算你的仓位价值，根据leverage bracket里的maxNominalValue找到在哪一档
//            Log.d("ttttttt-->positionValue", positionValue.toString())
            }
        }
    }

    /**
     * 获取维持保证金率
     */
    fun getMaintMarginRate(positionValue: String): String {
        var maintMarginRate = "" //维持保证金率
        if (leverageBracket == null) {
            return "0"
        }
        for (item in leverageBracket?.leverageBrackets!!) {
            //仓位价值和档位比较 ==-1 仓位价值小
            if (BigDecimal(positionValue).compareTo(BigDecimal(item?.maxNominalValue)) == -1) {
                maintMarginRate = item?.maintMarginRate
                break
            }
        }
        return maintMarginRate
    }


    /**
     * 获取交易对杠杆分层
     */
    fun getLeverageBracketDetail() {
        FutureApiServiceHelper.getLeverageBracketDetail(context, currentPairStatus?.pair, false,
            object : Callback<HttpRequestResultBean<LeverageBracketBean?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("iiiiii-->LeverageBracketDetail", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<LeverageBracketBean?>?) {
                    if (returnData != null) {
                        leverageBracket = returnData.result
                        onContractPositionModelListener?.onLeverageDetail(returnData.result)
                    }
                }
            })
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
                        onContractPositionModelListener?.onMarketPrice(marketPrice)
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
                        onContractPositionModelListener?.onFundingRate(fundRate)
                    }
                }
            })
    }

    /**
     * 获得ADL
     */
    fun getPositionAdlList(context: Context?) {
        FutureApiServiceHelper.getPositionAdl(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<ADLBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->getPositionAdlList", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<ADLBean?>?>?) {
                    if (returnData != null) {
                        adlList = returnData.result
                    }
                }
            })
    }

    /**
     * 获取adl信息
     */
    fun getAdlBean(symbol: String): ADLBean? {
        var adlBean: ADLBean? = null
        for (item in adlList!!) {
            if (item?.symbol.equals(symbol)) {
                adlBean = item!!
                break
            }
        }
        return adlBean
    }


    fun getContractSize(): String? {
        return currentPairStatus?.contractSize
    }


    fun setPrecision(precision: Int) {
        currentPairStatus?.precision = precision
    }

    fun getPrecision(): Int? {
        return currentPairStatus?.precision
    }

    fun getCurrentPriceCNY(): Double? {
        return currentPairStatus?.currentPriceCNY
    }

    fun getCurrentPrice(): Double? {
        return currentPairStatus?.currentPrice
    }

    fun getCurrentPairSymbol():String?{
        return currentPairStatus?.pair
    }


    interface OnContractPositionModelListener {
        /**
         * 标记价格变化
         */
        fun onMarketPrice(marketPrice: MarkPriceBean?)

        /**
         * 资金费率变化
         */
        fun onFundingRate(fundRate: FundingRateBean?)

        /**
         * 杠杆分层
         */
        fun onLeverageDetail(leverageBracket: LeverageBracketBean?)

        /**
         * 仓位数据获取
         */
        fun onGetPositionData(positionList: ArrayList<PositionBean?>?)

        fun onPosition(positionList: UserPositionBean?)
    }
}
