package com.black.base.model.money

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.black.base.R
import java.util.*

//发售促销
class PromotionsBuy : Parcelable {
    var id: Long? = null
    var name: String? = null
    var coinType: String? = null
    var listImageUrl: String? = null
    var bannerUrl: String? = null
    var nowAmount: Double? = null
    var totalAmount: Double? = null
    var type: Int? = null
    var status //3 结束 2 完成 1 正在进行 0 未开始
            : Int? = null
    var openPrice: String? = null
    var price: Double? = null
    var startTime: Long? = null
    var endTime: Long? = null
    var systemTime: Long? = null
    var supportCoin: ArrayList<SupportCoin?>? = null

    constructor()
    constructor(`in`: Parcel) {
        id = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        name = `in`.readString()
        coinType = `in`.readString()
        listImageUrl = `in`.readString()
        bannerUrl = `in`.readString()
        nowAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        totalAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        type = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        status = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readInt()
        }
        openPrice = `in`.readString()
        price = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        startTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        endTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        systemTime = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readLong()
        }
        supportCoin = `in`.createTypedArrayList(SupportCoin.CREATOR)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        if (id == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeLong(id!!)
        }
        parcel.writeString(name)
        parcel.writeString(coinType)
        parcel.writeString(listImageUrl)
        parcel.writeString(bannerUrl)
        if (nowAmount == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeDouble(nowAmount!!)
        }
        if (totalAmount == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeDouble(totalAmount!!)
        }
        if (type == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeInt(type!!)
        }
        if (status == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeInt(status!!)
        }
        parcel.writeString(openPrice)
        if (price == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeDouble(price!!)
        }
        if (startTime == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeLong(startTime!!)
        }
        if (endTime == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeLong(endTime!!)
        }
        if (systemTime == null) {
            parcel.writeByte(0.toByte())
        } else {
            parcel.writeByte(1.toByte())
            parcel.writeLong(systemTime!!)
        }
        parcel.writeTypedList(supportCoin)
    }

    val statusCode: Int
        get() = status ?: 3

    fun getStatusDisplay(context: Context): String {
        return when (statusCode) {
            0 -> context.getString(R.string.promotions_buy_not_start)
            1 -> context.getString(R.string.promotions_buy_doing)
            2 -> context.getString(R.string.promotions_finish)
            else -> context.getString(R.string.promotions_finish)
        }
    }

    val priceDisplay: String
        get() {
            val sb = StringBuilder()
            if (supportCoin != null) {
                for (i in supportCoin!!.indices) {
                    val coin = supportCoin!![i]
                    coin?.let {
                        if (sb.isEmpty()) {
                            sb.append(coin.priceDisplay)
                        } else {
                            sb.append(" / ").append(coin.priceDisplay)
                        }
                    }
                }
            }
            return sb.toString()
        }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PromotionsBuy?> = object : Parcelable.Creator<PromotionsBuy?> {
            override fun createFromParcel(`in`: Parcel): PromotionsBuy? {
                return PromotionsBuy(`in`)
            }

            override fun newArray(size: Int): Array<PromotionsBuy?> {
                return arrayOfNulls(size)
            }
        }
    }
}
