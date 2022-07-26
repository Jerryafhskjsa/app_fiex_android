package com.black.im.interfaces

import com.black.im.model.ConversationInfo
import com.black.im.widget.ConversationListLayout

/**
 * 会话列表窗口 [ConversationLayout] 由标题区 [TitleBarLayout] 与列表区 [ConversationListLayout]
 * <br></br>组成，每一部分都提供了 UI 样式以及事件注册的接口可供修改。
 */
interface IConversationLayout : ILayout {
    /**
     * 获取会话列表 List
     *
     * @return
     */
    val conversationList: ConversationListLayout?

    /**
     * 置顶会话
     *
     * @param position     该item在列表的索引
     * @param conversation 会话内容
     */
    fun setConversationTop(position: Int, conversation: ConversationInfo?)

    /**
     * 删除会话
     *
     * @param position     该item在列表的索引
     * @param conversation 会话内容
     */
    fun deleteConversation(position: Int, conversation: ConversationInfo?)
}