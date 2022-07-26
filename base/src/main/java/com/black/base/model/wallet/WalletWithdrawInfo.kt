package com.black.base.model.wallet

import android.os.Parcel
import android.os.Parcelable

class WalletWithdrawInfo : Parcelable {
    var code: String? = null
    var desc: String? = null
    var blockConfirm = 0.0
    var blockTime = 0.0
    var minChainDepositAmt = 0.0
    var minWithdrawSingle = 0.0
    var maxWithdrawSingle = 0.0
    var maxWithdrawOneDay = 0.0
    var withdrawFee = 0.0
    var minimumTradeAmount = 0.0
    var supportWithdraw = false
    var supportDeposit = false
    var supportAddrVerify = false
    var addrRegexpExpress: String? = null
    var supportMemoVerify = false
    var memoRegexpExpres: String? = null

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(code)
        dest.writeString(desc)
        dest.writeDouble(blockConfirm)
        dest.writeDouble(blockTime)
        dest.writeDouble(minChainDepositAmt)
        dest.writeDouble(minWithdrawSingle)
        dest.writeDouble(maxWithdrawSingle)
        dest.writeDouble(maxWithdrawOneDay)
        dest.writeDouble(withdrawFee)
        dest.writeDouble(minimumTradeAmount)
        dest.writeByte((if (supportWithdraw) 1 else 0).toByte())
        dest.writeByte((if (supportDeposit) 1 else 0).toByte())
        dest.writeByte((if (supportAddrVerify) 1 else 0).toByte())
        dest.writeString(addrRegexpExpress)
        dest.writeByte((if (supportMemoVerify) 1 else 0).toByte())
        dest.writeString(memoRegexpExpres)
    }

    companion object {
        // 实例化静态内部对象CREATOR实现接口Parcelable.Creator
        @JvmField
        val CREATOR: Parcelable.Creator<WalletWithdrawInfo?> = object : Parcelable.Creator<WalletWithdrawInfo?> {
            override fun newArray(size: Int): Array<WalletWithdrawInfo?> {
                return arrayOfNulls(size)
            }

            // 将Parcel对象反序列化为ParcelableDate
            override fun createFromParcel(source: Parcel): WalletWithdrawInfo? {
                val wallet = WalletWithdrawInfo()
                wallet.code = source.readString()
                wallet.desc = source.readString()
                wallet.blockConfirm = source.readDouble()
                wallet.blockTime = source.readDouble()
                wallet.minChainDepositAmt = source.readDouble()
                wallet.minWithdrawSingle = source.readDouble()
                wallet.maxWithdrawSingle = source.readDouble()
                wallet.maxWithdrawOneDay = source.readDouble()
                wallet.withdrawFee = source.readDouble()
                wallet.minimumTradeAmount = source.readDouble()
                wallet.supportWithdraw = source.readByte().toInt() == 1
                wallet.supportDeposit = source.readByte().toInt() == 1
                wallet.supportAddrVerify = source.readByte().toInt() == 1
                wallet.addrRegexpExpress = source.readString()
                wallet.supportMemoVerify = source.readByte().toInt() == 1
                wallet.memoRegexpExpres = source.readString()
                return wallet
            }
        }
    }
}