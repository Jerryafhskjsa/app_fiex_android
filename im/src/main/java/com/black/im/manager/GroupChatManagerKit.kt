package com.black.im.manager

import com.black.im.model.chat.ChatInfo
import com.black.im.model.chat.MessageInfo
import com.black.im.model.group.GroupApplyInfo
import com.black.im.model.group.GroupInfo
import com.black.im.model.group.GroupMemberInfo
import com.black.im.provider.GroupInfoProvider
import com.black.im.util.IUIKitCallBack
import com.black.im.util.MessageInfoUtil
import com.black.im.util.MessageInfoUtil.buildGroupCustomMessage
import com.black.im.util.TUIKitLog
import com.black.im.util.ToastUtil.toastLongMessage
import com.tencent.imsdk.*
import com.tencent.imsdk.TIMGroupManager.CreateGroupParam
import java.util.*

class GroupChatManagerKit private constructor() : ChatManagerKit() {
    private var mCurrentChatInfo: GroupInfo? = null
    private val mCurrentApplies: MutableList<GroupApplyInfo> = ArrayList()
    private val mCurrentGroupMembers: MutableList<GroupMemberInfo> = ArrayList()
    private var mGroupHandler: GroupNotifyHandler? = null
    var provider: GroupInfoProvider
        private set

    init {
        super.init()
        provider = GroupInfoProvider()
    }

    override fun getCurrentChatInfo(): ChatInfo? {
        return mCurrentChatInfo
    }

    override fun setCurrentChatInfo(info: ChatInfo?) {
        super.setCurrentChatInfo(info)
        mCurrentChatInfo = info as GroupInfo?
        mCurrentApplies.clear()
        mCurrentGroupMembers.clear()
        provider?.loadGroupInfo(mCurrentChatInfo!!)
    }

    override fun onReceiveSystemMessage(msg: TIMMessage) {
        super.onReceiveSystemMessage(msg)
        val ele = msg.getElement(0)
        val eleType = ele.type
        if (eleType == TIMElemType.GroupSystem) {
            TUIKitLog.i(TAG, "onReceiveSystemMessage msg = $msg")
            val groupSysEle = ele as TIMGroupSystemElem
            groupSystMsgHandle(groupSysEle)
        }
    }

    override fun addGroupMessage(msgInfo: MessageInfo?) {
        val groupTips: TIMGroupTipsElem
        groupTips = if (msgInfo?.getMsgType() == MessageInfo.MSG_TYPE_GROUP_JOIN || msgInfo?.getMsgType() == MessageInfo.MSG_TYPE_GROUP_QUITE
                || msgInfo?.getMsgType() == MessageInfo.MSG_TYPE_GROUP_KICK || msgInfo?.getMsgType() == MessageInfo.MSG_TYPE_GROUP_MODIFY_NAME
                || msgInfo?.getMsgType() == MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE) {
            val elem = msgInfo?.element as? TIMGroupTipsElem ?: return
            elem
        } else {
            return
        }
        if (msgInfo.getMsgType() == MessageInfo.MSG_TYPE_GROUP_JOIN) {
            val changeInfos = groupTips.changedGroupMemberInfo
            if (changeInfos.isNotEmpty()) {
                val keys: Iterator<String> = changeInfos.keys.iterator()
                while (keys.hasNext()) {
                    val member = GroupMemberInfo()
                    member.covertTIMGroupMemberInfo(changeInfos[keys.next()])
                    mCurrentGroupMembers.add(member)
                }
            } else {
                val member = GroupMemberInfo()
                member.covertTIMGroupMemberInfo(groupTips.opGroupMemberInfo)
                mCurrentGroupMembers.add(member)
            }
            mCurrentChatInfo?.memberDetails = mCurrentGroupMembers
        } else if (msgInfo?.getMsgType() == MessageInfo.MSG_TYPE_GROUP_QUITE || msgInfo.getMsgType() == MessageInfo.MSG_TYPE_GROUP_KICK) {
            val changeInfos = groupTips.changedGroupMemberInfo
            if (changeInfos.size > 0) {
                val keys: Iterator<String> = changeInfos.keys.iterator()
                while (keys.hasNext()) {
                    val id = keys.next()
                    for (i in mCurrentGroupMembers.indices) {
                        if (mCurrentGroupMembers[i].account == id) {
                            mCurrentGroupMembers.removeAt(i)
                            break
                        }
                    }
                }
            } else {
                val memberInfo = groupTips.opGroupMemberInfo
                for (i in mCurrentGroupMembers.indices) {
                    if (mCurrentGroupMembers[i].account == memberInfo.user) {
                        mCurrentGroupMembers.removeAt(i)
                        break
                    }
                }
            }
            mCurrentChatInfo?.memberDetails = mCurrentGroupMembers
        } else if (msgInfo.getMsgType() == MessageInfo.MSG_TYPE_GROUP_MODIFY_NAME || msgInfo.getMsgType() == MessageInfo.MSG_TYPE_GROUP_MODIFY_NOTICE) {
            val modifyList = groupTips.groupInfoList
            if (modifyList.size > 0) {
                val modifyInfo = modifyList[0]
                val modifyType = modifyInfo.type
                if (modifyType == TIMGroupTipsGroupInfoType.ModifyName) {
                    mCurrentChatInfo?.groupName = modifyInfo.content
                    if (mGroupHandler != null) {
                        mGroupHandler?.onGroupNameChanged(modifyInfo.content)
                    }
                } else if (modifyType == TIMGroupTipsGroupInfoType.ModifyNotification) {
                    mCurrentChatInfo?.notice = modifyInfo.content
                }
            }
        }
    }

