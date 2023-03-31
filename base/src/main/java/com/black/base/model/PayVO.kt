package com.black.base.model

import android.os.Parcel
import android.os.Parcelable


class PayVO() : Parcelable {
    var orderAmount: String? = null
    var ccyNo: String? = "ZAR"
    var busiCode: String? = "100901"
    var bankCode: String? = "ABSA"
    var coin: String? = "USDT"

    constructor(parcel: Parcel) : this() {
        orderAmount = parcel.readString()
        ccyNo = parcel.readString()
        busiCode = parcel.readString()
        bankCode = parcel.readString()
        coin = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(orderAmount)
        parcel.writeString(ccyNo)
        parcel.writeString(busiCode)
        parcel.writeString(bankCode)
        parcel.writeString(coin)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PayVO> {
        override fun createFromParcel(parcel: Parcel): PayVO {
            return PayVO(parcel)
        }

        override fun newArray(size: Int): Array<PayVO?> {
            return arrayOfNulls(size)
        }
    }

}