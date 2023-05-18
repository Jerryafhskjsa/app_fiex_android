package com.black.frying.contract.viewmodel.model

import com.black.base.model.future.SymbolBean
import com.black.base.util.CookieUtil
import com.black.base.util.LanguageUtil
import com.black.frying.FryingApplication
import java.util.*

data class FuturesCoinPair(val coinSrc: String, val coinTar: String) {
    companion object {
        const val DEFAULT_COIN_PAIR_BTC_ = "btc"
        const val DEFAULT_COIN_PAIR_USDT = "usdt"
        const val DEFAULT_COIN_PAIR = "btc_usdt"
        fun load(): FuturesCoinPair? {
            val context = FryingApplication.instance()
            val uPair = CookieUtil.getCurrentFutureUPair(context)
            return if (uPair.isNullOrEmpty()) {
                return null
            } else {
                val split = uPair.split("_")
                FuturesCoinPair(split[0], split[1])
            }
        }
    }

    fun reload(): FuturesCoinPair? {
        return load()
    }

    fun source(): String {
        return "${coinSrc}_$coinTar"
    }
    fun display(): String {
        return "$coinSrc/$coinTar"
    }
}

fun SymbolBean.display(): String {
    val languageSetting = LanguageUtil.getLanguageSetting(FryingApplication.instance())
    if (languageSetting?.locale == Locale.CHINESE) {
        return cnName
    }else {
        return enName
    }
//    return symbol.replace("_","/").uppercase(Locale.ROOT)
}


data class OrderInfo(
    val symbol :String,
    val origQty :Number,
    val orderType:String,
    val price :Number,
    val timeInForce :String,
    val orderSide :String,
    val positionSide :String,
    val triggerProfitPrice :Number,
    val triggerStopPrice :Number,
    val reduceOnly :Boolean
)
