package com.black.base.model.c2c

import android.content.Context
import com.black.base.R
import com.black.base.model.BaseAdapterItem
import java.util.*

class C2CBills: BaseAdapterItem() {
    var advertisingId: String? = null	//广告id	string
    var allegeStatus: Int? = 0	//申诉状态	integer
    var alleged: Boolean? = false	//是否申述中	boolean
    var amount: Double? = 0.0	//下单成交量	number
    var buyerFee: Double? = 0.0	//买价手续费	number
    var canceledReason: String? = null	//取消原因（用户主动取消，订单超时取消）	string
    var chatRoomId: String? = null	//聊天室房间id	string
    var checkLatestNews: Boolean? = false	//是否查看订单最新状态	boolean
    var coinType: String? = null	//币种	string
    var createTime: Date? = null		//string
    var currencyCoin: String? = null	//法币币种	string
    var currencyCoinAmount: Double? = 0.0	//法币总量	number
    var direction: String? = null	//方向	string
    var id: String? = null
    var merchantId: Int? = null	//商家ID	integer
    var merchantLevel: String? = null	//商家级别--如果是taker查询(whetherTaker=true)时，需要显示订单的商家级别；如果是商家查询时不需要显示merchantLevel=null	string
    var otherSideRealName: String? = null	//对方账户实名	string
    var payMethod:Int? = null	//支付方式	integer
    var price: Double? = 0.0	//下单成交价格	number
    var realChangeAmount: Double? = 0.0	//实际资产变动	number
    var receiptName:String? = null 	//付款方式实名	string
    var sellerFee:Double? = null	//卖家手续费	number
    var status: Int? = null	//订单状态(-1:取消,2:待付款,3:已付款,4:已放币(完成),5:卖单审核中)	integer
    var takerId: Int? = null	//下单的otc用户id	integer
    var updateTime: String? = null		//string
    var validTime: Date? = null	//有效时间	string
    var whetherTaker: Boolean? = false	//是否taker	boolean

    fun getStatusText(context: Context): String {

        var statusText = context.getString(R.string.number_default)
        when(status) {
            -1 -> {
                statusText = "取消"
            }
            2 -> {
                statusText = "待付款"
            }
            3 -> {
                statusText = "已付款"
            }
            4 -> {
                statusText = "已放币"
            }
            5 -> {
                statusText = "卖单审核中"
            }

        }
        return statusText
    }

}