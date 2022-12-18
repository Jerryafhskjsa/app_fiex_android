package com.black.base.util

import android.content.Context
import com.black.net.HttpCookieUtil

object LoginUtil {
    fun isFutureLogin(context: Context?): Boolean {
        val future_token = HttpCookieUtil.geFutureToken(context)
        return future_token != null
    }
}