package com.black.im.model.group

import com.tencent.imsdk.TIMGroupMemberInfo
import com.tencent.imsdk.TIMGroupMemberRoleType
import java.io.Serializable

class GroupMemberInfo : Serializable {
    var iconUrl: String? = null
    var account: String? = null
    var signature: String? = null
    var location: String? = null
    var birthday: String? = null
    var isTopChat = false
    var isFriend = false
    var joinTime: Long = 0
    var tinyId: Long = 0
    var memberType = 0
    var detail: TIMGroupMemberInfo? = null

    fun covertTIMGroupMemberInfo(info: TIMGroupMemberInfo?): GroupMemberInfo {
        account = info?.user
        tinyId = info?.tinyId ?: 0
        joinTime = info?.joinTime ?: 0
        memberType = info?.role ?: TIMGroupMemberRoleType.ROLE_TYPE_NOT_MEMBER
        return this
    }
}