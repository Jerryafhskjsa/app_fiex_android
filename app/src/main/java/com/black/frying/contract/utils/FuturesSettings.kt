package com.black.frying.contract.utils

import com.black.base.util.SharedPreferenceUtils


fun getBuyLeverageMultiple(): Int {
    return SharedPreferenceUtils.getData(FUTURE_BUY_LEVERAGE_MULTIPLE,20) as Int
}

fun setBuyLeverageMultiple(times :Int = 20){
    SharedPreferenceUtils.putData(FUTURE_BUY_LEVERAGE_MULTIPLE,times)
}

fun getSellLeverageMultiple(): Int {
    return SharedPreferenceUtils.getData(FUTURE_SELL_LEVERAGE_MULTIPLE,20) as Int
}
fun setSellLeverageMultiple(times :Int = 20){
    SharedPreferenceUtils.putData(FUTURE_SELL_LEVERAGE_MULTIPLE,times)
}