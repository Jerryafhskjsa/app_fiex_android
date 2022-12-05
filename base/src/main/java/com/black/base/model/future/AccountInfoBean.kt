package com.black.base.model.future

data class AccountInfoBean(
    val accountId: Int,
    val allowOpenPosition: Boolean,
    val allowTrade: Boolean,
    val allowTransfer: Boolean,
    val openTime: Any,
    val state: Int,
    val userId: Int
)