package com.black.base.model.c2c

class OtcReceiptDTO {
    var account:String? = null	//账户名		string
    var depositBank:String? = null	//	银行名称		string
    var emailCode:String? = null	//	邮箱code
    var googleCode:String? = null	//	谷歌code
    var name:String? = null	//	姓名
    var phoneCode:String? = null	//	电话code
    var receiptImage:String? = null	//	收款图片
    var subbranch:String? = null	//	开户地点
    var type: Int? = null	        //收款方式：0：银行卡，1：支付宝，2：微信

}