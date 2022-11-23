package com.black.base.model.future

data class PositionBean(
    val autoMargin: Boolean,
    val availableCloseSize: String,
    val closeOrderSize: String,
    val entryPrice: String,
    val isolatedMargin: String,
    val leverage: Int,
    val openOrderMarginFrozen: String,
    val positionSide: String,
    val positionSize: String,
    val positionType: String,
    val realizedProfit: String,
    val symbol: String
)