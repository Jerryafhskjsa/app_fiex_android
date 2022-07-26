package com.black.im.adapter.holders

import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import com.black.im.R
import com.black.im.model.chat.MessageInfo
import com.black.im.util.IMHelper.getUserUID
import com.black.im.util.TUIKit.appContext
import com.black.im.widget.MessageLayoutUI
import com.black.util.CommonUtil
import com.tencent.imsdk.TIMFriendshipManager
import skin.support.content.res.SkinCompatResources

class MessageTipsHolder(itemView: View?, properties: MessageLayoutUI.Properties?) : MessageEmptyHolder(itemView!!, properties) {
    private var mChatTipsTv: TextView? = null
    override fun getVariableLayout(): Int {
        return R.layout.message_adapter_content_tips
    }

    override fun initVariableViews() {
        mChatTipsTv = rootView.findViewById(R.id.chat_tips_tv)
    }

    override fun layoutViews(msg: MessageInfo?, position: Int) {
        super.layoutViews(msg, position)
        if (properties?.getTipsMessageBubble() != null) {
            mChatTipsTv?.background = properties?.getTipsMessageBubble()
        }
        if (properties?.getTipsMessageFontColor() != 0) {
            mChatTipsTv?.setTextColor(properties?.getTipsMessageFontColor()!!)
        }
        if (properties?.getTipsMessageFontSize() != 0) {
            mChatTipsTv?.textSize = properties?.getTipsMessageFontSize()!!.toFloat()
        }
        if (msg?.status == MessageInfo.MSG_STATUS_REVOKE) {
            when {
                msg.isSelf -> {
                    msg.extra = "您撤回了一条消息"
                }
                msg.isGroup -> {
                    val c1Code = CommonUtil.toHexEncodingColor(SkinCompatResources.getColor(appContext, R.color.C1))
                    var userName: String?
                    userName = if (msg.isSelf) {
                        if (TextUtils.isEmpty(properties?.getRightNameHard())) properties?.getRightNameDefault() else properties?.getRightNameHard()
                    } else {
                        if (TextUtils.isEmpty(properties?.getLeftNameHard())) properties?.getLeftNameDefault() else properties?.getLeftNameHard()
                    }
                    val profile = TIMFriendshipManager.getInstance().queryUserProfile(msg.fromUser)
                    if (profile == null) {
                        if (TextUtils.isEmpty(userName)) {
                            userName = msg.fromUser
                        }
                    } else {
                        userName = if (TextUtils.isEmpty(msg.groupNameCard)) {
                            if (!TextUtils.isEmpty(profile.nickName)) profile.nickName else if (!TextUtils.isEmpty(userName)) userName else msg.fromUser
                        } else {
                            msg.groupNameCard
                        }
                    }
                    val message = ("\"<font color=\"#" + c1Code + "\">"
                            + String.format("%s(UID:%s)", userName, getUserUID(msg.fromUser!!))
                            + "</font>\"")
                    msg.extra = message + "撤回了一条消息"
                }
                else -> {
                    msg.extra = "对方撤回了一条消息"
                }
            }
        }
        if (msg?.status == MessageInfo.MSG_STATUS_REVOKE
                || (msg?.getMsgType() ?: 0 >= MessageInfo.MSG_TYPE_GROUP_CREATE
                        && msg?.getMsgType() ?: 0 <= MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE)) {
            if (msg?.extra != null) {
                mChatTipsTv?.text = Html.fromHtml(msg.extra.toString())
            }
        }
    }
}