package com.black.base.model.future

data class UserBalanceBean(
    val coin: String,
    val isolatedMargin: String, // 逐仓保证金
    val openOrderMarginFrozen: String, // 订单冻结
    val underlyingType: Int,  //  1:币本位，2:U本位
    val walletBalance: String  // 钱包余额
)