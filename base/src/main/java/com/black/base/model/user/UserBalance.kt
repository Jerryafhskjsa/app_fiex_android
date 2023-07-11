package com.black.base.model.user

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class UserBalance : Parcelable {
    var coin: String? = null//币种
    var balance: String? = null//现货总余额
    var freeze:String? = null//现货冻结
    var availableBalance:String? = null//可用余额
    var estimatedTotalAmount:String? = null//总折合u
    var estimatedCynAmount:String? = null//总折合cny
    var estimatedAvailableAmount:String? = null//可用折合
    var estimatedCoinType:String? = null//折合币种
    //合约数据
    var bonus:String? = null//体验金余额
    var crossedMargin:String? = null//全仓起始保证金
    var isolatedMargin:String? = null//逐仓保证金冻结
    var openOrderMarginFrozen:String? = null//订单冻结
    var profit:String? = "0.0"//未实现盈亏
    var walletBalance:String? = null//钱包余额
}
