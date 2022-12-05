package com.black.base.model



class ContractMultiChooseBean() {
    var orientation:String? = null//开多(BUY),开空(SELL)
    var maxMultiple:Int? = null//最大倍数
    var defaultMultiple:Int? = null//默认倍数
    var type:Int? = null//0逐仓，1全仓
}