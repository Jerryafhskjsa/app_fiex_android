package com.black.im.adapter.holders

import android.graphics.Color
import android.net.Uri
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.black.im.R
import com.black.im.imageEngine.impl.GlideEngine
import com.black.im.model.ConversationInfo

/**
 * 自定义会话Holder
 */
class ConversationCustomHolder(itemView: View?) : ConversationBaseHolder(itemView!!) {
    protected var leftItemLayout: LinearLayout
    protected var titleText: TextView
    protected var messageText: TextView
    protected var timelineText: TextView
    protected var unreadText: TextView
    protected var imageView: ImageView
    override fun layoutViews(conversationInfo: ConversationInfo?, position: Int) {
        if (true == conversationInfo?.isTop) {
            leftItemLayout.setBackgroundColor(rootView.resources.getColor(R.color.top_conversation_color))
        } else {
            leftItemLayout.setBackgroundColor(Color.WHITE)
        }
        if (conversationInfo?.iconUrl != null) {
            GlideEngine.loadImage(imageView, Uri.parse(conversationInfo.iconUrl))
        }
        titleText.text = conversationInfo?.title
        messageText.text = ""
        timelineText.text = ""
        if (conversationInfo?.unRead ?: 0 > 0) {
            unreadText.visibility = View.VISIBLE
            unreadText.text = "" + conversationInfo?.unRead
        } else {
            unreadText.visibility = View.GONE
        }
        if (mAdapter?.mDateTextSize != 0) {
            timelineText.textSize = mAdapter?.mDateTextSize?.toFloat() ?: 0.toFloat()
        }
        if (mAdapter?.mBottomTextSize != 0) {
            messageText.textSize = mAdapter?.mBottomTextSize?.toFloat() ?: 0.toFloat()
        }
        if (mAdapter?.mTopTextSize != 0) {
            titleText.textSize = mAdapter?.mTopTextSize?.toFloat() ?: 0.toFloat()
        }
    }

    init {
        leftItemLayout = rootView.findViewById(R.id.item_left)
        imageView = rootView.findViewById(R.id.conversation_icon)
        titleText = rootView.findViewById(R.id.conversation_title)
        messageText = rootView.findViewById(R.id.conversation_last_msg)
        timelineText = rootView.findViewById(R.id.conversation_time)
        unreadText = rootView.findViewById(R.id.conversation_unread)
    }
}