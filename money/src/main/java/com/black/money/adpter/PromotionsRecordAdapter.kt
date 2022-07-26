package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.PromotionsRecord
import com.black.money.R
import com.black.money.databinding.ListItemPromotionsRecordBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil

class PromotionsRecordAdapter(context: Context, variableId: Int, data: ArrayList<PromotionsRecord?>?) : BaseRecycleDataBindAdapter<PromotionsRecord?, ListItemPromotionsRecordBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_promotions_record
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemPromotionsRecordBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val record = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.coinType?.setText(getString(R.string.promotions_buy, if (record?.distributionCoinType == null) "" else record.distributionCoinType))
        viewHolder?.status?.setText(record?.getStatusText(context))
        viewHolder?.pay?.setText(String.format("%s %s", if (record?.amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.amount, 8, 0, 4), if (record?.coinType == null) "" else record.coinType))
        viewHolder?.got?.setText(String.format("%s %s", if (record?.distributionAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.distributionAmount, 8, 0, 4), if (record?.distributionCoinType == null) "" else record.distributionCoinType))
        viewHolder?.price?.setText(String.format("%s %s", if (record?.price == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.price, 8, 0, 4), if (record?.coinType == null) "" else record.coinType))
        viewHolder?.time?.setText(if (record?.createdTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", record.createdTime!!))
    }
}