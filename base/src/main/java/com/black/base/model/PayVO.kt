package com.black.base.model

import android.os.Parcel
import android.os.Parcelable
import retrofit2.http.Body

class PayVO() : Parcelable {
    var orderAmount: String? = null
    var orderType: String? = null
    var accNo: String? = null
    var accName: String? = null
    var ccyNo: String? = "ZAR"
    var busiCode: String? = "100101"
    var bankCode: String? = "ABSA"
    var coin: String? = "USDT"

    constructor(parcel: Parcel) : this() {
        orderAmount = parcel.readString()
        orderType = parcel.readString()
        accNo = parcel.readString()
        accName = parcel.readString()
        ccyNo = parcel.readString()
        busiCode = parcel.readString()
        bankCode = parcel.readString()
        coin = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(orderAmount)
        parcel.writeString(orderType)
        parcel.writeString(accNo)
        parcel.writeString(accName)
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