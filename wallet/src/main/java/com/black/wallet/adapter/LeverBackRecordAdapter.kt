package com.black.wallet.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.wallet.LeverBorrowRecord
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ListItemLeverBackBinding

class LeverBackRecordAdapter(context: Context, variableId: Int, data: ArrayList<LeverBorrowRecord?>?) : BaseRecycleDataBindAdapter<LeverBorrowRecord?, ListItemLeverBackBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_lever_back
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemLeverBackBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val record = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.coinType?.setText(if (record?.coinType == null) nullAmount else record.coinType)
        viewHolder?.amount?.setText(if (record?.amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.amount, 9, 0, 8))
        viewHolder?.type?.setText(if (record?.operationType == null) nullAmount else record.getTypeText(context))
        viewHolder?.time?.setText(if (record?.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", record.createTime!!))
    }
}