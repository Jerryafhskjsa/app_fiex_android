package com.black.im.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.black.im.R
import com.black.im.adapter.holders.ConversationBaseHolder
import com.black.im.adapter.holders.ConversationCommonHolder
import com.black.im.adapter.holders.ConversationCustomHolder
import com.black.im.interfaces.IConversationProvider
import com.black.im.model.ConversationInfo
import com.black.im.provider.ConversationProvider
import com.black.im.util.TUIKit.appContext
import com.black.im.widget.ConversationListLayout
import java.util.*

class ConversationListAdapter : IConversationAdapter() {
    var mIsShowUnreadDot = true
    var mIsShowItemRoundIcon = false
    var mTopTextSize = 0
    var mBottomTextSize = 0
    var mDateTextSize = 0
    private var mDataSource: MutableList<ConversationInfo?>? = ArrayList()
    private var mOnItemClickListener: ConversationListLayout.OnItemClickListener? = null
    private var mOnItemLongClickListener: ConversationListLayout.OnItemLongClickListener? = null
    fun setOnItemClickListener(listener: ConversationListLayout.OnItemClickListener?) {
        mOnItemClickListener = listener
    }

    fun setOnItemLongClickListener(listener: ConversationListLayout.OnItemLongClickListener?) {
        mOnItemLongClickListener = listener
    }

    override fun setDataProvider(provider: IConversationProvider) {
        mDataSource = provider.getDataSource()
        (provider as? ConversationProvider)?.attachAdapter(this)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(appContext)
        val holder: RecyclerView.ViewHolder
        // 创建不同的 ViewHolder
        val view: View
        // 根据ViewType来创建条目
        if (viewType == ConversationInfo.TYPE_CUSTOM) {
            view = inflater.inflate(R.layout.conversation_custom_adapter, parent, false)
            holder = ConversationCustomHolder(view)
        } else {
            view = inflater.inflate(R.layout.conversation_adapter, parent, false)
            holder = ConversationCommonHolder(view)
        }
        if (holder != null) {
            (holder as ConversationBaseHolder).setAdapter(this)
        }
        return holder
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val conversationInfo = getItem(position)
        val baseHolder = holder as ConversationBaseHolder
        when (getItemViewType(position)) {
            ConversationInfo.TYPE_CUSTOM -> {
            }
            else -> {
                //设置点击和长按事件
                if (mOnItemClickListener != null) {
                    holder.itemView.setOnClickListener { view -> mOnItemClickListener!!.onItemClick(view, position, conversationInfo) }
                }
                if (mOnItemLongClickListener != null) {
                    holder.itemView.setOnLongClickListener { view ->
                        mOnItemLongClickListener!!.OnItemLongClick(view, position, conversationInfo)
                        true
                    }
                }
            }
        }
        baseHolder.layoutViews(conversationInfo, position)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is ConversationCommonHolder) {
            holder.conversationIconView.background = null
        }
    }

    override fun getItem(position: Int): ConversationInfo? {
        return if (mDataSource?.size ?: 0 == 0) null else mDataSource!![position]
    }

    override fun getItemCount(): Int {
        return mDataSource?.size ?: 0
    }

    override fun getItemViewType(position: Int): Int {
        if (mDataSource != null) {
            val conversation = mDataSource!![position]
            return conversation?.type ?: 1
        }
        return 1
    }

    fun addItem(position: Int, info: ConversationInfo) {
        mDataSource?.add(position, info)
        notifyItemInserted(position)
        notifyDataSetChanged()
    }

    fun removeItem(position: Int) {
        mDataSource?.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }

    fun setItemTopTextSize(size: Int) {
        mTopTextSize = size
    }

    fun setItemBottomTextSize(size: Int) {
        mBottomTextSize = size
    }

    fun setItemDateTextSize(size: Int) {
        mDateTextSize = size
    }

    fun enableItemRoundIcon(flag: Boolean) {
        mIsShowItemRoundIcon = flag
    }

    fun disableItemUnreadDot(flag: Boolean) {
        mIsShowUnreadDot = !flag
    }
}