package com.black.base.model.money

import android.os.Parcel
import android.os.Parcelable

class Regular : Parcelable {
    var id: Long? = null
    var coinType: String? = null
    var minAmount: Double? = null
    var annualrate: Double? = null
    var day: Int? = null
    var scale: Int? = null
    var status: Int? = null
    var defaultRate: Double? = null
    var rateImg: String? = null
    var defaultRateText: String? = null
    var rateText: String? = null
    var interestText: String? = null
    var sumLockAmount: Double? = null
    var totalInterestAmount: Double? = null
    var nextInterestAmount: Double? = null

    constructor()
    constructor(`in`: Parcel) {
        id = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        coinType = `in`.readString()
        minAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        annualrate = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        day = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        scale = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        status = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        defaultRate = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        rateImg = `in`.readString()
        defaultRateText = `in`.readString()
        rateText = `in`.readString()
        interestText = `in`.readString()
        sumLockAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        totalInterestAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        nextInterestAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (id == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(id!!)
        }
        dest.writeString(coinType)
        if (minAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(minAmount!!)
        }
        if (annualrate == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(annualrate!!)
        }
        if (day == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(day!!)
        }
        if (scale == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(scale!!)
        }
        if (status == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(status!!)
        }
        if (defaultRate == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(defaultRate!!)
        }
        dest.writeString(rateImg)
        dest.writeString(defaultRateText)
        dest.writeString(rateText)
        dest.writeString(interestText)
        if (sumLockAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(sumLockAmount!!)
        }
        if (totalInterestAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(totalInterestAmount!!)
        }
        if (nextInterestAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(nextInterestAmount!!)
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Regular?> = object : Parcelable.Creator<Regular?> {
            override fun createFromParcel(`in`: Parcel): Regular? {
                return Regular(`in`)
            }

            override fun newArray(size: Int): Array<Regular?> {
                return arrayOfNulls(size)
            }
        }
    }
}