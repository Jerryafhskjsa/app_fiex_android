package com.black.base.model.c2c

class C2CSellerMsg {
    var list: ArrayList<PayInfo?>? = null		//array	OtcReceiptModel
    var merchantName: String? = null	        //商家名称	string
    var merchantTel: String? = null	            //商家电话	string
    var payeeId: Int? = null  	                //收款人记录Id	integer(int32)
    var reason: String? = null  	            //拒绝原因	string
    var status: Int? = null 	                //状态0:未同意,1:审核中,2:审核通过,3:审核不通过,4:拒绝	integer(int32)
    var uid: String? = null	                    //商家Uid	string
}