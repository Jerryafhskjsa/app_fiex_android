package com.black.base.model.trade

class Limit {
    var isGreatThanTotalMax: Boolean? = null
    var isGreatThanUserMax: Boolean? = null
    var userMax: Double? = null
    var totalMax: Double? = null
    var canMiningWiccTotal: Double? = null
    var isWiccOver: Boolean? = null
    var pairStatus: Item? = null

    inner class Item {
        var canMiningTotal: Double? = null
        var platformMiningTotal: Double? = null
        var userMiningTotal: Double? = null
    }

    //新字段
    var canMiningTotal //平台今日可挖总量
            : Double? = null
    var platformMiningTotal //平台今日已挖数量
            : Double? = null
    var userMiningTotal //用户今日已挖数量
            : Double? = null
    var userCanMiningTotal //用户今日可挖数量
            : Double? = null
    var miningType: Long? = null
    var pair: String? = null
}