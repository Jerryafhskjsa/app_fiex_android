package com.black.im.model

import android.text.TextUtils
import com.black.im.indexlib.IndexBar.bean.BaseIndexPinyinBean
import com.tencent.imsdk.ext.group.TIMGroupBaseInfo
import com.tencent.imsdk.friendship.TIMFriend

class ContactItemBean : BaseIndexPinyinBean {
    companion object {
        const val INDEX_STRING_TOP = "↑"
    }

    var id: String? = null
        private set
    var isTop //是否是最上面的 不需要被转化成拼音的
            = false
        private set
    var isSelected = false
    var isBlackList = false
    var remark: String? = null
    var nickname: String? = null
    var avatarurl: String? = null
    var isGroup = false
    var isFriend = true
    var isEnable = true

    constructor() {}
    constructor(id: String?) {
        this.id = id
    }

    fun setId(id: String?): ContactItemBean {
        this.id = id
        return this
    }

    fun setTop(top: Boolean): ContactItemBean {
        isTop = top
        return this
    }

    override fun getTarget(): String? {
        if (!TextUtils.isEmpty(remark)) {
            return remark
        }
        return if (!TextUtils.isEmpty(nickname)) {
            nickname
        } else id
    }

    override val isNeedToPinyin: Boolean
        get() = !isTop

    override fun isShowSuspension(): Boolean {
        return !isTop
    }

    fun covertTIMFriend(friend: TIMFriend?): ContactItemBean {
        if (friend == null) {
            return this
        }
        setId(friend.identifier)
        remark = friend.remark
        nickname = friend.timUserProfile.nickName
        avatarurl = friend.timUserProfile.faceUrl
        return this
    }

    fun covertTIMGroupBaseInfo(group: TIMGroupBaseInfo?): ContactItemBean {
        if (group == null) {
            return this
        }
        setId(group.groupId)
        remark = group.groupName
        avatarurl = group.faceUrl
        isGroup = true
        return this
    }
}