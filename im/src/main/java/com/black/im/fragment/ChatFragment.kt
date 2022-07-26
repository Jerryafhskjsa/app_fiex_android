package com.black.im.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.black.base.fragment.BaseFragment
import com.black.base.model.community.RedPacket
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.im.R
import com.black.im.model.chat.ChatInfo
import com.black.im.model.chat.CustomMessageData
import com.black.im.model.chat.MessageInfo
import com.black.im.util.AudioPlayer
import com.black.im.util.IMConstData
import com.black.im.widget.ChatLayout
import com.black.im.widget.InputLayout.InputSendHelper
import com.black.im.widget.MessageLayout
import com.black.router.BlackRouter
import com.google.gson.JsonObject
import com.tencent.imsdk.*
import java.util.*

class ChatFragment : BaseFragment() {
    private var layout: View? = null
    private var chatLayout: ChatLayout? = null
    private var chatInfo: ChatInfo? = null
    private var onChatLayoutListener: OnChatLayoutListener? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return null
        }
        val bundle = arguments
        chatInfo = if (bundle == null) null else bundle.getSerializable(IMConstData.CHAT_INFO) as ChatInfo
        if (chatInfo == null) {
            return null
        }
        layout = inflater.inflate(R.layout.fragment_chat, container, false)
        initView()
        return layout
    }

    private fun initView() {
        //从布局文件中获取聊天面板组件
        chatLayout = layout?.findViewById(R.id.chat_layout)
        //单聊组件的默认UI和交互初始化
        chatLayout?.initDefault()
        if (onChatLayoutListener != null) {
            onChatLayoutListener?.onInitChatLayout(chatLayout)
        }
        /*
         * 需要聊天的基本信息
         */chatLayout?.setChatInfo(chatInfo)
        //获取单聊面板的标题栏
        chatLayout?.titleBar?.visibility = View.GONE
        chatLayout?.messageLayout?.setOnItemClickListener(object : MessageLayout.OnItemClickListener {
            override fun onMessageClick(view: View?, position: Int, messageInfo: MessageInfo?) {
                val messageClickCheckListener = chatLayout?.messageClickCheckListener
                if (messageClickCheckListener != null && !messageClickCheckListener.onMessageClickCheck(messageInfo)) {
                    return
                }
                if (messageInfo?.getMsgType() == MessageInfo.MSG_TYPE_CUSTOM_RED_PACKET) {
                    val customMessageData = messageInfo.customMessageData
                    val redPacket = if (customMessageData?.data == null) null else gson.fromJson(customMessageData.data, RedPacket::class.java)
                    if (redPacket != null) {
                        val properties = chatLayout?.messageLayout?.getProperties()
                        val sendName: String?
                        var sendAvatar: String? = null
                        val nameHard: String?
                        nameHard = if (messageInfo.isSelf) {
                            properties?.getRightNameHard()
                        } else {
                            properties?.getLeftNameHard()
                        }
                        if (nameHard != null) {
                            sendName = nameHard
                        } else {
                            val nameDefault: String? = if (messageInfo.isSelf) {
                                properties?.getRightNameDefault()
                            } else {
                                properties?.getLeftNameDefault()
                            }
                            val profile = TIMFriendshipManager.getInstance().queryUserProfile(messageInfo.fromUser)
                            if (profile == null) {
                                sendName = if (TextUtils.isEmpty(nameDefault)) messageInfo.fromUser else nameDefault
                            } else {
                                sendName = if (TextUtils.isEmpty(messageInfo.groupNameCard)) {
                                    if (!TextUtils.isEmpty(profile.nickName)) profile.nickName else if (!TextUtils.isEmpty(nameDefault)) nameDefault else messageInfo.fromUser
                                } else {
                                    messageInfo.groupNameCard
                                }
                                sendAvatar = profile.faceUrl
                            }
                        }
                        redPacket.sendName = sendName
                        redPacket.sendAvatar = sendAvatar
                        redPacket.status = messageInfo.customInt
                        val url = String.format("fbsex://openRedPacket?packetId=%s", redPacket.packetId)
                        val bundle = Bundle()
                        bundle.putParcelable(ConstData.RED_PACKET, redPacket)
                        BlackRouter.getInstance().build(url).with(bundle).withSingle { singleType, _ ->
                            if (messageInfo.customInt == RedPacket.NEW && singleType == RedPacket.IS_OPEN) {
                                //开启了红包，发送一个消息
                                sendRedPacketGotMessage(messageInfo.fromUser, TIMManager.getInstance().loginUser)
                            }
                            messageInfo.customInt = singleType
                            val adapter = chatLayout?.messageLayout?.adapter
                            adapter?.notifyItemChanged(position)
                        }.go(mFragment) { _, error -> error?.printStackTrace() }
                    }
                }
            }

            override fun onMessageLongClick(view: View?, position: Int, messageInfo: MessageInfo?) {
                //因为adapter中第一条为加载条目，位置需减1
                if (messageInfo?.getMsgType() == MessageInfo.MSG_TYPE_CUSTOM_RED_PACKET) {
                    return
                }
                view?.let {
                    chatLayout?.messageLayout?.showItemPopMenu(position - 1, messageInfo, view)
                }
            }

            override fun onUserIconClick(view: View?, position: Int, messageInfo: MessageInfo?) {
                if (null == messageInfo) {
                    return
                }
                if (chatInfo != null && chatInfo?.type == TIMConversationType.Group) {
                    val groupId = chatInfo?.id
                    val bundle = Bundle()
                    bundle.putString(ConstData.IM_GROUP_ID, groupId)
                    bundle.putString(ConstData.IM_USER_ID, messageInfo.fromUser)
                    BlackRouter.getInstance().build(RouterConstData.IM_GROUP_MEMBER).with(bundle).go(mFragment)
                }
                val idList: MutableList<String?> = ArrayList()
                idList.add(messageInfo.fromUser)
                TIMFriendshipManager.getInstance().getUsersProfile(idList, true, object : TIMValueCallBack<List<TIMUserProfile?>?> {
                    override fun onError(i: Int, s: String) {}
                    override fun onSuccess(timUserProfiles: List<TIMUserProfile?>?) {
                        chatLayout?.messageLayout?.refreshAdapter()
                    }
                })
            }
        })
        chatLayout?.inputLayout?.setInputHelper(object : InputSendHelper {
            override fun sendRedPacket() {
                BlackRouter.getInstance().build(RouterConstData.RED_PACKET_CREATE)
                        .withRequestCode(ConstData.CREATE_RED_PACKET)
                        .go(mFragment)
            }
        })
    }

    override fun onResume() {
        super.onResume()
    }

    private var isDestroyed = false
    override fun onPause() {
        super.onPause()
        AudioPlayer.instance.stopPlay()
        if (activity != null && activity!!.isFinishing) {
            destroy()
        }
    }

    private fun destroy() {
        if (isDestroyed) {
            return
        }
        isDestroyed = true
        if (chatLayout != null) {
            chatLayout?.exitChat()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroy()
    }

    override fun onDetach() {
        super.onDetach()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ConstData.CREATE_RED_PACKET -> if (data != null) {
                    val redPacketId = data.getStringExtra(ConstData.RED_PACKET_ID)
                    val redPacketText = data.getStringExtra(ConstData.RED_PACKET_TEXT)
                    sendRedPacketMessage(redPacketId, redPacketText)
                }
            }
        }
    }

    private fun sendRedPacketMessage(redPacketId: String, redPacketText: String) {
        val redPacketMessage = JsonObject()
        redPacketMessage.addProperty("subType", CustomMessageData.TYPE_RED_PACKET)
        val redPacketData = JsonObject()
        redPacketData.addProperty("title", redPacketText)
        redPacketData.addProperty("packetId", redPacketId)
        redPacketMessage.add("data", redPacketData)
        chatLayout?.inputLayout?.sendCustomMessage(redPacketMessage.toString())
    }

    private fun sendRedPacketGotMessage(ownerId: String?, openerId: String) {
        val redPacketMessage = JsonObject()
        redPacketMessage.addProperty("subType", CustomMessageData.TYPE_RED_PACKET_GOT)
        val redPacketData = JsonObject()
        redPacketData.addProperty("openerId", openerId)
        redPacketData.addProperty("ownerId", ownerId)
        redPacketMessage.add("data", redPacketData)
        chatLayout?.inputLayout?.sendCustomMessage(redPacketMessage.toString())
    }

    fun setOnChatLayoutListener(onChatLayoutListener: OnChatLayoutListener?) {
        this.onChatLayoutListener = onChatLayoutListener
    }

    interface OnChatLayoutListener {
        fun onInitChatLayout(chatLayout: ChatLayout?)
    }
}