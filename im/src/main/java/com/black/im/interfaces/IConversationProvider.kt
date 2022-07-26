package com.black.im.interfaces

import com.black.im.adapter.IConversationAdapter
import com.black.im.model.ConversationInfo

/**
 * 会话列表数据源
 */
interface IConversationProvider {
    /**
     * 获取具体的会话数据集合，ConversationContainer依据该数据集合展示会话列表
     *
     * @return
     */
    fun getDataSource(): MutableList<ConversationInfo?>?

    /**
     * 批量添加会话条目
     *
     * @param conversations 会话数据集合
     * @return
     */
    fun addConversations(conversations: MutableList<ConversationInfo?>?): Boolean

    /**
     * 删除会话条目
     *
     * @param conversations 会话数据集合
     * @return
     */
    fun deleteConversations(conversations: MutableList<ConversationInfo?>?): Boolean

    /**
     * 更新会话条目
     *
     * @param conversations 会话数据集合
     * @return
     */
    fun updateConversations(conversations: MutableList<ConversationInfo?>?): Boolean

    /**
     * 绑定会话适配器时触发的调用，在调用[IConversationAdapter.setDataProvider]时自动调用
     *
     * @param adapter 会话UI显示适配器
     * @return
     */
    fun attachAdapter(adapter: IConversationAdapter?)
}