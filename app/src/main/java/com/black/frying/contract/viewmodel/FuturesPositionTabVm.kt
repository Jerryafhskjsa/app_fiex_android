package com.black.frying.contract.viewmodel

import android.content.Context
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.Constants
import com.black.base.model.future.PositionBean
import com.black.frying.contract.state.FutureGlobalStateViewModel
import com.black.frying.service.FutureService
import com.black.frying.service.FutureService.getAdlBean
import com.black.frying.service.FutureService.getMaintMarginRate
import com.black.util.Callback
import java.math.BigDecimal

class FuturesPositionTabVm : ViewModel() {
    lateinit var mContext: Context
    lateinit var mGlobal: FutureGlobalStateViewModel

    fun init(fragment: Fragment,global: FutureGlobalStateViewModel) {
        mContext = fragment.context!!
        mGlobal = global
        mGlobal.markPriceBeanLiveData.observe(fragment.viewLifecycleOwner){
            updateCurrentPosition(it?.p,it?.s,true)
        }
    }

     var positionListLD:MutableLiveData< ArrayList<PositionBean?>> = MutableLiveData<ArrayList<PositionBean?>>(ArrayList()) //持仓的订单

    /**
     * 获取当前持仓数据
     */
    fun getPositionData(all:Boolean?) {
        var symbol:String? = mGlobal.symbolBeanLiveData.value?.symbol
        if(all == true){
            symbol = null
        }
        FutureApiServiceHelper.getPositionList(mContext, symbol, false,
            object : Callback<HttpRequestResultBean<ArrayList<PositionBean?>?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("iiiiii-->positionData--error", error.toString())
                }

                override fun callback(returnData: HttpRequestResultBean<ArrayList<PositionBean?>?>?) {
                    returnData?.result?.apply {
                        positionListLD.apply {
                            updateCurrentPosition(null,null,false)
                            postValue( ArrayList(filter { it?.positionSize!!.toInt() > 0 }))
                        }
                    }
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
     * isSocket = true更新socket推的数据
     * isSocket = false更新http请求的数据
     */
    private fun updateCurrentPosition(flagPrice:String?,symbol: String?,isSocket:Boolean?) {
        for (positionBean in positionListLD.value!!) {
            if (positionBean?.positionSize.equals("0")) {
                return
            }
            if(isSocket == true){
                if(positionBean?.symbol.equals(symbol)){
                    positionBean?.flagPrice = flagPrice
                }
            }else{
                positionBean?.flagPrice = FutureService.getMarkPrice(positionBean?.symbol)?.p
            }
            var contractSize =  FutureService.getContractSize(symbol)
            //仓位价值=开仓均价 * 数量 * 面值
            var positionValue = BigDecimal(positionBean?.positionSize)
                .multiply(BigDecimal(positionBean?.entryPrice))
                .multiply(BigDecimal(contractSize.toString()))
            //获取维持保证金率
            var maintMarginRate = getMaintMarginRate(positionValue.toString())
            //维持保证金 = 开仓均价 * 数量 * 面值 * 维持保证金率
            var maintMargin = BigDecimal(positionBean?.positionSize)
                .multiply(BigDecimal(contractSize.toString()))
                .multiply(BigDecimal(positionBean?.entryPrice.toString()))
                .multiply(BigDecimal(maintMarginRate))
            Log.d("ttttttt--->maintMargin", maintMargin.toString())
            var adlBean = getAdlBean(positionBean?.symbol!!)
            Log.d("ttttttt--->adlBean", adlBean.toString())
            if (positionBean?.positionSide.equals(Constants.LONG)) {
                positionBean.adl = adlBean?.longQuantile
            } else {
                positionBean.adl = adlBean?.shortQuantile
            }
            var liquidationPrice: BigDecimal? = null//强平价格
            var floatProfit: BigDecimal? = null//未实现盈亏
            var floatProfitRate: BigDecimal? = null//未实现盈亏收益率
            if (positionBean?.positionType.equals("CROSSED")) { //全仓
                var positionSide = positionBean?.positionSide
                positionBean?.bondAmount = maintMargin.toString()
                //多仓强平价格 = (开仓均价 * 数量 * 面值 - dex) / (数量 * 面值)
                //空仓强平价格 = (开仓均价 * 数量 * 面值 + dex) / (数量 * 面值)
                //dex（共享保证金） = 钱包余额 - ∑逐仓仓位保证金 - ∑全仓维持保证金 - ∑委托保证金 + ∑除本仓位其他全仓仓位未实现盈亏
                if (positionBean?.positionSide.equals("LONG")) { //做多
                    liquidationPrice = BigDecimal(positionValue.toString())
                        .subtract(FutureService.getDex(positionBean!!, positionSide!!))
                        .divide(
                            BigDecimal(positionBean?.positionSize)
                                .multiply(BigDecimal(contractSize.toString())),
                            4,
                            BigDecimal.ROUND_HALF_UP
                        )
                    Log.d("ttttttt-->全仓做多--强平价格", liquidationPrice.toString())
                    floatProfit = FutureService.getFloatProfit(positionBean!!)
                    Log.d("ttttttt-->全仓做多--浮动盈亏", floatProfit.toString())
                    floatProfitRate = floatProfit
                        .divide(
                            BigDecimal(positionBean?.isolatedMargin),
                            2,
                            BigDecimal.ROUND_HALF_UP
                        )
                        .multiply(BigDecimal("100"))
                    Log.d("ttttttt-->全仓做多--浮动盈亏收益率", floatProfitRate.toString())
                } else if (positionBean?.positionSide.equals("SHORT")) { //做空
                    liquidationPrice = BigDecimal(positionValue.toString())
                        .add(FutureService.getDex(positionBean!!, positionSide!!))
                        .divide(
                            BigDecimal(positionBean?.positionSize)
                                .multiply(BigDecimal(contractSize.toString())),
                            4,
                            BigDecimal.ROUND_HALF_UP
                        )
                    Log.d("ttttttt-->全仓做空--强平价格", liquidationPrice.toString())
                    floatProfit = FutureService.getFloatProfit(positionBean!!)
                    Log.d("ttttttt-->全仓做空--浮动盈亏", floatProfit.toString())
                    floatProfitRate = floatProfit
                        .divide(
                            BigDecimal(positionBean?.isolatedMargin),
                            2,
                            BigDecimal.ROUND_HALF_UP
                        )
                        .multiply(BigDecimal("100"))
                    Log.d("ttttttt-->全仓做空--浮动盈亏收益率", floatProfitRate.toString())
                }
            } else if (positionBean?.positionType.equals("ISOLATED")) { //逐仓订单
                positionBean?.bondAmount = positionBean?.isolatedMargin
                //多仓强平价格 = (开仓均价 * 数量 * 面值 + 维持保证金 - 仓位保证金) / (数量 * 面值)
                if (positionBean?.positionSide.equals("LONG")) { //做多
                    liquidationPrice = BigDecimal(positionBean?.entryPrice)
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
                    floatProfit = FutureService.getFloatProfit(positionBean!!)
                    Log.d("ttttttt-->逐仓做多--浮动盈亏", floatProfit.toString())
                    //收益率=收益/isolatedMargin*100
                    floatProfitRate = floatProfit
                        .divide(
                            BigDecimal(positionBean?.isolatedMargin),
                            2,
                            BigDecimal.ROUND_HALF_UP
                        )
                        .multiply(BigDecimal("100"))
                    Log.d("ttttttt-->逐仓做多--浮动盈亏收益率", floatProfitRate.toString())
                } else if (positionBean?.positionSide.equals("SHORT")) {  //做空
                    //空仓[强平价格 = (开仓均价 * 数量 * 面值 - 维持保证金 + 仓位保证金) / (数量 * 面值)
                    liquidationPrice = BigDecimal(positionBean?.entryPrice)
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
                    floatProfit = FutureService.getFloatProfit(positionBean!!)
                    Log.d("ttttttt-->逐仓做空--浮动盈亏", floatProfit.toString())
                    //收益率=收益/isolatedMargin*100
                    floatProfitRate = floatProfit
                        .divide(
                            BigDecimal(positionBean?.isolatedMargin),
                            2,
                            BigDecimal.ROUND_HALF_UP
                        )
                        .multiply(BigDecimal("100"))
                    Log.d("ttttttt-->逐仓做多--浮动盈亏收益率", floatProfitRate.toString())
                }

            }
            Log.d("ttttttt-->维持保证金率maintMarginRate", maintMarginRate)
            positionBean?.forceStopPrice = liquidationPrice.toString()
            positionBean?.unRealizedProfit = floatProfit.toString()
            positionBean?.profitRate = floatProfitRate.toString() + "%"

            //计算你的仓位价值，根据leverage bracket里的maxNominalValue找到在哪一档
//            Log.d("ttttttt-->positionValue", positionValue.toString())
        }
    }

}


