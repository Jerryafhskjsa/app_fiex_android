package com.black.base.model.c2c

import java.math.BigDecimal

class OrderConfig {
    var coinAmountMax: BigDecimal? = null	//币种最大数量限制	number(bigdecimal)
    var coinAmountMin: BigDecimal? = null	//币种最小数量限制	number(bigdecimal)
    var coinType:String? = null	//币种	string
    var currencyCoin: String? = null	//法币	string
    var currencyCoinAmountMax: BigDecimal? = null	//法币最大数量限制	number(bigdecimal)
    var currencyCoinAmountMin: BigDecimal? = null	//法币最小数量限制	number(bigdecimal)
}