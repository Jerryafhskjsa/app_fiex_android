package com.black.base.model.future

data class BalanceDetailBean(
    val availableBalance: String, //可用余额
    val bonus: String,
    val coin: String,
    val crossedMargin: String,  //全仓保证金
    val isolatedMargin: String, //逐仓保证金
    val openOrderMarginFrozen: String, //委托保证金
    val walletBalance: String //钱包余额
)