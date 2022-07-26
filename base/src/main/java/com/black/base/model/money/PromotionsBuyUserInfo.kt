package com.black.base.model.money

import java.util.*

class PromotionsBuyUserInfo {
    var kyc: Boolean? = null
    var support: Boolean? = null
    var join: Boolean? = null
    var coinVOS: ArrayList<CoinVOS?>? = null

    inner class CoinVOS {
        var coinType: String? = null
        var cof: String? = null
        var price: String? = null
        var balance: Double? = null
    }
}
