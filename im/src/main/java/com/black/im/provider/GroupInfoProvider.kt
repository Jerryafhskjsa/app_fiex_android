package com.black.im.provider

import android.text.TextUtils
import com.black.im.manager.ConversationManagerKit
import com.black.im.manager.GroupChatManagerKit
import com.black.im.model.group.GroupApplyInfo
import com.black.im.model.group.GroupInfo
import com.black.im.model.group.GroupMemberInfo
import com.black.im.util.IUIKitCallBack
import com.black.im.util.TUIKitConstants
import com.black.im.util.TUIKitLog
import com.black.im.util.ToastUtil.toastLongMessage
import com.tencent.imsdk.*
import com.tencent.imsdk.ext.group.*
import java.util.*

class GroupInfoProvider {
    private var mGroupInfo: GroupInfo? = null
    private var mSelfInfo: GroupMemberInfo? = null
    var groupMembers: MutableList<GroupMemberInfo>? = ArrayList()
        private set
    var applyList: MutableList<GroupApplyInfo> = ArrayList()
        private set
    private var mPendencyTime: Long = 0
    private fun reset() {
        mGroupInfo = GroupInfo()
        groupMembers = ArrayList()
        mSelfInfo = null
        mPendencyTime = 0
    }

    fun loadGroupInfo(info: GroupInfo) {
        mGroupInfo = info
        groupMembers = info.memberDetails
    }

    fun loadGroupInfo(groupId: String?, callBack: IUIKitCallBack?) {
        reset()
        // 串行异步加载群组信息
        loadGroupPublicInfo(groupId, object : IUIKitCallBack {
            override fun onSuccess(data: Any?) { // 设置群的一般信息，比如名称、类型等
                mGroupInfo?.covertTIMGroupDetailInfo(data as TIMGroupDetailInfoResult?)
                // 设置是否为置顶聊天
                val isTop = if (groupId == null) false else ConversationManagerKit.instance.isTopConversation(groupId)
                mGroupInfo?.isTopChat = isTop
                // 异步获取群成员
                loadGroupMembers(mGroupInfo, callBack)
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                TUIKitLog.e(TAG, "loadGroupPublicInfo failed, code: $errCode|desc: $errMsg")
                callBack?.onError(module, errCode, errMsg)
            }
        })
    }

    fun deleteGroup(callBack: IUIKitCallBack) {
        mGroupInfo?.id?.let {
            TIMGroupManager.getInstance().deleteGroup(it, object : TIMCallBack {
                override fun onError(code: Int, desc: String) {
                    callBack.onError(TAG, code, desc)
                    TUIKitLog.e(TAG, "deleteGroup failed, code: $code|desc: $desc")
                }

                override fun onSuccess() {
                    callBack.onSuccess(null)
                    ConversationManagerKit.instance.deleteConversation(it, true)
                    GroupChatManagerKit.instance.onGroupForceExit()
                }
            })
        }
    }

    fun loadGroupPublicInfo(groupId: String?, callBack: IUIKitCallBack) {
        val groupList: MutableList<String?> = ArrayList()
        groupList.add(groupId)
        TIMGroupManager.getInstance().getGroupInfo(groupList, object : TIMValueCallBack<List<TIMGroupDetailInfoResult>> {
            override fun onError(code: Int, desc: String) {
                TUIKitLog.e(TAG, "loadGroupPublicInfo failed, code: $code|desc: $desc")
                callBack.onError(TAG, code, desc)
            }

            override fun onSuccess(timGroupDetailInfoResults: List<TIMGroupDetailInfoResult>) {
                if (timGroupDetailInfoResults.isNotEmpty()) {
                    val info = timGroupDetailInfoResults[0]
                    TUIKitLog.i(TAG, info.toString())
                    callBack.onSuccess(info)
                }
            }
        })
    }

