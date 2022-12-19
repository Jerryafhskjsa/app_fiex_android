package com.black.base.model.future

import com.black.base.model.BaseAdapterItem
import com.black.util.Findable

open class PositionBean : BaseAdapterItem() {
    val autoMargin: Boolean? = null//是否自动追加保证金
    val availableCloseSize: String? = null  //可平仓数量（张）
    val closeOrderSize: String? = null //平仓挂单数量（张）
    val entryPrice: String? = null //开仓均价
    val isolatedMargin: String? = null //逐仓保证金
    val leverage: Int? = null  //杠杆倍数
    val openOrderMarginFrozen: String? = null //开仓订单保证金占用
    val positionSide: String? = null //持仓方向(LONG,SHORT)
    val positionSize: String? = null //持仓数量（张）
    val positionType: String? = null //仓位类型(ISOLATED[逐仓])
    val realizedProfit: String? = null //已实现盈亏
    val symbol: String? = null //交易对

    /******/
    //本地计算的数据
    var unRealizedProfit: String? = null//未实现盈亏
    var profitRate: String? = null//收益率
    var forceStopPrice: String? = null//强平价格
    var bondAmount: String? = null//保证金
    var flagPrice: String? = null//标记价格
    var adl: Int? = 0

    override fun toString(): String {
        return "PositionBean(autoMargin=$autoMargin, availableCloseSize=$availableCloseSize, closeOrderSize=$closeOrderSize, entryPrice=$entryPrice, isolatedMargin=$isolatedMargin, leverage=$leverage, openOrderMarginFrozen=$openOrderMarginFrozen, positionSide=$positionSide, positionSize=$positionSize, positionType=$positionType, realizedProfit=$realizedProfit, symbol=$symbol, unRealizedProfit=$unRealizedProfit, profitRate=$profitRate, forceStopPrice=$forceStopPrice, bondAmount=$bondAmount, flagPrice=$flagPrice, adl=$adl)"
    }
    /******/



}

