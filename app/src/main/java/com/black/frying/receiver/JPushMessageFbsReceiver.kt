package com.black.frying.receiver

import android.content.Context
import android.net.Uri
import android.util.Log
import cn.jpush.android.api.JPushMessage
import cn.jpush.android.api.NotificationMessage
import cn.jpush.android.service.JPushMessageReceiver
import com.black.base.util.FryingUtil
import com.black.frying.FryingApplication
import com.black.router.BlackRouter
import com.google.gson.Gson
import com.google.gson.JsonObject

class JPushMessageFbsReceiver : JPushMessageReceiver() {
    override fun onTagOperatorResult(context: Context, jPushMessage: JPushMessage) {
        super.onTagOperatorResult(context, jPushMessage)
    }

    override fun onCheckTagOperatorResult(context: Context, jPushMessage: JPushMessage) {
        super.onCheckTagOperatorResult(context, jPushMessage)
    }

    override fun onAliasOperatorResult(context: Context, jPushMessage: JPushMessage) {
        super.onAliasOperatorResult(context, jPushMessage)
    }

    override fun onMobileNumberOperatorResult(context: Context, jPushMessage: JPushMessage) {
        super.onMobileNumberOperatorResult(context, jPushMessage)
    }

    override fun onNotifyMessageOpened(context: Context, notificationMessage: NotificationMessage) {
        super.onNotifyMessageOpened(context, notificationMessage)
        try {
            val extras = notificationMessage.notificationExtras
            val extrasJson = if (extras == null) null else Gson().fromJson(extras, JsonObject::class.java)
            val url = if (extrasJson == null) null else extrasJson["url"].asString
            val uri = if (url == null) null else Uri.parse(url)
            if (uri != null && uri.isHierarchical) {
                val currentActivity = FryingApplication.currentActivity
                //锁屏界面不可跳转
                if (currentActivity != null && !FryingUtil.cannotJumpNotificationRouter(currentActivity)) {
                    BlackRouter.getInstance().build(url).go(currentActivity)
                }
            }
        } catch (ignored: Exception) {
        }
    }
}