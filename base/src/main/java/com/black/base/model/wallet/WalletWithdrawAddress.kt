package com.black.base.model.wallet

import android.os.Parcel
import android.os.Parcelable
import com.black.base.model.BaseAdapterItem

class WalletWithdrawAddress : BaseAdapterItem, Parcelable {
    var coinType: String? = null
    var coinWallet: String? = null
    var id: Long? = null
    var memo: String? = null
    var name: String? = null

    constructor()
    constructor(`in`: Parcel) {
        coinType = `in`.readString()
        coinWallet = `in`.readString()
        id = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        memo = `in`.readString()
        name = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(coinType)
        dest.writeString(coinWallet)
        if (id == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeLong(id!!)
        }
        dest.writeString(memo)
        dest.writeString(name)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<WalletWithdrawAddress?> = object : Parcelable.Creator<WalletWithdrawAddress?> {
            override fun createFromParcel(`in`: Parcel): WalletWithdrawAddress? {
                return WalletWithdrawAddress(`in`)
            }

            override fun newArray(size: Int): Array<WalletWithdrawAddress?> {
                return arrayOfNulls(size)
            }
        }
    }
}