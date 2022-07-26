package com.black.im.adapter

import androidx.recyclerview.widget.RecyclerView
import com.black.im.interfaces.IConversationProvider
import com.black.im.model.ConversationInfo

/**
 * ConversationLayout 的适配器，用户可自定义实现
 */
abstract class IConversationAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    /**
     * 设置适配器的数据源，该接口一般由ConversationContainer自动调用
     *
     * @param provider
     */
    abstract fun setDataProvider(provider: IConversationProvider)

    /**
     * 获取适配器的条目数据，返回的是ConversationInfo对象或其子对象
     *
     * @param position
     * @return ConversationInfo
     */
    abstract fun getItem(position: Int): ConversationInfo?
}