    fun loadGroupMembers(result: Any?, callBack: IUIKitCallBack?) {
        mGroupInfo?.id?.let {
            TIMGroupManager.getInstance().getGroupMembers(it, object : TIMValueCallBack<List<TIMGroupMemberInfo?>> {
                override fun onError(code: Int, desc: String) {
                    TUIKitLog.e(TAG, "loadGroupMembers failed, code: $code|desc: $desc")
                    callBack?.onError(TAG, code, desc)
                }

                override fun onSuccess(timGroupMemberInfos: List<TIMGroupMemberInfo?>) {
                    val members: MutableList<GroupMemberInfo> = ArrayList()
                    for (i in timGroupMemberInfos.indices) {
                        val member = GroupMemberInfo()
                        members.add(member.covertTIMGroupMemberInfo(timGroupMemberInfos[i]))
                    }
                    groupMembers = members
                    mGroupInfo?.memberDetails = groupMembers
                    loadGroupMembersDetail(0, object : IUIKitCallBack {
                        override fun onSuccess(data: Any?) {
                            val memberCount = groupMembers?.size ?: 0
                            val adminMembers = ArrayList<GroupMemberInfo>()
                            val normalMembers = ArrayList<GroupMemberInfo>()
                            for (i in 0 until memberCount) {
                                val memberInfo = groupMembers!![i]
                                when (memberInfo.detail?.role) {
                                    TIMGroupMemberRoleType.ROLE_TYPE_OWNER -> mGroupInfo?.ownerInfo = memberInfo
                                    TIMGroupMemberRoleType.ROLE_TYPE_ADMIN -> adminMembers.add(memberInfo)
                                    TIMGroupMemberRoleType.ROLE_TYPE_NORMAL -> normalMembers.add(memberInfo)
                                }
                            }
                            mGroupInfo?.adminMemberDetails = adminMembers
                            mGroupInfo?.normalMemberDetails = normalMembers
                            callBack?.onSuccess(result)
                        }

                        override fun onError(module: String?, errCode: Int, errMsg: String?) {
                            callBack?.onError(module, errCode, errMsg)
                        }
                    })
                }
            })
        }
    }

    private fun loadGroupMembersDetail(begin: Int, callBack: IUIKitCallBack?) {
        if (callBack == null) {
            return
        }
        val memberIds = ArrayList<String>()
        if (groupMembers?.size == 0) {
            return
        }
        val end: Int = if (begin + PAGE > groupMembers?.size ?: 0) {
            groupMembers?.size ?: 0
        } else {
            begin + PAGE
        }
        for (i in begin until end) {
            groupMembers!![i].account?.let {
                memberIds.add(it)
            }
        }
        mGroupInfo?.id?.let {
            TIMGroupManager.getInstance().getGroupMembersInfo(it, memberIds, object : TIMValueCallBack<MutableList<TIMGroupMemberInfo>> {
                override fun onError(code: Int, desc: String) {
                    TUIKitLog.e(TAG, "getGroupMembersInfo failed, code: $code|desc: $desc")
                    callBack.onError(TAG, code, desc)
                }

                override fun onSuccess(timGroupMemberInfos: MutableList<TIMGroupMemberInfo>) {
                    TUIKitLog.i(TAG, "getGroupMembersInfo success: " + timGroupMemberInfos.size)
                    for (i in begin until end) {
                        val memberInfo = groupMembers!![i]
                        for (j in timGroupMemberInfos.indices.reversed()) {
                            val detail = timGroupMemberInfos[j]
                            if (memberInfo.account == detail.user) {
                                memberInfo.detail = detail
                                timGroupMemberInfos.removeAt(j)
                                break
                            }
                        }
                    }
                    if (end < groupMembers?.size ?: 0) {
                        loadGroupMembersDetail(end, callBack)
                    } else {
                        callBack.onSuccess(null)
                    }
                }
            })
        }
    }

    fun modifyGroupInfo(value: Any, type: Int, callBack: IUIKitCallBack) {
        mGroupInfo?.id?.let {
            val param = TIMGroupManager.ModifyGroupInfoParam(it)
            when (type) {
                TUIKitConstants.Group.MODIFY_GROUP_NAME -> {
                    param.setGroupName(value.toString())
                }
                TUIKitConstants.Group.MODIFY_GROUP_NOTICE -> {
                    param.setNotification(value.toString())
                }
                TUIKitConstants.Group.MODIFY_GROUP_JOIN_TYPE -> {
                    param.addOption = TIMGroupAddOpt.values()[(value as Int)]
                }
            }
            TIMGroupManager.getInstance().modifyGroupInfo(param, object : TIMCallBack {
                override fun onError(code: Int, desc: String) {
                    TUIKitLog.i(TAG, "modifyGroupInfo faild tyep| value| code| desc $value:$type:$code:$desc")
                    callBack.onError(TAG, code, desc)
                }

                override fun onSuccess() {
                    when (type) {
                        TUIKitConstants.Group.MODIFY_GROUP_NAME -> {
                            mGroupInfo?.groupName = value.toString()
                        }
                        TUIKitConstants.Group.MODIFY_GROUP_NOTICE -> {
                            mGroupInfo?.notice = value.toString()
                        }
                        TUIKitConstants.Group.MODIFY_GROUP_JOIN_TYPE -> {
                            mGroupInfo?.joinType = (value as Int)
                        }
                    }
                    callBack.onSuccess(value)
                }
            })
        }
    }

