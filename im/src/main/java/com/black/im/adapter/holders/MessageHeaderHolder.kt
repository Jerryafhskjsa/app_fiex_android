package com.black.im.adapter.holders

import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.black.im.model.chat.MessageInfo
import com.black.im.widget.MessageLayoutUI

class MessageHeaderHolder(itemView: View, properties: MessageLayoutUI.Properties?) : MessageBaseHolder(itemView, properties) {
    private var mLoading = false
    fun setLoadingStatus(loading: Boolean) {
        mLoading = loading
    }

    override fun layoutViews(msg: MessageInfo?, position: Int) {
        val param = rootView.layoutParams as RecyclerView.LayoutParams
        if (mLoading) {
            param.height = LinearLayout.LayoutParams.WRAP_CONTENT
            param.width = LinearLayout.LayoutParams.MATCH_PARENT
            rootView.visibility = View.VISIBLE
        } else {
            param.height = 0
            param.width = 0
            rootView.visibility = View.GONE
        }
        rootView.layoutParams = param
    }
}