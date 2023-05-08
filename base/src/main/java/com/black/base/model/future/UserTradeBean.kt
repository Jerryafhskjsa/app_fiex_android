package com.black.base.model.future

data class UserTradeBean(
    val marginUnfrozen: String,
    val orderId: String,
    val price: String,
    val quantity: String,
    val timestamp: Long
)