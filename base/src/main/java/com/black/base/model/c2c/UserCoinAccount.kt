package com.black.base.model.c2c



class UserCoinAccount {
    var coinAmount: Double? = null	//可用数量	number(bigdecimal)
    var coinFroze: Double? = null	//冻结数量	number(bigdecimal)
    var coinType: String? = null	//币种	string
    var id: String? = null		    //string
    var updateTime: Timestamp? = null	//最后更新时间	Timestamp	Timestamp
    var userId: String? = null	   //用户Id	string
    var walletType: String? = null	//OTC	string
}