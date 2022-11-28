package com.black.frying.service

import android.content.Context
import android.util.Log
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.*
import com.black.frying.model.OrderItem
import com.black.net.HttpCookieUtil
import com.black.util.Callback
import io.reactivex.Observable
import java.math.BigDecimal

object FutureService {

    val symbol="btc_usdt"

    var symbolList: ArrayList<SymbolBean>? = null
    var buyList: ArrayList<OrderItem>? = null
    var sellList: ArrayList<OrderItem>? = null

    var markPriceBeanList: ArrayList<MarkPriceBean>? = null
    var markPrice: MarkPriceBean? = null

    var positionValue: Double? = null // 仓位价值

    var contractSize: Double? = null //合约面值

    var leverageBracket:LeverageBracketResp?=null

    /**
     * 初始化合约交易对
     */
    fun getSymbolList(context: Context?) {
        FutureApiServiceHelper.getSymbolList(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<SymbolBean>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->initSymbol",error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<SymbolBean>?>?) {
                    if (returnData != null) {
                        symbolList = returnData.result
                        Log.d("ttttttt-->initSymbol","--"+symbolList?.size)
                    }
                }
            })
    }

    fun initFutureSymbol(context: Context?) {
        if (symbolList == null) {
            getSymbolList(context);
        }
    }

    fun initMarkPrice(context: Context?) {
        FutureApiServiceHelper.getMarkPrice(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<MarkPriceBean>?>?>() {
                override fun error(type: Int, error: Any?) {
                    TODO("Not yet implemented")
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<MarkPriceBean>?>?) {
                    if (returnData != null) {
                        markPriceBeanList = returnData.result
                    }
                }
            })
    }

    fun getMarkPrice(symbol: String) {
        for (markPriceBean in markPriceBeanList!!) {
            if (markPriceBean.s.equals(symbol)) {
                markPrice = markPriceBean
                break
            }
        }
    }

    /**
     * 获取交易对的合约面值
     */
    fun getContractSize(symbol: String): Double? {
        for (symbolItem in symbolList!!) {
            if (symbolItem.symbol.equals(symbol)) {
                contractSize = symbolItem.contractSize.toDouble()
                break
            }
        }
        return contractSize
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
        Log.d("ttttttt-->contractSize", contractSize.toString());
//        Observable.zip()
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
    fun getOrderPosition(context: Context?) {
        FutureApiServiceHelper.getPositionList(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<PositionBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->error", error.toString());
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<PositionBean?>?>?) {
                    if (returnData != null) {
                        var positionList = returnData.result
                        if (positionList != null) {
                            for (positionBean in positionList) {
                                //仓位价值=开仓均价 * 数量 * 面值
                                positionValue=BigDecimal(positionBean?.positionSize).multiply(BigDecimal(positionBean?.entryPrice)).multiply(
                                    BigDecimal(contractSize.toString())).toDouble()
                                var maintMarginRate=""; //维持保证金率
                                for(item in leverageBracket?.leverageBrackets!!){
                                    //仓位价值和档位比较 ==-1 仓位价值小
                                    if(BigDecimal(positionValue.toString()).compareTo(BigDecimal(item?.maxNominalValue))==-1){
                                        maintMarginRate=item?.maintMarginRate
                                        break
                                    }
                                }
                                //维持保证金 = 开仓均价 * 数量 * 面值 * 维持保证金率
                                var maintMargin=BigDecimal(positionValue.toString()).multiply(BigDecimal(maintMarginRate))
                                //强平价格=（维持保证金-仓位保证金+开仓均价*数量*面值）/数量*面值
                                if(positionBean?.positionSide.equals("LONG")){ //做多

                                }else{ //做空

                                }
                                Log.d("ttttttt-->maintMarginRate", maintMarginRate)
                                Log.d("ttttttt-->maintMargin", maintMargin.toString());
                                //计算你的仓位价值，根据leverage bracket里的maxNominalValue找到在哪一档
                                Log.d("ttttttt-->positionValue", positionValue.toString())
                                Log.d("ttttttt-->positionValue", positionBean.toString());
                            }
                        }
                    }
                }

            })
    }

    /**
     * 获取杠杆分层
     *
     */
    fun getLeverageBracketList(context: Context?) {
        FutureApiServiceHelper.getLeverageBracketList(context, false,
            object : Callback<HttpRequestResultBean<ArrayList<LeverageBracketResp?>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttttttt-->LeverageBracketList", error.toString());
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<LeverageBracketResp?>?>?) {
                    if (returnData != null) {
                        var list = returnData.result
                        if (list != null) {
                            for(item in list){
                                if(item?.symbol.equals(symbol)){
                                    leverageBracket=item
                                    break
                                }
                            }
                        }

                    }
                }
            })
    }

}