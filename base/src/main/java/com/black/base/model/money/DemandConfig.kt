package com.black.base.model.money

import java.util.*

class DemandConfig {
    var totalInterestUsdtAmount //总收益折合USDT
            : Double? = null
    var lockUsdtAmount //锁仓USDT
            : Double? = null
    var totalLastInterestUsdtAmount //上次收益USDT总和
            : Double? = null
    var totalNextInterestUsdtAmount //下一次收益USDT总和
            : Double? = null
    var coinTypeConf: ArrayList<Demand?>? = null
}
