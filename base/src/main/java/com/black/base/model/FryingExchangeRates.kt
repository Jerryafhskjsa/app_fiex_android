package com.black.base.model

class FryingExchangeRates ( rateCode: Int, rateText: String) {
    companion object {
        const val cny = 0
        const val usd = 1
        const val jpy = 2
        const val krw = 3
        const val vnd = 4
    }


    var rateCode
            : Int
    var rateText
            : String

    init {
        this.rateCode = rateCode
        this.rateText = rateText
    }

    val isValid: Boolean
        get() = rateCode == 0 || rateCode == 1 || rateCode == 2 || rateCode == 3 || rateCode == 4

    override fun toString(): String {
        return rateText
    }

    override fun equals(other: Any?): Boolean {
        if (other == null || !isValid) {
            return false
        }
        if (other is FryingExchangeRates) {
            return if ( !other.isValid) {
                false
            } else  rateCode == other.rateCode && rateText == other.rateText
        }
        return false
    }

}