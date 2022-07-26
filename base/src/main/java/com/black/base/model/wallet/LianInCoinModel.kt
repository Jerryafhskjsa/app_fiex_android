package com.black.base.model.wallet

import com.black.base.view.ListDialogModel

class LianInCoinModel : ListDialogModel {
    var coinType //"USDT",
            : String? = null
    var address //"",
            : String? = null
    var memo //"",
            : String? = null
    var chainSupportDeposit //true,
            = false
    var chainType //"OMIN"
            : String? = null
    var account: String? = null
    var minChainDepositAmt: String? = null
    override fun getShowText(): String {
        return chainType!!
    }

    override fun enableClick(): Boolean {
        return chainSupportDeposit
    }
}
