package com.black.base.model.user

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.black.base.BaseApplication
import com.black.base.R

class PaymentMethodType : Parcelable {
    var type: String? = null

    constructor()
    constructor(`in`: Parcel) {
        type = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    val payIconRes: Int
        get() = getPayIconRes(type)

    fun getPayIconRes(payType: String?): Int {
        if (TextUtils.isEmpty(payType)) {
            return 0
        }
        when (payType) {
            PaymentMethod.BANK -> return R.drawable.icon_payment_bank
            PaymentMethod.ALIPAY -> return R.drawable.icon_payment_alipay
            PaymentMethod.WECHAT -> return R.drawable.icon_payment_wechat
        }
        return 0
    }

    fun getPayTypeText(context: Context): String {
        if (TextUtils.isEmpty(type)) {
            return context.getString(R.string.number_default)
        }
        when (type) {
            PaymentMethod.BANK -> return "银行卡"
            PaymentMethod.ALIPAY -> return "支付宝"
            PaymentMethod.WECHAT -> return "微信"
        }
        return context.getString(R.string.number_default)
    }

    override fun toString(): String {
        return getPayTypeText(BaseApplication.instance)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PaymentMethodType?> = object : Parcelable.Creator<PaymentMethodType?> {
            override fun createFromParcel(`in`: Parcel): PaymentMethodType? {
                return PaymentMethodType(`in`)
            }

            override fun newArray(size: Int): Array<PaymentMethodType?> {
                return arrayOfNulls(size)
            }
        }
    }
}