package com.black.base.model.wallet

import android.content.Context
import com.black.base.R

class WalletTransferRecord {
    var id: String? = null
    var userId: String? = null
    var pair: String? = null
    var coinType: String? = null
    var amount: Double? = null
    var createTime: Long? = null
    var type: Int? = null

    fun getTypeText(context: Context): String {
        return if (type == null) {
            context.resources.getString(R.string.number_default)
        } else when (type) {
            1 -> "币币账户到逐仓账户"
            2 -> "逐仓账户到币币账户"
            else -> context.resources.getString(R.string.number_default)
        }
    }
}