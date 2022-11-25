package com.black.base.util

import android.content.Context
import android.text.TextUtils
import com.black.base.model.FryingStyleChange

object StyleChangeUtil {
    private const val EXCHANGE_STYLE_CODE = "exchange_style_code"
    private const val EXCHANGE_STYLE_TEXT = "exchange_style_text"

    fun setStyleChangeSetting(context: Context, fryingStyleChange: FryingStyleChange) {
        val preferences = CookieUtil.getSharedPreferences(context)
        val editor = preferences.edit()
        val styleCode = fryingStyleChange.styleCode
        val styleText = fryingStyleChange.styleText
        editor.putInt(EXCHANGE_STYLE_CODE, styleCode)
        editor.putString(EXCHANGE_STYLE_TEXT, styleText)
        editor.commit()
    }

    fun getStyleChangeSetting(context: Context): FryingStyleChange? {
        val preferences = CookieUtil.getSharedPreferences(context)
        val changeStyleCode = preferences.getInt(EXCHANGE_STYLE_CODE, -1)
        val changeStyleText = preferences.getString(EXCHANGE_STYLE_TEXT, null)
        return if (changeStyleCode == -1 || TextUtils.isEmpty(changeStyleText)) {
            null
        } else {
            FryingStyleChange(changeStyleCode,changeStyleText!!)
        }
    }

}