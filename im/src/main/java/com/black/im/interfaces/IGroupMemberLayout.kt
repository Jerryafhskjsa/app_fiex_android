package com.black.im.interfaces

import com.black.im.model.group.GroupInfo

interface IGroupMemberLayout : ILayout {
    fun setDataSource(dataSource: GroupInfo?)
}