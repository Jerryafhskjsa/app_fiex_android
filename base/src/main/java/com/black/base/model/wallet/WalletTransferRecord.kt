package com.black.base.model.wallet

import android.content.Context
import com.black.base.R

class WalletTransferRecord {
    var id: Long? = null
    var userId: Long? = null
    var bizId:Long? = null
    var accountId:Long? = null
    var fromWalletType:String? = null
    var toWalletType:String? = null
    var coin: String? = null
    var amount: Double? = null
    var status:Int? = null
    var createdTime: String? = null
    var updatedTime: String? = null

    fun getTypeText(context: Context): String {
        var fromTypeText = "-"
        var toTypeText = "-"
        when(fromWalletType){
            "spot" ->fromTypeText = context.getString(R.string.spot_account)
            "contract" ->fromTypeText = context.getString(R.string.contract_account)
        }
        when(toWalletType){
            "spot" ->toTypeText = context.getString(R.string.spot_account)
            "contract" ->toTypeText = context.getString(R.string.contract_account)
        }
        return fromTypeText+context.getString(R.string.arrival)+toTypeText
    }
}