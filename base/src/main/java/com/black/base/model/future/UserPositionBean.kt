package com.black.base.model.future

data class UserPositionBean(
    val availableCloseSize: String,//  可平仓张数
    val closeOrderSize: String,  //  平仓挂单数量
    val entryPrice: String, //  开仓均价
    val isolatedMargin: String, //  逐仓保证金
    val leverage: Int,  // 杠杆倍数
    val openOrderMarginFrozen: String,  //  开仓订单占用保证金
    val positionSide: String,
    val positionSize: String,
    val positionType: String,
    val realizedProfit: String, //  已实现盈亏
    val symbol: String
)