package com.black.base.model.c2c

import android.os.Parcel
import android.os.Parcelable
import retrofit2.http.Body
import java.math.BigDecimal
import java.util.*

class C2COrderDetails() : Parcelable {
    var advertisingId: String? = null	    //广告id	string
    var advertisingRemark: String? = null	//广告备注	string
    var allegeStatus: Int? = null	        //申诉状态	integer(int32)
    var alleged: Boolean? = null	        //是否申述中	boolean
    var amount: BigDecimal? = null	        //下单成交量	number(bigdecimal)
    var buyerFee: BigDecimal? = null	    //买价手续费	number(bigdecimal)
    var canAllege: Boolean? = null	        //是否可申述	boolean
    var canAllegeEndTime: Date? = null	//可申述结束时间	string(date-time)
    var canAllegeStartTime: Date? = null 	//可申述开始时间	string(date-time)
    var canCancelTime: Date? = null	    //可以取消订单时间，(status=2(待付款),3(已付款)、5(审核中)时可取消，其他状态时=null)，status=5时小于当前时间可以取消，status=2时大于当前时间可以取消	string(date-time)
    var canceledReason: String? = null	    //取消原因（用户主动取消，订单超时取消）	string
    var chatRoomId: String? = null	        //聊天室房间id	string
    var checkLatestNews: Boolean? = null	//是否查看订单最新状态	boolean
    var coinType: String? = null	        //币种	string
    var createTime: Date? = null		    //string(date-time)
    var currencyCoin: String? = null	    //法币币种	string
    var currencyCoinAmount: BigDecimal? = null	//法币总量	number(bigdecimal)
    var direction: String? = null	        //方向	string
    var id:String? = null            		//string
    var merchantAccid: String? = null	    //商家accid	string
    var merchantId: Int? = null	            //商家ID	integer(int32)
    var merchantLevel: String? = null	    //商家级别--如果是taker查询(whetherTaker=true)时，需要显示订单的商家级别；如果是商家查询时不需要显示merchantLevel=null	string
    var merchantNickname: String? = null	//商家昵称	string
    var otherSideAllOrders30Days: Int? = null//对方30天订单数	integer(int64)
    var otherSideCompletedOrders30Days: Int? = null	//对方30天完成订单数	integer(int64)
    var otherSideCompletion30Days: Double? = null	//对方30天完成率	number(double)
    var otherSideRN: String? = null	       //对方账户实名	string
    var otherSideRealName: String? = null  //对方账户实名（商家时是商家昵称）	string
    var otherSideTel: String? = null	   //对方电话	string
    var payEeRealName: String? = null	   //买家付款实名	string
    var payInfo: OtcReceiptModel? = null	   //买家付款方式详情	OtcReceiptModel	OtcReceiptModel
    var payMethod: Int? = null	           //支付方式	integer(int32)
    var price: BigDecimal? = null	       //下单成交价格	number(bigdecimal)
    var prosecutor: Boolean? = null	       //是否申述方	boolean
    var realChangeAmount: BigDecimal? = null//实际资产变动	number(bigdecimal)
    var realName: String? = null	       //用户自己的实名	string
    var receiptInfo: OtcReceiptModel? = null   //卖家收款方式详情	OtcReceiptModel	OtcReceiptModel
    var sellerFee: BigDecimal? = null	   //卖家手续费	number(bigdecimal)
    var status: Int? = null	               //订单状态(-1:取消,2:待付款,3:已付款,4:已放币(完成),5:卖单审核中)	integer(int32)
    var takerId: Int? = null        	   //下单的otc用户id	integer(int32)
    var updateTime: Date? = null		   //string(date-time)
    var validTime: Date? = null	       //有效时间	string(date-time)
    var whetherTaker: String? = null       //是否taker	boolean

    constructor(parcel: Parcel) : this() {
        advertisingId = parcel.readString()
        advertisingRemark = parcel.readString()
        allegeStatus = parcel.readValue(Int::class.java.classLoader) as? Int
        alleged = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        canAllege = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        canceledReason = parcel.readString()
        chatRoomId = parcel.readString()
        checkLatestNews = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        coinType = parcel.readString()
        currencyCoin = parcel.readString()
        direction = parcel.readString()
        id = parcel.readString()
        merchantAccid = parcel.readString()
        merchantId = parcel.readValue(Int::class.java.classLoader) as? Int
        merchantLevel = parcel.readString()
        merchantNickname = parcel.readString()
        otherSideAllOrders30Days = parcel.readValue(Int::class.java.classLoader) as? Int
        otherSideCompletedOrders30Days = parcel.readValue(Int::class.java.classLoader) as? Int
        otherSideCompletion30Days = parcel.readValue(Double::class.java.classLoader) as? Double
        otherSideRN = parcel.readString()
        otherSideRealName = parcel.readString()
        otherSideTel = parcel.readString()
        payEeRealName = parcel.readString()
        payInfo = parcel.readParcelable(OtcReceiptModel::class.java.classLoader)
        payMethod = parcel.readValue(Int::class.java.classLoader) as? Int
        prosecutor = parcel.readValue(Boolean::class.java.classLoader) as? Boolean
        realName = parcel.readString()
        receiptInfo = parcel.readParcelable(OtcReceiptModel::class.java.classLoader)
        status = parcel.readValue(Int::class.java.classLoader) as? Int
        takerId = parcel.readValue(Int::class.java.classLoader) as? Int
        whetherTaker = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(advertisingId)
        parcel.writeString(advertisingRemark)
        parcel.writeValue(allegeStatus)
        parcel.writeValue(alleged)
        parcel.writeValue(canAllege)
        parcel.writeString(canceledReason)
        parcel.writeString(chatRoomId)
        parcel.writeValue(checkLatestNews)
        parcel.writeString(coinType)
        parcel.writeString(currencyCoin)
        parcel.writeString(direction)
        parcel.writeString(id)
        parcel.writeString(merchantAccid)
        parcel.writeValue(merchantId)
        parcel.writeString(merchantLevel)
        parcel.writeString(merchantNickname)
        parcel.writeValue(otherSideAllOrders30Days)
        parcel.writeValue(otherSideCompletedOrders30Days)
        parcel.writeValue(otherSideCompletion30Days)
        parcel.writeString(otherSideRN)
        parcel.writeString(otherSideRealName)
        parcel.writeString(otherSideTel)
        parcel.writeString(payEeRealName)
        parcel.writeParcelable(payInfo, flags)
        parcel.writeValue(payMethod)
        parcel.writeValue(prosecutor)
        parcel.writeString(realName)
        parcel.writeParcelable(receiptInfo, flags)
        parcel.writeValue(status)
        parcel.writeValue(takerId)
        parcel.writeString(whetherTaker)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<C2COrderDetails> {
        override fun createFromParcel(parcel: Parcel): C2COrderDetails {
            return C2COrderDetails(parcel)
        }

        override fun newArray(size: Int): Array<C2COrderDetails?> {
            return arrayOfNulls(size)
        }
    }

}