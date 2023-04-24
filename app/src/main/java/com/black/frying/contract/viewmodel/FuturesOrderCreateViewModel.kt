package com.black.frying.contract.viewmodel

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.black.base.view.DeepControllerWindow
import com.black.frying.contract.biz.model.FuturesRepository
import com.black.frying.contract.state.FutureGlobalStateViewModel
import com.fbsex.exchange.R
import kotlinx.coroutines.launch
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
    val orderType = MutableLiveData<String>(ORDER_TYPE_LIMIT)

    //订单价格
    val orderPrice = MutableLiveData<BigDecimal>()

    //订单数量
    val orderNum = MutableLiveData<BigDecimal>()

    val timeInForce = MutableLiveData<String>("GTC")

    // 界面计算逻辑

    //止盈止损
    val showLimitPrice = MutableLiveData<Boolean>(false)


    fun start() {

    }

    fun refreshOrderType() {
        val typeList = getCurrentPairOrderTypeList()
        if (typeList.isNotEmpty()) {
            orderType.value = typeList.first()
        }
    }
    fun openPosition(){
        startPosition()
    }
    fun startPosition() {
        val openOption = buyOrSell.value
        //开仓
        val (orderSide, positionSide) = if (openOption == true) {
            Pair("BUY", "LONG")
        } else {
            Pair("SELL", "SHORT")
        }
        //limit market
        val orderType = orderType.value
        if (orderType.isNullOrEmpty()){
            return
        }
        val reduceOnly = false
        val timeInForce = "GTC"//select
        //reduceOnly
        val origQty = getContractCount()//计算 合约张数

        val showLimit = showLimitPrice.value
        val (triggerProfitPrice, triggerStopPrice) = if (showLimit == true) {
            //收集 价格
            Pair(BigDecimal.ZERO, BigDecimal.ZERO)
        } else {
            Pair(BigDecimal.ZERO, BigDecimal.ZERO)
        }

        _orderCreate(
            orderSide,
            orderType,
            positionSide,
            origQty,
            reduceOnly,
            timeInForce,
            triggerProfitPrice,
            triggerStopPrice
        )
    }


    fun closePosition() {
        startPosition()
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
    private fun _orderCreate(
        orderSide: String,
        orderType: String,
        positionSide: String,
        origQty: BigDecimal,
        reduceOnly: Boolean,
        timeInForce: String,
        triggerProfitPrice: BigDecimal?,
        triggerStopPrice: BigDecimal
    ) {
        globalStateViewModel?.symbolBean?.let { symbolBean ->
            val symbol = symbolBean.symbol
            viewModelScope.launch {
                //确认订单信息后提交
                //
                val price = getCurrentPrice()//获取限价 或者 买一卖一价
                val createOrder = FuturesRepository.createOrder(
                    symbol,
                    origQty,
                    orderType,
                    price,
                    timeInForce,
                    orderSide,
                    positionSide,
                    triggerProfitPrice,
                    triggerStopPrice,
                    reduceOnly
                )
                Log.e(TAG, "_orderCreate: createOrder :$createOrder")
            }
        }


    }

    private fun getContractCount(): BigDecimal {
        return BigDecimal.ZERO
    }
    fun getCurrentPrice(): BigDecimal {
        return BigDecimal.ZERO
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

     fun getTimeInForceList(): List<String> {
        return globalStateViewModel?.symbolBean?.let {
            val timeInfoFore = it.supportTimeInForce
            Log.d(TAG, "getTimeInForceList() called  timeInfoFore:$timeInfoFore")
            val split = timeInfoFore.split(",")
            return@let split.toList()
        } ?: emptyList()
    }


    private fun getCurrentPairOrderTypeList(): List<String> {
        return globalStateViewModel?.symbolBean?.let {
            val supportOrderType = it.supportOrderType
            Log.d(TAG, "getCurrentPairOrderTypeList() called  supportOrderType:$supportOrderType")
            val split = supportOrderType.split(",")
            return@let split.toList()
        } ?: emptyList()
    }

    fun performClickShowLimitInput(checked: Boolean) {
        showLimitPrice.value = checked
    }

    fun selectTimeInForce(item: String) {
        timeInForce.value = item
    }
}