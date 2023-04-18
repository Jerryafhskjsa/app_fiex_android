package com.black.base.model.c2c

import android.os.Parcel
import android.os.Parcelable
import com.black.base.model.BaseAdapterItem
import com.black.base.model.wallet.WalletBill
import kotlinx.android.parcel.Parcelize
import java.util.*
import kotlin.Comparator
@Parcelize

class C2CMainAD  : BaseAdapterItem() ,Parcelable {
    var canCreateOrderForQueryUser: Boolean? = null	//当前用户是否可下此广告的订单
    var coinType: String? = null	//币种
    var completedOrders: Int? = 0	//完成订单数
    var completion: Double? = null	//订单完成率	number
    var createTime: String? = null
    var currencyCoin: String? = null	//法币币种	string
    var currentPrice:Double? = null	//当前价格，页面展示用此字段展示单价	number
    var direction: String? = null	//方向	string
    var finishedOrderAmount:Double? = null	//	此广告已完成量	number
    var id:String? = null
    var merchantAllOrders30Days: Int? = 0	//	商家30天订单数	integer
    var merchantCompletedOrders30Days: Int? = 0	//	商家30天完成订单数	integer
    var merchantId: Int? = 0	//	otc商家id	integer
    var merchantLevel: String? = null	//商家级别	string
    var merchantTotalCompletion:Double? = null	//	商家订单完成率	number
    var notCanCreateOrderReasonCode: String? = null	//不可以创建订单原因code	string
    var notCanCreateOrderReasonMsg: String? = null	//不可以创建订单原因	string
    var payMethods: String? = "1,2,3"	//付款方式,支付方式(1:支付宝，2:微信，3:银行卡);以逗号(,)号分隔的字符串	string
    var priceParam:Double? = null	//	单价参数,(priceType=1时为下单价，priceType=2,3时为动态价格的浮动参数)	number
    var priceType: Int? = 0	//	单价类型（1:固定单价，2：指数价格，3：盘口价格）
    var realName: String? = null	//商家昵称	string
    var registeredDays:Double? = null	//	注册天数
    var remark: String? = null	//商家备注	string
    var singleLimitMax:Double? = null	//	单笔最大下单额度（法币总量）	number
    var singleLimitMin:Double? = null	//	单笔最小下单额度（法币总量）	number
    var status: Int? = 0	//	广告状态(1:新建，2:上架，审核中,3：上架，审核通过，4：下架,5:审核不通过)
    var totalAmount:Double? = null	//	可买入或者出售总量
    var updateTime: String? = null
/*
    constructor(`in`: Parcel): this() {
        canCreateOrderForQueryUser = `in`.readByte().toInt() != 0
        coinType = `in`.readString()
        completedOrders = `in`.readInt()
        completion = `in`.readDouble()
        createTime= `in`.readString()
        currencyCoin = `in`.readString()
        currentPrice = `in`.readDouble()
        direction = `in`.readString()
        finishedOrderAmount = `in`.readDouble()
        id = `in`.readString()
        merchantAllOrders30Days = `in`.readInt()
        merchantCompletedOrders30Days = `in`.readInt()
        merchantId = `in`.readInt()
        merchantLevel = `in`.readString()
        merchantTotalCompletion = `in`.readDouble()
        notCanCreateOrderReasonCode= `in`.readString()
        notCanCreateOrderReasonMsg= `in`.readString()
        payMethods = `in`.readString()
        priceParam = `in`.readDouble()
        priceType = `in`.readInt()
        realName = `in`.readString()
        registeredDays = `in`.readDouble()
        remark = `in`.readString()
        singleLimitMax = `in`.readDouble()
        singleLimitMin = `in`.readDouble()
        status = `in`.readInt()
        totalAmount = `in`.readDouble()
        updateTime = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte((if (canCreateOrderForQueryUser!!) 1 else 0).toByte())
        dest.writeString(coinType)
        dest.writeInt(completedOrders!!)
        dest.writeDouble(completion!!)
        dest.writeString(createTime)
        dest.writeString(currencyCoin)
        dest.writeDouble(currentPrice!!)
        dest.writeString(direction)
        dest.writeDouble(finishedOrderAmount!!)
        dest.writeString(id)
        dest.writeInt(merchantAllOrders30Days!!)
        dest.writeInt(merchantCompletedOrders30Days!!)
        dest.writeInt(merchantId!!)
        dest.writeString(merchantLevel)
        dest.writeDouble(merchantTotalCompletion!!)
        dest.writeString(notCanCreateOrderReasonCode)
        dest.writeString(notCanCreateOrderReasonMsg)
        dest.writeString(payMethods)
        dest.writeDouble(priceParam!!)
        dest.writeInt(priceType!!)
        dest.writeString(realName)
        dest.writeDouble(registeredDays!!)
        dest.writeString(remark)
        dest.writeDouble(singleLimitMax!!)
        dest.writeDouble(singleLimitMin!!)
        dest.writeInt(status!!)
        dest.writeDouble(totalAmount!!)
        dest.writeString(updateTime)

    }
    companion object CREATOR : Parcelable.Creator<C2CMainAD> {
        override fun createFromParcel(parcel: Parcel): C2CMainAD {
            return C2CMainAD(parcel)
        }

        override fun newArray(size: Int): Array<C2CMainAD?> {
            return arrayOfNulls(size)
        }
    }*/
}


