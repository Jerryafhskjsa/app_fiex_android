package com.black.base.model

import java.math.BigDecimal

class AssetTransfer {
    var amount: BigDecimal? = null
    var coin: String? = null//大写
    var fromWalletType: String? = null//spot或contract
    var toWalletType:String? = null//spot或contract
}