    //群系统消息处理，不需要显示信息的
    private fun groupSystMsgHandle(groupSysEle: TIMGroupSystemElem) {
        val type = groupSysEle.subtype
        if (type == TIMGroupSystemElemType.TIM_GROUP_SYSTEM_ADD_GROUP_ACCEPT_TYPE) { //ToastUtil.toastLongMessage("您已被同意加入群：" + groupSysEle.getGroupId());
        } else if (type == TIMGroupSystemElemType.TIM_GROUP_SYSTEM_ADD_GROUP_REFUSE_TYPE) {
            toastLongMessage("您被拒绝加入群：" + groupSysEle.groupId)
        } else if (type == TIMGroupSystemElemType.TIM_GROUP_SYSTEM_KICK_OFF_FROM_GROUP_TYPE) {
            toastLongMessage("您已被踢出群：" + groupSysEle.groupId)
            ConversationManagerKit.instance.deleteConversation(groupSysEle.groupId, true)
            if (mCurrentChatInfo != null && groupSysEle.groupId == mCurrentChatInfo?.id) {
                onGroupForceExit()
            }
        } else if (type == TIMGroupSystemElemType.TIM_GROUP_SYSTEM_DELETE_GROUP_TYPE) {
            toastLongMessage("您所在的群" + groupSysEle.groupId + "已解散")
            if (mCurrentChatInfo != null && groupSysEle.groupId == mCurrentChatInfo?.id) {
                onGroupForceExit()
            }
        }
    }

    fun onGroupForceExit() {
        if (mGroupHandler != null) {
            mGroupHandler?.onGroupForceExit()
        }
    }

    override fun destroyChat() {
        super.destroyChat()
        mCurrentChatInfo = null
        mGroupHandler = null
        mCurrentApplies.clear()
        mCurrentGroupMembers.clear()
    }

    fun setGroupHandler(mGroupHandler: GroupNotifyHandler?) {
        this.mGroupHandler = mGroupHandler
    }

    fun onApplied(unHandledSize: Int) {
        if (mGroupHandler != null) {
            mGroupHandler?.onApplied(unHandledSize)
        }
    }

    override fun isGroup(): Boolean {
        return true
    }

    override fun assembleGroupMessage(message: MessageInfo?) {
        message?.isGroup = true
        message?.fromUser = TIMManager.getInstance().loginUser
    }

    interface GroupNotifyHandler {
        fun onGroupForceExit()
        fun onGroupNameChanged(newName: String?)
        fun onApplied(size: Int)
    }

    companion object {
        private val TAG = GroupChatManagerKit::class.java.simpleName
        private var mKit: GroupChatManagerKit? = null
        val instance: GroupChatManagerKit
            get() {
                if (mKit == null) {
                    mKit = GroupChatManagerKit()
                }
                return mKit!!
            }

        private fun sendTipsMessage(conversation: TIMConversation, message: TIMMessage, callBack: IUIKitCallBack?) {
            conversation.sendMessage(message, object : TIMValueCallBack<TIMMessage?> {
                override fun onError(code: Int, desc: String) {
                    TUIKitLog.i(TAG, "sendTipsMessage fail:$code=$desc")
                    callBack?.onError(TAG, code, desc)
                }

                override fun onSuccess(timMessage: TIMMessage?) {
                    TUIKitLog.i(TAG, "sendTipsMessage onSuccess")
                    callBack?.onSuccess(timMessage)
                }
            })
        }

        fun createGroupChat(chatInfo: GroupInfo, callBack: IUIKitCallBack) {
            if (chatInfo.groupType == null || chatInfo.groupName == null) {
                return
            }
            val param = CreateGroupParam(chatInfo.groupType!!, chatInfo.groupName!!)
            if (chatInfo.joinType > -1) {
                param.addOption = TIMGroupAddOpt.values()[chatInfo.joinType]
            }
            param.introduction = chatInfo.notice
            val infos: MutableList<TIMGroupMemberInfo> = ArrayList()
            chatInfo.memberDetails?.let {
                for (i in it.indices) {
                    val memberInfo = TIMGroupMemberInfo(it[i].account!!)
                    infos.add(memberInfo)
                }
            }
            param.members = infos
            TIMGroupManager.getInstance().createGroup(param, object : TIMValueCallBack<String?> {
                override fun onError(code: Int, desc: String) {
                    callBack.onError(TAG, code, desc)
                    TUIKitLog.e(TAG, "createGroup failed, code: $code|desc: $desc")
                }

                override fun onSuccess(groupId: String?) {
                    chatInfo.id = groupId
                    val message = TIMManager.getInstance().loginUser + "创建群组"
                    val createTips = buildGroupCustomMessage(MessageInfoUtil.GROUP_CREATE, message)
                    val conversation = TIMManager.getInstance().getConversation(TIMConversationType.Group, groupId)
                    try {
                        Thread.sleep(200)
                    } catch (e: InterruptedException) {
                    }
                    sendTipsMessage(conversation, createTips, object : IUIKitCallBack {
                        override fun onSuccess(data: Any?) {
                            callBack.onSuccess(groupId)
                        }

                        override fun onError(module: String?, errCode: Int, errMsg: String?) {
                            TUIKitLog.e(TAG, "sendTipsMessage failed, code: $errCode|desc: $errMsg")
                        }
                    })
                }
            })
        }
    }
}