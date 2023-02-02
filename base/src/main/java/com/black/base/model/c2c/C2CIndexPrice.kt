package com.black.base.model.c2c

import java.math.BigDecimal

class C2CIndexPrice {
    var coinType: String? = null	//币种
    var currencyCoin: String? = null	//法币
    var direction: String? = null	//方向
    var price: BigDecimal? = null	//价格
    var priceType: Int?	= null    //价格类型
}