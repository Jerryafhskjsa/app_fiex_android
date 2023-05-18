package com.black.base.model.future

import com.black.base.model.BaseAdapterItem

/**
 * 计划委托bean
 */
open class PlansBean : BaseAdapterItem(){
//    val closePosition: Boolean? = null//是否触发全平
    val createdTime: Long? = null  //创建时间
    val entrustId: String? = null //委托id
    val entrustType: String? = null //委托类型
//    val marketOrderLevel: Int? = null //市价最优档
    val orderSide: String? = null  //买卖方向
    val ordinary: Boolean? = null //
    val origQty: String? = null //数量（张）
    val positionSide: String? = null //持仓方向
    val price: String? = null //订单价格
    val state: String? = null //订单状态 NOT_TRIGGERED：新建委托（未触发）；TRIGGERING：触发中；TRIGGERED：已触发；USER_REVOCATION：用户撤销；PLATFORM_REVOCATION：平台撤销（拒绝）；EXPIRED：已过期
    val stopPrice: String? = null //触发价格
    val symbol:String? = null//交易对
    val timeInForce:String? = null//有效方式
    val triggerPriceType:String? = null//触发价格类型
    val triggerProfitPrice:String? = null//止盈价格
    val triggerStopPrice:String? = null//止损价格
    //计算委托数量
    var amount: String? = null
}

