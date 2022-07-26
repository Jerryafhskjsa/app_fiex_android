package com.black.frying.util

import android.content.Context
import cn.udesk.UdeskSDKManager
import cn.udesk.config.UdeskConfig
import com.black.base.model.user.UserInfo
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.util.CommonUtil
import udesk.core.UdeskConst
import java.util.*

object UdeskUtil {
    fun initUdesk(context: Context) {
        UdeskSDKManager.getInstance().initApiKey(context.applicationContext, ConstData.UDESK_DOMAIN, ConstData.UDESK_APP_KEY, ConstData.UDESK_APP_ID)
    }

    fun start(context: Context) {
        //默认系统字段是Udesk已定义好的字段，开发者可以直接传入这些用户信息，供客服查看。
        val userInfo = CookieUtil.getUserInfo(context)
        var sdkToken = getUdeskToken(userInfo)
        if (sdkToken == null) {
            sdkToken = randomToken
        }
        val info: MutableMap<String, String?> = HashMap()
        //sdktoken 必填**
        info[UdeskConst.UdeskUserInfo.USER_SDK_TOKEN] = sdkToken
        //以下信息是可选
        info[UdeskConst.UdeskUserInfo.NICK_NAME] = if (userInfo == null) "" else userInfo.username
        info[UdeskConst.UdeskUserInfo.EMAIL] = if (userInfo == null) "" else userInfo.email
        info[UdeskConst.UdeskUserInfo.CELLPHONE] = if (userInfo == null) "" else userInfo.tel
        info[UdeskConst.UdeskUserInfo.DESCRIPTION] = ""
        //只设置用户基本信息的配置
        val builder = UdeskConfig.Builder()
        builder.setDefualtUserInfo(info)
        UdeskSDKManager.getInstance().entryChat(context.applicationContext, builder.build(), sdkToken)
    }

    private val randomToken: String
        get() = CommonUtil.MD5(Math.random().toString())

    private fun getUdeskToken(userInfo: UserInfo?): String? {
        return userInfo?.udeskSignature
    }
}