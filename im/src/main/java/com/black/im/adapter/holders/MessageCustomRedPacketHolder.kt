package com.black.im.adapter.holders

import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.black.base.model.community.RedPacket
import com.black.im.R
import com.black.im.interfaces.ICustomMessageViewGroup
import com.black.im.model.chat.MessageInfo
import com.black.im.util.TUIKit.appContext
import com.black.im.widget.MessageLayoutUI
import com.google.gson.Gson
import skin.support.content.res.SkinCompatResources

class MessageCustomRedPacketHolder(itemView: View, properties: MessageLayoutUI.Properties?) : MessageContentHolder(itemView, properties), ICustomMessageViewGroup {
    private val gson: Gson = Gson()
    private var redPacketLayout: View? = null
    private var titleView: TextView? = null
    private var statusView: TextView? = null
    override fun getVariableLayout(): Int {
        return R.layout.message_adapter_content_costomer_red_packet
    }

    override fun initVariableViews() {
        redPacketLayout = rootView.findViewById(R.id.red_packet_layout)
        titleView = rootView.findViewById(R.id.title)
        statusView = rootView.findViewById(R.id.status)
    }

    override fun layoutViews(msg: MessageInfo?, position: Int) {
        super.layoutViews(msg, position)
    }

    override fun layoutVariableViews(msg: MessageInfo?, position: Int) {
        msgContentFrame.setOnClickListener { v ->
            if (onItemClickListener != null) {
                onItemClickListener?.onMessageClick(v, position, msg)
            }
        }
        val drawable = SkinCompatResources.getDrawable(appContext, R.drawable.bg_chat_message_left)
        msgContentFrame.background = drawable
        val redPacket = msg?.redPacket
        if (redPacket != null) {
            titleView?.text = redPacket.title
            when (msg.customInt) {
                RedPacket.NEW -> {
                    redPacketLayout?.alpha = 1f
                    statusView?.text = "查看红包"
                }
                RedPacket.IS_OPEN -> {
                    redPacketLayout?.alpha = 0.6f
                    statusView?.text = "已领取红包"
                }
                RedPacket.IS_OVER -> {
                    redPacketLayout?.alpha = 0.6f
                    statusView?.text = "红包已被领完"
                }
                RedPacket.IS_OVER_TIME -> {
                    redPacketLayout?.alpha = 0.6f
                    statusView?.text = "红包已过期"
                }
                else -> {
                    redPacketLayout?.alpha = 1f
                    statusView?.text = "查看红包"
                }
            }
            msgContentFrame.isEnabled = true
        } else {
            msgContentFrame.isEnabled = false
        }
    }

    override fun addMessageItemView(view: View?) {
        (rootView as RelativeLayout).removeAllViews()
        (rootView as RelativeLayout).addView(chatTimeText, 0)
        if (view != null) {
            (rootView as RelativeLayout).addView(view, 1)
        }
    }

    override fun addMessageContentView(view: View?) {
        if (view != null) {
            msgContentFrame.removeAllViews()
            msgContentFrame.addView(view)
        }
    }

}