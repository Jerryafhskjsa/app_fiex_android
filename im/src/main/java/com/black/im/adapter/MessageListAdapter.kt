package com.black.im.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.black.im.adapter.holders.MessageBaseHolder
import com.black.im.adapter.holders.MessageContentHolder
import com.black.im.adapter.holders.MessageCustomHolder
import com.black.im.adapter.holders.MessageHeaderHolder
import com.black.im.interfaces.IChatProvider
import com.black.im.interfaces.IOnCustomMessageDrawListener
import com.black.im.model.chat.MessageInfo
import com.black.im.util.BackgroundTasks
import com.black.im.util.ChatLayoutHelper.isVisibleBottom
import com.black.im.util.TUIKitLog
import com.black.im.widget.MessageLayout
import java.util.*

class MessageListAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var mLoading = true
    private lateinit var mRecycleView: MessageLayout
    private var mDataSource: MutableList<MessageInfo?>? = ArrayList()
    var onItemClickListener: MessageLayout.OnItemClickListener? = null
    private var mOnCustomMessageDrawListener: IOnCustomMessageDrawListener? = null
    fun setOnCustomMessageDrawListener(listener: IOnCustomMessageDrawListener?) {
        mOnCustomMessageDrawListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MessageBaseHolder.Factory.getInstance(mRecycleView, parent, this, viewType)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = getItem(position)
        val baseHolder = holder as MessageBaseHolder
        baseHolder.setOnItemClickListener(onItemClickListener)
        when (getItemViewType(position)) {
            MSG_TYPE_HEADER_VIEW -> (baseHolder as MessageHeaderHolder).setLoadingStatus(mLoading)
            MessageInfo.MSG_TYPE_CUSTOM -> {
                val customHolder = holder as MessageCustomHolder
                if (mOnCustomMessageDrawListener != null) {
                    mOnCustomMessageDrawListener!!.onDraw(customHolder, msg)
                }
            }
            MessageInfo.MSG_TYPE_TEXT, MessageInfo.MSG_TYPE_IMAGE, MessageInfo.MSG_TYPE_VIDEO, MessageInfo.MSG_TYPE_CUSTOM_FACE, MessageInfo.MSG_TYPE_AUDIO, MessageInfo.MSG_TYPE_FILE -> {
            }
            else -> if (msg!!.getMsgType() < MessageInfo.MSG_TYPE_TIPS) {
                TUIKitLog.e(TAG, "Never be here!")
            }
        }
        baseHolder.layoutViews(msg, position)
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecycleView = recyclerView as MessageLayout
    }

    fun showLoading() {
        if (mLoading) {
            return
        }
        mLoading = true
        notifyItemChanged(0)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is MessageContentHolder) {
            holder.msgContentFrame.background = null
        }
    }

    fun notifyDataSourceChanged(type: Int, value: Int) {
        BackgroundTasks.instance?.postDelayed(Runnable {
            mLoading = false
            if (type == MessageLayout.DATA_CHANGE_TYPE_REFRESH) {
                notifyDataSetChanged()
                mRecycleView!!.scrollToEnd()
            } else if (type == MessageLayout.DATA_CHANGE_TYPE_ADD_BACK) {
                notifyItemRangeInserted(mDataSource!!.size + 1, value)
                if (isVisibleBottom(mRecycleView!!)) {
                    mRecycleView!!.scrollToEnd()
                }
            } else if (type == MessageLayout.DATA_CHANGE_TYPE_UPDATE) {
                notifyItemChanged(value + 1)
            } else if (type == MessageLayout.DATA_CHANGE_TYPE_LOAD || type == MessageLayout.DATA_CHANGE_TYPE_ADD_FRONT) { //加载条目为数0，只更新动画
                if (value == 0) {
                    notifyItemChanged(0)
                } else { //加载过程中有可能之前第一条与新加载的最后一条的时间间隔不超过5分钟，时间条目需去掉，所以这里的刷新要多一个条目
                    if (itemCount > value) {
                        notifyItemRangeInserted(0, value)
                    } else {
                        notifyItemRangeInserted(0, value)
                    }
                }
            } else if (type == MessageLayout.DATA_CHANGE_TYPE_DELETE) {
                notifyItemRemoved(value + 1)
                notifyDataSetChanged()
                mRecycleView!!.scrollToEnd()
            }
        }, 100)
    }

    override fun getItemCount(): Int {
        return mDataSource!!.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0) {
            return MSG_TYPE_HEADER_VIEW
        }
        val msg = getItem(position)
        return msg!!.getMsgType()
    }

    fun setDataSource(provider: IChatProvider?) {
        if (provider == null) {
            mDataSource?.clear()
        } else {
            mDataSource = provider.getDataSourceShowing()
            provider.setAdapter(this)
        }
        notifyDataSourceChanged(MessageLayout.DATA_CHANGE_TYPE_REFRESH, itemCount)
    }

    fun setDataSource(provider: IChatProvider?, messageInfos: MutableList<MessageInfo?>?) {
        var messageInfos1 = messageInfos
        messageInfos1 = messageInfos1 ?: provider?.getDataSourceShowing()
        if (messageInfos1 == null) {
            mDataSource?.clear()
        } else {
            mDataSource = messageInfos1
            provider?.setAdapter(this)
        }
        notifyDataSourceChanged(MessageLayout.DATA_CHANGE_TYPE_REFRESH, itemCount)
    }

    fun getItem(position: Int): MessageInfo? {
        return if (position == 0 || mDataSource!!.size == 0) {
            null
        } else mDataSource!![position - 1]
    }

    companion object {
        const val MSG_TYPE_HEADER_VIEW = -99
        private val TAG = MessageListAdapter::class.java.simpleName
    }
}