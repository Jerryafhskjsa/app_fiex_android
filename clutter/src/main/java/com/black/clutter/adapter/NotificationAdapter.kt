package com.black.clutter.adapter

import android.content.Context
import android.util.Log
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.clutter.NoticeHome
import com.black.clutter.R
import com.black.clutter.databinding.ListItemNotificationBinding

class NotificationAdapter(context: Context, variableId: Int, data: ArrayList<NoticeHome.NoticeHomeItem?>?) : BaseRecycleDataBindAdapter<NoticeHome.NoticeHomeItem?, ListItemNotificationBinding>(context, variableId, data) {

    override fun getResourceId(): Int {
        return R.layout.list_item_notification
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemNotificationBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val noticeHome = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.title?.setText(if (noticeHome?.title == null) "" else noticeHome.title)
        viewHolder?.time?.setText(if (noticeHome?.created_at == null) "" else noticeHome.created_at)
        viewHolder?.contentDes?.setText(if (noticeHome?.body == null) "" else noticeHome.body)
    }

}