    fun modifyMyGroupNickname(nickname: String?, callBack: IUIKitCallBack) {
        if (mGroupInfo == null) {
            toastLongMessage("modifyMyGroupNickname fail: NO GROUP")
        }
        mGroupInfo?.id?.let {
            val param = TIMGroupManager.ModifyMemberInfoParam(it, TIMManager.getInstance().loginUser)
            param.nameCard = nickname!!
            TIMGroupManager.getInstance().modifyMemberInfo(param, object : TIMCallBack {
                override fun onError(code: Int, desc: String) {
                    callBack.onError(TAG, code, desc)
                    toastLongMessage("modifyMyGroupNickname fail: $code=$desc")
                }

                override fun onSuccess() {
                    callBack.onSuccess(null)
                }
            })
        }
    }

    val selfGroupInfo: GroupMemberInfo?
        get() {
            if (mSelfInfo != null) {
                return mSelfInfo
            }
            for (i in groupMembers?.indices!!) {
                val memberInfo = groupMembers!![i]
                if (TextUtils.equals(memberInfo.account, TIMManager.getInstance().loginUser)) {
                    mSelfInfo = memberInfo
                    return memberInfo
                }
            }
            return null
        }

    fun setTopConversation(flag: Boolean) {
        mGroupInfo?.id?.let {
            ConversationManagerKit.instance.setConversationTop(it, flag)
        }
    }

    fun quitGroup(callBack: IUIKitCallBack) {
        mGroupInfo?.id?.let {
            TIMGroupManager.getInstance().quitGroup(it, object : TIMCallBack {
                override fun onError(code: Int, desc: String) {
                    TUIKitLog.e(TAG, "quitGroup failed, code: $code|desc: $desc")
                    callBack.onError(TAG, code, desc)
                }

                override fun onSuccess() {
                    ConversationManagerKit.instance.deleteConversation(it, true)
                    GroupChatManagerKit.instance.onGroupForceExit()
                    callBack.onSuccess(null)
                    reset()
                }
            })
        }
    }

    fun inviteGroupMembers(addMembers: List<String?>?, callBack: IUIKitCallBack) {
        if (addMembers == null || addMembers.isEmpty()) {
            return
        }
        mGroupInfo?.id?.let {
            TIMGroupManager.getInstance().inviteGroupMember(it, addMembers, object : TIMValueCallBack<List<TIMGroupMemberResult>> {
                override fun onError(code: Int, desc: String) {
                    TUIKitLog.e(TAG, "addGroupMembers failed, code: $code|desc: $desc")
                    callBack.onError(TAG, code, desc)
                }

                override fun onSuccess(timGroupMemberResults: List<TIMGroupMemberResult>) {
                    val adds: MutableList<String> = ArrayList()
                    if (timGroupMemberResults.isNotEmpty()) {
                        for (i in timGroupMemberResults.indices) {
                            val res = timGroupMemberResults[i]
                            if (res.result == 3L) {
                                callBack.onSuccess("邀请成功，等待对方接受")
                                return
                            }
                            if (res.result > 0) {
                                adds.add(res.user)
                            }
                        }
                    }
                    if (adds.size > 0) {
                        loadGroupMembers(adds, callBack)
                    }
                }
            })
        }
    }

