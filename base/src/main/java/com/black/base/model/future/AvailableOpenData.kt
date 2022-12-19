package com.black.base.model.future

import java.math.BigDecimal

data class AvailableOpenData(
    var longMaxOpen: BigDecimal, //可平多
    var longMargin: BigDecimal, //可平空
    var shortMaxOpen: BigDecimal, //多仓持仓
    var shortMargin: BigDecimal //空仓持仓
)