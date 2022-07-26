package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.DemandRecord
import com.black.money.R
import com.black.money.databinding.ListItemDemandRecordBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil

class DemandRecordAdapter(context: Context, variableId: Int, data: ArrayList<DemandRecord?>?) : BaseRecycleDataBindAdapter<DemandRecord?, ListItemDemandRecordBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_demand_record
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemDemandRecordBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val record = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.coinType?.setText(String.format("收益%s", if (record?.coinType == null) nullAmount else record.coinType))
        viewHolder?.status?.setText("已完成")
        viewHolder?.time?.setText(if (record?.date == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", record.date!!))
        viewHolder?.amount?.setText(if (record?.amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.amount, 15, 0, 12))
    }
}