package com.black.frying.contract.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.math.BigDecimal

class FuturesOrderCreateViewModel : ViewModel() {
    companion object{
        const val ORDER_TYPE_LIMIT = "limit"
        const val ORDER_TYPE_MARKET = "market"
    }
    // TODO: Implement the ViewModel
    // 开仓 || 平仓
    val buyOrSell = MutableLiveData<Boolean>()
    // 订单类型 市价委托 ｜ 限价委托
    val orderType = MutableLiveData<Int>()

    //订单价格
    val orderPrice = MutableLiveData<BigDecimal>()
    //订单数量
    val orderNum = MutableLiveData<BigDecimal>()

    // 界面计算逻辑

}