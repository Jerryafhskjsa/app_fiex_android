package com.black.base.model

import android.os.Parcel
import android.os.Parcelable
import com.black.base.model.wallet.Wallet
import java.util.*
import kotlin.Comparator

/**
 * 支持的划转币种
 */
class CanTransferCoin {
    var coin: String? = null
    var fromType: String? = null
    var id: Int? = null
    var isTransfer:Int? = null
    var toType:String? = null


    val sortLetter: Char?
        get() = if (coin == null || coin!!.trim { it <= ' ' }.isEmpty()) {
            null
        } else coin!!.trim { it <= ' ' }.toUpperCase(Locale.getDefault())[0]

    companion object {
        var COMPARATOR_CHOOSE_COIN: java.util.Comparator<CanTransferCoin?> = Comparator { o1, o2 ->
            if (o1 == null || o2 == null) {
                return@Comparator 0
            }
            val c1 = o1.sortLetter
            val c2 = o2.sortLetter
            if (c1 == null || c2 == null) 0 else c1.compareTo(c2)
        }
    }
}