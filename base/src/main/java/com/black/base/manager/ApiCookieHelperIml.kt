package com.black.base.manager

import android.content.Context
import com.black.base.util.UrlConfig
import com.black.net.ApiCookieHelper
import okhttp3.Request


class ApiCookieHelperIml(context: Context?) : ApiCookieHelper {
    override fun canSaveGlobalCookie(request: Request): Boolean {
        val fullUrl = request.url().toString()
        return fullUrl.endsWith(UrlConfig.Config.URL_GEETEST_INIT)
    }

    override fun useGlobalCookie(request: Request): Boolean {
        val fullUrl = request.url().toString()
        return fullUrl.endsWith(UrlConfig.Money.URL_PROMOTIONS_ADD)
    }
}
