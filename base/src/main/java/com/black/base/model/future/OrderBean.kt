package com.black.base.model.future

import com.black.base.model.BaseAdapterItem


open class OrderBean: BaseAdapterItem() {
    val items: ArrayList<OrderBeanItem>? = null
    var total: Int = 0

}


open class OrderBeanItem: BaseAdapterItem(){
    val avgPrice: String? = null //成交均价
    val clientOrderId: String? = null
    val closePosition: Boolean? = null  //是否条件全平仓
    val closeProfit: String? = null //平仓盈亏
    val createdTime: Long? = null
    val executedQty: Int? = null //已成交数量（张）
    val forceClose: Boolean? = null //是否是全平订单
    val marginFrozen: String? = null //占用保证金
    val orderId: String? = null
    val orderSide: String? = null //买卖方向
    val orderType: String? = null //订单类型
    val origQty: Int? = null //数量（张）
    val positionSide: String? = null  //持仓方向
    val price: String?  = null//委托价格
    val sourceId: Int? = null //条件触发id
    val state: String? = null  //订单状态 NEW：新建订单（未成交）；PARTIALLY_FILLED：部分成交；PARTIALLY_CANCELED：部分撤销；FILLED：全部成交；CANCELED：已撤销；REJECTED：下单失败；EXPIRED：已过期
    val symbol: String? = null //交易对
    val timeInForce: String? = null //有效类型
    val triggerProfitPrice: String? = null  //止盈触发价
    val triggerStopPrice: String? = null //止损触发价
}
