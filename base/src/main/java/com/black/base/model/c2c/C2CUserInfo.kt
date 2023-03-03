package com.black.base.model.c2c

class C2CUserInfo {
    var accid: String? = null	//网易聊天accid	string
    var allOrders: Int? = null	//总订单数	integer(int64)
    var coinVOS: ArrayList<CoinVOS?>? = null 	//otc币种	array	OtcCoinVO
    var completedOrders: Int? = null 	//完成订单数	integer(int64)
    var completion: Double? = null	    //完成率	number(bigdecimal)
    var countrySupport: Boolean? = null	//	boolean
    var kyc: Boolean? = null		    //boolean
    var levels: ArrayList<C2CLevels?>? = null    //otc商家等级	array	OtcMerchantLevel
    var merchant:Boolean? = null		//boolean
    var name: String? = null    	    //真实姓名	string
    var otcAuth: Boolean? = null		//boolean
    var otcFrozen: Boolean? = null	    //otc是否冻结	boolean
    var payMethods: ArrayList<Int?>? = null	//使用中收付款类型	array	integer
    var payeeNames: ArrayList<String?>? = null	//收款人姓名集合	array	string
    var registeredDays: Int? = null	 //注册天数	integer(int64)
    var tel: String? = null	         //电话	string
    var telCountryCode: String? = null	//电话国家码	string
    var token: String? = null	     //网易聊天token	string
    var unfrozenTime: String? = null 	//otc解冻时间	string(date-time)
}