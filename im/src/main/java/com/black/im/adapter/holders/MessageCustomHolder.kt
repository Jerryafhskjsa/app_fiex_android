package com.black.im.adapter.holders

import android.text.Html
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.black.im.R
import com.black.im.interfaces.ICustomMessageViewGroup
import com.black.im.model.chat.MessageInfo
import com.black.im.util.TUIKit.appContext
import com.black.im.widget.MessageLayoutUI
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources

class MessageCustomHolder(itemView: View?, properties: MessageLayoutUI.Properties?) : MessageContentHolder(itemView!!, properties), ICustomMessageViewGroup {
    private var msgBodyText: TextView? = null
    override fun getVariableLayout(): Int {
        return R.layout.message_adapter_content_text
    }

    override fun initVariableViews() {
        msgBodyText = rootView.findViewById(R.id.msg_body_tv)
    }

    override fun layoutViews(msg: MessageInfo?, position: Int) {
        super.layoutViews(msg, position)
    }

    override fun layoutVariableViews(msg: MessageInfo?, position: Int) {
        msgBodyText?.visibility = View.VISIBLE
        val c1Code = CommonUtil.toHexEncodingColor(SkinCompatResources.getColor(appContext, R.color.C1))
        msgBodyText?.text = Html.fromHtml("<font color=\"#$c1Code\">[不支持的自定义消息]</font>")
        if (properties?.getChatContextFontSize() != 0) {
            msgBodyText?.textSize = properties?.getChatContextFontSize()!!.toFloat()
        }
        if (true == msg?.isSelf) {
            msgBodyText?.setTextColor(SkinCompatResources.getColor(appContext, R.color.T4))
            if (properties?.getRightChatContentFontColor() != 0) {
                msgBodyText?.setTextColor(properties?.getRightChatContentFontColor()!!)
            }
        } else {
            msgBodyText?.setTextColor(SkinCompatResources.getColor(appContext, R.color.T1))
            if (properties?.getLeftChatContentFontColor() != 0) {
                msgBodyText?.setTextColor(properties?.getLeftChatContentFontColor()!!)
            }
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