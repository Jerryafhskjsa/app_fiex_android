package com.black.base.model.future

data class UserStepRate(
    val coinBasedTotalTradeVolume: String,
    val discountLevel: Int,
    val lackTradeVolume: String,
    val levelReturnDay: Int,
    val makerFee: String,
    val nextLvTradeVolume: String,
    val specialType: Boolean,
    val takerFee: String,
    val totalTradeVolume: String,
    val uBasedTotalTradeVolume: String,
    val ubasedTotalTradeVolume: Double
)