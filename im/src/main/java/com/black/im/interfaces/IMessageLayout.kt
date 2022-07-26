package com.black.im.interfaces

import androidx.recyclerview.widget.RecyclerView
import com.black.im.action.PopMenuAction
import com.black.im.adapter.MessageListAdapter
import com.black.im.widget.MessageLayout

/**
 * 消息区域 [MessageLayout] 继承自 [RecyclerView]，提供了消息的展示功能。<br></br>
 * 本类提供了大量的方法以供定制化需求，包括外观设置、事件点击，以及自定义消息的展示等。
 */
interface IMessageLayout : IMessageProperties {
    /**
     * 设置消息列表的适配器 [MessageListAdapter]
     *
     * @param adapter
     */
    fun setAdapter(adapter: MessageListAdapter?)

    /**
     * 获得消息列表的点击事件
     *
     * @return
     */
    fun getOnItemClickListener(): MessageLayout.OnItemClickListener?

    /**
     * 设置消息列表的事件监听器 [MessageLayout.OnItemClickListener]
     *
     * @param listener
     */
    fun setOnItemClickListener(listener: MessageLayout.OnItemClickListener?)

    /**
     * 获取 PopMenu 的 Action 列表
     *
     * @return
     */
    fun getPopActions(): List<PopMenuAction?>?

    /**
     * 给 PopMenu 加入一条自定义 action
     *
     * @param action 菜单选项 [PopMenuAction], 可以自定义图片、文字以及点击事件
     */
    fun addPopAction(action: PopMenuAction?)

    /**
     * 设置自定义的消息渲染时的回调，当TUIKit内部在刷新自定义消息时会调用这个回调
     *
     * @param listener [IOnCustomMessageDrawListener]
     */
    fun setOnCustomMessageDrawListener(listener: IOnCustomMessageDrawListener?)
}