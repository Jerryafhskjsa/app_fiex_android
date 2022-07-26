package com.black.im.adapter.holders

import android.graphics.Color
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.black.im.R
import com.black.im.model.ConversationInfo
import com.black.im.model.chat.MessageInfo
import com.black.im.util.DateTimeUtil.getTimeFormatText
import com.black.im.util.TUIKit.appContext
import com.black.im.widget.ConversationIconView
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources
import java.util.*

class ConversationCommonHolder(itemView: View?) : ConversationBaseHolder(itemView!!) {
    var conversationIconView: ConversationIconView
    protected var leftItemLayout: LinearLayout
    protected var titleText: TextView
    protected var messageText: TextView
    protected var timelineText: TextView
    protected var unreadText: TextView

    init {
        leftItemLayout = rootView.findViewById(R.id.item_left)
        conversationIconView = rootView.findViewById(R.id.conversation_icon)
        titleText = rootView.findViewById(R.id.conversation_title)
        messageText = rootView.findViewById(R.id.conversation_last_msg)
        timelineText = rootView.findViewById(R.id.conversation_time)
        unreadText = rootView.findViewById(R.id.conversation_unread)
    }

    override fun layoutViews(conversationInfo: ConversationInfo?, position: Int) {
        val lastMsg = conversationInfo?.lastMessage
        if (lastMsg != null && lastMsg.status == MessageInfo.MSG_STATUS_REVOKE) {
            if (lastMsg.isSelf) {
                lastMsg.extra = "您撤回了一条消息"
            } else if (lastMsg.isGroup) {
                val c1Code = CommonUtil.toHexEncodingColor(SkinCompatResources.getColor(appContext, R.color.C1))
                val message = ("\"<font color=\"#" + c1Code + "\">"
                        + (if (!TextUtils.isEmpty(lastMsg.groupNameCard)) lastMsg.groupNameCard else lastMsg.fromUser)
                        + "</font>\"")
                lastMsg.extra = message + "撤回了一条消息"
            } else {
                lastMsg.extra = "对方撤回了一条消息"
            }
        }
        if (true == conversationInfo?.isTop) {
            leftItemLayout.setBackgroundColor(rootView.resources.getColor(R.color.top_conversation_color))
        } else {
            leftItemLayout.setBackgroundColor(Color.WHITE)
        }
        conversationIconView.setIconUrls(null) // 如果自己要设置url，这行代码需要删除
        if (true == conversationInfo?.isGroup) {
            if (true == mAdapter?.mIsShowItemRoundIcon) {
                conversationIconView.setBitmapResId(R.drawable.conversation_group)
            } else {
                conversationIconView.setDefaultImageResId(R.drawable.conversation_group)
            }
        } else {
            if (true == mAdapter?.mIsShowItemRoundIcon) {
                conversationIconView.setBitmapResId(R.drawable.conversation_c2c)
            } else {
                conversationIconView.setDefaultImageResId(R.drawable.conversation_c2c)
            }
        }
        titleText.text = conversationInfo?.title
        messageText.text = ""
        timelineText.text = ""
        if (lastMsg != null) {
            if (lastMsg.extra != null) {
                messageText.text = Html.fromHtml(lastMsg.extra.toString())
                messageText.setTextColor(rootView.resources.getColor(R.color.list_bottom_text_bg))
            }
            timelineText.text = getTimeFormatText(Date(lastMsg.msgTime * 1000))
        }
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
        //        if (mIsShowUnreadDot) {
//            holder.unreadText.setVisibility(View.GONE);
//        }
        if (!TextUtils.isEmpty(conversationInfo?.iconUrl)) {
            val urlList: MutableList<String> = ArrayList()
            urlList.add(conversationInfo?.iconUrl!!)
            conversationIconView.setIconUrls(urlList)
            urlList.clear()
        }
        //// 由子类设置指定消息类型的views
        layoutVariableViews(conversationInfo, position)
    }

    fun layoutVariableViews(conversationInfo: ConversationInfo?, position: Int) {}
}