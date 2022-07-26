package com.black.im.interfaces

import com.black.im.model.group.GroupMemberInfo

interface IGroupMemberChangedCallback {
    fun onMemberRemoved(memberInfo: GroupMemberInfo?)
}
