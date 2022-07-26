package com.black.base.model.money

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class Demand : Parcelable {
    var coinType: String? = null
    var totalInterestAmount: Double? = null
    var lastInterestAmount: Double? = null
    var nextInterestAmount: Double? = null
    var minAmount: Double? = null
    var maxAmount: Double? = null
    var precision: Int? = null
    var lockAmount: Double? = null
    var status: Boolean? = null
    var rateConfDto: ArrayList<DemandRate?>? = null
    var rateText: String? = null
    var rateImg: String? = null
    var interestText: String? = null
    var distributionCoinType: String? = null

    constructor()
    constructor(`in`: Parcel) {
        coinType = `in`.readString()
        totalInterestAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        lastInterestAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        nextInterestAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        minAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        maxAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        precision = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        lockAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        val tmpStatus = `in`.readByte()
        status = if (tmpStatus.toInt() == 0) null else tmpStatus.toInt() == 1
        rateConfDto = `in`.createTypedArrayList(DemandRate.CREATOR)
        rateText = `in`.readString()
        rateImg = `in`.readString()
        interestText = `in`.readString()
        distributionCoinType = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(coinType)
        if (totalInterestAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(totalInterestAmount!!)
        }
        if (lastInterestAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(lastInterestAmount!!)
        }
        if (nextInterestAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(nextInterestAmount!!)
        }
        if (minAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(minAmount!!)
        }
        if (maxAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(maxAmount!!)
        }
        if (precision == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(precision!!)
        }
        if (lockAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(lockAmount!!)
        }
        dest.writeByte((if (status == null) 0 else if (status!!) 1 else 2).toByte())
        dest.writeTypedList(rateConfDto)
        dest.writeString(rateText)
        dest.writeString(rateImg)
        dest.writeString(interestText)
        dest.writeString(distributionCoinType)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Demand?> = object : Parcelable.Creator<Demand?> {
            override fun createFromParcel(`in`: Parcel): Demand? {
                return Demand(`in`)
            }

            override fun newArray(size: Int): Array<Demand?> {
                return arrayOfNulls(size)
            }
        }
    }
}