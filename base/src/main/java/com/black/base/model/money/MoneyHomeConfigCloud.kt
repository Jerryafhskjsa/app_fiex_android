package com.black.base.model.money

class MoneyHomeConfigCloud {
    var id: Long? = null
    var coinType: String? = null
    var day: Int? = null
    var distributionCoinType: String? = null
    var electricityBill: Double? = null
    var status //0未开始  1售卖中    2售卖结束   3完成（挖矿中）  4完成
            : Int? = null
    var buyStartTime: Long? = null
    var buyEndTime: Long? = null
    var createTime: Long? = null
    var endTime: Long? = null
    var startTime //挖矿时间
            : Long? = null
    var computePower: String? = null
    var expectedInterest: Double? = null
    var interestCoinType: String? = null
    var price: Double? = null
    var totalFinancing: Double? = null
    var nowFinancing: Double? = null
    var img: String? = null
    var buyMinNum: Double? = null
    var period: Int? = null
}