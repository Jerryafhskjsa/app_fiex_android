package com.black.base.model.wallet

import android.os.Parcel
import android.os.Parcelable
import com.black.base.model.BaseAdapterItem
import com.black.util.CommonUtil
import java.math.BigDecimal
import java.util.*


class WalletLever : BaseAdapterItem, Parcelable {
    var pair: String? = null
    var borrowPrice: Double? = null
    var riskRate: Double? = null
    var coinType: String? = null
    var coinAmount //数量 可使用
            : BigDecimal? = null
    var coinWallet //资产地址
            : String? = null
    var coinFroze //冻结
            : Double? = null
    var totalAmount //总量
            : Double? = null
    var totalAmountCny //总量cny
            : Double? = null
    var coinBorrow: Double? = null
    var coinInterest: Double? = null
    var estimatedCoinType: String? = null
    var estimatedTotalAmount: Double? = null
    var estimatedAvailableAmount: Double? = null
    var afterCoinType: String? = null
    var afterCoinFroze: Double? = null
    var afterCoinAmount: BigDecimal? = null
    var afterCoinWallet: String? = null
    var afterTotalAmount: Double? = null
    var afterTotalAmountCny //总量cny
            : Double? = null
    var afterCoinBorrow: Double? = null
    var afterCoinInterest: Double? = null
    var afterEstimatedCoinType: String? = null
    var afterEstimatedTotalAmount: Double? = null
    var afterEstimatedAvailableAmount: Double? = null

    constructor()
    constructor(`in`: Parcel) {
        pair = `in`.readString()
        borrowPrice = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        riskRate = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        coinType = `in`.readString()
        coinAmount = CommonUtil.parseBigDecimal(`in`.readString())
        coinWallet = `in`.readString()
        coinFroze = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        totalAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        totalAmountCny = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        coinBorrow = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        coinInterest = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        estimatedCoinType = `in`.readString()
        estimatedTotalAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        estimatedAvailableAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        afterCoinType = `in`.readString()
        afterCoinFroze = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        afterCoinAmount = CommonUtil.parseBigDecimal(`in`.readString())
        afterCoinWallet = `in`.readString()
        afterTotalAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        afterTotalAmountCny = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        afterCoinBorrow = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        afterCoinInterest = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        afterEstimatedCoinType = `in`.readString()
        afterEstimatedTotalAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        afterEstimatedAvailableAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(pair)
        if (borrowPrice == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(borrowPrice!!)
        }
        if (riskRate == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(riskRate!!)
        }
        dest.writeString(coinType)
        dest.writeString(if (coinAmount == null) null else coinAmount.toString())
        dest.writeString(coinWallet)
        if (coinFroze == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(coinFroze!!)
        }
        if (totalAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(totalAmount!!)
        }
        if (totalAmountCny == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(totalAmountCny!!)
        }
        if (coinBorrow == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(coinBorrow!!)
        }
        if (coinInterest == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(coinInterest!!)
        }
        dest.writeString(estimatedCoinType)
        if (estimatedTotalAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(estimatedTotalAmount!!)
        }
        if (estimatedAvailableAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(estimatedAvailableAmount!!)
        }
        dest.writeString(afterCoinType)
        if (afterCoinFroze == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(afterCoinFroze!!)
        }
        dest.writeString(if (coinAmount == null) null else coinAmount.toString())
        dest.writeString(afterCoinWallet)
        if (afterTotalAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(afterTotalAmount!!)
        }
        if (afterTotalAmountCny == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(afterTotalAmountCny!!)
        }
        if (afterCoinBorrow == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(afterCoinBorrow!!)
        }
        if (afterCoinInterest == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(afterCoinInterest!!)
        }
        dest.writeString(afterEstimatedCoinType)
        if (afterEstimatedTotalAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(afterEstimatedTotalAmount!!)
        }
        if (afterEstimatedAvailableAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(afterEstimatedAvailableAmount!!)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun getType(): Int {
        return SPOT_ACCOUNT
    }

    val sortLetter: Char?
        get() = if (coinType == null || coinType!!.trim { it <= ' ' }.isEmpty()) {
            null
        } else coinType!!.trim { it <= ' ' }.toUpperCase(Locale.getDefault())[0]

    fun createCoinWallet(): Wallet {
        val wallet = Wallet()
        wallet.coinType = coinType
        wallet.coinAmount = coinAmount
        wallet.coinWallet = coinWallet
        wallet.coinFroze = coinFroze!!
        wallet.totalAmount = totalAmount!!
        wallet.estimatedCoinType = estimatedCoinType
        wallet.estimatedTotalAmount = estimatedTotalAmount!!
        wallet.estimatedAvailableAmount = estimatedAvailableAmount!!
        return wallet
    }

    fun createSetWallet(): Wallet {
        val wallet = Wallet()
        wallet.coinType = afterCoinType
        wallet.coinAmount = afterCoinAmount
        wallet.coinWallet = afterCoinWallet
        wallet.coinFroze = afterCoinFroze!!
        wallet.totalAmount = afterTotalAmount!!
        wallet.estimatedCoinType = afterEstimatedCoinType
        wallet.estimatedTotalAmount = afterEstimatedTotalAmount!!
        wallet.estimatedAvailableAmount = afterEstimatedAvailableAmount!!
        return wallet
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<WalletLever?> = object : Parcelable.Creator<WalletLever?> {
            override fun createFromParcel(`in`: Parcel): WalletLever? {
                return WalletLever(`in`)
            }

            override fun newArray(size: Int): Array<WalletLever?> {
                return arrayOfNulls(size)
            }
        }
        var COMPARATOR_CHOOSE_COIN: Comparator<WalletLever?> = Comparator { o1, o2 ->
            if (o1 == null || o2 == null) {
                return@Comparator 0
            }
            val c1 = o1.sortLetter
            val c2 = o2.sortLetter
            if (c1 == null || c2 == null) 0 else c1.compareTo(c2)
        }
    }
}