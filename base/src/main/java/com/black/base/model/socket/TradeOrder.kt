package com.black.base.model.socket

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.black.base.R
import com.black.base.model.BaseAdapterItem
import java.util.*

//委托订单
class TradeOrder : BaseAdapterItem, Parcelable {
    /*fiex********************/
    var avgPrice:Double? = null//成交均价 == dealAvgPrice
    var clientOrderId:String? = null//自定义订单I
    var createdTime: Long? = null//创建时间
    var executedQty:Int? = null//已成交数量
    var forceClose:Boolean? = false//是否是全平订单
    var marginFrozen:Double? = null//占用保证金 == frozenAmountByOrder
    var orderId:String? = null//订单id == id
    var orderSide:String? = null//订单方向 == direction
    var orderType: String? = null//订单类型
    var origQty:String? = null//数量(张)
    var price: Double? = null//委托价格
    var sourceId:Long? = null//条件触发id
    var state:String? = null//订单状态 = status
    var symbol:String? = null//交易对 = pair
    var timeInForce:String? = null//有效类型
    var triggerProfitPrice:Double? = null//止盈触发价
    var triggerStopPrice:Double? = null//止损触发价
    /*fiex********************/
    var pair: String? = null
    var dealAmount: Double? = null
    var priceString: String? = null
    var updateTime //成交时间
            : Long = 0
    var tradeDealDirection: String? = null
    var formattedPrice: String? = null
    var exchangeAmount = 0.0
    var exchangeAmountFormat: String? = null
    var anchorAmount = 0.0
    var id: String? = null
    var userId: String? = null
    var totalAmount: Double? = null
    var frozenAmountByOrder = 0.0
    var feeRate = 0.0
    var status = 0
    var direction: String? = null
    var dealAvgPrice //成交均价
            : Double? = null

    var weightPercent //权重占比，绘制挂单进度条使用
            = 0.0
    var beforeAmount //自身和前面所有挂单数量，扫单使用
            = 0.0

    constructor()
    constructor(`in`: Parcel) {
        pair = `in`.readString()
        dealAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        price = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        priceString = `in`.readString()
        createdTime = `in`.readLong()
        updateTime = `in`.readLong()
        tradeDealDirection = `in`.readString()
        formattedPrice = `in`.readString()
        orderType = `in`.readString()
        exchangeAmount = `in`.readDouble()
        exchangeAmountFormat = `in`.readString()
        anchorAmount = `in`.readDouble()
        id = `in`.readString()
        userId = `in`.readString()
        totalAmount = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        frozenAmountByOrder = `in`.readDouble()
        feeRate = `in`.readDouble()
        status = `in`.readInt()
        direction = `in`.readString()
        dealAvgPrice = if (`in`.readByte().toInt() == 0) {
            null
        } else {
            `in`.readDouble()
        }
        weightPercent = `in`.readDouble()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(pair)
        if (dealAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(dealAmount!!)
        }
        if (price == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(price!!)
        }
        dest.writeString(priceString)
        dest.writeLong(createdTime!!)
        dest.writeLong(updateTime)
        dest.writeString(tradeDealDirection)
        dest.writeString(formattedPrice)
        dest.writeString(orderType)
        dest.writeDouble(exchangeAmount)
        dest.writeString(exchangeAmountFormat)
        dest.writeDouble(anchorAmount)
        dest.writeString(id)
        dest.writeString(userId)
        if (totalAmount == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(totalAmount!!)
        }
        dest.writeDouble(frozenAmountByOrder)
        dest.writeDouble(feeRate)
        dest.writeInt(status)
        dest.writeString(direction)
        if (dealAvgPrice == null) {
            dest.writeByte(0.toByte())
        } else {
            dest.writeByte(1.toByte())
            dest.writeDouble(dealAvgPrice!!)
        }
        dest.writeDouble(weightPercent)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun getType(): Int {
        return TRADE_ORDER
    }

    fun getStatusDisplay(context: Context): String { //        0=进行中
//        1=部分成交
//        8=全部成交
//        9=取消
//        10=失败
        return when (status) {
            1 -> context.getString(R.string.trade_part)
            8 -> context.getString(R.string.trade_deal)
            9 -> context.getString(R.string.trade_canceled)
            10 -> context.getString(R.string.trade_failed)
            else -> context.getString(R.string.trade_start)
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<TradeOrder?> = object : Parcelable.Creator<TradeOrder?> {
            override fun createFromParcel(`in`: Parcel): TradeOrder? {
                return TradeOrder(`in`)
            }

            override fun newArray(size: Int): Array<TradeOrder?> {
                return arrayOfNulls(size)
            }
        }
        var COMPARATOR_UP = Comparator<TradeOrder?> { o1, o2 -> if (o1?.price == null || o2?.price == null || o1.price === o2.price) 0 else if (o1.price!! > o2.price!!) 1 else -1 }
        var COMPARATOR_DOWN = Comparator<TradeOrder?> { o1, o2 -> if (o1?.price == null || o2?.price == null || o1.price === o2.price) 0 else if (o1.price!! > o2.price!!) -1 else 1 }
        var COMPARATOR_DEAL = Comparator<TradeOrder?> { o1, o2 -> if (o1?.createdTime == null || o2?.createdTime == null || o1.createdTime == o2.createdTime) 0 else if (o1.createdTime!! > o2.createdTime!!) -1 else 1 }
    }
}
