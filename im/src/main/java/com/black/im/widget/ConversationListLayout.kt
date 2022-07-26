package com.black.im.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.im.adapter.ConversationListAdapter
import com.black.im.adapter.IConversationAdapter
import com.black.im.interfaces.IConversationListLayout
import com.black.im.manager.CustomLinearLayoutManager
import com.black.im.model.ConversationInfo

class ConversationListLayout : RecyclerView, IConversationListLayout {
    private var mAdapter: ConversationListAdapter? = null

    constructor(context: Context?) : super(context!!) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) {
        init()
    }

    fun init() {
        isLayoutFrozen = false
        setItemViewCacheSize(0)
        setHasFixedSize(true)
        isFocusableInTouchMode = false
        val linearLayoutManager = CustomLinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager = linearLayoutManager
    }

    override fun setBackground(resId: Int) {
        setBackgroundColor(resId)
    }

    override fun disableItemUnreadDot(flag: Boolean) {
        mAdapter?.disableItemUnreadDot(flag)
    }

    override fun enableItemRoundIcon(flag: Boolean) {
        mAdapter?.enableItemRoundIcon(flag)
    }

    override fun setItemTopTextSize(size: Int) {
        mAdapter?.setItemTopTextSize(size)
    }

    override fun setItemBottomTextSize(size: Int) {
        mAdapter?.setItemBottomTextSize(size)
    }

    override fun setItemDateTextSize(size: Int) {
        mAdapter?.setItemDateTextSize(size)
    }

    override val listLayout: ConversationListLayout
        get() = this

    override fun getAdapter(): ConversationListAdapter? {
        return mAdapter
    }

    override fun setAdapter(adapter: IConversationAdapter?) {
        super.setAdapter(adapter)
        mAdapter = adapter as ConversationListAdapter?
    }

    override fun setOnItemClickListener(listener: OnItemClickListener?) {
        mAdapter?.setOnItemClickListener(listener)
    }

    override fun setOnItemLongClickListener(listener: OnItemLongClickListener?) {
        mAdapter?.setOnItemLongClickListener(listener)
    }

    interface OnItemClickListener {
        fun onItemClick(view: View?, position: Int, messageInfo: ConversationInfo?)
    }

    interface OnItemLongClickListener {
        fun OnItemLongClick(view: View?, position: Int, messageInfo: ConversationInfo?)
    }
}