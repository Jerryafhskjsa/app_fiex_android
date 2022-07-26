package com.black.base.model.money

import android.os.Parcel
import android.os.Parcelable

class SupportCoin : Parcelable {
    var coin: String? = null
    var cof: String? = null
    var price: String? = null
    val priceDisplay: String
        get() = String.format("%s %s", if (price == null) "" else price, if (coin == null) "" else coin)

    constructor()
    constructor(`in`: Parcel) {
        coin = `in`.readString()
        cof = `in`.readString()
        price = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(coin)
        parcel.writeString(cof)
        parcel.writeString(price)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SupportCoin?> = object : Parcelable.Creator<SupportCoin?> {
            override fun createFromParcel(`in`: Parcel): SupportCoin? {
                return SupportCoin(`in`)
            }

            override fun newArray(size: Int): Array<SupportCoin?> {
                return arrayOfNulls(size)
            }
        }
    }
}