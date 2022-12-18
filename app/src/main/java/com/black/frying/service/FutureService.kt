package com.black.frying.service

import android.content.Context
import android.util.Log
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.*
import com.black.base.util.FutureSocketData
import com.black.frying.model.OrderItem
import com.black.util.Callback
import java.math.BigDecimal
import java.math.RoundingMode

object FutureService {

    val symbol = "btc_usdt"

    val underlyingType = "U_BASED"

    var symbolList: ArrayList<SymbolBean>? = null

    var orderList: OrderBean? = null

    var orderLongList: ArrayList<OrderBeanItem>? = null
    var orderShortList: ArrayList<OrderBeanItem>? = null

    var adlList: ArrayList<ADLBean?>? = null

    var balanceList: ArrayList<BalanceDetailBean>? = null  //资产列表

    var symbolBean: SymbolBean? = null

    var buyList: ArrayList<OrderItem>? = null
    var sellList: ArrayList<OrderItem>? = null

    var markPriceList: ArrayList<MarkPriceBean>? = null

    var markPrice: MarkPriceBean? = null

    var positionValue: BigDecimal? = null // 仓位价值

    var contractSize: BigDecimal? = null //合约面值

    var leverageBracket: LeverageBracketBean? = null

    var balanceDetail: BalanceDetailBean? = null

    var allPositionList: ArrayList<PositionBean?>? = null //持仓的订单

    var positionList: ArrayList<PositionBean?>? = null //持仓的订单

    var longPositionList: ArrayList<PositionBean>? = null //做多的持仓

    var shortPositionList: ArrayList<PositionBean>? = null //做空的持仓

    var userStepRate: UserStepRate? = null//用户费率


    fun initFutureData(context: Context?) {
//        if (symbolList == null) {
        initSymbolList(context)
//        }
//        if (markPriceList == null) {
        initMarkPrice(context)
//        }
//        if (positionList == null) {
        initPositionList(context)
//        }
//        if (leverageBracket == null) {
        initLeverageBracketList(context)
//        }
//        if (balanceDetail == null) {
        initBalanceList(context)
        initBalanceByCoin(context)
//        }
//        if (orderList == null) {
        initOrderList(context);
//        }
        initUserStepRate(context)

        initPositionAdl(context)

    }

    /**
     * 平仓
     * 获取可以平的数量
     */
    fun getAvailableCloseData(inputPrice: String?,currentPrice:String?): CloseData {
        var buyPrice = BigDecimal(inputPrice)
        var sellPrice = BigDecimal(inputPrice).max(BigDecimal(currentPrice))

        var longPositionBean = currentSymbolPositionValue(Constants.LONG)
        var shortPositionBean = currentSymbolPositionValue(Constants.SHORT)
        var longPositionSizeBD = BigDecimal.ZERO
        var shortPositionSizeBD = BigDecimal.ZERO
        var longBD = BigDecimal.ZERO
        var shortBD = BigDecimal.ZERO

        if (longPositionBean != null) {
            longPositionSizeBD = BigDecimal(longPositionBean?.positionSize)
            longBD = BigDecimal(longPositionBean?.availableCloseSize)
        }
        if (shortPositionBean != null) {
            shortPositionSizeBD = BigDecimal(shortPositionBean?.positionSize)
            shortBD = BigDecimal(shortPositionBean?.availableCloseSize)
        }
        var long = longBD.times(BigDecimal(contractSize.toString())).times(sellPrice)
        var short = shortBD.times(BigDecimal(contractSize.toString())).times(buyPrice)

        var longPosition =
            longPositionSizeBD.times(contractSize.toString().toBigDecimal()).times(sellPrice)

        var shortPosition =
            shortPositionSizeBD.times(contractSize.toString().toBigDecimal()).times(buyPrice)

        var closeData = CloseData(long, short, longPosition, shortPosition)
        Log.d("ttttttt-->closeData", closeData.toString());
        return closeData
    }


