package com.black.base.share

import android.content.Context

abstract class SharePlatform {
    abstract fun share(shareParams: ShareParams?, listener: ShareResultListener?)

    companion object {
        const val PLATFORM_WECHAT = "wechat"
        const val PLATFORM_WECHAT_MOMENTS = "wechat_moments"
        const val SHARE_TEXT = 1
        const val SHARE_IMAGE = 2
        const val SHARE_WEBPAGE = 4
        const val NO_IMAGE = 1
        const val NO_WECHAT = 2
        const val WECHAT_REG_FAILED = 4
        fun getPlatform(context: Context, platformType: String?): SharePlatform {
            return when (platformType) {
                PLATFORM_WECHAT -> WechatPlatform.getInstance(context)
                PLATFORM_WECHAT_MOMENTS -> WechatMomentsPlatform.getInstance(context)
                else -> throw RuntimeException("type error")
            }
        }

        /**
         * 判断 用户是否安装微信客户端
         */
        fun isWeixinAvilible(context: Context): Boolean {
            val packageManager = context.packageManager // 获取packagemanager
            val pinfo = packageManager.getInstalledPackages(0) // 获取所有已安装程序的包信息
            if (pinfo != null) {
                for (i in pinfo.indices) {
                    val pn = pinfo[i].packageName
                    if (pn == "com.tencent.mm") {
                        return true
                    }
                }
            }
            return false
        }
    }
}