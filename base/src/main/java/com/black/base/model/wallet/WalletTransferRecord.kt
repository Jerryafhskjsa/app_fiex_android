package com.black.base.model.wallet

import android.content.Context
import com.black.base.R
import com.black.base.util.TimeUtil
import java.text.SimpleDateFormat

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
            "SPOT" ->fromTypeText = context.getString(R.string.spot_account)
            "CONTRACT" ->fromTypeText = context.getString(R.string.contract_account)
            "OTC" ->fromTypeText = context.getString(R.string.otc_account)
        }
        when(toWalletType){
            "SPOT" ->toTypeText = context.getString(R.string.spot_account)
            "CONTRACT" ->toTypeText = context.getString(R.string.contract_account)
            "OTC" ->fromTypeText = context.getString(R.string.otc_account)
        }
        return fromTypeText+context.getString(R.string.arrival)+toTypeText
    }
    fun getDateDes(context: Context):String?{
        return createdTime?.toLong()?.let { TimeUtil.getTimeStr(it,"yyyy/MM/dd HH:mm:ss") }
    }
}