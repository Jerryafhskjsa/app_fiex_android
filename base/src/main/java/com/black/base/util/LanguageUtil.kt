package com.black.base.util

import android.content.Context
import android.os.Build
import android.text.TextUtils
import com.black.base.model.FryingLanguage
import java.util.*

object LanguageUtil {
    private const val LANGUAGE = "language"
    private const val COUNTRY = "country"
    private const val LANGUAGE_CODE = "language_code"
    private const val LANGUAGE_TEXT = "language_text"
    /**
     * 更改应用语言
     *
     * @param context
     * @param locale      语言地区
     * @param persistence 是否持久化
     */
    fun changeAppLanguage(context: Context, locale: FryingLanguage?, persistence: Boolean) {
        if (locale == null) {
            return
        }
        val resources = context.resources
        val metrics = resources.displayMetrics
        val configuration = resources.configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(locale.locale)
        } else {
            configuration.locale = locale.locale
        }
        resources.updateConfiguration(configuration, metrics)
        if (persistence) {
            saveLanguageSetting(context, locale)
        }
    }

    private fun saveLanguageSetting(context: Context, fryingLanguage: FryingLanguage) {
        val preferences = CookieUtil.getSharedPreferences(context)
        val editor = preferences.edit()
        val locale = fryingLanguage.locale
        editor.putString(LANGUAGE, locale?.language)
        editor.putString(COUNTRY, locale?.country)
        editor.putInt(LANGUAGE_CODE, fryingLanguage.languageCode)
        editor.putString(LANGUAGE_TEXT, fryingLanguage.languageText)
        editor.commit()
    }

    fun getLanguageSetting(context: Context): FryingLanguage? {
        val preferences = CookieUtil.getSharedPreferences(context)
        val language = preferences.getString(LANGUAGE, null)
        val country = preferences.getString(COUNTRY, null)
        val languageCode = preferences.getInt(LANGUAGE_CODE, -1)
        val languageText = preferences.getString(LANGUAGE_TEXT, null)
        return if (languageCode == -1 || TextUtils.isEmpty(languageText) || TextUtils.isEmpty(language)) {
            null
        } else {
            FryingLanguage(Locale(language, country), languageCode, languageText!!)
        }
    }

    fun isSameWithSetting(context: Context): Boolean {
        val current = context.resources.configuration.locale
        val settingLocale = getLanguageSetting(context)
        return settingLocale?.locale == null || current == settingLocale.locale
    }
}