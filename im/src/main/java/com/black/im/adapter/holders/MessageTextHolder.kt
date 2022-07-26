package com.black.im.adapter.holders

import android.view.View
import android.widget.TextView
import com.black.im.R
import com.black.im.manager.FaceManager.handlerEmojiText
import com.black.im.model.chat.MessageInfo
import com.black.im.util.TUIKit.appContext
import com.black.im.widget.MessageLayoutUI
import skin.support.content.res.SkinCompatResources

class MessageTextHolder(itemView: View, properties: MessageLayoutUI.Properties?) : MessageContentHolder(itemView, properties) {
    private var msgBodyText: TextView? = null
    override fun getVariableLayout(): Int {
        return R.layout.message_adapter_content_text
    }

    override fun initVariableViews() {
        msgBodyText = rootView.findViewById(R.id.msg_body_tv)
    }

    override fun layoutVariableViews(msg: MessageInfo?, position: Int) {
        msgBodyText?.visibility = View.VISIBLE
        handlerEmojiText(msgBodyText, msg?.extra.toString())
        if (properties?.getChatContextFontSize() != 0) {
            msgBodyText?.textSize = properties?.getChatContextFontSize()?.toFloat() ?: 0.toFloat()
        }
        if (true == msg?.isSelf) {
            msgBodyText?.setTextColor(SkinCompatResources.getColor(appContext, R.color.T4))
            properties?.getRightChatContentFontColor()?.let {
                if (it != 0) {
                    msgBodyText?.setTextColor(it)
                }
            }
        } else {
            msgBodyText?.setTextColor(SkinCompatResources.getColor(appContext, R.color.T1))
            properties?.getRightChatContentFontColor()?.let {
                if (it != 0) {
                    msgBodyText?.setTextColor(it)
                }
            }
        }
    }
}