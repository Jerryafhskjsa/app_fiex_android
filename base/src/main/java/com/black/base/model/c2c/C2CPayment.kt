package com.black.base.model.c2c

import android.content.Context
import android.text.TextUtils
import com.black.base.BaseApplication
import com.black.base.R

class C2CPayment {
    companion object {
        const val BANK = "bank"
        const val ALIPAY = "alipay"
        const val WECHAT = "wechat"
    }

    var id: Int? = null
    var userId: String? = null
    var type: String? = null
    var payeeName: String? = null
    var account: String? = null
    var url: String? = null
    var isAvailable: Int? = null
    var bankName: String? = null
    var branchBankName: String? = null
    var level: Int? = null
    val payIconRes: Int
        get() = getPayIconRes(type)

    private fun getPayIconRes(payType: String?): Int {
        if (TextUtils.isEmpty(payType)) {
            return 0
        }
        when (payType) {
            BANK -> return R.drawable.icon_c2c_bank
            ALIPAY -> return R.drawable.icon_c2c_alipay
            WECHAT -> return R.drawable.icon_c2c_wechat
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

    override fun toString(): String {
        return getPayTypeText(BaseApplication.instance)
    }

    override fun equals(other: Any?): Boolean {
        if (other is C2CPayment) {
            if (id == null && other.id == null) {
                return true
            }
            return if (id == null || other.id == null) {
                false
            } else id == other.id
        }
        return false
    }
}