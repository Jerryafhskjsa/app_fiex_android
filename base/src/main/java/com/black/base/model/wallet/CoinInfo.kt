package com.black.base.model.wallet

import android.os.Parcel
import android.os.Parcelable
import com.black.util.Filter

class CoinInfo : Parcelable {
    var coinType: String? = null
    var coinFullName: String? = null
    var areaType: String? = null
    var maxPrecision //最大精度
            = 0
    var memoNeeded = false
    var minWithdrawSingle: Double? = null
    var maxWithdrawSingle: Double? = null
    var maxWithdrawOneDay: Double? = null
    var withdrawFee: Double? = null
    var minimumTradeAmount: Double? = null
    var minimumDepositAmount: Double? = null
    var blockConfirm: String? = null
    var blockTime: String? = null
    var orderNo: Int? = null
    var supportTrade //交易
            : Boolean? = null
    var supportDeposit //充值
            : Boolean? = null
    var supportWithdraw //提币
            : Boolean? = null
    var addrRegexpExpress: String? = null
    var memoRegexpExpres: String? = null
    var maxRedPacketAmount: Double? = null
    var minRedPacketAmount: Double? = null
    var redPacketAmountPrecision: Int? = null
    var logosUrl: String? = null
    var communityURL: String? = null
    var groupId: String? = null
    var supportRedPacket //红包支持
            : Boolean? = null

    constructor()
    constructor(`in`: Parcel) {
        coinType = `in`.readString()
        coinFullName = `in`.readString()
        areaType = `in`.readString()
        maxPrecision = `in`.readInt()
        memoNeeded = `in`.readByte().toInt() != 0
        minWithdrawSingle = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        maxWithdrawSingle = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        maxWithdrawOneDay = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        withdrawFee = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        minimumTradeAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        minimumDepositAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        blockConfirm = `in`.readString()
        blockTime = `in`.readString()
        orderNo = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        val tmpSupportTrade = `in`.readByte()
        supportTrade = if (tmpSupportTrade.toInt() == 0) null else tmpSupportTrade.toInt() == 1
        val tmpSupportDeposit = `in`.readByte()
        supportDeposit = if (tmpSupportDeposit.toInt() == 0) null else tmpSupportDeposit.toInt() == 1
        val tmpSupportWithdraw = `in`.readByte()
        supportWithdraw = if (tmpSupportWithdraw.toInt() == 0) null else tmpSupportWithdraw.toInt() == 1
        addrRegexpExpress = `in`.readString()
        memoRegexpExpres = `in`.readString()
        maxRedPacketAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        minRedPacketAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        redPacketAmountPrecision = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        logosUrl = `in`.readString()
        communityURL = `in`.readString()
        groupId = `in`.readString()
        val tmpSupportRedPacket = `in`.readByte()
        supportRedPacket = if (tmpSupportRedPacket.toInt() == 0) null else tmpSupportRedPacket.toInt() == 1
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(coinType)
        dest.writeString(coinFullName)
        dest.writeString(areaType)
        dest.writeInt(maxPrecision)
        dest.writeByte((if (memoNeeded) 1 else 0).toByte())
        if (minWithdrawSingle == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(minWithdrawSingle!!)
        }
        if (maxWithdrawSingle == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(maxWithdrawSingle!!)
        }
        if (maxWithdrawOneDay == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(maxWithdrawOneDay!!)
        }
        if (withdrawFee == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(withdrawFee!!)
        }
        if (minimumTradeAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(minimumTradeAmount!!)
        }
        if (minimumDepositAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(minimumDepositAmount!!)
        }
        dest.writeString(blockConfirm)
        dest.writeString(blockTime)
        if (orderNo == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(orderNo!!)
        }
        dest.writeByte((if (supportTrade == null) 0 else if (supportTrade!!) 1 else 2).toByte())
        dest.writeByte((if (supportDeposit == null) 0 else if (supportDeposit!!) 1 else 2).toByte())
        dest.writeByte((if (supportWithdraw == null) 0 else if (supportWithdraw!!) 1 else 2).toByte())
        dest.writeString(addrRegexpExpress)
        dest.writeString(memoRegexpExpres)
        if (maxRedPacketAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(maxRedPacketAmount!!)
        }
        if (minRedPacketAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(minRedPacketAmount!!)
        }
        if (redPacketAmountPrecision == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeInt(redPacketAmountPrecision!!)
        }
        dest.writeString(logosUrl)
        dest.writeString(communityURL)
        dest.writeString(groupId)
        dest.writeByte((if (supportRedPacket == null) 0 else if (supportRedPacket!!) 1 else 2).toByte())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        val FILTER_RED_PACKET: Filter<CoinInfo?> = Filter<CoinInfo?> { coinInfo ->
            if (coinInfo == null) {
                false
            } else coinInfo.supportRedPacket != null && coinInfo.supportRedPacket!!
        }
        @JvmField
        val CREATOR: Parcelable.Creator<CoinInfo?> = object : Parcelable.Creator<CoinInfo?> {
            override fun createFromParcel(`in`: Parcel): CoinInfo? {
                return CoinInfo(`in`)
            }

            override fun newArray(size: Int): Array<CoinInfo?> {
                return arrayOfNulls(size)
            }
        }
    }
}