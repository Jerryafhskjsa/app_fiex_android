package com.black.base.model.c2c

class PayInfo {
    var account: String? = null	   //联系方式	string
    var depositBank: String? = null//银行	string
    var id: Int? = null       	   //id	integer
    var name: String? = null	   //支付方式名称	string
    var receiptImage: String? = null//	二维码图片	string
    var status: Int? = null	       //状态0:未使用,1:使用中	integer
    var type: Int? = null   	   //类型	integer
}