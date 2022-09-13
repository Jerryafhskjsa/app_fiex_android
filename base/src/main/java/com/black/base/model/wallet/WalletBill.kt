package com.black.base.model.wallet

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.black.base.R
import com.black.base.model.BaseAdapterItem
import kotlinx.android.parcel.Parcelize

class WalletBill() : BaseAdapterItem() , Parcelable {
    var availableChange:String? = null
    var coin:String? = null
    var createdTime:Long? = null
    var frozeChange:String? = null
    var id: String? = null
    var type:String? = null

    var amount: Double? = null
    var availableAmountChange: Double? = null
    var frozenAmountChange: Double? = null
    var businessType: String? = null
    var coinType: String? = null
    var businessTime: Long? = null

    constructor(parcel: Parcel) : this() {
        availableChange = parcel.readString()
        coin = parcel.readString()
        createdTime = parcel.readValue(Long::class.java.classLoader) as? Long
        frozeChange = parcel.readString()
        id = parcel.readString()
        type = parcel.readString()
        amount = parcel.readValue(Double::class.java.classLoader) as? Double
        availableAmountChange = parcel.readValue(Double::class.java.classLoader) as? Double
        frozenAmountChange = parcel.readValue(Double::class.java.classLoader) as? Double
        businessType = parcel.readString()
        coinType = parcel.readString()
        businessTime = parcel.readValue(Long::class.java.classLoader) as? Long
    }

    fun getBusinessType(context: Context): String {
        return if (businessType == null) {
            ""
        } else when (businessType) {
            "DEPOSIT" -> context.getString(R.string.wallet_bill_deposit)
            "WITHDRAW" -> context.getString(R.string.wallet_bill_withdraw)
            "CANCEL_WITHDRAW" -> context.getString(R.string.wallet_bill_cancel_withdraw)
            "CREATE_TRADE" -> context.getString(R.string.wallet_bill_create_trade)
            "CANCEL_TRADE" -> context.getString(R.string.wallet_bill_cancel_trade)
            "TRADE_DEAL" -> context.getString(R.string.wallet_bill_trade_deal)
            "C2C_PLACE_ORDER" -> context.getString(R.string.wallet_bill_c2c_place_order)
            "C2C_PROCESS_ORDER" -> context.getString(R.string.wallet_bill_c2c_process_order)
            "FOUNDATION_FINANCING" -> "抢购"
            "FOUNDATION_INVITE_REWARD" -> "抢购邀请奖励"
            "USER_DEPOSIT_MONEY_INSERTES" -> "存币生息"
            "USER_AIRDROP_AWARD" -> "空投奖励"
            "JOIN_PURCHASE_FINANCING" -> "申购"
            "PURCHASE_FINANCING" -> "申购结算"
            "PURCHASE_INVITE_REWARD" -> "申购邀请奖励"
            "FOUNDATION_INVITE_REWARD_MORE" -> "抢购间推奖励"
            "FOUNDATION_NODE_REWARD" -> "繁星计划奖励"
            "DIVIDEND_SHAREHOLDER_TRANSFER" -> "股东分红"
            "USER_LOTTERY_AWARD" -> "抽奖奖励"
            "LECTURER_AWARD" -> "讲师工资"
            "C2C_MERCHANT_LOCK_AMOUNT" -> "C2C商家保证金"
            "C2C_MERCHANT_UNLOCK_AMOUNT" -> "C2C商家解锁保证金"
            "C2C_ADMIN_AUDIT" -> "C2C交易冻结"
            "C2C_TRADE_CANCEL" -> "C2C取消交易"
            "DEPOSITE_INTERNAL_TRANSFER" -> "充值(内部转账)"
            "USER_COMMUNITY_BONUS" -> "社区分红"
            "STAFF_AWARD_FROZEN" -> "员工奖励冻结"
            "STAFF_AWARD" -> "员工奖励发放"
            "CURRENT_FIREST_AWARD" -> "聚宝盆直推奖励"
            "CURRENT_SECOND_AWARD" -> "聚宝盆间推奖励"
            "LEAGUE_ANNUAL" -> "笑傲江湖利息"
            "LEAGUE_INVITE_PROFIT" -> "笑傲江湖直推奖励"
            "LEAGUE_OWNER_PROFIT" -> "笑傲江湖掌门奖励"
            "CURRENT_USER_LOCK_IN" -> "聚宝盆存入"
            "CURRENT_USER_LOCK_OUT" -> "聚宝盆取出"
            "MINING_BKK" -> context.getString(R.string.wallet_bill_mining_bkk)
            "MINING_SHARE" -> context.getString(R.string.wallet_bill_mining_share)
            "MINING_AIR_DROP" -> context.getString(R.string.wallet_bill_mining_air_drop)
            "BKK_BONUS_ON_DEAL" -> context.getString(R.string.wallet_bill_bkk_bonus_on_deal)
            "BORROWING" -> context.getString(R.string.wallet_bill_borrowing)
            "RED_PACKET_TRANSFER" -> context.getString(R.string.wallet_bill_red_packet_transfer)
            "TRIBE_JOIN" -> "加入部落"
            "TRIBE_QUIT" -> "部落释放"
            "TRIBE_CREATE" -> "创建部落"
            "FEE_BONUS" -> "手续费奖励"
            "TRIBE_BID" -> "竞标冻结"
            "TRIBE_BID_FREEZE" -> "竞标解冻"
            "WITHDRAW_CONFIRM" -> "提现"
            "WITHDRAW_FEE" -> "提现手续费"
            "WITHDRAW_CONFIRM_FEE" -> "提现手续费"
            "CANCEL_WITHDRAW_FEE" -> "取消提现"
            "DIVIDEND_TRANSFER" -> "分红"
            "TRIBE_PROFIT_TRANS" -> "部落收益"
            "LOCK_USER_LOCK_IN" -> "活利宝转入"
            else -> ""
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(availableChange)
        parcel.writeString(coin)
        parcel.writeValue(createdTime)
        parcel.writeString(frozeChange)
        parcel.writeString(id)
        parcel.writeString(type)
        parcel.writeValue(amount)
        parcel.writeValue(availableAmountChange)
        parcel.writeValue(frozenAmountChange)
        parcel.writeString(businessType)
        parcel.writeString(coinType)
        parcel.writeValue(businessTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WalletBill> {
        override fun createFromParcel(parcel: Parcel): WalletBill {
            return WalletBill(parcel)
        }

        override fun newArray(size: Int): Array<WalletBill?> {
            return arrayOfNulls(size)
        }
    }
}