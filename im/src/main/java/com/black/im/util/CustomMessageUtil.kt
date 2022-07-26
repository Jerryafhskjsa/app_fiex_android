package com.black.im.util

import android.text.TextUtils
import com.black.base.model.community.RedPacket
import com.black.base.model.community.RedPacketGot
import com.black.base.util.FryingUtil
import com.black.im.R
import com.black.im.model.chat.CustomMessageData
import com.black.im.model.chat.MessageInfo
import com.black.im.widget.MessageLayoutUI
import com.black.util.CommonUtil
import com.google.gson.Gson
import com.tencent.imsdk.*

object CustomMessageUtil {
    private var gson: Gson? = null
    private fun getGson(): Gson? {
        if (gson == null) {
            gson = Gson()
        }
        return gson
    }

    var properties: MessageLayoutUI.Properties = MessageLayoutUI.Properties.getInstance()
    fun init(properties: MessageLayoutUI.Properties) {
        CustomMessageUtil.properties = properties
    }

    fun getCustomMessageData(ele: TIMElem?): CustomMessageData? {
        val type = if (ele == null) TIMElemType.Invalid else ele.type
        if (type == TIMElemType.Custom) {
            val customElem = ele as TIMCustomElem?
            try {
                val data = String(customElem!!.data)
                return getGson()!!.fromJson(data, CustomMessageData::class.java)
            } catch (ignored: Exception) {
            }
        }
        return null
    }

    fun getSubType(messageInfo: MessageInfo): Int {
        return if (messageInfo.getMsgType() == MessageInfo.MSG_TYPE_CUSTOM) {
            val customMessageData = messageInfo.customMessageData
            if (customMessageData != null) {
                if (TextUtils.equals(customMessageData.subType, CustomMessageData.TYPE_RED_PACKET)) {
                    var redPacket: RedPacket? = null
                    try {
                        redPacket = if (customMessageData.data == null) null else gson!!.fromJson(customMessageData.data, RedPacket::class.java)
                    } catch (ignored: Exception) {
                    }
                    messageInfo.redPacket = redPacket
                    return MessageInfo.MSG_TYPE_CUSTOM_RED_PACKET
                } else if (TextUtils.equals(customMessageData.subType, CustomMessageData.TYPE_RED_PACKET_GOT)) { //判断红包是否跟自己有关系，没有不用显示该消息
                    var redPacketGot: RedPacketGot? = null
                    try {
                        redPacketGot = if (customMessageData.data == null) null else gson!!.fromJson(customMessageData.data, RedPacketGot::class.java)
                    } catch (ignored: Exception) {
                    }
                    if (redPacketGot != null) {
                        val selfId = TIMManager.getInstance().loginUser
                        val openerId = redPacketGot.openerId
                        val ownerId = redPacketGot.ownerId
                        if (!TextUtils.equals(openerId, selfId) && !TextUtils.equals(selfId, ownerId)) {
                            return MessageInfo.MSG_TYPE_CUSTOM_UNKNOWN
                        } else {
                            var firstUserName: String? = null
                            var secondUserName: String? = null
                            if (TextUtils.equals(openerId, selfId) && TextUtils.equals(ownerId, selfId)) { //我领取了自己发送的红包
                                firstUserName = "我"
                                secondUserName = "自己"
                            } else if (TextUtils.equals(openerId, selfId)) { //我领取了其他人发的红包
                                firstUserName = "我"
                                secondUserName = getUserName(messageInfo, ownerId)
                            } else if (TextUtils.equals(ownerId, selfId)) { //其他人领取了我的红包
                                firstUserName = getUserName(messageInfo, openerId)
                                secondUserName = "我"
                            }
                            val text = String.format("%s领取了%s发的%s",
                                    CommonUtil.getTextMaxLength(firstUserName, 6),
                                    CommonUtil.getTextMaxLength(secondUserName, 6),
                                    FryingUtil.translateToHtmlTextAddColor(TUIKit.appContext, "红包", R.color.C4))
                            redPacketGot.text = FryingUtil.getHtmlText(text)
                        }
                        messageInfo.redPacketGot = redPacketGot
                        return MessageInfo.MSG_TYPE_CUSTOM_RED_PACKET_GOT
                    }
                    return MessageInfo.MSG_TYPE_CUSTOM_UNKNOWN
                }
            }
            MessageInfo.MSG_TYPE_CUSTOM
        } else {
            messageInfo.getMsgType()
        }
    }

    internal fun getUserName(msg: MessageInfo, userId: String?): String? {
        var userName: String? = null
        userName = if (msg.isSelf) {
            if (TextUtils.isEmpty(properties.getRightNameHard())) properties.getRightNameDefault() else properties.getRightNameHard()
        } else {
            if (TextUtils.isEmpty(properties.getLeftNameHard())) properties.getLeftNameDefault() else properties.getLeftNameHard()
        }
        val profile = TIMFriendshipManager.getInstance().queryUserProfile(userId)
        if (profile == null) {
            if (TextUtils.isEmpty(userName)) {
                userName = userId
            }
        } else {
            userName = if (TextUtils.isEmpty(msg.groupNameCard)) {
                if (!TextUtils.isEmpty(profile.nickName)) profile.nickName else if (!TextUtils.isEmpty(userName)) userName else userId
            } else {
                msg.groupNameCard
            }
        }
        return userName
    }
}
