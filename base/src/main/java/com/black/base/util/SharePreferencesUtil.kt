package com.black.base.util

import android.content.Context
import android.content.SharedPreferences
import com.black.base.BaseApplication

object SharePreferencesUtil {
    private val TAG: String = BaseApplication.instance.getPackageName()
    fun setTextValue(tag: String?, value: String?) {
        val sp: SharedPreferences = BaseApplication.instance.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putString(tag, value)
        editor.apply()
    }

    fun setTextValue(tag: String?, value: Boolean) {
        val sp: SharedPreferences = BaseApplication.instance.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putBoolean(tag, value)
        editor.apply()
    }

    fun setTextValue(tag: String?, value: Long) {
        val sp: SharedPreferences = BaseApplication.instance.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        val editor = sp.edit()
        editor.putLong(tag, value)
        editor.apply()
    }

    fun getTextValueBooble(tag: String?): Boolean {
        val sp: SharedPreferences = BaseApplication.instance.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        return sp.getBoolean(tag, false)
    }

    fun getTextValue(tag: String?): String {
        val sp: SharedPreferences = BaseApplication.instance.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        return sp.getString(tag, "") ?: ""
    }

    fun getTextValueLong(tag: String?): Long {
        val sp: SharedPreferences = BaseApplication.instance.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        return sp.getLong(tag, 0)
    }

    /**
     * 清空缓存
     */
    fun clearSharePrefrence() {
        val mSPreferences: SharedPreferences = BaseApplication.instance.getSharedPreferences(TAG, Context.MODE_PRIVATE)
        mSPreferences.edit().clear().apply()
    }
}