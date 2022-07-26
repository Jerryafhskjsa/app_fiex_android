package com.black.base.model.money

import android.os.Parcel
import android.os.Parcelable

class DemandRate : Parcelable {
    var rate: Double? = null
    var day: String? = null

    constructor()
    constructor(`in`: Parcel) {
        rate = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        day = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        if (rate == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(rate!!)
        }
        dest.writeString(day)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DemandRate?> = object : Parcelable.Creator<DemandRate?> {
            override fun createFromParcel(`in`: Parcel): DemandRate? {
                return DemandRate(`in`)
            }

            override fun newArray(size: Int): Array<DemandRate?> {
                return arrayOfNulls(size)
            }
        }
    }
}
