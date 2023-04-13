package com.black.base.model.c2c

import android.os.Parcel
import android.os.Parcelable


class OtcReceiptModel() : Parcelable {
    var account:String? = null	//账户名		string
    var depositBank:String? = null	//	银行名称		string
    var emailCode:String? = null	//	邮箱code
    var googleCode:String? = null	//	谷歌code
    var name:String? = null	//	姓名
    var phoneCode:String? = null//	电话code
    var receiptImage:String? = null	//	收款图片
    var subbranch:String? = null	//	开户地点
    var type: Int? = null	        //收款方式：0：银行卡，1：支付宝，2：微信

    constructor(parcel: Parcel) : this() {
        account = parcel.readString()
        depositBank = parcel.readString()
        emailCode = parcel.readString()
        googleCode = parcel.readString()
        name = parcel.readString()
        phoneCode = parcel.readString()
        receiptImage = parcel.readString()
        subbranch = parcel.readString()
        type = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(account)
        parcel.writeString(depositBank)
        parcel.writeString(emailCode)
        parcel.writeString(googleCode)
        parcel.writeString(name)
        parcel.writeString(phoneCode)
        parcel.writeString(receiptImage)
        parcel.writeString(subbranch)
        parcel.writeValue(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OtcReceiptModel> {
        override fun createFromParcel(parcel: Parcel): OtcReceiptModel {
            return OtcReceiptModel(parcel)
        }

        override fun newArray(size: Int): Array<OtcReceiptModel?> {
            return arrayOfNulls(size)
        }
    }

}