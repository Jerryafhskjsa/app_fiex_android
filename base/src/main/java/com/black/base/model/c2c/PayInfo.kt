package com.black.base.model.c2c

import android.os.Parcel
import android.os.Parcelable
import com.black.base.model.BaseAdapterItem

class PayInfo() : BaseAdapterItem(), Parcelable {
    var account: String? = null	   //联系方式	string
    var depositBank: String? = null//银行	string
    var id: Int? = null       	   //id	integer
    var name: String? = null	   //支付方式名称	string
    var receiptImage: String? = null//	二维码图片	string
    var status: Int? = null	       //状态0:未使用,1:使用中	integer
    var type: Int? = null   	   //类型	integer

    constructor(parcel: Parcel) : this() {
        account = parcel.readString()
        depositBank = parcel.readString()
        id = parcel.readValue(Int::class.java.classLoader) as? Int
        name = parcel.readString()
        receiptImage = parcel.readString()
        status = parcel.readValue(Int::class.java.classLoader) as? Int
        type = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(account)
        parcel.writeString(depositBank)
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(receiptImage)
        parcel.writeValue(status)
        parcel.writeValue(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PayInfo> {
        override fun createFromParcel(parcel: Parcel): PayInfo {
            return PayInfo(parcel)
        }

        override fun newArray(size: Int): Array<PayInfo?> {
            return arrayOfNulls(size)
        }
    }

}