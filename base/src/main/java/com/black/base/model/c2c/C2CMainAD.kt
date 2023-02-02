package com.black.base.model.c2c

import java.math.BigDecimal

class C2CMainAD {
    var canCreateOrderForQueryUser: Boolean? = null	//当前用户是否可下此广告的订单
    var coinType: String? = null	//币种
    var completedOrders: Int? = null	//完成订单数
    var completion: String? = null	//订单完成率	number
    var createTime: String? = null
    var currencyCoin: String? = null	//法币币种	string
    var currentPrice:BigDecimal? = null	//当前价格，页面展示用此字段展示单价	number
    var direction: String? = null	//方向	string
    var finishedOrderAmount:BigDecimal? = null	//	此广告已完成量	number
    var id:String? = null
    var merchantAllOrders30Days: Int? = null	//	商家30天订单数	integer
    var merchantCompletedOrders30Days: Int? = null	//	商家30天完成订单数	integer
    var merchantId: Int? = null	//	otc商家id	integer
    var merchantLevel: String? = null	//商家级别	string
    var merchantTotalCompletion:BigDecimal? = null	//	商家订单完成率	number
    var notCanCreateOrderReasonCode: String? = null	//不可以创建订单原因code	string
    var notCanCreateOrderReasonMsg: String? = null	//不可以创建订单原因	string
    var payMethods: String? = null	//付款方式,支付方式(1:支付宝，2:微信，3:银行卡);以逗号(,)号分隔的字符串	string
    var priceParam:BigDecimal? = null	//	单价参数,(priceType=1时为下单价，priceType=2,3时为动态价格的浮动参数)	number
    var priceType: Int? = null	//	单价类型（1:固定单价，2：指数价格，3：盘口价格）
    var realName: String? = null	//商家昵称	string
    var registeredDays:BigDecimal? = null	//	注册天数
    var remark: String? = null	//商家备注	string
    var singleLimitMax:BigDecimal? = null	//	单笔最大下单额度（法币总量）	number
    var singleLimitMin:BigDecimal? = null	//	单笔最小下单额度（法币总量）	number
    var status: Int? = null	//	广告状态(1:新建，2:上架，审核中,3：上架，审核通过，4：下架,5:审核不通过)
    var totalAmount:BigDecimal? = null	//	可买入或者出售总量
    var updateTime: String? = null
    var more: Boolean? = null
}