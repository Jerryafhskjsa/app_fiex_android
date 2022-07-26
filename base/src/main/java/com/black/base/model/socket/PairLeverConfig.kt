package com.black.base.model.socket

//交易对杠杆配置
class PairLeverConfig {
    var id: String? = null
    var pair: String? = null
    var minAmount: String? = null
    var maxMultiple: Double? = null
    var leverRate: Double? = null
    var warnRate: Double? = null
    var borrowRate: Double? = null
    var outRate: Double? = null
    var type: Int? = null
    var open: Boolean? = null
    var createTime: Long? = null
    var updateTime: Long? = null

    companion object {
        const val ONE = 2
        const val ALL = 1
    }
}