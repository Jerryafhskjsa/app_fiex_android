package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.CloudPowerBuyRecord
import com.black.money.R
import com.black.money.databinding.ListItemCloudPowerBuyRecordBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil

class CloudPowerBuyRecordAdapter(context: Context, variableId: Int, data: ArrayList<CloudPowerBuyRecord?>?) : BaseRecycleDataBindAdapter<CloudPowerBuyRecord?, ListItemCloudPowerBuyRecordBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_cloud_power_buy_record
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemCloudPowerBuyRecordBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val record = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.coinType?.setText(String.format("云算力代币 %s", if (record?.distributionCoinType == null) nullAmount else record.distributionCoinType))
        viewHolder?.power?.setText(String.format("1 %s = 1T算力", if (record?.distributionCoinType == null) nullAmount else record.distributionCoinType))
        viewHolder?.amount?.setText(String.format("%s %s",
                if (record?.distributionCoinAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.distributionCoinAmount, 9, 0, 8),
                if (record?.distributionCoinType == null) nullAmount else record.distributionCoinType))
        viewHolder?.orderMoney?.setText(String.format("%s %s",
                if (record?.payAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.payAmount, 9, 0, 8),
                if (record?.payCoinType == null) nullAmount else record.payCoinType))

        viewHolder?.mining?.setText(if (record?.miningStartTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", record.miningStartTime!!))
        viewHolder?.cycle?.setText(String.format("%s天", if (record?.day == null) nullAmount else NumberUtil.formatNumberNoGroup(record.day)))
        viewHolder?.orderNumber?.setText(if (record?.id == null) nullAmount else record.id)
        viewHolder?.date?.setText(if (record?.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", record.createTime!!))
    }
}