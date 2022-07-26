package com.black.im.adapter.holders

import android.view.View
import com.black.im.model.chat.MessageInfo
import com.black.im.widget.MessageLayoutUI

//未知的自定义消息
class MessageCustomUnknownHolder(itemView: View?, properties: MessageLayoutUI.Properties?) : MessageEmptyHolder(itemView!!, properties) {
    override fun getVariableLayout(): Int {
        return 0
    }

    override fun initVariableViews() {}
    override fun layoutViews(msg: MessageInfo?, position: Int) {}
}