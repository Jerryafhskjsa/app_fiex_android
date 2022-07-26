package com.black.community.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.community.FactionMember
import com.black.base.util.ImageLoader
import com.black.base.util.UrlConfig
import com.black.community.R
import com.black.community.databinding.ListItemFactionMemberBinding

class FactionMemberListAdapter(context: Context, variableId: Int, data: ArrayList<FactionMember?>?) : BaseRecycleDataBindAdapter<FactionMember?, ListItemFactionMemberBinding>(context, variableId, data) {
    private var imageLoader: ImageLoader? = null
    private var ownerAvatarUrl: String? = null
    private var memberAvatarUrl: String? = null

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_faction_member
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemFactionMemberBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val member = getItem(position)
        val viewHolder = holder.dataBing
        if (member?.type != null && member.type == 1) {
            if (ownerAvatarUrl != null) {
                imageLoader?.loadImage(viewHolder?.icon, UrlConfig.getHost(context) + ownerAvatarUrl)
            }
        } else {
            if (memberAvatarUrl != null) {
                imageLoader?.loadImage(viewHolder?.icon, UrlConfig.getHost(context) + memberAvatarUrl)
            }
        }
        viewHolder?.userName?.setText(if (member?.userName == null) nullAmount else member.userName)
        viewHolder?.uid?.setText(String.format("UID:%s", if (member?.userId == null) nullAmount else member.userId))
    }

    fun setMemberAvatarUrl(memberAvatarUrl: String?) {
        this.memberAvatarUrl = memberAvatarUrl
    }

    fun setOwnerAvatarUrl(ownerAvatarUrl: String?) {
        this.ownerAvatarUrl = ownerAvatarUrl
    }

}