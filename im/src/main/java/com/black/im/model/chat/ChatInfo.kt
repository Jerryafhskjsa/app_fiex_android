package com.black.im.model.chat

import com.tencent.imsdk.TIMConversationType
import java.io.Serializable

/**
 * 聊天信息基本类
 */
open class ChatInfo : Serializable {
    /**
     * 获取聊天的标题，单聊一般为对方名称，群聊为群名字
     *
     * @return
     */
    /**
     * 设置聊天的标题，单聊一般为对方名称，群聊为群名字
     *
     * @param chatName
     */
    var chatName: String? = null
    /**
     * 获取聊天类型，C2C为单聊，Group为群聊
     *
     * @return
     */
    /**
     * 设置聊天类型，C2C为单聊，Group为群聊
     *
     * @param type
     */
    open var type = TIMConversationType.C2C
    /**
     * 获取聊天唯一标识
     *
     * @return
     */
    /**
     * 设置聊天唯一标识
     *
     * @param id
     */
    var id: String? = null
    /**
     * 是否为置顶的会话
     *
     * @return
     */
    /**
     * 设置会话是否置顶
     *
     * @param topChat
     */
    var isTopChat = false

}