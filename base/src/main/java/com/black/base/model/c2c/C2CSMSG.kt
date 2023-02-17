package com.black.base.model.c2c

class C2CSMSG {
    var authentication: Boolean? = null	//身份认证	boolean
    var avgReleaseTime: Int? = null	    //商家平均放行时间（秒）	integer(int32)
    var completionRate: String? = null	//商家订单完成率	string
    var emailAuthentication: Boolean? = null	//邮箱认证	boolean
    var level:  String? = null	        //商家级别	string
    var merchantAuthentication: Boolean? = null	//商家认证	boolean
    var merchantCompleted30Days: Int? = null	//商家30天完成订单数	integer(int32)
    var merchantCompletedTotal: Int? = null	    //商家总完成订单数	integer(int32)
    var name: String? = null	       //商家名称	string
    var phoneAuthentication: Boolean? = null	//手机认证	boolean
}