    fun initPositionList(context: Context?) {
        FutureApiServiceHelper.getPositionList(context, symbol = null, false,
            object : Callback<HttpRequestResultBean<ArrayList<PositionBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->initPositionList--error", error.toString());
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<PositionBean?>?>?) {
                    positionList = ArrayList()
                    allPositionList = ArrayList()
                    longPositionList = ArrayList()
                    shortPositionList = ArrayList()
                    if (returnData != null) {
                        allPositionList = returnData.result
                        for (positionBean in allPositionList!!) {
                            if (positionBean?.symbol.equals(symbol)) {
                                positionList!!.add(positionBean)
                                Log.d("ttttttt-->initPositionList", positionBean.toString());
                                if (positionBean?.positionSide.equals(Constants.LONG)) {
                                    if (positionBean != null) {
                                        longPositionList!!.add(positionBean)
                                    }
                                } else if (positionBean?.positionSide.equals(Constants.SHORT)) {
                                    if (positionBean != null) {
                                        shortPositionList!!.add(positionBean)
                                    }
                                }
                            }
                        }
                    }
                }

            })
    }

    /**
     * 初始化合约交易对
     */
    fun initSymbolList(context: Context?) {
        FutureApiServiceHelper.getSymbolList(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<SymbolBean>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->initSymbol--error", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<SymbolBean>?>?) {
                    if (returnData != null) {
                        symbolList = returnData.result
                        Log.d("ttttttt-->initSymbol", "--" + symbolList.toString())
                    }
                }
            })
    }


    /**
     * 初始化所有标记价格
     */
    fun initMarkPrice(context: Context?) {
        FutureApiServiceHelper.getMarkPrice(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<MarkPriceBean>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->initMarkPrice--error", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<MarkPriceBean>?>?) {
                    if (returnData != null) {
                        markPriceList = returnData.result
                        Log.d("ttttttt-->initMarkPrice", markPriceList.toString())
                    }
                }
            })
    }

    fun getMarkPrice(symbol: String): MarkPriceBean? {
        var markPrice: MarkPriceBean? = null
        for (markPriceBean in markPriceList!!) {
            if (markPriceBean.s.equals(symbol)) {
                markPrice = markPriceBean
                break
            }
        }
        return markPrice
    }

    /**
     * 获取交易对的合约面值
     */
    fun getContractSize(symbol: String): BigDecimal? {
        for (symbolItem in symbolList!!) {
            if (symbolItem.symbol.equals(symbol)) {
                contractSize = symbolItem.contractSize.toBigDecimal()
                break
            }
        }
        return contractSize
    }

    /**
     * 获取订单列表
     */
    fun initOrderList(context: Context?) {
        FutureApiServiceHelper.getOrderList(1, 10, Constants.UNFINISHED, context, false,
            object : Callback<HttpRequestResultBean<OrderBean>>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->initOrderList--error", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<OrderBean>?) {
                    if (returnData != null) {
                        var orderData = returnData?.result
                        var orderList = orderData?.items
                        Log.d("ttttttt-->initOrderList--", orderData.toString())
                        orderLongList = ArrayList()
                        orderShortList = ArrayList()
                        for (item in orderList!!) {
                            if (item.positionSide.equals(Constants.LONG)) {
                                orderLongList!!.add(item)
                            } else if (item.positionSide.equals(Constants.SHORT)) {
                                orderShortList!!.add(item)
                            }
                        }
                    }
                }
            })
    }

    /**
     * 获取资金费率
     */
    fun getFundingRate(context: Context?, symbol: String) {
        FutureApiServiceHelper.getFundingRate(
            symbol,
            context,
            false,
            object : Callback<HttpRequestResultBean<FundingRateBean?>?>() {
                override fun error(type: Int, error: Any?) {

                }

                override fun callback(returnData: HttpRequestResultBean<FundingRateBean?>?) {
                    Log.d("ttttttt-->getFundingRate", returnData.toString());
                }

            })
    }


    fun getDepthOrder(context: Context?, symbol: String) {
        getMarkPrice(symbol);
        FutureApiServiceHelper.getDepthData(context, symbol, 30, false,
            object : Callback<HttpRequestResultBean<DepthBean?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->error", error.toString());
                }

                override fun callback(returnData: HttpRequestResultBean<DepthBean?>?) {
                    if (returnData != null) {
                        buyList = ArrayList()
                        sellList = ArrayList()
                        val a = returnData.result?.a
                        val b = returnData.result?.b

                        if (a != null) {
                            var t = 0.0;
                            for (buy in a) {
                                //价格
                                var price = buy?.get(0)
                                //张数
                                var count = buy?.get(1)
                                //计算出每个订单的USDT数量
                                var quantity =
                                    BigDecimal(count).multiply(BigDecimal(contractSize.toString()))
//                                        .multiply(BigDecimal(markPrice?.p))
                                t = t.toBigDecimal().add(quantity).toDouble()
                                var orderBean =
                                    price?.let {
                                        OrderItem(
                                            it.toDouble(), quantity.toDouble(),
                                            count?.toInt() ?: 0, t
                                        )
                                    }
                                if (orderBean != null) {
                                    buyList!!.add(orderBean)
                                }
                            }
                        }
                        if (b != null) {
                            var t = 0.0;
                            for (sell in b) {
                                var price = sell?.get(0)
                                var count = sell?.get(1)
                                var quantity =
                                    BigDecimal(count).multiply(BigDecimal(contractSize.toString()))
//                                        .multiply(BigDecimal(markPrice?.p))

                                t = t.toBigDecimal().add(quantity).toDouble()
                                var orderBean =
                                    price?.let {
                                        OrderItem(
                                            it.toDouble(),
                                            quantity.toDouble(),
                                            count?.toInt() ?: 0,
                                            t
                                        )
                                    }
                                if (orderBean != null) {
                                    sellList!!.add(orderBean)
                                }
                            }
                        }
                    }
                    Log.d("ttttttt-->contractSize", buyList.toString());
                    Log.d("ttttttt-->contractSize", sellList.toString());
                }
            })
    }

    fun getAccountInfo(context: Context?) {
        FutureApiServiceHelper.getAccountInfo(context, false,
            object : Callback<HttpRequestResultBean<AccountInfoBean?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->error", error.toString());
                }

                override fun callback(returnData: HttpRequestResultBean<AccountInfoBean?>?) {
                    Log.d("ttttttt-->account", returnData.toString());
                }

            })
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
     */
    fun getCurrentPosition(context: Context?) {
        for (positionBean in positionList!!) {
            if (positionBean?.positionSize.equals("0")) {
                return
            }
            Log.d("ttttttt-->positionValue", positionBean.toString())
            //仓位价值=开仓均价 * 数量 * 面值
            positionValue = BigDecimal(positionBean?.positionSize)
                .multiply(BigDecimal(positionBean?.entryPrice))
                .multiply(BigDecimal(contractSize.toString()))
            //获取维持保证金率
            var maintMarginRate = getMaintMarginRate(positionValue.toString())

            //维持保证金 = 开仓均价 * 数量 * 面值 * 维持保证金率
            var maintMargin = BigDecimal(positionBean?.positionSize)
                .multiply(BigDecimal(contractSize.toString()))
                .multiply(BigDecimal(positionBean?.entryPrice.toString()))
                .multiply(BigDecimal(maintMarginRate))
            Log.d("ttttttt--->maintMargin", maintMargin.toString());

            var adlBean = getAdlBean(positionBean?.symbol!!)
            Log.d("ttttttt--->adlBean", adlBean.toString());

            if (positionBean?.positionType.equals("CROSSED")) { //全仓
                var positionSide = positionBean?.positionSide
                //多仓强平价格 = (开仓均价 * 数量 * 面值 - dex) / (数量 * 面值)
                //空仓强平价格 = (开仓均价 * 数量 * 面值 + dex) / (数量 * 面值)
                //dex（共享保证金） = 钱包余额 - ∑逐仓仓位保证金 - ∑全仓维持保证金 - ∑委托保证金 + ∑除本仓位其他全仓仓位未实现盈亏
                if (positionBean?.positionSide.equals("LONG")) { //做多
                    var liquidationPrice = BigDecimal(positionValue.toString())
                        .subtract(getDex(positionBean!!, positionSide!!))
                        .divide(
                            BigDecimal(positionBean?.positionSize)
                                .multiply(BigDecimal(contractSize.toString())),
                            4,
                            BigDecimal.ROUND_HALF_UP
                        )
                    Log.d("ttttttt-->全仓做多--强平价格", liquidationPrice.toString())
                    var floatProfit = getFloatProfit(positionBean!!)
                    Log.d("ttttttt-->全仓做多--浮动盈亏", floatProfit.toString())
                    var floatProfitRate = floatProfit
                        .divide(
                            BigDecimal(positionBean?.isolatedMargin),
                            4,
                            BigDecimal.ROUND_HALF_UP
                        )
                        .multiply(BigDecimal("100"))
                    Log.d("ttttttt-->全仓做多--浮动盈亏收益率", floatProfitRate.toString())
                } else if (positionBean?.positionSide.equals("SHORT")) { //做空
                    var liquidationPrice = BigDecimal(positionValue.toString())
                        .add(getDex(positionBean!!, positionSide!!))
                        .divide(
                            BigDecimal(positionBean?.positionSize)
                                .multiply(BigDecimal(contractSize.toString())),
                            4,
                            BigDecimal.ROUND_HALF_UP
                        )
                    Log.d("ttttttt-->全仓做空--强平价格", liquidationPrice.toString())
                    var floatProfit = getFloatProfit(positionBean!!)
                    Log.d("ttttttt-->全仓做空--浮动盈亏", floatProfit.toString())
                    var floatProfitRate = floatProfit
                        .divide(
                            BigDecimal(positionBean?.isolatedMargin),
                            4,
                            BigDecimal.ROUND_HALF_UP
                        )
                        .multiply(BigDecimal("100"))
                    Log.d("ttttttt-->全仓做空--浮动盈亏收益率", floatProfitRate.toString())
                }
            } else if (positionBean?.positionType.equals("ISOLATED")) { //逐仓订单
                //多仓强平价格 = (开仓均价 * 数量 * 面值 + 维持保证金 - 仓位保证金) / (数量 * 面值)
                if (positionBean?.positionSide.equals("LONG")) { //做多
                    var liquidationPrice = BigDecimal(positionBean?.entryPrice)
                        .multiply(BigDecimal(positionBean?.positionSize))
                        .multiply(BigDecimal(contractSize.toString()))
                        .add(maintMargin)
                        .subtract(BigDecimal(positionBean?.isolatedMargin))
                        .divide(
                            BigDecimal(positionBean?.positionSize)
                                .multiply(BigDecimal(contractSize.toString())),
                            4,
                            BigDecimal.ROUND_HALF_UP
                        )
                    Log.d("ttttttt-->逐仓做多--强平价格", liquidationPrice.toString())
                    var floatProfit = getFloatProfit(positionBean!!)
                    Log.d("ttttttt-->逐仓做多--浮动盈亏", floatProfit.toString())
                    //收益率=收益/isolatedMargin*100
                    var floatProfitRate = floatProfit
                        .divide(
                            BigDecimal(positionBean?.isolatedMargin),
                            4,
                            BigDecimal.ROUND_HALF_UP
                        )
                        .multiply(BigDecimal("100"))
                    Log.d("ttttttt-->逐仓做多--浮动盈亏收益率", floatProfitRate.toString())
//                                        var a=BigDecimal(positionBean?.positionSize).multiply(BigDecimal(contractSize.toString()))
//                                        Log.d("ttttttt-->a",a.toString())
//                                        var b=BigDecimal(positionBean?.entryPrice).multiply(BigDecimal(positionBean?.isolatedMargin).subtract(maintMargin))
//                                        Log.d("ttttttt-->b",b.toString())
//                                        var c=a.add(b)
//                                        Log.d("ttttttt-->c",c.toString())
//                                        var d=BigDecimal(positionValue.toString()).divide(c,4,BigDecimal.ROUND_HALF_UP)
//                                        Log.d("ttttttt-->d",d.toString())
//                                       var price=BigDecimal(positionValue.toString()).divide(BigDecimal(positionBean?.positionSize)
//                                           .multiply(BigDecimal(contractSize.toString())).add(BigDecimal(positionBean?.entryPrice)
//                                               .multiply(BigDecimal(positionBean?.isolatedMargin).subtract(maintMargin))),4,BigDecimal.ROUND_HALF_UP)
//                                        Log.d("ttttttt-->做多强平价格", price.toString())
                } else if (positionBean?.positionSide.equals("SHORT")) {  //做空
                    //空仓[强平价格 = (开仓均价 * 数量 * 面值 - 维持保证金 + 仓位保证金) / (数量 * 面值)
                    var liquidationPrice = BigDecimal(positionBean?.entryPrice)
                        .multiply(BigDecimal(positionBean?.positionSize))
                        .multiply(BigDecimal(contractSize.toString()))
                        .add(BigDecimal(positionBean?.isolatedMargin))
                        .subtract(maintMargin)
                        .divide(
                            BigDecimal(positionBean?.positionSize)
                                .multiply(BigDecimal(contractSize.toString())),
                            4,
                            BigDecimal.ROUND_HALF_UP
                        )
                    Log.d("ttttttt-->逐仓做空--强平价格", liquidationPrice.toString())
                    var floatProfit = getFloatProfit(positionBean!!)
                    Log.d("ttttttt-->逐仓做空--浮动盈亏", floatProfit.toString())
                    //收益率=收益/isolatedMargin*100
                    var floatProfitRate = floatProfit
                        .divide(
                            BigDecimal(positionBean?.isolatedMargin),
                            4,
                            BigDecimal.ROUND_HALF_UP
                        )
                        .multiply(BigDecimal("100"))
                    Log.d("ttttttt-->逐仓做多--浮动盈亏收益率", floatProfitRate.toString())
//                                        var a=BigDecimal(positionBean?.positionSize).multiply(BigDecimal(contractSize.toString()))
//                                        var b=BigDecimal(positionBean?.entryPrice).multiply(maintMargin.subtract(BigDecimal(positionBean?.isolatedMargin)))
//                                        var c=a.add(b)
//                                        Log.d("ttttttt-->c",c.toString())
//                                        var d=BigDecimal(positionValue.toString()).divide(c,4,BigDecimal.ROUND_HALF_UP)
//                                        Log.d("ttttttt-->d",d.toString())
//
//                                        var price=BigDecimal(positionValue.toString()).divide(BigDecimal(positionBean?.positionSize)
//                                            .multiply(BigDecimal(contractSize.toString())).add(BigDecimal(positionBean?.entryPrice)
//                                                .multiply(maintMargin.subtract(BigDecimal(positionBean?.isolatedMargin)))),4,BigDecimal.ROUND_HALF_UP)
//                                        Log.d("ttttttt-->做空强平价格", price.toString())
                }

            }
            Log.d("ttttttt-->维持保证金率maintMarginRate", maintMarginRate)
            //计算你的仓位价值，根据leverage bracket里的maxNominalValue找到在哪一档
            Log.d("ttttttt-->positionValue", positionValue.toString())
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
     * 获取最大名义价值
     */
    fun getMaxNominalValue(leverage: Int): String {
        var maxNominalValue = "" //维持保证金率
        if (leverageBracket == null) {
            return "0"
        }
        for (item in leverageBracket?.leverageBrackets!!) {
//            Log.d("ttttttt-->该层最大名义价值", item?.maxNominalValue)
            //根据当前的杠杆备注 找到所处的位置最大价值
            if (BigDecimal(leverage).compareTo(BigDecimal(item?.maxLeverage)) == -1) {
                maxNominalValue = item?.maxNominalValue
            }
        }
        return maxNominalValue
    }

    /**
     * 获取资产列表
     */
    fun initBalanceList(context: Context?) {
        FutureApiServiceHelper.getBalanceList(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<BalanceDetailBean>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->initBalanceList", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<BalanceDetailBean>?>?) {
                    if (returnData != null) {
                        balanceList = returnData?.result
                        Log.d("ttttttt-->initBalanceList", balanceList.toString())
                    }

                }

            })
    }

    /**
     * 获取资产
     */
    fun initBalanceByCoin(context: Context?) {
        var coin = symbol.split("_")[1]
        FutureApiServiceHelper.getBalanceDetail(context, coin, underlyingType, false,
            object : Callback<HttpRequestResultBean<BalanceDetailBean?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->getBalanceDetail", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<BalanceDetailBean?>?) {
                    Log.d("ttttttt-->getBalanceDetail", returnData?.result.toString())
                    if (returnData != null) {
                        balanceDetail = returnData?.result!!
                    }
                }
            })
    }


    /**
     * 获取dex值
     */
    fun getDex(positionBean: PositionBean, positionSide: String): BigDecimal {

        var crossMaintMargin: BigDecimal = BigDecimal(0)
        var crossedFloatProfit: BigDecimal = BigDecimal(0)
        var entryPrice = positionBean.entryPrice
        var positionSize = positionBean.positionSize
        var maintMarginRate =
            getMaintMarginRate(getValue(entryPrice!!, positionSize!!, contractSize.toString()))
        if (positionBean?.positionType?.equals(Constants.ISOLATED) == true && !positionBean.positionSide.equals(
                "0"
            )
        ) {
            if (underlyingType.equals("U_BASED")) {
                if (positionBean?.positionSide.equals(positionSide)) {
                    var fp = getFloatProfit(positionBean);
                    crossedFloatProfit = crossedFloatProfit.plus(fp);
                }
                crossMaintMargin = crossMaintMargin.plus(
                    BigDecimal(getValue(entryPrice, positionSize, contractSize.toString())).times(
                        BigDecimal(maintMarginRate)
                    ),
                )
            } else if (underlyingType.equals("COIN_BASED")) {
                if (!positionBean?.positionSide.equals(positionSide)) {
                    var fp = getFloatProfit(positionBean);
                    crossedFloatProfit = crossedFloatProfit.plus(fp);
                }
                crossMaintMargin = crossMaintMargin.plus(
                    BigDecimal(
                        getCoinValue(
                            entryPrice,
                            positionSize,
                            contractSize.toString()
                        )
                    ).times(BigDecimal(maintMarginRate)),
                );
            }
        }
        return BigDecimal(balanceDetail?.walletBalance)
            .minus(BigDecimal(balanceDetail?.isolatedMargin))
            .minus(crossMaintMargin)
            .minus(BigDecimal(balanceDetail?.openOrderMarginFrozen))
            .plus(crossedFloatProfit)
    }

    /**
     * 获取浮动盈亏
     */
    fun getFloatProfit(positionBean: PositionBean): BigDecimal {

        var markPriceBean = FutureSocketData.markPrice
        if (markPriceBean == null) {
            markPriceBean = getMarkPrice(symbol)
        }
        Log.d("ttt---->markPrice--", markPriceBean?.p)

        var floatProfit: BigDecimal = BigDecimal(0)
        var base = BigDecimal(positionBean?.positionSize).multiply(contractSize)
        if (underlyingType.equals("U_BASED")) {
            if (positionBean.positionSide.equals("LONG")) {
                floatProfit =
                    BigDecimal(markPriceBean?.p).subtract(BigDecimal(positionBean.entryPrice))
                        .multiply(base)
            } else if (positionBean.positionSide.equals("SHORT")) {
                floatProfit =
                    BigDecimal(positionBean.entryPrice).subtract(BigDecimal(markPriceBean?.p))
                        .multiply(base)
            }
        } else if (underlyingType.equals("COIN_BASED")) { //币本位
            if (positionBean.positionSide.equals("LONG")) {
                floatProfit = BigDecimal("1")
                    .divide(BigDecimal(positionBean.entryPrice), 4, BigDecimal.ROUND_HALF_UP)
                    .subtract(
                        BigDecimal("1")
                            .divide(BigDecimal(markPriceBean?.p), 4, BigDecimal.ROUND_HALF_UP)
                    )
                    .multiply(base)
            } else if (positionBean.positionSide.equals("SHORT")) {
                floatProfit = BigDecimal("1")
                    .divide(BigDecimal(markPriceBean?.p), 4, BigDecimal.ROUND_HALF_UP)
                    .subtract(
                        BigDecimal("1")
                            .divide(
                                BigDecimal(positionBean.entryPrice),
                                4,
                                BigDecimal.ROUND_HALF_UP
                            )
                    )
                    .multiply(base)
            }
        }
        return floatProfit
    }


    /**
     * 计算获取浮动盈亏
     */
    fun getFloatProfit(positionBean: PositionBean, markPrice: MarkPriceBean): BigDecimal {

        var floatProfit: BigDecimal = BigDecimal(0)
        var base = BigDecimal(positionBean?.positionSize).multiply(contractSize)
        if (underlyingType.equals("U_BASED")) {
            if (positionBean.positionSide.equals("LONG")) {
                floatProfit =
                    BigDecimal(markPrice?.p).subtract(BigDecimal(positionBean.entryPrice))
                        .multiply(base)
            } else if (positionBean.positionSide.equals("SHORT")) {
                floatProfit =
                    BigDecimal(positionBean.entryPrice).subtract(BigDecimal(markPrice?.p))
                        .multiply(base)
            }
        } else if (underlyingType.equals("COIN_BASED")) { //币本位
            if (positionBean.positionSide.equals("LONG")) {
                floatProfit = BigDecimal("1")
                    .divide(BigDecimal(positionBean.entryPrice), 4, BigDecimal.ROUND_HALF_UP)
                    .subtract(
                        BigDecimal("1")
                            .divide(BigDecimal(markPrice?.p), 4, BigDecimal.ROUND_HALF_UP)
                    )
                    .multiply(base)
            } else if (positionBean.positionSide.equals("SHORT")) {
                floatProfit = BigDecimal("1")
                    .divide(BigDecimal(markPrice?.p), 4, BigDecimal.ROUND_HALF_UP)
                    .subtract(
                        BigDecimal("1")
                            .divide(
                                BigDecimal(positionBean.entryPrice),
                                4,
                                BigDecimal.ROUND_HALF_UP
                            )
                    )
                    .multiply(base)
            }
        }
        return floatProfit
    }


    /**
     * 获取杠杆分层
     *
     */
    fun initLeverageBracketList(context: Context?) {
        FutureApiServiceHelper.getLeverageBracketList(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<LeverageBracketBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->LeverageBracketList", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<LeverageBracketBean?>?>?) {
                    if (returnData != null) {
                        var list = returnData.result
                        Log.d("ttttttt-->LeverageBracketList", list.toString())
                        if (list != null) {
                            for (item in list) {
                                if (item?.symbol.equals(symbol)) {
                                    leverageBracket = item
                                    break
                                }
                            }
                        }

                    }
                }
            })
    }

    /**
     * 获得ADL
     */
    fun initPositionAdl(context: Context?) {
        FutureApiServiceHelper.getPositionAdl(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<ADLBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->LeverageBracketList", error.toString())
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

    /**
     * 下单
     */
    fun createOrder(
        context: Context?,
        orderSide: String,
        orderType: String,
        symbol: String?,
        positionSide: String?,
        price: Double?,
        timeInForce: String?,
        origQty: Int,
        reduceOnly: Boolean?
    ) {
        FutureApiServiceHelper.createOrder(context,
            orderSide,
            orderType,
            symbol,
            positionSide,
            price,
            timeInForce,
            origQty,
            null,
            null,
            reduceOnly,
            false,
            object : Callback<HttpRequestResultBean<String>?>() {
                override fun callback(returnData: HttpRequestResultBean<String>?) {
                    if (returnData != null) {
                        Log.d("ttttttt-->createOrder", returnData.result.toString())
                    }
                }

                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->createOrder--error", error.toString())
                }

            })
    }

    /**
     * 获取用户的阶梯费率
     */
    fun initUserStepRate(context: Context?) {
        FutureApiServiceHelper.getUserStepRate(context, false,
            object : Callback<HttpRequestResultBean<UserStepRate>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->initUserStepRate--error", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<UserStepRate>?) {
                    if (returnData != null) {
                        userStepRate = returnData?.result
                        Log.d("ttttttt-->initUserStepRate", userStepRate.toString())
                    }
                }

            })
    }

    /**
     * U本位时：
    余额多仓最大可开 = 可用余额 / (最新成交价【输入框中的价格】* 面值 * (起始保证金率 + 2 * Taker费率))；
    风险档位多仓最大可开 = (最大名义价值 - 持仓价值 - 订单名义价值) / (最新成交价【输入框中的价格】* 面值)
    可开多:Min(余额多仓最大可开,风险档位多仓最大可开)
    其中最大名义价值 = 当前杠杆所处的最大档位中的最大名义价值

    余额空仓最大可开 = 可用余额 / ((最新成交价【max(最新成交价，输入框中的价格）】* 面值 * ((起始保证金率+Taker费率) * (1 + Taker费率) + Taker费率 * (1 - 维持保证金率))))
    风险档位空仓最大可开 =  (最大名义价值 - 持仓价值 - 订单名义价值) / ((最新成交价【max(最新成交价, 输入框中的价格)】* 面值)
    可开空：Min(余额空仓最大可开,风险档位空仓最大可开)
    其中起始保证金率=1/杠杆倍数
    维持保证金率根据杠杆倍数在杠杆分层接口里匹配
     */
    fun getAvailableOpenData(
        inputPrice: BigDecimal,
        leverage: Int,
        amount: BigDecimal,
        amountPercent: BigDecimal
    ) {

        var buyPrice = inputPrice;

        var longMaxOpenSheet =
            getUserLongMaxOpen(buyPrice, leverage).setScale(0, BigDecimal.ROUND_DOWN)

        var longMaxOpen =
            sheet2CurrentUnit(longMaxOpenSheet.toString(), buyPrice.toString())
        Log.d("ttttttt-->longMaxOpen---", longMaxOpen.toString())
        var longInputSheetAmount = currentUnit2Sheet(amount, buyPrice)
//        Log.d("ttttttt-->longInputSheetAmount---", longInputSheetAmount.toString())
        var longSheetAmount: BigDecimal = BigDecimal.ZERO
        longSheetAmount = if (longInputSheetAmount.compareTo(BigDecimal(0)) == 1) {
            longInputSheetAmount.setScale(0, RoundingMode.DOWN)
        } else {
            longMaxOpen.multiply(amountPercent.divide(BigDecimal(100), 4, RoundingMode.DOWN))
                .setScale(0, RoundingMode.DOWN)
        }
//        Log.d("ttttttt-->longSheetAmount--", longSheetAmount.toString())

        var longMargin = getLongMargin(buyPrice, longSheetAmount, leverage)
        Log.d("ttttttt-->longMargin---", longMargin.toString())


        //获取最新成交价
        var tickerBean = FutureSocketData.tickerList.get(symbol)
        var sellPrice = inputPrice.max(BigDecimal(tickerBean?.c))
        var shortMaxOpenSheet =
            getUserShortMaxOpen(sellPrice, leverage).setScale(0, BigDecimal.ROUND_DOWN)
        var shortMaxOpen =
            sheet2CurrentUnit(shortMaxOpenSheet.toString(), sellPrice.toString())
        var shortInputSheetAmount = currentUnit2Sheet(amount, sellPrice)
//        Log.d("ttttttt-->shortInputSheetAmount---", shortInputSheetAmount.toString())

        var shortSheetAmount: BigDecimal = BigDecimal.ZERO
        shortSheetAmount = if (shortInputSheetAmount.compareTo(BigDecimal(0)) == 1) {
            shortInputSheetAmount.setScale(0, RoundingMode.DOWN)
        } else {
            shortMaxOpen.multiply(amountPercent.divide(BigDecimal(100), 4, RoundingMode.DOWN))
                .setScale(0, RoundingMode.DOWN)
        }
//        Log.d("ttttttt-->shortSheetAmount---", shortSheetAmount.toString())
        Log.d("ttttttt-->shortMaxOpen--", shortMaxOpen.toString())
        var shortMargin = getShortMargin(sellPrice, shortSheetAmount, leverage)
        Log.d("ttttttt-->shortMargin--", shortMargin.toString())

    }

    /**
     * 计算多仓起始保证金
     * 多仓起始保证金 = 最新成交价【输入框中的价格】* 持仓数量 * 面值 * (起始保证金率 + 2 * Taker费率)
     * 起始保证金率 = 1 / 杠杆倍数
     * 计算多仓起始保证金
     * @param price: 最新成交价【输入框中的价格】
     * @param amount: 张数
     */
    fun getLongMargin(price: BigDecimal, amount: BigDecimal, leverage: Int): BigDecimal {
        var positonValue =
            getValue(price.toString(), amount.toString(), contractSize.toString()).toBigDecimal()

        var result =
            positonValue
                .multiply(
                    BigDecimal(1).divide(
                        BigDecimal(leverage),
                        4,
                        RoundingMode.DOWN
                    ).add(
                        BigDecimal(userStepRate?.takerFee).times(
                            BigDecimal(2)
                        )
                    )
                )

        return result
    }

    /**
     *
     * 计算空仓起始保证金
     * 空仓起始保证金 = 最新成交价【max(最新成交价，输入框中的价格）】* 持仓数量 * 面值 * (起始保证金率 * (1 + Taker费率) + Taker费率 * (1 - 维持保证金率))
     * 起始保证金率 = 1 / 杠杆倍数
     * 维持保证金率 = 杠杆所处的最大档位的维持保证金率
     * @param price: 最新成交价【max(最新成交价，输入框中的价格）】
     * @param amount: 张数
     */
    fun getShortMargin(price: BigDecimal, amount: BigDecimal, leverage: Int): BigDecimal {

        //维持保证金率
        var maintMarginRate = getLeverageMaxBracket(leverage)?.maintMarginRate

        var positonValue =
            getValue(price.toString(), amount.toString(), contractSize.toString()).toBigDecimal()


        var a =
            BigDecimal(userStepRate?.takerFee).multiply(BigDecimal.ONE.subtract(maintMarginRate?.toBigDecimal()))

        var b = BigDecimal(1).divide(BigDecimal(leverage), 4, RoundingMode.DOWN)
            .multiply(BigDecimal.ONE.add(BigDecimal(userStepRate?.takerFee)))


        var result = positonValue.multiply(a.add(b))
        return result
    }

    /**
     * 可开空
     * 最新成交价【max(最新成交价，输入框中的价格）】
     */
    fun getUserShortMaxOpen(sellPrice: BigDecimal, leverage: Int): BigDecimal {

        var b = getBalanceShortMaxOpen(sellPrice, leverage)
        var a = getBracketShortMaxAmount(sellPrice, leverage)
//        Log.d("ttttttt-->shortMaxOpen--a", a.toString())
//        Log.d("ttttttt-->shortMaxOpen--b", b.toString())
        return a.min(b)
    }


    /**
     * 可开多
     */
    fun getUserLongMaxOpen(buyPrice: BigDecimal, leverage: Int): BigDecimal {
        var a = getBracketLongMaxAmount(buyPrice, leverage)
        var b = getBalanceLongMaxOpen(buyPrice, leverage)
//        Log.d("ttttttt-->longMaxOpen--a", a.toString())
//        Log.d("ttttttt-->longMaxOpen--b", b.toString())
        return a.min(b)
    }

    /**
     * U本位时
     * 风险档位空仓最大可开 =  (最大名义价值 - 持仓价值 - 订单名义价值) / ((最新成交价【max(最新成交价, 输入框中的价格)】* 面值)
     *
     */
    fun getBracketShortMaxAmount(price: BigDecimal, leverage: Int): BigDecimal {
        //最大名义价值
        var maxNominalValue = getMaxNominalValue(leverage)

        //订单名义价值
        var orderValue = currentSymbolOrderValue(Constants.SHORT)

        var positionBean = currentSymbolPositionValue(Constants.SHORT)
        //持仓价值
        var positionValue = BigDecimal(positionBean?.positionSize)
            .multiply(BigDecimal(positionBean?.entryPrice))
            .multiply(BigDecimal(contractSize.toString()))
        var result = BigDecimal(maxNominalValue)
            .minus(BigDecimal(positionValue.toString()))
            .minus(orderValue!!)
            .divide(price.times(contractSize!!), 8, RoundingMode.DOWN)
        return result
    }

    /**
     * U本位时
     * 余额空仓最大可开 = 可用余额 / ((最新成交价【max(最新成交价，输入框中的价格）】* 面值 * ((起始保证金率+Taker费率) * (1 + Taker费率) + Taker费率 * (1 - 维持保证金率))))
     * 1. 起始保证金率 = 1 / 杠杆倍数
     * 2. 维持保证金率 = 杠杆所处的最大档位的维持保证金率
     */
    fun getBalanceShortMaxOpen(price: BigDecimal, leverage: Int): BigDecimal {
        var availableBalanceDisplay = getAvailableBalanceDisplay(balanceDetail!!)

        var b = price.multiply(contractSize)
            .multiply(
                BigDecimal("1").divide(BigDecimal(leverage), 8, RoundingMode.DOWN)
                    .add(BigDecimal(userStepRate?.takerFee))
                    .multiply(BigDecimal(1).add(BigDecimal(userStepRate?.takerFee)))
                    .add(
                        BigDecimal(userStepRate?.takerFee).multiply(
                            BigDecimal(1).subtract(
                                BigDecimal(
                                    getLeverageMaxBracket(leverage)?.maintMarginRate
                                )
                            )
                        )
                    )
            )
        var result = availableBalanceDisplay.divide(b, 8, RoundingMode.DOWN)
        return result
    }

    /**
     * U本位时
     * 风险档位多仓最大可开 = (最大名义价值 - 持仓价值 - 订单名义价值) / (最新成交价【输入框中的价格】* 面值)
     */
    fun getBracketLongMaxAmount(inputPrice: BigDecimal, leverage: Int): BigDecimal {
        //最大名义价值
        var maxNominalValue = getMaxNominalValue(leverage)

        //订单名义价值
        var orderValue = currentSymbolOrderValue(Constants.LONG)

        var positionBean = currentSymbolPositionValue(Constants.LONG)
        //持仓价值
        var positionValue = BigDecimal(positionBean?.positionSize)
            .multiply(BigDecimal(positionBean?.entryPrice))
            .multiply(BigDecimal(contractSize.toString()))
        var result = BigDecimal(maxNominalValue)
            .minus(BigDecimal(positionValue.toString()))
            .minus(orderValue!!)
            .divide(inputPrice.times(contractSize!!), 8, RoundingMode.DOWN)
        return result
    }

    /**
     * 当前交易对持仓价值
     */
    fun currentSymbolPositionValue(positionSide: String): PositionBean {
        var longPositionBean: PositionBean? = null
        var shortPositionBean: PositionBean? = null
        for (item in longPositionList!!) {
            longPositionBean = item
        }
        for (item in shortPositionList!!) {
            shortPositionBean = item
        }
        if (positionSide.equals(Constants.LONG)) {
            return longPositionBean!!
        } else {
            return shortPositionBean!!
        }
    }

    /**
     * U本位时
     * 余额多仓最大可开 = 可用余额 / (最新成交价【输入框中的价格】* 面值 * (起始保证金率 + 2 * Taker费率))
     */
    fun getBalanceLongMaxOpen(inputPrice: BigDecimal, leverage: Int): BigDecimal {

        //可用余额 = max(0，真实可用 + ∑该结算货币下的全仓未实现盈亏)
        var availableBalanceDisplay = getAvailableBalanceDisplay(balanceDetail!!)
        if (availableBalanceDisplay == null) {
            return BigDecimal.ZERO
        }
        //余额多仓最大可开
        var result = availableBalanceDisplay.divide(
            inputPrice.multiply(contractSize).multiply(
                BigDecimal("1").divide(BigDecimal(leverage), 8, RoundingMode.DOWN)
                    .add(BigDecimal(userStepRate?.takerFee).multiply(BigDecimal("2")))
            ),
            8, RoundingMode.DOWN
        )
        return result

    }

    /**
     * 该结算货币下的全仓未实现盈亏
     */
    fun getAvailableBalanceDisplay(balanceDetail: BalanceDetailBean): BigDecimal {

        var coin = balanceDetail?.coin
        var availableBalance = balanceDetail.availableBalance

        var floatProfit: BigDecimal? = BigDecimal(0)
        var crossedFloatProfit: BigDecimal? = BigDecimal(0)

        if (coin.equals("usdt")) {
            for (p in positionList!!) {
                if (p?.symbol!!.split("_")[1].equals("usdt")) {
                    var fp = getFloatProfit(p!!)
                    floatProfit = floatProfit!!.plus(fp)
                    if (p.positionType.equals(Constants.CROSSED)) {
                        crossedFloatProfit = crossedFloatProfit!!.plus(fp)
                    }
                }
            }
        } else {
            for (p in positionList!!) {
                if (p?.symbol!!.split("_")[0].equals(coin)) {
                    var fp = getFloatProfit(p!!)
                    floatProfit = floatProfit!!.plus(fp)
                    if (p.positionType.equals(Constants.CROSSED)) {
                        crossedFloatProfit = crossedFloatProfit!!.plus(fp)
                    }
                }
            }
        }
//        Log.d("ttt--->availableBalanceDisplay", availableBalance)
//        Log.d("ttt--->crossedFloatProfit", crossedFloatProfit.toString())
        var a = BigDecimal(availableBalance).plus(crossedFloatProfit!!)
        // 可用余额 = max(0，真实可用 + ∑该结算货币下的全仓未实现盈亏)
        return BigDecimal(0).max(a)
    }


    /**
     *  当前交易对订单名义价值
    // U本位
    // 1. 多仓订单名义价值 = 订单占用保证金 / (初始保证金率 + 2 * taker费率)
    // 2. 空仓订单名义价值 = 订单占用保证金 / (起始保证金率 * (1 + Taker费率) + Taker费率 * (1 - 维持保证金率))
    // 币本位
    // 多仓：订单名义价值 = 订单占用保证金 / (起始保证金率 * (1 + Taker费率) + Taker费率 * (1 - 维持保证金率【杠杆倍数】))
    // 空仓：订单名义价值 = 订单占用保证金 / (起始保证金率 + 2 * Taker费率)
     */
    fun currentSymbolOrderValue(positionSide: String): BigDecimal {

        var longResult = BigDecimal(0)
        var shortResult = BigDecimal(0)
        var longLeverage = 20
        var shortLeverage = 20
        for (item in longPositionList!!) {
            longLeverage = item.leverage!!
        }
        for (item in shortPositionList!!) {
            shortLeverage = item.leverage!!
        }
        //U本位
        if (underlyingType.equals("U_BASED")) {
            for (item in orderLongList!!) {
                var value = BigDecimal(item.marginFrozen).divide(
                    BigDecimal(1).divide(BigDecimal(longLeverage), 8, RoundingMode.DOWN).plus(
                        BigDecimal(userStepRate?.takerFee).times(
                            BigDecimal(
                                2
                            )
                        )
                    ),
                    8, RoundingMode.DOWN
                )
                longResult = longResult.add(value)
            }
            for (item in orderShortList!!) {
                var value = BigDecimal(item.marginFrozen).divide(
                    BigDecimal(1)
                        .divide(BigDecimal(shortLeverage), 4, RoundingMode.DOWN)
                        .times(BigDecimal(1).plus(BigDecimal(userStepRate?.takerFee)))
                        .plus(
                            BigDecimal(1).minus(BigDecimal(getLeverageMaxBracket(shortLeverage)?.maintMarginRate))
                                .times(BigDecimal(userStepRate?.takerFee))
                        ), 4, RoundingMode.DOWN
                )
                shortResult = shortResult.add(value);
            }

        } else if (underlyingType.equals("COIN_BASED")) { //币本位
            for (item in orderLongList!!) {
                var value = BigDecimal(item.marginFrozen).divide(
                    BigDecimal(1)
                        .divide(BigDecimal(longLeverage), 4, RoundingMode.DOWN)
                        .times(BigDecimal(1).plus(BigDecimal(userStepRate?.takerFee)))
                        .plus(
                            BigDecimal(1).minus(BigDecimal(getLeverageMaxBracket(longLeverage)?.maintMarginRate))
                                .times(BigDecimal(userStepRate?.takerFee))
                        ), 4, RoundingMode.DOWN
                )
                longResult = longResult.add(value);
            }

            for (item in orderShortList!!) {
                var value = BigDecimal(item.marginFrozen).divide(
                    BigDecimal(1).divide(BigDecimal(shortLeverage), 4, RoundingMode.DOWN).plus(
                        BigDecimal(userStepRate?.takerFee).times(
                            BigDecimal(
                                2
                            )
                        )
                    ),
                    4, RoundingMode.DOWN
                )
                shortResult = shortResult.add(value)
            }
        }
        if (positionSide.equals(Constants.LONG)) {
            return longResult
        } else {
            return shortResult
        }
    }


    /**
     * 获取杠杆所处的的最高风险档位
     */
    fun getLeverageMaxBracket(leverage: Int): LeverageBracket? {
        var leverageBracketItem: LeverageBracket? = null
        for (item in leverageBracket?.leverageBrackets!!) {
            if (leverage < item?.maxLeverage.toInt()) {
                leverageBracketItem = item
            }
        }
        return leverageBracketItem
    }

    /**
     * 获取价值
     */
    fun getValue(price: String, amount: String, contractSize: String): String {
        var result = BigDecimal(price).times(BigDecimal(amount)).times(BigDecimal(contractSize))
        return result.toString()
    }

    fun getCoinValue(price: String, amount: String, contractSize: String): String {
        var result = BigDecimal(amount).times(BigDecimal(contractSize)).div(BigDecimal(price))
        return result.toString()
    }


    fun sheet2CurrentUnit(positionSize: String, price: String): BigDecimal {
        var resultBN =
            BigDecimal(positionSize).times(contractSize!!).times(BigDecimal(price))
        return resultBN
    }

    fun currentUnit2Sheet(value: BigDecimal, price: BigDecimal): BigDecimal {
        return usdt2Sheet(value, price)
    }

    fun usdt2Sheet(value: BigDecimal, price: BigDecimal): BigDecimal {

        var result =
            value.divide(price, 8, RoundingMode.DOWN).divide(contractSize, 8, RoundingMode.DOWN)
        return result
    }
}