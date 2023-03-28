package com.black.base.model.future

import com.tencent.imsdk.ext.group.TIMGroupPendencyGetType

open class OrderBean(
    val items: ArrayList<OrderBeanItem>,
    var total: Int = 0

)


data class OrderBeanItem(
    val avgPrice: String?, //成交均价
    val clientOrderId: String?,
    val closePosition: Boolean?,  //是否条件全平仓
    val closeProfit: String?, //平仓盈亏
    val createdTime: Long?,
    val executedQty: Int?, //已成交数量（张）
    val forceClose: Boolean?, //是否是全平订单
    val marginFrozen: String?, //占用保证金
    val orderId: String?,
    val orderSide: String?, //买卖方向
    val orderType: String?, //订单类型
    val origQty: Int?, //数量（张）
    val positionSide: String?,  //持仓方向
    val price: String?, //委托价格
    val sourceId: Int?, //条件触发id
    val state: String?,  //订单状态 NEW：新建订单（未成交）；PARTIALLY_FILLED：部分成交；PARTIALLY_CANCELED：部分撤销；FILLED：全部成交；CANCELED：已撤销；REJECTED：下单失败；EXPIRED：已过期
    val symbol: String?, //交易对
    val timeInForce: String?, //有效类型
    val triggerProfitPrice: String?,  //止盈触发价
    val triggerStopPrice: String? //止损触发价
)
