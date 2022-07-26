package com.black.base.model.user

import android.content.Context
import android.text.TextUtils
import com.black.base.R

class PaymentMethod {
    var id: String? = null
    var userId: String? = null
    var type: String? = null
    var payeeName: String? = null
    var content: String? = null
    var isAvailable: Int? = null
    var account: String? = null
    var url: String? = null
    var bankName: String? = null
    var branchBankName: String? = null
    var level: String? = null
    val payIconRes: Int
        get() = getPayIconRes(type)

    fun getPayIconRes(payType: String?): Int {
        if (TextUtils.isEmpty(payType)) {
            return 0
        }
        when (payType) {
            BANK -> return R.drawable.icon_payment_bank
            ALIPAY -> return R.drawable.icon_payment_alipay
            WECHAT -> return R.drawable.icon_payment_wechat
        }
        return 0
    }

    fun getPayTypeText(context: Context): String {
        if (TextUtils.isEmpty(type)) {
            return context.getString(R.string.number_default)
        }
        when (type) {
            BANK -> return "银行卡"
            ALIPAY -> return "支付宝"
            WECHAT -> return "微信"
        }
        return context.getString(R.string.number_default)
    }

    companion object {
        const val BANK = "bank"
        const val ALIPAY = "alipay"
        const val WECHAT = "wechat"
        const val IS_ACTIVE = 1
        const val IS_NOT_ACTIVE = -1
        const val IS_DELETED = -2
    }
}