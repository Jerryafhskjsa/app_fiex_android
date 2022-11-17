package com.black.base.util

import android.content.Context
import android.text.TextUtils
import com.black.base.model.FryingExchangeRates

object ExchangeRatesUtil {
    private const val EXCHANGE_RATE_CODE = "exchange_rate_code"
    private const val EXCHANGE_RATE_TEXT = "exchange_rate_text"

    fun setExchangeRatesSetting(context: Context, fryingExchangeRates: FryingExchangeRates) {
        val preferences = CookieUtil.getSharedPreferences(context)
        val editor = preferences.edit()
        val rateCode = fryingExchangeRates.rateCode
        val rateText = fryingExchangeRates.rateText
        editor.putInt(EXCHANGE_RATE_CODE, rateCode)
        editor.putString(EXCHANGE_RATE_TEXT, rateText)
        editor.commit()
    }

    fun getExchangeRatesSetting(context: Context): FryingExchangeRates? {
        val preferences = CookieUtil.getSharedPreferences(context)
        val exchangeRateCode = preferences.getInt(EXCHANGE_RATE_CODE, -1)
        val exchangeRateText = preferences.getString(EXCHANGE_RATE_TEXT, null)
        return if (exchangeRateCode == -1 || TextUtils.isEmpty(exchangeRateText)) {
            null
        } else {
            FryingExchangeRates(exchangeRateCode,exchangeRateText)
        }

    }

}