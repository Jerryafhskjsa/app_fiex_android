package com.black.frying.contract.viewmodel.dto

import com.black.base.model.future.FundRateBean
import com.black.base.model.future.UserBalanceBean
import java.math.BigDecimal

class FuturesCoinInfoDTo(
    val coinName: String,
    val priceSincePercent: BigDecimal,
    val isCollect: Boolean
)


class UserBalanceDto(
    var coin: String,
    var isolatedMargin: String, // 逐仓保证金
    var openOrderMarginFrozen: String, // 订单冻结
    var underlyingType: Int,  //  1:币本位，2:U本位
    var walletBalance: String, // 钱包余额
    val s: String, //交易对
    val r: String,// 资金费率
    val t: String //时间戳
) {
    companion object {
        fun copyFrom(
            userBalanceBean: UserBalanceBean?,
            foundRateBean: FundRateBean?
        ): UserBalanceDto {
            return UserBalanceDto(
                coin = userBalanceBean?.coin ?: "",
                isolatedMargin = userBalanceBean?.isolatedMargin ?: "",
                openOrderMarginFrozen = userBalanceBean?.openOrderMarginFrozen ?: "",
                underlyingType = userBalanceBean?.underlyingType ?: 0,
                walletBalance = userBalanceBean?.walletBalance ?: "",
                s = foundRateBean?.s ?: "",
                r = foundRateBean?.r ?: "",
                t = foundRateBean?.t ?: "",
            )
        }
    }
}