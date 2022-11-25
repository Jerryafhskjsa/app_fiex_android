package com.black.base.model.future

data class PositionBean(
    val autoMargin: Boolean, //是否自动追加保证金
    val availableCloseSize: String,  //可平仓数量（张）
    val closeOrderSize: String, //平仓挂单数量（张）
    val entryPrice: String, //开仓均价
    val isolatedMargin: String, //逐仓保证金
    val leverage: Int,  //杠杆倍数
    val openOrderMarginFrozen: String, //开仓订单保证金占用
    val positionSide: String, //持仓方向
    val positionSize: String, //持仓数量（张）
    val positionType: String, //仓位类型
    val realizedProfit: String, //已实现盈亏
    val symbol: String //交易对
)