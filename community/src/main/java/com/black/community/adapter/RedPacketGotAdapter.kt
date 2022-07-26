package com.black.community.adapter

import android.content.Context
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.community.RedPacketGotRecord
import com.black.base.util.ImageLoader
import com.black.community.R
import com.black.community.databinding.ListItemRedPacketGotBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil

class RedPacketGotAdapter(context: Context, variableId: Int, data: ArrayList<RedPacketGotRecord?>?) : BaseRecycleDataBindAdapter<RedPacketGotRecord?, ListItemRedPacketGotBinding>(context, variableId, data) {
    private var imageLoader: ImageLoader? = null

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_red_packet_got
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemRedPacketGotBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val record = getItem(position)
        val viewHolder = holder.dataBing
        imageLoader?.loadImage(viewHolder?.avatar, record?.avatar)

        viewHolder?.name?.setText(if (record?.userName == null) nullAmount else record.userName)
        viewHolder?.amount?.setText(String.format("%s %s",
                if (record?.amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.amount, 9, 0, 8),
                if (record?.coinType == null) nullAmount else record.coinType))
        viewHolder?.gotTime?.setText(if (record?.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", record.createTime!!))

        if (record?.luckiest != null && true == record.luckiest) {
            viewHolder?.best?.visibility = View.VISIBLE
        } else {
            viewHolder?.best?.visibility = View.GONE
        }
    }
}