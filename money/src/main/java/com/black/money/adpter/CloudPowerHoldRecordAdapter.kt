package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.CloudPowerHoldRecord
import com.black.money.R
import com.black.money.databinding.ListItemCloudPowerHoldRecordBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources

class CloudPowerHoldRecordAdapter(context: Context, variableId: Int, data: ArrayList<CloudPowerHoldRecord?>?) : BaseRecycleDataBindAdapter<CloudPowerHoldRecord?, ListItemCloudPowerHoldRecordBinding>(context, variableId, data) {
    private var colorC1 = 0
    private var colorT5: Int = 0
    private var colorT2: Int = 0

    override fun resetSkinResources() {
        super.resetSkinResources()
        colorC1 = SkinCompatResources.getColor(context, R.color.C1)
        colorT5 = SkinCompatResources.getColor(context, R.color.T5)
        colorT2 = SkinCompatResources.getColor(context, R.color.T2)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_cloud_power_hold_record
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemCloudPowerHoldRecordBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val record = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.coinType?.setText(String.format("云算力代币 %s", if (record?.coinType == null) nullAmount else record.coinType))
        viewHolder?.power?.setText(String.format("1 %s = 1T算力", if (record?.coinType == null) nullAmount else record.coinType))
        val status = record?.statusCode ?: 0
        viewHolder?.status?.setText(if (record == null) nullAmount else record.getStatusText(context))
        when (status) {
            3 -> viewHolder?.status?.setTextColor(colorC1)
            4 -> viewHolder?.status?.setTextColor(colorT5)
            else -> viewHolder?.status?.setTextColor(colorT2)
        }
        viewHolder?.amount?.setText(String.format("%s %s",
                if (record?.amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.amount, 9, 0, 8),
                if (record?.coinType == null) nullAmount else record.coinType))
        viewHolder?.totalReward?.setText(String.format("%s %s",
                if (record?.totalInterest == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.totalInterest, 9, 0, 8),
                if (record?.coinType == null) nullAmount else record.coinType))
        viewHolder?.endTime?.setText(if (record?.endTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", record.endTime!!))
        viewHolder?.cycle?.setText(String.format("%s天", if (record?.day == null) nullAmount else NumberUtil.formatNumberNoGroup(record.day)))
    }

}