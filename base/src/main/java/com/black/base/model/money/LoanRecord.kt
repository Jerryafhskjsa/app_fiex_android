package com.black.base.model.money

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.black.base.R

class LoanRecord : Parcelable {
    var id: String? = null
    var userId: String? = null
    var borrowAmount: Double? = null
    var borrowCoinType: String? = null
    var mortgageCoinType: String? = null
    var mortgageAmount: Double? = null
    var borrowingMortgageScale: Double? = null
    var burstScale: Double? = null
    var warnScale: Double? = null
    var minMortgageAmount: Double? = null
    var precisions: Int? = null
    var numberDays: Int? = null
    var rate: Double? = null
    var defaultRate: Double? = null
    var overdueRate: Double? = null
    var burstBorrowPrice: Double? = null
    var burstMortgagePrice: Double? = null
    var status: Int? = null
    var interest: Double? = null
    var createTime: Long? = null
    var repaymentTime: Long? = null
    var expireTime //最迟还款日期
            : Long? = null
    var riskRate: Double? = null

    constructor()
    constructor(`in`: Parcel) {
        id = `in`.readString()
        userId = `in`.readString()
        borrowAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        borrowCoinType = `in`.readString()
        mortgageCoinType = `in`.readString()
        mortgageAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        borrowingMortgageScale = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        burstScale = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        warnScale = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        minMortgageAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        precisions = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        numberDays = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        rate = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        defaultRate = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        overdueRate = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        burstBorrowPrice = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        burstMortgagePrice = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        status = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        interest = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        createTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        repaymentTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        expireTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        riskRate = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(userId)
        if (borrowAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(borrowAmount!!)
        }
        dest.writeString(borrowCoinType)
        dest.writeString(mortgageCoinType)
        if (mortgageAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(mortgageAmount!!)
        }
        if (borrowingMortgageScale == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(borrowingMortgageScale!!)
        }
        if (burstScale == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(burstScale!!)
        }
        if (warnScale == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(warnScale!!)
        }
        if (minMortgageAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(minMortgageAmount!!)
        }
        if (precisions == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(precisions!!)
        }
        if (numberDays == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(numberDays!!)
        }
        if (rate == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(rate!!)
        }
        if (defaultRate == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(defaultRate!!)
        }
        if (overdueRate == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(overdueRate!!)
        }
        if (burstBorrowPrice == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(burstBorrowPrice!!)
        }
        if (burstMortgagePrice == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(burstMortgagePrice!!)
        }
        if (status == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(status!!)
        }
        if (interest == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(interest!!)
        }
        if (createTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(createTime!!)
        }
        if (repaymentTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(repaymentTime!!)
        }
        if (expireTime == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(expireTime!!)
        }
        if (riskRate == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(riskRate!!)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getStatusText(context: Context): String {
        if (status != null) {
            when (status) {
                0 -> return "借款失败"
                1 -> return "计息中"
                2 -> return "已到期"
                3 -> return "已逾期"
                4, 5, 11 -> return "已还款"
                12 -> return "已爆仓"
            }
        }
        return context.resources.getString(R.string.number_default)
    }

    val statusInt: Int
        get() = status ?: Int.MAX_VALUE

    val isDoing: Boolean
        get() = statusInt < 4

    val precision: Int
        get() = precisions ?: 0

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<LoanRecord?> = object : Parcelable.Creator<LoanRecord?> {
            override fun createFromParcel(`in`: Parcel): LoanRecord? {
                return LoanRecord(`in`)
            }

            override fun newArray(size: Int): Array<LoanRecord?> {
                return arrayOfNulls(size)
            }
        }
    }
}