package com.black.im.interfaces

import com.black.im.model.group.GroupInfo

interface IGroupMemberRouter {
    fun forwardListMember(info: GroupInfo?)
    fun forwardAddMember(info: GroupInfo?)
    fun forwardDeleteMember(info: GroupInfo?)
}
