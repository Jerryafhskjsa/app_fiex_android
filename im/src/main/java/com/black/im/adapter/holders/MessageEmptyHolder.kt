package com.black.im.adapter.holders

import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.black.im.R
import com.black.im.model.chat.MessageInfo
import com.black.im.util.DateTimeUtil.getTimeFormatText
import com.black.im.widget.MessageLayoutUI
import java.util.*

abstract class MessageEmptyHolder(itemView: View, properties: MessageLayoutUI.Properties?) : MessageBaseHolder(itemView, properties!!) {
    var chatTimeLayout: View
    var chatTimeText: TextView
    var msgContentFrame: FrameLayout

    init {
        rootView = itemView
        chatTimeLayout = itemView.findViewById(R.id.chat_time_layout)
        chatTimeText = itemView.findViewById(R.id.chat_time_tv)
        msgContentFrame = itemView.findViewById(R.id.msg_content_fl)
        initVariableLayout()
    }

    private fun initVariableLayout() {
        if (getVariableLayout() != 0) {
            setVariableLayout(getVariableLayout())
        }
    }

    abstract fun getVariableLayout(): Int

    private fun setVariableLayout(resId: Int) {
        if (msgContentFrame.childCount == 0) {
            View.inflate(rootView.context, resId, msgContentFrame)
        }
        initVariableViews()
    }

    abstract fun initVariableViews()
    override fun layoutViews(msg: MessageInfo?, position: Int) {
        //// 时间线设置
        if (properties?.getChatTimeBubble() != null) {
            chatTimeText.background = properties?.getChatTimeBubble()
        }
        if (properties?.getChatTimeFontColor() != 0) {
            chatTimeText.setTextColor(properties?.getChatTimeFontColor()!!)
        }
        if (properties?.getChatTimeFontSize() != 0) {
            chatTimeText.textSize = properties?.getChatTimeFontSize()!!.toFloat()
        }
        if (position > 1) {
            val last = mAdapter?.getItem(position - 1)
            if (last != null) {
                if (msg?.msgTime ?: 0 - last.msgTime >= 5 * 60) {
                    chatTimeLayout.visibility = View.VISIBLE
                    chatTimeText.visibility = View.VISIBLE
                    chatTimeText.text = getTimeFormatText(Date((msg?.msgTime ?: 0) * 1000))
                } else {
                    chatTimeLayout.visibility = View.GONE
                    chatTimeText.visibility = View.GONE
                }
            }
        } else {
            chatTimeLayout.visibility = View.VISIBLE
            chatTimeText.visibility = View.VISIBLE
            chatTimeText.text = getTimeFormatText(Date((msg?.msgTime ?: 0) * 1000))
        }
    }
}