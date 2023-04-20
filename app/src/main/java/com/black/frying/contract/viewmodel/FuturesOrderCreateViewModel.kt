package com.black.frying.contract.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.black.base.view.DeepControllerWindow
import com.black.frying.contract.state.FutureGlobalStateViewModel
import com.fbsex.exchange.R
import java.math.BigDecimal

class FuturesOrderCreateViewModel : ViewModel() {
    companion object {
        const val ORDER_TYPE_LIMIT = "LIMIT"
        const val ORDER_TYPE_MARKET = "MARKET"
    }

    var globalStateViewModel: FutureGlobalStateViewModel? = null

    // 开仓 || 平仓
    val buyOrSell = MutableLiveData<Boolean>(true)

    // 订单类型 市价委托 ｜ 限价委托
    val orderType = MutableLiveData<String>()

    //订单价格
    val orderPrice = MutableLiveData<BigDecimal>()

    //订单数量
    val orderNum = MutableLiveData<BigDecimal>()

    // 界面计算逻辑

    //止盈止损
    val showLimitPrice = MutableLiveData<Boolean>()


    fun start() {

    }

    fun refreshOrderType(){
        val typeList = getCurrentPairOrderTypeList()
        if (typeList.isNotEmpty()) {
            orderType.value = typeList.first()
        }
    }

    /**
     * 下单接口
     * 买卖方向->orderSide:BUY;SELL
     * 订单类型->orderType:LIMIT；MARKET
     * 数量（张）->origQty
     * 只减仓->reduceOnly (true,false)
     * 有效方式->timeInForce:GTC;IOC;FOK;GTX
     * 仓位方向：LONG;SHORT
     * 止盈价->triggerProfitPrice(number)
     * 止损价->triggerStopPrice(number)
     * 仓位方向->positionSide:LONG(平仓卖｜开仓买),SHORT（平仓买 ｜开仓卖）
     *
     */
    fun _orderCreate() {

//        FuturesRepository.createOrder()
    }

    fun changeOrderType(buy: Boolean) {
        buyOrSell.value = buy
    }

    fun performClickChangePriceType(activity: Activity) {
        val typeList = getCurrentPairOrderTypeList()
        if (typeList.isEmpty()) {
            return
        }
        refreshOrderType()
        DeepControllerWindow(activity,
            activity.getString(R.string.select_order_type),
            orderType.value,
            data = typeList,
            onReturnListener = object : DeepControllerWindow.OnReturnListener<String> {
                override fun onReturn(window: DeepControllerWindow<String>, item: String) {
                    orderType.value = item
                }
            }
        ).show()

    }


    private fun getCurrentPairOrderTypeList(): List<String> {
        return globalStateViewModel?.symbolBean?.let {
            val supportOrderType = it.supportOrderType
            Log.d(TAG, "getCurrentPairOrderTypeList() called  supportOrderType:$supportOrderType")
            val split = supportOrderType.split(",")
            return@let split.toList()
        } ?: emptyList()
    }
}