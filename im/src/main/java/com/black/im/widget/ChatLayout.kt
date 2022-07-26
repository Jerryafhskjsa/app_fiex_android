package com.black.im.widget

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import android.view.View.OnClickListener
import com.black.im.R
import com.black.im.activity.GroupApplyManagerActivity
import com.black.im.activity.GroupInfoActivity
import com.black.im.interfaces.ITitleBarLayout
import com.black.im.manager.C2CChatManagerKit
import com.black.im.manager.ChatManagerKit
import com.black.im.manager.GroupChatManagerKit
import com.black.im.model.chat.ChatInfo
import com.black.im.model.group.GroupApplyInfo
import com.black.im.model.group.GroupInfo
import com.black.im.util.IUIKitCallBack
import com.black.im.util.TUIKitConstants
import com.black.im.util.ToastUtil.toastLongMessage
import com.tencent.imsdk.TIMConversationType

class ChatLayout : AbsChatLayout, GroupChatManagerKit.GroupNotifyHandler {
    private var mGroupInfo: GroupInfo? = null
    private var mGroupChatManager: GroupChatManagerKit? = null
    private var mC2CChatManager: C2CChatManagerKit? = null
    private var isGroup = false

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun setChatInfo(chatInfo: ChatInfo?) {
        super.setChatInfo(chatInfo)
        if (chatInfo == null) {
            return
        }
        isGroup = chatInfo.type != TIMConversationType.C2C
        if (isGroup) {
            mGroupChatManager = GroupChatManagerKit.instance
            mGroupChatManager?.setGroupHandler(this)
            val groupInfo = GroupInfo()
            groupInfo.id = chatInfo.id
            groupInfo.chatName = chatInfo.chatName
            mGroupChatManager?.setCurrentChatInfo(groupInfo)
            mGroupInfo = groupInfo
            loadChatMessages(null)
            loadApplyList()
            titleBar?.getRightIcon()?.setImageResource(R.drawable.chat_group)
            titleBar?.setOnRightClickListener(OnClickListener {
                if (mGroupInfo != null) {
                    val intent = Intent(context, GroupInfoActivity::class.java)
                    intent.putExtra(TUIKitConstants.Group.GROUP_ID, mGroupInfo?.id)
                    context.startActivity(intent)
                } else {
                    toastLongMessage("请稍后再试试~")
                }
            })
            mGroupApplyLayout?.setOnNoticeClickListener(OnClickListener {
                val intent = Intent(context, GroupApplyManagerActivity::class.java)
                intent.putExtra(TUIKitConstants.Group.GROUP_INFO, mGroupInfo)
                context.startActivity(intent)
            })
        } else {
            titleBar?.getRightIcon()?.setImageResource(R.drawable.chat_c2c)
            mC2CChatManager = C2CChatManagerKit.instance
            mC2CChatManager?.setCurrentChatInfo(chatInfo)
            loadChatMessages(null)
        }
    }

    override val chatManager: ChatManagerKit
        get() = if (isGroup) {
            mGroupChatManager!!
        } else {
            mC2CChatManager!!
        }

    private fun loadApplyList() {
        mGroupChatManager?.provider?.loadGroupApplies(object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                val applies = data as List<GroupApplyInfo>?
                if (applies != null && applies.isNotEmpty()) {
                    mGroupApplyLayout?.content?.text = context.getString(R.string.group_apply_tips, applies.size)
                    mGroupApplyLayout?.visibility = View.VISIBLE
                }
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                toastLongMessage("loadApplyList onError: $errMsg")
            }
        })
    }

    override fun onGroupForceExit() {
        if (context is Activity) {
            (context as Activity).finish()
        }
    }

    override fun onGroupNameChanged(newName: String?) {
        titleBar?.setTitle(newName, ITitleBarLayout.POSITION.MIDDLE)
    }

    override fun onApplied(size: Int) {
        if (size == 0) {
            mGroupApplyLayout?.visibility = View.GONE
        } else {
            mGroupApplyLayout?.content?.text = context.getString(R.string.group_apply_tips, size)
            mGroupApplyLayout?.visibility = View.VISIBLE
        }
    }
}