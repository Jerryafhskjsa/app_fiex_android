package com.black.im.model

import android.graphics.Bitmap
import com.black.im.model.chat.MessageInfo
import java.io.Serializable

class ConversationInfo : Serializable, Comparable<ConversationInfo?> {
    companion object {
        const val TYPE_COMMON = 1
        const val TYPE_CUSTOM = 2
    }

    /**
     * 会话类型，自定义会话or普通会话
     */
    var type = 0
    /**
     * 消息未读数
     */
    var unRead = 0
    /**
     * 会话ID
     */
    var conversationId: String? = null
    /**
     * 会话标识，C2C为对方用户ID，群聊为群组ID
     */
    var id: String? = null
    /**
     * 会话头像url
     */
    var iconUrl: String? = null
    /**
     * 会话标题
     */
    var title: String? = null
    /**
     * 会话头像
     */
    var icon: Bitmap? = null
    /**
     * 是否为群会话
     */
    var isGroup = false
    /**
     * 是否为置顶会话
     */
    var isTop = false
    /**
     * 获得最后一条消息的时间，单位是秒
     */
    /**
     * 设置最后一条消息的时间，单位是秒
     * @param lastMessageTime
     */
    /**
     * 最后一条消息时间
     */
    var lastMessageTime: Long = 0
    /**
     * 最后一条消息，MessageInfo对象
     */
    var lastMessage: MessageInfo? = null

    override operator fun compareTo(other: ConversationInfo?): Int {
        return if (lastMessageTime > other?.lastMessageTime ?: 0) -1 else 1
    }

    override fun toString(): String {
        return "ConversationInfo{" +
                "type=" + type +
                ", unRead=" + unRead +
                ", conversationId='" + conversationId + '\'' +
                ", id='" + id + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", title='" + title + '\'' +
                ", icon=" + icon +
                ", isGroup=" + isGroup +
                ", top=" + isTop +
                ", lastMessageTime=" + lastMessageTime +
                ", lastMessage=" + lastMessage +
                '}'
    }
}