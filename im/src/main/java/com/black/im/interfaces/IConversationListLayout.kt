package com.black.im.interfaces

import com.black.im.adapter.ConversationListAdapter
import com.black.im.adapter.IConversationAdapter
import com.black.im.widget.ConversationListLayout

/**
 * ConversationListLayout 的适配器，用户可自定义实现
 */
interface IConversationListLayout {
    /**
     * 设置会话界面背景，非ListView区域
     *
     * @param resId
     */
    fun setBackground(resId: Int)

    /**
     * 设置会话Item点击监听
     *
     * @param listener
     */
    fun setOnItemClickListener(listener: ConversationListLayout.OnItemClickListener?)

    /**
     * 设置会话Item长按监听
     *
     * @param listener
     */
    fun setOnItemLongClickListener(listener: ConversationListLayout.OnItemLongClickListener?)

    /**
     * 不显示小红点未读消息条数开关
     *
     * @param flag 默认false，表示显示
     */
    fun disableItemUnreadDot(flag: Boolean)

    /**
     * 会话Item头像圆角化
     *
     * @param flag
     */
    fun enableItemRoundIcon(flag: Boolean)

    /**
     * 设置会话Item顶部字体大小
     *
     * @param size
     */
    fun setItemTopTextSize(size: Int)

    /**
     * 设置会话Item底部字体大小
     *
     * @param size
     */
    fun setItemBottomTextSize(size: Int)

    /**
     * 设置会话Item日期字体大小
     *
     * @param size
     */
    fun setItemDateTextSize(size: Int)

    /**
     * 获取会话列表ListView
     *
     * @return
     */
    val listLayout: ConversationListLayout?

    /**
     * 获取会话列表Adapter
     *
     * @return
     */
    fun getAdapter(): ConversationListAdapter?

    /**
     * 设置会话Adapter
     *
     * @param adapter
     */
    fun setAdapter(adapter: IConversationAdapter?)
}