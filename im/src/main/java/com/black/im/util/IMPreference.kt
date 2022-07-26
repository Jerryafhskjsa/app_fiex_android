package com.black.im.util

import android.content.Context
import android.content.SharedPreferences

object IMPreference {
    fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(context.packageName + ".IM", Context.MODE_PRIVATE)
    }

    fun isLogin(context: Context): Boolean {
        val prefs = getSharedPreferences(context)
        return prefs.getBoolean(IMConstData.AUTO_LOGIN, false)
    }

    fun saveLoginStatus(context: Context, isLogin: Boolean) {
        val prefs = getSharedPreferences(context)
        prefs.edit().putBoolean(IMConstData.AUTO_LOGIN, isLogin).apply()
    }
}
