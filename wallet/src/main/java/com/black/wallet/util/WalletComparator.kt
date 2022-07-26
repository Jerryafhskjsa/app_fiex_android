package com.black.wallet.util

import com.black.base.model.wallet.Wallet
import java.util.*

class WalletComparator() : Comparator<Wallet?> {
    companion object {
        const val NORMAL = 0
        const val UP = 1
        const val DOWN = 2
    }

    var cnyType: Int = NORMAL
    var usableType: Int = NORMAL
    var frozeType: Int = NORMAL
    var nameType: Int = NORMAL

    constructor(nameType: Int, usableType: Int, frozeType: Int, cnyType: Int) : this() {
        this.nameType = nameType
        this.usableType = usableType
        this.frozeType = frozeType
        this.cnyType = cnyType
    }

    override fun compare(o1: Wallet?, o2: Wallet?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        if (nameType != NORMAL) {
            return compareByName(o1, o2)
        }
        if (usableType != NORMAL) {
            return compareByUsable(o1, o2)
        }
        if (frozeType != NORMAL) {
            return compareByFroze(o1, o2)
        }
        return if (cnyType != NORMAL) {
            compareByCny(o1, o2)
        } else compareDefault(o1, o2)
    }

    fun compareDefault(o1: Wallet, o2: Wallet): Int {
        return o1.coinOrder - o2.coinOrder
    }

    fun compareByName(o1: Wallet?, o2: Wallet?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        if (o1.coinType == null || o2.coinType == null) {
            return 0
        }
        return if (nameType == UP) {
            o1.coinType!!.compareTo(o2.coinType!!)
        } else if (nameType == DOWN) {
            -o1.coinType!!.compareTo(o2.coinType!!)
        } else {
            compareDefault(o1, o2)
        }
    }

    fun compareByUsable(o1: Wallet?, o2: Wallet?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        return if (usableType == UP) {
            o1.coinAmount?.compareTo(o2.coinAmount) ?: 0
        } else if (usableType == DOWN) {
            -(o1.coinAmount?.compareTo(o2.coinAmount) ?: 0)
        } else {
            compareDefault(o1, o2)
        }
    }

    fun compareByFroze(o1: Wallet?, o2: Wallet?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        return if (frozeType == UP) {
            java.lang.Double.compare(o1.coinFroze, o2.coinFroze)
        } else if (frozeType == DOWN) {
            -java.lang.Double.compare(o1.coinFroze, o2.coinFroze)
        } else {
            compareDefault(o1, o2)
        }
    }

    fun compareByCny(o1: Wallet?, o2: Wallet?): Int {
        if (o1 == null || o2 == null) {
            return 0
        }
        if (o1.totalAmountCny == null || o2.totalAmountCny == null) {
            return 0
        }
        return if (cnyType == UP) {
            java.lang.Double.compare(o1.totalAmountCny!!, o2.totalAmountCny!!)
        } else if (cnyType == DOWN) {
            -java.lang.Double.compare(o1.totalAmountCny!!, o2.totalAmountCny!!)
        } else {
            compareDefault(o1, o2)
        }
    }
}