    fun removeGroupMembers(delMembers: List<GroupMemberInfo>?, callBack: IUIKitCallBack) {
        if (delMembers == null || delMembers.isEmpty()) {
            return
        }
        val members: MutableList<String> = ArrayList()
        for (i in delMembers.indices) {
            delMembers[i].account?.let {
                members.add(it)
            }
        }
        mGroupInfo?.id?.let {
            val param = TIMGroupManager.DeleteMemberParam(it, members)
            TIMGroupManager.getInstance().deleteGroupMember(param, object : TIMValueCallBack<MutableList<TIMGroupMemberResult>> {
                override fun onError(code: Int, desc: String) {
                    TUIKitLog.e(TAG, "removeGroupMembers failed, code: $code|desc: $desc")
                    callBack.onError(TAG, code, desc)
                }

                override fun onSuccess(timGroupMemberResults: MutableList<TIMGroupMemberResult>) {
                    val dels: MutableList<String> = ArrayList()
                    for (i in timGroupMemberResults.indices) {
                        val res = timGroupMemberResults[i]
                        if (res.result == 1L) {
                            dels.add(res.user)
                        }
                    }
                    for (i in dels.indices) {
                        for (j in groupMembers?.indices?.reversed()!!) {
                            if (groupMembers!![j].account == dels[i]) {
                                groupMembers?.removeAt(j)
                                break
                            }
                        }
                    }
                    mGroupInfo?.memberDetails = groupMembers
                    callBack.onSuccess(dels)
                }
            })
        }
    }

    fun loadGroupApplies(callBack: IUIKitCallBack) {
        loadApplyInfo(object : IUIKitCallBack {
            override fun onSuccess(data: Any?) {
                if (mGroupInfo == null) {
                    callBack.onError(TAG, 0, "no groupInfo")
                    return
                }
                val groupId = mGroupInfo?.id
                val allApplies = data as List<GroupApplyInfo>?
                val applyInfos: MutableList<GroupApplyInfo> = ArrayList()
                for (i in allApplies?.indices!!) {
                    val applyInfo = allApplies[i]
                    if (groupId == applyInfo.pendencyItem.groupId && applyInfo.pendencyItem.handledStatus == TIMGroupPendencyHandledStatus.NOT_HANDLED) {
                        applyInfos.add(applyInfo)
                    }
                }
                applyList = applyInfos
                callBack.onSuccess(applyInfos)
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                TUIKitLog.e(TAG, "loadApplyInfo failed, code: $errCode|desc: $errMsg")
                callBack.onError(module, errCode, errMsg)
            }
        })
    }

    private fun loadApplyInfo(callBack: IUIKitCallBack) {
        val applies: MutableList<GroupApplyInfo> = ArrayList()
        val param = TIMGroupPendencyGetParam()
        param.timestamp = mPendencyTime
        TIMGroupManager.getInstance().getGroupPendencyList(param, object : TIMValueCallBack<TIMGroupPendencyListGetSucc> {
            override fun onError(code: Int, desc: String) {
                TUIKitLog.e(TAG, "getGroupPendencyList failed, code: $code|desc: $desc")
                callBack.onError(TAG, code, desc)
            }

            override fun onSuccess(timGroupPendencyListGetSucc: TIMGroupPendencyListGetSucc) {
                mPendencyTime = timGroupPendencyListGetSucc.meta.nextStartTimestamp
                val pendencies = timGroupPendencyListGetSucc.pendencies
                for (i in pendencies.indices) {
                    val info = GroupApplyInfo(pendencies[i])
                    info.status = 0
                    applies.add(info)
                }
                callBack.onSuccess(applies)
            }
        })
    }

    fun acceptApply(item: GroupApplyInfo, callBack: IUIKitCallBack) {
        item.pendencyItem.accept("", object : TIMCallBack {
            override fun onError(code: Int, desc: String) {
                TUIKitLog.e(TAG, "acceptApply failed, code: $code|desc: $desc")
                callBack.onError(TAG, code, desc)
            }

            override fun onSuccess() {
                item.status = GroupApplyInfo.APPLIED
                callBack.onSuccess(null)
            }
        })
    }

    fun refuseApply(item: GroupApplyInfo, callBack: IUIKitCallBack) {
        item.pendencyItem.refuse("", object : TIMCallBack {
            override fun onError(code: Int, desc: String) {
                TUIKitLog.e(TAG, "refuseApply failed, code: $code|desc: $desc")
                callBack.onError(TAG, code, desc)
            }

            override fun onSuccess() {
                item.status = GroupApplyInfo.REFUSED
                callBack.onSuccess(null)
            }
        })
    }

    companion object {
        private val TAG = GroupInfoProvider::class.java.simpleName
        private const val PAGE = 50
    }
}