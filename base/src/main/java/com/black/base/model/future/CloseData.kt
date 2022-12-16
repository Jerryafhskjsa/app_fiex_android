package com.black.base.model.future

import java.math.BigDecimal

data class CloseData(
    var long: BigDecimal, //可平多
    var short: BigDecimal, //可平空
    var longPosition: BigDecimal, //多仓持仓
    var shortPosition: BigDecimal //空仓持仓
)