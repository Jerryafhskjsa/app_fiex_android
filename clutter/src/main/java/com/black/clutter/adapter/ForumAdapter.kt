package com.black.clutter.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.clutter.Forum
import com.black.base.util.ImageLoader
import com.black.clutter.R
import com.black.clutter.databinding.ListItemForumBinding

class ForumAdapter(context: Context, variableId: Int, data: ArrayList<Forum?>?) : BaseRecycleDataBindAdapter<Forum?, ListItemForumBinding>(context, variableId, data) {
    private var imageLoader: ImageLoader? = null

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_forum
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemForumBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val forum = getItem(position)
        val viewHolder = holder.dataBing
        imageLoader?.loadImage(viewHolder?.icon, forum?.iconUrl)
        viewHolder?.title?.setText(if (forum?.channelName == null) "" else forum.channelName)
        viewHolder?.number?.setText(if (forum?.channelAccount == null) "" else forum.channelAccount)
    }
}