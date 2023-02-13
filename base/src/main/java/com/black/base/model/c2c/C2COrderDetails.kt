package com.black.base.model.c2c

import java.math.BigDecimal

class C2COrderDetails {
    var advertisingId: String? = null	    //广告id	string
    var advertisingRemark: String? = null	//广告备注	string
    var allegeStatus: Int? = null	        //申诉状态	integer(int32)
    var alleged: Boolean? = null	        //是否申述中	boolean
    var amount: BigDecimal? = null	        //下单成交量	number(bigdecimal)
    var buyerFee: BigDecimal? = null	    //买价手续费	number(bigdecimal)
    var canAllege: Boolean? = null	        //是否可申述	boolean
    var canAllegeEndTime: String? = null	//可申述结束时间	string(date-time)
    var canAllegeStartTime: String? = null 	//可申述开始时间	string(date-time)
    var canCancelTime: String? = null	    //可以取消订单时间，(status=2(待付款),3(已付款)、5(审核中)时可取消，其他状态时=null)，status=5时小于当前时间可以取消，status=2时大于当前时间可以取消	string(date-time)
    var canceledReason: String? = null	    //取消原因（用户主动取消，订单超时取消）	string
    var chatRoomId: String? = null	        //聊天室房间id	string
    var checkLatestNews: Boolean? = null	//是否查看订单最新状态	boolean
    var coinType: String? = null	        //币种	string
    var createTime: String? = null		    //string(date-time)
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
    var payInfo: PayInfo? = null	   //买家付款方式详情	OtcReceiptModel	OtcReceiptModel
    var payMethod: Int? = null	           //支付方式	integer(int32)
    var price: BigDecimal? = null	       //下单成交价格	number(bigdecimal)
    var prosecutor: Boolean? = null	       //是否申述方	boolean
    var realChangeAmount: BigDecimal? = null//实际资产变动	number(bigdecimal)
    var realName: String? = null	       //用户自己的实名	string
    var receiptInfo: ReceiptInfo? = null   //卖家收款方式详情	OtcReceiptModel	OtcReceiptModel
    var sellerFee: BigDecimal? = null	   //卖家手续费	number(bigdecimal)
    var status: Int? = null	               //订单状态(-1:取消,2:待付款,3:已付款,4:已放币(完成),5:卖单审核中)	integer(int32)
    var takerId: Int? = null        	   //下单的otc用户id	integer(int32)
    var updateTime: String? = null		   //string(date-time)
    var validTime: String? = null	       //有效时间	string(date-time)
    var whetherTaker: String? = null       //是否taker	boolean
}