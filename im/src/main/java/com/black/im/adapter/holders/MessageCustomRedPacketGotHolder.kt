package com.black.im.adapter.holders

import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.black.base.util.FryingUtil
import com.black.im.R
import com.black.im.model.chat.MessageInfo
import com.black.im.util.TUIKit.appContext
import com.black.im.widget.MessageLayoutUI
import com.black.util.CommonUtil
import com.google.gson.Gson
import com.tencent.imsdk.TIMManager

//红包领取显示
class MessageCustomRedPacketGotHolder(itemView: View, properties: MessageLayoutUI.Properties?) : MessageEmptyHolder(itemView, properties) {
    private val gson: Gson = Gson()
    private var mChatTipsTv: TextView? = null
    override fun getVariableLayout(): Int {
        return R.layout.message_adapter_content_costomer_red_packet_got
    }

    override fun initVariableViews() {
        mChatTipsTv = rootView.findViewById(R.id.chat_tips_tv)
    }

    override fun layoutViews(msg: MessageInfo?, position: Int) {
        val redPacketGot = msg?.redPacketGot
        if (properties?.getTipsMessageBubble() != null) {
            mChatTipsTv?.background = properties?.getTipsMessageBubble()
        }
        if (properties?.getTipsMessageFontColor() != 0) {
            mChatTipsTv?.setTextColor(properties?.getTipsMessageFontColor()!!)
        }
        if (properties?.getTipsMessageFontSize() != 0) {
            mChatTipsTv?.textSize = properties?.getTipsMessageFontSize()!!.toFloat()
        }
        if (redPacketGot != null) {
            val selfId = TIMManager.getInstance().loginUser
            val openerId = redPacketGot.openerId
            val ownerId = redPacketGot.ownerId
            var firstUserName: String? = null
            var secondUserName: String? = null
            if (TextUtils.equals(openerId, selfId) && TextUtils.equals(ownerId, selfId)) { //我领取了自己发送的红包
                firstUserName = "我"
                secondUserName = "自己"
            } else if (TextUtils.equals(openerId, selfId)) { //我领取了其他人发的红包
                firstUserName = "我"
                secondUserName = getUserName(msg, ownerId)
            } else if (TextUtils.equals(ownerId, selfId)) { //其他人领取了我的红包
                firstUserName = getUserName(msg, openerId)
                secondUserName = "我"
            }
            val text = String.format("%s领取了%s发的%s",
                    CommonUtil.getTextMaxLength(firstUserName, 6),
                    CommonUtil.getTextMaxLength(secondUserName, 6),
                    FryingUtil.translateToHtmlTextAddColor(appContext, "红包", R.color.C4))
            FryingUtil.setHtmlText(mChatTipsTv, text)
            //            mChatTipsTv.setText("");
//                mChatTipsTv.setText(redPacketGot.text);
        } else {
            mChatTipsTv?.text = ""
        }
    }

}