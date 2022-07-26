package com.black.im.interfaces

import com.black.im.util.TUIKitLog
import com.tencent.imsdk.TIMConversation
import com.tencent.imsdk.TIMGroupTipsElem
import com.tencent.imsdk.TIMMessage

/**
 * IM事件监听
 */
abstract class IMEventListener {
    /**
     * 被踢下线时回调
     */
    fun onForceOffline() {
        TUIKitLog.d(TAG, "recv onForceOffline")
    }

    /**
     * 用户票据过期
     */
    fun onUserSigExpired() {
        TUIKitLog.d(TAG, "recv onUserSigExpired")
    }

    /**
     * 连接建立
     */
    fun onConnected() {
        TUIKitLog.d(TAG, "recv onConnected")
    }

    /**
     * 连接断开
     *
     * @param code 错误码
     * @param desc 错误描述
     */
    fun onDisconnected(code: Int, desc: String) {
        TUIKitLog.d(TAG, "recv onDisconnected, code $code|desc $desc")
    }

    /**
     * WIFI需要验证
     *
     * @param name wifi名称
     */
    fun onWifiNeedAuth(name: String) {
        TUIKitLog.d(TAG, "recv onWifiNeedAuth, wifi payeeName $name")
    }

    /**
     * 部分会话刷新（包括多终端已读上报同步）
     *
     * @param conversations 需要刷新的会话列表
     */
    fun onRefreshConversation(conversations: List<TIMConversation?>?) {
        TUIKitLog.d(TAG, "recv onRefreshConversation, size " + (conversations?.size ?: 0))
    }

    /**
     * 收到新消息回调
     *
     * @param msgs 收到的新消息
     */
    fun onNewMessages(msgs: List<TIMMessage?>?) {
        TUIKitLog.d(TAG, "recv onNewMessages, size " + (msgs?.size ?: 0))
    }

    /**
     * 群Tips事件通知回调
     *
     * @param elem 群tips消息
     */
    fun onGroupTipsEvent(elem: TIMGroupTipsElem) {
        TUIKitLog.d(TAG, "recv onGroupTipsEvent, groupid: " + elem.groupId + "|type: " + elem.tipsType)
    }

    companion object {
        private val TAG = IMEventListener::class.java.simpleName
    }
}