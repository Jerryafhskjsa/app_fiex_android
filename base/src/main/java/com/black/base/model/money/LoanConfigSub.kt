package com.black.base.model.money

import android.os.Parcel
import android.os.Parcelable
import java.util.*

class LoanConfigSub : Parcelable {
    var borrowCoinType: String? = null
    var mortgageCoinType: String? = null
    var borrowingMortgageScale: Double? = null
    var burstScale: Double? = null
    var warnScale: Double? = null
    var minMortgageAmount: Double? = null
    var borrowPrecision: Int? = null
    var mortgagePrecision: Int? = null
    var stage: ArrayList<LoanConfigStage?>? = null

    constructor()
    constructor(`in`: Parcel) {
        borrowCoinType = `in`.readString()
        mortgageCoinType = `in`.readString()
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
        borrowPrecision = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        mortgagePrecision = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        stage = `in`.createTypedArrayList(LoanConfigStage.CREATOR)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(borrowCoinType)
        dest.writeString(mortgageCoinType)
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
        if (borrowPrecision == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(borrowPrecision!!)
        }
        if (mortgagePrecision == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(mortgagePrecision!!)
        }
        dest.writeTypedList(stage)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return borrowCoinType ?: ""
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<LoanConfigSub?> = object : Parcelable.Creator<LoanConfigSub?> {
            override fun createFromParcel(`in`: Parcel): LoanConfigSub? {
                return LoanConfigSub(`in`)
            }

            override fun newArray(size: Int): Array<LoanConfigSub?> {
                return arrayOfNulls(size)
            }
        }
    }
}
