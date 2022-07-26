package com.black.im.model.group

import com.black.im.model.chat.ChatInfo
import com.tencent.imsdk.TIMConversationType
import com.tencent.imsdk.TIMManager
import com.tencent.imsdk.ext.group.TIMGroupDetailInfoResult

class GroupInfo : ChatInfo() {
    /**
     * 获取群类型，Public/Private/ChatRoom
     *
     * @return
     */
    /**
     * 设置群类型
     *
     * @param groupType
     */
    var groupType: String? = null
    /**
     * 获取群成员数量
     *
     * @return
     */
    /**
     * 设置群成员数量
     *
     * @param memberCount
     */
    var memberCount = 0
        get() = if (memberDetails != null) {
            memberDetails!!.size
        } else field
    /**
     * 获取群名称
     *
     * @return
     */
    /**
     * 设置群名称
     *
     * @param groupName
     */
    var groupName: String? = null
    /**
     * 获取群公告
     *
     * @return
     */
    /**
     * 设置群公告
     *
     * @param signature
     */
    var notice: String? = null
    /**
     * 群简介
     */
    /**
     * 群简介
     */
    var introduction: String? = null
    /**
     * 获取成员详细信息
     *
     * @return
     */
    /**
     * 设置成员详细信息
     *
     * @param memberDetails
     */
    var memberDetails: MutableList<GroupMemberInfo>? = null
    /**
     * 回去加群验证方式
     *
     * @return
     */
    /**
     * 设置加群验证方式
     *
     * @param joinType
     */
    var joinType = 0
    private var owner: String? = null
    var ownerInfo: GroupMemberInfo? = null
    var adminMemberDetails: MutableList<GroupMemberInfo>? = null
    var normalMemberDetails: MutableList<GroupMemberInfo>? = null

    init {
        type = TIMConversationType.Group
    }

    /**
     * 返回是否是群主
     *
     * @return
     */
    fun isOwner(): Boolean {
        return TIMManager.getInstance().loginUser == owner
    }

    fun getOwner(): String? {
        return owner
    }

    /**
     * 设置是否是群主
     *
     * @param owner
     */
    fun setOwner(owner: String?) {
        this.owner = owner
    }

    /**
     * 从SDK转化为TUIKit的群信息bean
     *
     * @param detailInfo
     * @return
     */
    fun covertTIMGroupDetailInfo(detailInfo: TIMGroupDetailInfoResult?): GroupInfo {
        chatName = detailInfo?.groupName
        groupName = detailInfo?.groupName
        id = detailInfo?.groupId
        notice = detailInfo?.groupNotification
        memberCount = detailInfo?.memberNum?.toInt() ?: 0
        groupType = detailInfo?.groupType
        setOwner(detailInfo?.groupOwner)
        joinType = detailInfo?.addOption?.value?.toInt() ?: 0
        introduction = detailInfo?.groupIntroduction
        return this
    }
}