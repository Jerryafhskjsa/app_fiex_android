package com.black.base.model.wallet

import android.os.Parcel
import android.os.Parcelable
import com.black.base.model.BaseAdapterItem
import com.black.util.CommonUtil
import java.math.BigDecimal
import java.util.*

class Wallet : BaseAdapterItem, Parcelable {
    var coinType: String? = null//币种名称
    var coinTypeDes:String? = null//币种全称
    var estimatedAvailableAmountCny:Double? = 0.0//可使用折合成cny
    var coinIconUrl:String? = null//币种icon
    var coinAmount //数量 可使用
            : BigDecimal? = null
    var estimatedAvailableAmount:Double = 0.0//可使用折合成u

    var coinWallet //资产地址
            : String? = null
    var memo //资产地址
            : String? = null
    var coinFroze //冻结
            = 0.0
    var totalAmount //总量
            = 0.0
    var totalAmountCny //总量
            : Double? = null
    var estimatedCoinType: String? = null
    var estimatedTotalAmount = 0.0
    var createTime: Long = 0
    var updateTime: Long = 0
    var minChainDepositAmt: String? = null
    var coinOrder = 0

    constructor()
    constructor(`in`: Parcel) {
        coinType = `in`.readString()
        coinTypeDes = `in`.readString()
        coinIconUrl = `in`.readString()
        coinAmount = CommonUtil.parseBigDecimal(`in`.readString())
        coinWallet = `in`.readString()
        memo = `in`.readString()
        coinFroze = `in`.readDouble()
        estimatedAvailableAmountCny = `in`.readDouble()
        totalAmount = `in`.readDouble()
        totalAmountCny = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        estimatedCoinType = `in`.readString()
        estimatedTotalAmount = `in`.readDouble()
        estimatedAvailableAmount = `in`.readDouble()
        createTime = `in`.readLong()
        updateTime = `in`.readLong()
        minChainDepositAmt = `in`.readString()
    }

    override fun getType(): Int {
        return SPOT_ACCOUNT
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(coinType)
        dest.writeString(coinTypeDes)
        dest.writeString(coinIconUrl)
        dest.writeString(if (coinAmount == null) null else coinAmount.toString())
        dest.writeString(coinWallet)
        dest.writeString(memo)
        dest.writeDouble(coinFroze)
        dest.writeDouble(estimatedAvailableAmountCny!!)
        dest.writeDouble(totalAmount)
        if (totalAmountCny == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(totalAmountCny!!)
        }
        dest.writeString(estimatedCoinType)
        dest.writeDouble(estimatedTotalAmount)
        dest.writeDouble(estimatedAvailableAmount)
        dest.writeLong(createTime)
        dest.writeLong(updateTime)
        dest.writeString(minChainDepositAmt)
    }

    val sortLetter: Char?
        get() = if (coinType == null || coinType!!.trim { it <= ' ' }.isEmpty()) {
            null
        } else coinType!!.trim { it <= ' ' }.toUpperCase(Locale.getDefault())[0]

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Wallet?> = object : Parcelable.Creator<Wallet?> {
            override fun createFromParcel(`in`: Parcel): Wallet? {
                return Wallet(`in`)
            }

            override fun newArray(size: Int): Array<Wallet?> {
                return arrayOfNulls(size)
            }
        }
        var COMPARATOR_CHOOSE_COIN: Comparator<Wallet?> = Comparator { o1, o2 ->
            if (o1 == null || o2 == null) {
                return@Comparator 0
            }
            val c1 = o1.sortLetter
            val c2 = o2.sortLetter
            if (c1 == null || c2 == null) 0 else c1.compareTo(c2)
        }
    }
}
