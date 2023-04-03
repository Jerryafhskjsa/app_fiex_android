package com.black.base.model

import android.os.Parcel
import android.os.Parcelable


class payOrder() : Parcelable {
    var orderType: String? = null
    var id: String? = null
    var merNo: String? = null
    var pname: String? = null
    var pemail: String? = null
    var phone: String? = null
    var orderAmount: Double? = 0.0
    var ccyNo: String? = null
    var busiCode: String? = null
    var bankCode: String? = null
    var pageUrl: String? = null
    var coin: String? = null
    var amount: Double? = 0.0
    var price: Double? = 0.0
    var fee: Double? = 0.0
    var payStatus: Int? = null
    var orderNo: String? = null
    var createTime: Long? = null
    var updateTime: Long? = null
    var userId: String? = null



    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        merNo = parcel.readString()
        pname = parcel.readString()
        pemail = parcel.readString()
        phone = parcel.readString()
        orderAmount = parcel.readValue(Double::class.java.classLoader) as? Double
        ccyNo = parcel.readString()
        busiCode = parcel.readString()
        bankCode = parcel.readString()
        pageUrl = parcel.readString()
        coin = parcel.readString()
        amount = parcel.readValue(Double::class.java.classLoader) as? Double
        price = parcel.readValue(Double::class.java.classLoader) as? Double
        payStatus = parcel.readValue(Int::class.java.classLoader) as? Int
        orderNo = parcel.readString()
        createTime = parcel.readValue(Long::class.java.classLoader) as? Long
        updateTime = parcel.readValue(Long::class.java.classLoader) as? Long
        userId = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(merNo)
        parcel.writeString(pname)
        parcel.writeString(pemail)
        parcel.writeString(phone)
        parcel.writeValue(orderAmount)
        parcel.writeString(ccyNo)
        parcel.writeString(busiCode)
        parcel.writeString(bankCode)
        parcel.writeString(pageUrl)
        parcel.writeString(coin)
        parcel.writeValue(amount)
        parcel.writeValue(price)
        parcel.writeValue(payStatus)
        parcel.writeString(orderNo)
        parcel.writeValue(createTime)
        parcel.writeValue(updateTime)
        parcel.writeString(userId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<payOrder> {
        override fun createFromParcel(parcel: Parcel): payOrder {
            return payOrder(parcel)
        }

        override fun newArray(size: Int): Array<payOrder?> {
            return arrayOfNulls(size)
        }
    }

}