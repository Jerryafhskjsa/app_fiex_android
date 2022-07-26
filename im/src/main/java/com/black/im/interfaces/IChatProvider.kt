package com.black.im.interfaces

import com.black.im.adapter.MessageListAdapter
import com.black.im.model.chat.MessageInfo

interface IChatProvider {
    /**
     * 获取聊天消息数据
     *
     * @return
     */
    fun getDataSource(): MutableList<MessageInfo?>?

    /**
     * 获取聊天消息顯示数据
     *
     * @return
     */
    fun getDataSourceShowing(): MutableList<MessageInfo?>?

    /**
     * 批量添加聊天消息
     *
     * @param messages 聊天消息
     * @param front    是否往前加（前：消息列表的头部，对应聊天界面的顶部，后：消息列表的尾部，对应聊天界面的底部）
     * @return
     */
    fun addMessageList(messages: MutableList<MessageInfo?>?, front: Boolean): Boolean

    /**
     * 批量添加聊天消息
     *
     * @param messages 聊天消息
     * @param front    是否往前加（前：消息列表的头部，对应聊天界面的顶部，后：消息列表的尾部，对应聊天界面的底部）
     * @return
     */
    fun addMessageListShowing(messages: MutableList<MessageInfo?>?, front: Boolean): Boolean


    /**
     * 批量删除聊天消息
     *
     * @param messages 聊天消息
     * @return
     */
    fun deleteMessageList(messages: MutableList<MessageInfo?>?): Boolean


    /**
     * 批量更新聊天消息
     *
     * @param messages 聊天消息
     * @return
     */
    fun updateMessageList(messages: MutableList<MessageInfo?>?): Boolean


    /**
     * 绑定会话适配器时触发的调用
     *
     * @param adapter 会话UI显示适配器
     */
    fun setAdapter(adapter: MessageListAdapter?)
}