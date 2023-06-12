package com.black.base.util

import android.content.Context
import android.text.TextUtils
import com.black.base.model.FryingStyleChange
import com.black.base.model.FutureSecondChange

object FutureSecond {
    private const val FUTURE_SECOND_CODE = "future_second_code"
    private const val FUTURE_SECOND_TEXT = "future_sercond_text"

    fun setFutureSecondSetting(context: Context, futureSecondChange: FutureSecondChange) {
        val preferences = CookieUtil.getSharedPreferences(context)
        val editor = preferences.edit()
        val styleCode = futureSecondChange.futureCode
        val styleText = futureSecondChange.futureText
        editor.putInt(FUTURE_SECOND_CODE, styleCode)
        editor.putBoolean(FUTURE_SECOND_TEXT, styleText)
        editor.commit()
    }

    fun getFutureSecondSetting(context: Context): FutureSecondChange? {
        val preferences = CookieUtil.getSharedPreferences(context)
        val changeStyleCode = preferences.getInt(FUTURE_SECOND_CODE, -1)
        val changeStyleText = preferences.getBoolean(FUTURE_SECOND_TEXT, true)
        return if (changeStyleCode == -1) {
            null
        } else {
            FutureSecondChange(changeStyleCode,changeStyleText)
        }
    }

}