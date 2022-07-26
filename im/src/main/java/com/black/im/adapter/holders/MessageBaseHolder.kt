package com.black.im.adapter.holders

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.black.im.R
import com.black.im.adapter.MessageListAdapter
import com.black.im.model.chat.MessageInfo
import com.black.im.util.TUIKit.appContext
import com.black.im.widget.MessageLayout
import com.black.im.widget.MessageLayoutUI.Properties
import com.tencent.imsdk.TIMFriendshipManager

abstract class MessageBaseHolder(itemView: View, properties: Properties?) : RecyclerView.ViewHolder(itemView) {
    var mAdapter: MessageListAdapter? = null
    var properties: Properties? = Properties.getInstance()
    protected var rootView: View
    protected var onItemClickListener: MessageLayout.OnItemClickListener? = null
        private set

    init {
        this.properties = properties
        rootView = itemView
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        mAdapter = adapter as MessageListAdapter?
    }

    fun setOnItemClickListener(listener: MessageLayout.OnItemClickListener?) {
        onItemClickListener = listener
    }

    abstract fun layoutViews(msg: MessageInfo?, position: Int)

    object Factory {
        fun getInstance(messageLayout: MessageLayout, parent: ViewGroup?, adapter: RecyclerView.Adapter<*>?, viewType: Int): RecyclerView.ViewHolder {
            val inflater = LayoutInflater.from(appContext)
            var holder: RecyclerView.ViewHolder? = null
            var view: View? = null
            // 头部的holder
            if (viewType == MessageListAdapter.MSG_TYPE_HEADER_VIEW) {
                view = inflater.inflate(R.layout.message_adapter_content_header, parent, false)
                holder = MessageHeaderHolder(view, messageLayout.getProperties())
                return holder
            }
            // 加群消息等holder
            if (viewType >= MessageInfo.MSG_TYPE_TIPS) {
                view = inflater.inflate(R.layout.message_adapter_item_empty, parent, false)
                holder = MessageTipsHolder(view, messageLayout.getProperties())
            }
            // 未知的自定义消息
            if (viewType == MessageInfo.MSG_TYPE_CUSTOM_UNKNOWN) {
                view = inflater.inflate(R.layout.message_adapter_item_custom_unknown, parent, false)
                holder = MessageCustomUnknownHolder(view, messageLayout.getProperties())
            }
            // 领取群红包消息
            if (viewType == MessageInfo.MSG_TYPE_CUSTOM_RED_PACKET_GOT) {
                //                view = inflater.inflate(R.layout.message_adapter_item_custom_unknown, parent, false);
                //                holder = new MessageCustomUnknownHolder(view, messageLayout.getProperties());
                view = inflater.inflate(R.layout.message_adapter_item_custom_tips_empty, parent, false)
                holder = MessageCustomRedPacketGotHolder(view, messageLayout.getProperties())
            }
            // 领取群红包消息
            //            if (viewType == MessageInfo.MSG_TYPE_CUSTOM_RED_PACKET) {
            //                view = inflater.inflate(R.layout.message_adapter_item_custom_unknown, parent, false);
            //                holder = new MessageCustomUnknownHolder(view, messageLayout.getProperties());
            //            }
            // 具体消息holder
            view = inflater.inflate(R.layout.message_adapter_item_content, parent, false)
            when (viewType) {
                MessageInfo.MSG_TYPE_TEXT -> holder = MessageTextHolder(view, messageLayout.getProperties())
                MessageInfo.MSG_TYPE_IMAGE, MessageInfo.MSG_TYPE_VIDEO, MessageInfo.MSG_TYPE_CUSTOM_FACE -> holder = MessageImageHolder(view, messageLayout.getProperties())
                MessageInfo.MSG_TYPE_AUDIO -> holder = MessageAudioHolder(view, messageLayout.getProperties())
                MessageInfo.MSG_TYPE_FILE -> holder = MessageFileHolder(view, messageLayout.getProperties())
                MessageInfo.MSG_TYPE_CUSTOM -> holder = MessageCustomHolder(view, messageLayout.getProperties())
                MessageInfo.MSG_TYPE_CUSTOM_RED_PACKET -> holder = MessageCustomRedPacketHolder(view, messageLayout.getProperties())
            }
            if (holder != null) {
                (holder as MessageEmptyHolder).setAdapter(adapter)
            }
            return holder!!
        }
    }

    protected fun getUserName(msg: MessageInfo, userId: String?): String? {
        var userName: String? = null
        userName = if (msg.isSelf) {
            if (TextUtils.isEmpty(properties?.getRightNameHard())) properties?.getRightNameDefault() else properties?.getRightNameHard()
        } else {
            if (TextUtils.isEmpty(properties?.getLeftNameHard())) properties?.getLeftNameDefault() else properties?.getLeftNameHard()
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