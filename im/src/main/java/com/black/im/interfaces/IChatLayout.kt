package com.black.im.interfaces

import com.black.im.model.chat.ChatInfo
import com.black.im.model.chat.MessageInfo
import com.black.im.widget.InputLayout
import com.black.im.widget.MessageLayout
import com.black.im.widget.NoticeLayout

/**
 * 聊天窗口 [ChatLayout] 提供了消息的展示与发送等功能，界面布局从上到下分为四个部分: <br></br>
 * <pre>    标题区 [TitleBarLayout]，
 * 提醒区 [NoticeLayout]，
 * 消息区 [MessageLayout]，
 * 输入区 [InputLayout]，</pre>
 * 每个区域提供了多样的方法以供定制使用。
 */
interface IChatLayout : ILayout {
    /**
     * 获取聊天窗口 Input 区域 Layout
     *
     * @return
     */
    val inputLayout: InputLayout?

    /**
     * 获取聊天窗口 Message 区域 Layout
     *
     * @return
     */
    val messageLayout: MessageLayout?

    /**
     * 获取聊天窗口 Notice 区域 Layout
     *
     * @return
     */
    val noticeLayout: NoticeLayout?

    /**
     * 设置当前的会话 ID，会话面板会依据该 ID 加载会话所需的相关信息，如消息记录，用户（群）信息等
     *
     * @param chatInfo
     */
    fun setChatInfo(chatInfo: ChatInfo?)

    /**
     * 退出聊天，释放相关资源（一般在 activity finish 时调用）
     */
    fun exitChat()

    /**
     * 初始化参数
     */
    fun initDefault()

    /**
     * 加载聊天消息
     */
    fun loadMessages()

    /**
     * 发送消息
     *
     * @param msg   消息
     * @param retry 是否重试
     */
    fun sendMessage(msg: MessageInfo?, retry: Boolean)
}