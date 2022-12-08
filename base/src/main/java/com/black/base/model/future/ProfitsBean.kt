package com.black.base.model.future

import com.black.base.model.BaseAdapterItem

/**
 * 止盈止损列表bean
 */
open class ProfitsBean : BaseAdapterItem(){
    val profitId: String? = null//委托id
    val symbol: String? = null  //交易对
    val positionSide: String? = null //仓位方向(LONG,SHORT)
    val origQty: String? = null //数量（张）
    val triggerPriceType: String? = null //
    val triggerProfitPrice: String? = null  //止盈价格
    val triggerStopPrice: String? = null //止损价格
    val entryPrice: String? = null //开仓均价
    val positionSize: String? = null //持仓数量（张）
    val isolatedMargin: String? = null //逐仓保证金
    val executedQty:String? = null//实际成交
    val positionType:String? = null//持仓类型(CROSSED[全仓]，ISOLATED[逐仓])
    val state:String? = null //订单状态 NOT_TRIGGERED：新建委托（未触发）；TRIGGERING：触发中；TRIGGERED：已触发；USER_REVOCATION：用户撤销；PLATFORM_REVOCATION：平台撤销（拒绝）；EXPIRED：已过期
    val createdTime:Long? = null//时间
}

