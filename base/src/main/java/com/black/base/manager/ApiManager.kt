package com.black.base.manager

import android.content.Context
import android.util.Log
import com.black.base.util.*
import com.black.net.ApiManagerImpl
import com.black.net.HttpCookieUtil
import com.black.util.CommonUtil
import java.io.File

class ApiManager {
    private var apiManagerIml: ApiManagerImpl? = null
    fun <T> getService(tClass: Class<T>?): T? {
        return apiManagerIml?.create(tClass)
    }

    companion object {
        private var apiManager: ApiManager? = null
        private val instance: ApiManager
            get() {
                if (apiManager == null) {
                    apiManager = ApiManager()
                }
                return apiManager!!
            }

        fun build(context: Context): ApiManager {
            return build(context.applicationContext, false)
        }

        /**
         * 测速专用
         */
        fun buildTestSpeed(context: Context,url:String?):ApiManager{
            var context1 = context
            context1 = context1.applicationContext
            val apiManager = instance
            val language = LanguageUtil.getLanguageSetting(context1)
            val lang = if (language != null && language.languageCode == 4) "en" else "zh-cn"
            val deviceId = CommonUtil.getDeviceId(context1)
            apiManager.apiManagerIml = ApiManagerImpl.getInstance(context1, ConstData.CACHE_PATH,
                "$url/pro/", deviceId, lang,null, ApiCookieHelperIml(context1), HttpInterceptHelperIml())
            return apiManager
        }

        fun build(context: Context, noToken: Boolean): ApiManager {
            var context1 = context
            context1 = context1.applicationContext
            val apiManager = instance
            val language = LanguageUtil.getLanguageSetting(context1)
            val lang = if (language != null && language.languageCode == 4) "en" else "zh-cn"
            val deviceId = CommonUtil.getDeviceId(context1)
            apiManager.apiManagerIml = ApiManagerImpl.getInstance(context1, ConstData.CACHE_PATH, UrlConfig.getHost(context1), deviceId, lang, if (noToken) null else HttpCookieUtil.getUcToken(context1), ApiCookieHelperIml(context1), HttpInterceptHelperIml())
            return apiManager
        }

        fun build(context: Context,apiType:String): ApiManager {
            return build(context.applicationContext, false,apiType)
        }

        /**
         * apiType->uc,api,pro
         */
        fun build(context: Context, noToken: Boolean,apiType : String): ApiManager {
            var context1 = context.applicationContext
            val apiManager = instance
            val language = LanguageUtil.getLanguageSetting(context1)
            val lang = if (language != null && language.languageCode == 4) "en" else "zh-cn"
            val deviceId = CommonUtil.getDeviceId(context1)
            var realUrl = UrlConfig.getFiexHost(context1,apiType)
            var token = if (noToken) null else HttpCookieUtil.getUcToken(context1)
            apiManager.apiManagerIml = ApiManagerImpl.getInstance(context1, ConstData.CACHE_PATH, realUrl, deviceId, lang, token, ApiCookieHelperIml(context1), HttpInterceptHelperIml())
            return apiManager
        }

        fun clearCache() {
            ApiManagerImpl.clearCache()
            val cacheFile = File(ConstData.CACHE_PATH)
            try {
                if (cacheFile != null && cacheFile.exists() && cacheFile.isDirectory) {
                    val files = cacheFile.listFiles()
                    if (files != null) {
                        for (file in files) {
                            file.delete()
                        }
                    }
                }
            } catch (ignored: Throwable) {
            }
        }
    }
}