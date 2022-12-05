package com.black.base.model.wallet

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.text.TextUtils
import com.black.base.R
import com.black.base.model.BaseAdapterItem
import java.util.*

class FinancialRecord : BaseAdapterItem, Parcelable {
    var actionType: String? = null
    var id: String? = null
    var txNetworkId: String? = null
    var txFromWallet: String? = null
    var txToWallet: String? = null
    var txCoin: String? = null
    var txStatus //0已完成 1待审核 2已取消 3确认中 4审核通过 5转账中 -1 失败 7退回
            : String? = null
    var createdTime: String? = null
    var txAmount = 0.0
    var txNetworkTime: String? = null
    var confirmations: String? = null
    var explorerLink: String? = null
    var memo: String? = null
    var txFee = 0.0
    var txType //1充币 -1 提笔
            : Int? = null

    constructor()
    constructor(`in`: Parcel) {
        actionType = `in`.readString()
        id = `in`.readString()
        txNetworkId = `in`.readString()
        txFromWallet = `in`.readString()
        txToWallet = `in`.readString()
        txCoin = `in`.readString()
        txStatus = `in`.readString()
        createdTime = `in`.readString()
        txAmount = `in`.readDouble()
        txFee = `in`.readDouble()
        txNetworkTime = `in`.readString()
        confirmations = `in`.readString()
        explorerLink = `in`.readString()
        memo = `in`.readString()
        txType = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
    }

    fun getStatusText(context: Context): String {
        /*
        //        默认显示：全部状态
        //剩余状态:
        //
        //充币记录:
        //待确定
        //未完成
        //已完成
        //
        //提币详情记录：
        //待确认
        //待审核
        //已取消
        //已完成
         */
        var statusText = context.getString(R.string.number_default)
        when {
            TextUtils.equals("0", txStatus) -> {
                statusText = context.getString(R.string.financial_deal)
            }
            TextUtils.equals("1", txStatus) -> {
                statusText = context.getString(R.string.financial_wait_check)
            }
            TextUtils.equals("2", txStatus) -> {
                statusText = context.getString(R.string.financial_canceled)
            }
            TextUtils.equals("3", txStatus) -> {
                statusText = context.getString(R.string.financial_ensuming)
            }
            TextUtils.equals("4", txStatus) -> {
                statusText = context.getString(R.string.financial_checked)
            }
            TextUtils.equals("5", txStatus) -> {
                statusText = context.getString(R.string.financial_doing)
            }
            TextUtils.equals("-1", txStatus) -> {
                statusText = context.getString(R.string.financial_failed)
            }
            TextUtils.equals("7", txStatus) -> {
                statusText = context.getString(R.string.rall_back)
            }
        }
        return statusText
    }

    override fun getType(): Int {
        return FINANCIAL_RECORD
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(actionType)
        dest.writeString(id)
        dest.writeString(txNetworkId)
        dest.writeString(txFromWallet)
        dest.writeString(txToWallet)
        dest.writeString(txCoin)
        dest.writeString(txStatus)
        dest.writeString(createdTime)
        dest.writeDouble(txAmount)
        dest.writeString(txNetworkTime)
        dest.writeString(confirmations)
        dest.writeString(explorerLink)
        dest.writeString(memo)
        if (txType == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(txType!!)
        }
    }

    companion object CREATOR : Parcelable.Creator<FinancialRecord?> {
            override fun createFromParcel(`in`: Parcel): FinancialRecord? {
                return FinancialRecord(`in`)
            }

            override fun newArray(size: Int): Array<FinancialRecord?> {
                return arrayOfNulls(size)
            }
        }

    }
