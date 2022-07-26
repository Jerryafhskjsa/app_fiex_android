package com.black.base.model.money

import java.util.*

class RegularConfig {
    var totalInterestUsdtAmount //总收益折合USDT
            : Double? = null
    var lockUsdtAmount //锁仓USDT
            : Double? = null
    var totalNextInterestUsdtAmount //下一次收益USDT总和
            : Double? = null
    var coinTypeConf: ArrayList<Regular?>? = null
}
