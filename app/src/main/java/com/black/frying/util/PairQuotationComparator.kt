package com.black.frying.util

import com.black.base.model.socket.PairStatus
import java.util.*

class PairQuotationComparator(var coinType: Int,var tradeVolmeType:Int, var priceType: Int, var rangeType: Int) : Comparator<PairStatus?> {
    companion object {
        const val NORMAL = 0
        const val UP = 1
        const val DOWN = 2
    }

    override fun compare(o1: PairStatus?, o2: PairStatus?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        if (coinType != NORMAL) {
            return compareByCoin(o1, o2)
        }
        if(tradeVolmeType != NORMAL){
            return compareByTradeVolume(o1,o2)
        }
        if (priceType != NORMAL) {
            return compareByPrice(o1, o2)
        }
        return if (rangeType != NORMAL) {
            compareBySince(o1, o2)
        } else compareDefault(o1, o2)
    }

    private fun compareDefault(o1: PairStatus?, o2: PairStatus?): Int {
        return if (o1 == null || o2 == null) {
            0
        } else Integer.compare(o1.order_no, o2.order_no)
    }

    private fun compareByCoin(o1: PairStatus?, o2: PairStatus?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        if (o1.pair == null || o2.pair == null) {
            return 0
        }
        return if (coinType == UP) {
            o1.pair!!.compareTo(o2.pair!!)
        } else if (coinType == DOWN) {
            -o1.pair!!.compareTo(o2.pair!!)
        } else {
            compareDefault(o1, o2)
        }
    }

    private fun compareByTradeVolume(o1: PairStatus?, o2: PairStatus?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        if (o1.tradeAmount == null || o2.tradeAmount == null) {
            return 0
        }
        return if (tradeVolmeType == UP) {
            (o1.tradeAmount!!).compareTo(o2.tradeAmount!!)
        } else if (tradeVolmeType == DOWN) {
            -o1.tradeAmount!!.compareTo(o2.tradeAmount!!)
        } else {
            compareDefault(o1, o2)
        }
    }

    private fun compareByPrice(o1: PairStatus?, o2: PairStatus?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        return if (priceType == UP) {
            o1.currentPrice.compareTo(o2.currentPrice)
        } else if (priceType == DOWN) {
            -o1.currentPrice.compareTo(o2.currentPrice)
        } else {
            compareDefault(o1, o2)
        }
    }

    fun compareBySince(o1: PairStatus?, o2: PairStatus?): Int {
        if (o1 == null || o2 == null || o1.priceChangeSinceToday == null || o2.priceChangeSinceToday == null) {
            return 0
        }
        return if (rangeType == UP) {
            (o1.priceChangeSinceToday!!).compareTo(o2.priceChangeSinceToday!!)
        } else if (rangeType == DOWN) {
            -(o1.priceChangeSinceToday!!).compareTo(o2.priceChangeSinceToday!!)
        } else {
            compareDefault(o1, o2)
        }
    }
}