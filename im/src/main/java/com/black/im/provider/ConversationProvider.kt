package com.black.im.provider

import com.black.im.adapter.ConversationListAdapter
import com.black.im.adapter.IConversationAdapter
import com.black.im.interfaces.IConversationProvider
import com.black.im.model.ConversationInfo
import java.util.*

class ConversationProvider : IConversationProvider {
    private val mDataSource: MutableList<ConversationInfo?> = ArrayList()
    private var mAdapter: ConversationListAdapter? = null
    override fun getDataSource(): MutableList<ConversationInfo?>? {
        return mDataSource
    }

    /**
     * 设置会话数据源
     *
     * @param dataSource
     */
    fun setDataSource(dataSource: List<ConversationInfo?>?) {
        dataSource?.let {
            mDataSource.clear()
            mDataSource.addAll(dataSource)
            updateAdapter()
        }
    }

    /**
     * 批量添加会话数据
     *
     * @param conversations 会话数据集合
     * @return
     */
    override fun addConversations(conversations: MutableList<ConversationInfo?>?): Boolean {
        var flag = false
        conversations?.let {
            if (conversations.size == 1) {
                val conversation = conversations[0]
                for (i in mDataSource.indices) {
                    if (mDataSource[i]?.id == conversation?.id) return true
                }
            }
            flag = mDataSource.addAll(conversations)
        }
        if (flag) {
            updateAdapter()
        }
        return flag
    }

    /**
     * 批量删除会话数据
     *
     * @param conversations 会话数据集合
     * @return
     */
    override fun deleteConversations(conversations: MutableList<ConversationInfo?>?): Boolean {
        val removeIndexs: MutableList<Int> = ArrayList()
        conversations?.let {
            for (i in mDataSource.indices) {
                for (j in conversations.indices) {
                    if (mDataSource[i]?.id == conversations[j]?.id) {
                        removeIndexs.add(i)
                        conversations.removeAt(j)
                        break
                    }
                }
            }
            if (removeIndexs.size > 0) {
                for (i in removeIndexs.indices) {
                    mDataSource.removeAt(removeIndexs[i])
                }
                updateAdapter()
                return true
            }
        }
        return false
    }

    /**
     * 删除单个会话数据
     *
     * @param index 会话在数据源集合的索引
     * @return
     */
    fun deleteConversation(index: Int) {
        if (mDataSource.removeAt(index) != null) {
            updateAdapter()
        }
    }

    /**
     * 删除单个会话数据
     *
     * @param id 会话ID
     * @return
     */
    fun deleteConversation(id: String) {
        for (i in mDataSource.indices) {
            if (mDataSource[i]!!.id == id) {
                if (mDataSource.removeAt(i) != null) {
                    updateAdapter()
                }
                return
            }
        }
    }

    /**
     * 批量更新会话
     *
     * @param conversations 会话数据集合
     * @return
     */
    override fun updateConversations(conversations: MutableList<ConversationInfo?>?): Boolean {
        var flag = false
        conversations?.let {
            for (i in mDataSource.indices) {
                for (j in conversations.indices) {
                    val update = conversations[j]
                    if (mDataSource[i]?.id == update?.id) {
                        mDataSource.removeAt(i)
                        mDataSource.add(i, update)
                        conversations.removeAt(j)
                        flag = true
                        break
                    }
                }
            }
        }
        return if (flag) {
            updateAdapter()
            true
        } else {
            false
        }
    }

    /**
     * 清空会话
     */
    fun clear() {
        mDataSource.clear()
        updateAdapter()
        mAdapter = null
    }

    /**
     * 会话会话列界面，在数据源更新的地方调用
     */
    fun updateAdapter() {
        if (mAdapter != null) {
            mAdapter?.notifyDataSetChanged()
        }
    }

    /**
     * 会话列表适配器绑定数据源是的回调
     *
     * @param adapter 会话UI显示适配器
     */
    override fun attachAdapter(adapter: IConversationAdapter?) {
        mAdapter = adapter as ConversationListAdapter
    }
}