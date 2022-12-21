package com.black.base.model.future

data class UserOrderBean(
    val avgPrice: String,
    val executedQty: String,
    val marginFrozen: String,
    val positionType: String,
    val sourceId: String,
    val sourceType: String,
    val state: String,
    val symbol: String,
    val timestamp: Int
)