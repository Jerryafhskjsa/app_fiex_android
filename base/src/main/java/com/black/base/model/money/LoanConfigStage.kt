package com.black.base.model.money

import android.os.Parcel
import android.os.Parcelable
import com.black.base.BaseApplication
import com.black.base.R
import com.black.util.NumberUtil

class LoanConfigStage constructor(`in`: Parcel) : Parcelable {
    var numberDays: Int? = null
    var rate: Double? = null
    var defaultRate: Double? = null
    var overdueRate: Double? = null
    override fun writeToParcel(dest: Parcel, flags: Int) {
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
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        val nullAmount: String = BaseApplication.instance.resources.getString(R.string.number_default)
        return String.format("%s 天, 年化利率 %s%%",
                if (numberDays == null) nullAmount else NumberUtil.formatNumberNoGroup(numberDays),
                if (rate == null) nullAmount else NumberUtil.formatNumberNoGroup(rate!! * 100, 2, 2))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<LoanConfigStage?> = object : Parcelable.Creator<LoanConfigStage?> {
            override fun createFromParcel(`in`: Parcel): LoanConfigStage? {
                return LoanConfigStage(`in`)
            }

            override fun newArray(size: Int): Array<LoanConfigStage?> {
                return arrayOfNulls(size)
            }
        }
    }

    init {
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
    }
}