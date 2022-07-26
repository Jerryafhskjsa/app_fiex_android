package com.black.im.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.black.im.R
import com.black.im.imageEngine.impl.GlideEngine
import com.black.im.interfaces.IGroupMemberRouter
import com.black.im.model.group.GroupInfo
import com.black.im.model.group.GroupMemberInfo
import com.black.im.util.BackgroundTasks
import com.black.im.util.TUIKit.appContext
import com.black.im.util.TUIKitConstants
import com.tencent.imsdk.TIMGroupMemberRoleType
import com.tencent.imsdk.TIMManager
import java.util.*

class GroupInfoAdapter : BaseAdapter() {
    companion object {
        private const val ADD_TYPE = -100
        private const val DEL_TYPE = -101
        private const val OWNER_PRIVATE_MAX_LIMIT = 10 //讨论组,owner可以添加成员和删除成员，
        private const val OWNER_PUBLIC_MAX_LIMIT = 11 //公开群,owner不可以添加成员，但是可以删除成员
        private const val OWNER_CHATROOM_MAX_LIMIT = 11 //聊天室,owner不可以添加成员，但是可以删除成员
        private const val NORMAL_PRIVATE_MAX_LIMIT = 11 //讨论组,普通人可以添加成员
        private const val NORMAL_PUBLIC_MAX_LIMIT = 12 //公开群,普通人没有权限添加成员和删除成员
        private const val NORMAL_CHATROOM_MAX_LIMIT = 12 //聊天室,普通人没有权限添加成员和删除成员
    }

    private val mGroupMembers: MutableList<GroupMemberInfo> = ArrayList()
    private var mTailListener: IGroupMemberRouter? = null
    private var mGroupInfo: GroupInfo? = null
    fun setManagerCallBack(listener: IGroupMemberRouter?) {
        mTailListener = listener
    }

    override fun getCount(): Int {
        return mGroupMembers.size
    }

    override fun getItem(i: Int): GroupMemberInfo {
        return mGroupMembers[i]
    }

    override fun getItemId(i: Int): Long {
        return 0
    }

    override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View? {
        var view1 = view
        val holder: MyViewHolder
        if (view1 == null) {
            view1 = LayoutInflater.from(appContext).inflate(R.layout.group_member_adpater, viewGroup, false)
            holder = MyViewHolder()
            holder.memberIcon = view1.findViewById(R.id.group_member_icon)
            holder.memberName = view1.findViewById(R.id.group_member_name)
            view1.tag = holder
        } else {
            holder = view1.tag as MyViewHolder
        }
        val info = getItem(i)
        if (!TextUtils.isEmpty(info.iconUrl)) {
            GlideEngine.loadImage(holder.memberIcon, info.iconUrl, null)
        }
        if (!TextUtils.isEmpty(info.account)) {
            holder.memberName!!.text = info.account
        } else {
            holder.memberName!!.text = ""
        }
        view1?.setOnClickListener(null)
        holder.memberIcon!!.background = null
        if (info.memberType == ADD_TYPE) {
            holder.memberIcon?.setImageResource(R.drawable.add_group_member)
            holder.memberIcon?.setBackgroundResource(R.drawable.bottom_action_border)
            view1?.setOnClickListener {
                if (mTailListener != null) {
                    mTailListener!!.forwardAddMember(mGroupInfo)
                }
            }
        } else if (info.memberType == DEL_TYPE) {
            holder.memberIcon?.setImageResource(R.drawable.del_group_member)
            holder.memberIcon?.setBackgroundResource(R.drawable.bottom_action_border)
            view1?.setOnClickListener {
                if (mTailListener != null) {
                    mTailListener!!.forwardDeleteMember(mGroupInfo)
                }
            }
        }
        return view1
    }

    fun setDataSource(info: GroupInfo) {
        mGroupInfo = info
        mGroupMembers.clear()
        val members: List<GroupMemberInfo>? = info.memberDetails
        if (members != null) {
            var shootMemberCount = 0
            // 公开群/聊天室 只有APP管理员可以邀请他人入群
            when {
                TextUtils.equals(info.groupType, TUIKitConstants.GroupType.TYPE_PRIVATE) -> {
                    shootMemberCount = if (info.isOwner()) {
                        if (members.size > OWNER_PRIVATE_MAX_LIMIT) OWNER_PRIVATE_MAX_LIMIT else members.size
                    } else {
                        if (members.size > NORMAL_PRIVATE_MAX_LIMIT) NORMAL_PRIVATE_MAX_LIMIT else members.size
                    }
                }
                TextUtils.equals(info.groupType, TUIKitConstants.GroupType.TYPE_PUBLIC) -> {
                    shootMemberCount = if (info.isOwner()) {
                        if (members.size > OWNER_PUBLIC_MAX_LIMIT) OWNER_PUBLIC_MAX_LIMIT else members.size
                    } else {
                        if (members.size > NORMAL_PUBLIC_MAX_LIMIT) NORMAL_PUBLIC_MAX_LIMIT else members.size
                    }
                }
                TextUtils.equals(info.groupType, TUIKitConstants.GroupType.TYPE_CHAT_ROOM) -> {
                    shootMemberCount = if (info.isOwner()) {
                        if (members.size > OWNER_CHATROOM_MAX_LIMIT) OWNER_CHATROOM_MAX_LIMIT else members.size
                    } else {
                        if (members.size > NORMAL_CHATROOM_MAX_LIMIT) NORMAL_CHATROOM_MAX_LIMIT else members.size
                    }
                }
            }
            for (i in 0 until shootMemberCount) {
                mGroupMembers.add(members[i])
            }
            if (TextUtils.equals(info.groupType, TUIKitConstants.GroupType.TYPE_PRIVATE)) {
                // 公开群/聊天室 只有APP管理员可以邀请他人入群
                val add = GroupMemberInfo()
                add.memberType = ADD_TYPE
                mGroupMembers.add(add)
            }
            var self: GroupMemberInfo? = null
            for (i in mGroupMembers.indices) {
                val memberInfo = mGroupMembers[i]
                if (TextUtils.equals(memberInfo.account, TIMManager.getInstance().loginUser)) {
                    self = memberInfo
                    break
                }
            }
            if (info.isOwner() || self != null && self.memberType == TIMGroupMemberRoleType.ROLE_TYPE_ADMIN) {
                val del = GroupMemberInfo()
                del.memberType = DEL_TYPE
                mGroupMembers.add(del)
            }
            BackgroundTasks.instance?.runOnUiThread(Runnable { notifyDataSetChanged() })
        }
    }

    private inner class MyViewHolder {
        var memberIcon: ImageView? = null
        var memberName: TextView? = null
    }
}