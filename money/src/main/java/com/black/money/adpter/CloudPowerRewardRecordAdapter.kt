package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.CloudPowerRewardRecord
import com.black.money.R
import com.black.money.databinding.ListItemCloudPowerRewardRecordBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil

class CloudPowerRewardRecordAdapter(context: Context, variableId: Int, data: ArrayList<CloudPowerRewardRecord?>?) : BaseRecycleDataBindAdapter<CloudPowerRewardRecord?, ListItemCloudPowerRewardRecordBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_cloud_power_reward_record
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemCloudPowerRewardRecordBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val record = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.amount?.setText(String.format("+ %s %s",
                if (record?.amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.amount, 9, 0, 8),
                if (record?.coinType == null) nullAmount else record.coinType))
        viewHolder?.power?.setText(String.format("%s %s",
                if (record?.holdAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.holdAmount, 9, 0, 8),
                if (record?.holdCoinType == null) nullAmount else record.holdCoinType))
        viewHolder?.date?.setText(if (record?.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", record.createTime!!))

    }
}