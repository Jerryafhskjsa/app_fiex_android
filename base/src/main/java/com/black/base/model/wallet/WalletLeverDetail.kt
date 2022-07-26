package com.black.base.model.wallet

import java.math.BigDecimal

class WalletLeverDetail {
    var userId: String? = null
    var pair: String? = null
    var createTime: Long? = null

    var totalCNY //总资产CNY
            : Double? = null
    var netAssetsCNY //净资产CNY
            : Double? = null
    var totalDebtCNY //总负债CNY
            : Double? = null

    var coinType: String? = null
    var coinAmount //可用
            : BigDecimal? = null
    var coinFroze //冻结
            : BigDecimal? = null
    var coinBorrow //已借
            : BigDecimal? = null
    var coinInterest //利息
            : BigDecimal? = null
    var coinMaxBorrow //可借
            : BigDecimal? = null
    var coinMaxTransfer //可划转
            : BigDecimal? = null
    var totalAmount: BigDecimal? = null
    var estimatedCoinType: String? = null
    var estimatedTotalAmount: BigDecimal? = null
    var estimatedAvailableAmount: BigDecimal? = null

    var afterCoinType: String? = null
    var afterCoinAmount: BigDecimal? = null
    var afterCoinFroze: BigDecimal? = null
    var afterCoinBorrow: BigDecimal? = null
    var afterCoinInterest: BigDecimal? = null
    var afterCoinMaxBorrow: BigDecimal? = null
    var afterCoinMaxTransfer: BigDecimal? = null
    var afterTotalAmount: BigDecimal? = null
    var afterEstimatedCoinType: String? = null
    var afterEstimatedTotalAmount: BigDecimal? = null
    var afterEstimatedAvailableAmount: BigDecimal? = null

    var riskRate: BigDecimal? = null
    var burstPrice: BigDecimal? = null
    var minAmount: BigDecimal? = null
    var afterMinAmount: BigDecimal? = null
    var warnRate: BigDecimal? = null
    var burstRate: BigDecimal? = null
    var outRate: BigDecimal? = null
    var accountType: String? = null
    var interestRate: BigDecimal? = null
}
