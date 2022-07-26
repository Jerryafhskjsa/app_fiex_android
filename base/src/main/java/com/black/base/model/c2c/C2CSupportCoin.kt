package com.black.base.model.c2c

import android.os.Parcel
import android.os.Parcelable

class C2CSupportCoin : Parcelable {
    var coinType: String? = null
    var coinName: String? = null
    var minOrderAmount: Double? = null
    var precision: Int? = null

    constructor()
    constructor(`in`: Parcel) {
        coinType = `in`.readString()
        coinName = `in`.readString()
        minOrderAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        precision = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(coinType)
        dest.writeString(coinName)
        if (minOrderAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(minOrderAmount!!)
        }
        if (precision == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(precision!!)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<C2CSupportCoin?> = object : Parcelable.Creator<C2CSupportCoin?> {
            override fun createFromParcel(`in`: Parcel): C2CSupportCoin? {
                return C2CSupportCoin(`in`)
            }

            override fun newArray(size: Int): Array<C2CSupportCoin?> {
                return arrayOfNulls(size)
            }
        }
    }
}