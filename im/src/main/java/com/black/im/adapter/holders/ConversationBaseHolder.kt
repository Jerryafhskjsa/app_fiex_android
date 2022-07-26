package com.black.im.adapter.holders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.black.im.adapter.ConversationListAdapter
import com.black.im.model.ConversationInfo

abstract class ConversationBaseHolder(protected var rootView: View) : RecyclerView.ViewHolder(rootView) {
    protected var mAdapter: ConversationListAdapter? = null
    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        mAdapter = adapter as ConversationListAdapter?
    }

    abstract fun layoutViews(conversationInfo: ConversationInfo?, position: Int)

}