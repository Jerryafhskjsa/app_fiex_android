package com.black.im.adapter

import android.content.Context
import android.text.TextUtils
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.util.ImageLoader
import com.black.im.R
import com.black.im.databinding.ListItemGroupInfoMemberSimpleBinding
import com.black.im.model.group.GroupMemberInfo
import com.tencent.imsdk.TIMFriendshipManager

class GroupInfoMemberSimpleAdapter(context: Context, variableId: Int, data: MutableList<GroupMemberInfo>?) : BaseRecycleDataBindAdapter<GroupMemberInfo, ListItemGroupInfoMemberSimpleBinding>(context, variableId, data) {
    private var imageLoader: ImageLoader? = null

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_group_info_member_simple
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemGroupInfoMemberSimpleBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val memberInfo = getItem(position)?.detail
        val viewHolder = holder.dataBing
        val nameDefault = "FBSexer"
        if (memberInfo != null) {
            val profile = TIMFriendshipManager.getInstance().queryUserProfile(memberInfo.user)
            if (profile == null) {
                viewHolder?.name?.setText(nameDefault)
                imageLoader?.loadImage(viewHolder?.avatar, null, R.drawable.icon_avatar)
            } else {
                if (TextUtils.isEmpty(memberInfo.nameCard)) {
                    viewHolder?.name?.setText(if (!TextUtils.isEmpty(profile.nickName)) profile.nickName else if (!TextUtils.isEmpty(nameDefault)) nameDefault else memberInfo.user)
                } else {
                    viewHolder?.name?.setText(memberInfo.nameCard)
                }
                if (!TextUtils.isEmpty(profile.faceUrl)) {
                    imageLoader?.loadImage(viewHolder?.avatar, profile.faceUrl)
                } else {
                    imageLoader?.loadImage(viewHolder?.avatar, null, R.drawable.icon_avatar)
                }
            }
        } else {
            viewHolder?.name?.setText(nameDefault)
            imageLoader?.loadImage(viewHolder?.avatar, null, R.drawable.icon_avatar)
        }
    }
}