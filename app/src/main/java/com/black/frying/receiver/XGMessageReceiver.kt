package com.black.frying.receiver

import android.content.Context
import android.net.Uri
import android.util.Log
import com.black.base.util.FryingUtil
import com.black.frying.FryingApplication
import com.black.router.BlackRouter
import com.tencent.android.tpush.*

class XGMessageReceiver : XGPushBaseReceiver() {
    override fun onRegisterResult(context: Context, i: Int, xgPushRegisterResult: XGPushRegisterResult) {}
    override fun onUnregisterResult(context: Context, i: Int) {}
    override fun onSetTagResult(context: Context, i: Int, s: String) {}
    override fun onDeleteTagResult(context: Context, i: Int, s: String) {}
    override fun onTextMessage(context: Context, xgPushTextMessage: XGPushTextMessage) {
        //        String content = xgPushTextMessage.getContent();
//        try {
//            Uri uri = Uri.parse(content);
//            if (uri != null && uri.isHierarchical()) {
//                BlackRouter.getInstance().build(content).go(context);
//            }
//        } catch (Exception ignored) {
//
//        }
    }

    override fun onNotifactionClickedResult(context: Context, message: XGPushClickedResult) {
        val customContent = message.customContent
//        if (customContent != null && customContent.length() != 0) {
//            try {
//                JSONObject obj = new JSONObject(customContent);
//                // key1为前台配置的key
//                if (!obj.isNull("router")) {
//                    String router = obj.getString("router");
//                    Activity currentActivity = FryingApplication.getCurrentActivity();
//                    Log.e("XGMessageReceiver", "currentActivity:" + currentActivity);
//                    //锁屏界面不可跳转
//                    if (currentActivity != null && FryingUtil.cannotJumpNotificationRouter(currentActivity)) {
//                        BlackRouter.getInstance().build(router).go(currentActivity);
//                    }
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        try {
            val uri = Uri.parse(customContent)
            if (uri != null && uri.isHierarchical) {
                val currentActivity = FryingApplication.currentActivity
                //锁屏界面不可跳转
                if (currentActivity != null && !FryingUtil.cannotJumpNotificationRouter(currentActivity)) {
                    BlackRouter.getInstance().build(customContent).go(currentActivity)
                }
            }
        } catch (ignored: Exception) {
        }
    }

    override fun onNotifactionShowedResult(context: Context, xgPushShowedResult: XGPushShowedResult) {}
}