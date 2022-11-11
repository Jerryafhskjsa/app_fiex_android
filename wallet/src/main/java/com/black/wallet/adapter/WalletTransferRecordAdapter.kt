package com.black.wallet.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.wallet.WalletTransferRecord
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ListItemWalletTransferRecordBinding

class WalletTransferRecordAdapter(context: Context, variableId: Int, data: ArrayList<WalletTransferRecord?>?) : BaseRecycleDataBindAdapter<WalletTransferRecord?, ListItemWalletTransferRecordBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_wallet_transfer_record
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemWalletTransferRecordBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val record = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.coinType?.setText(if (record?.coin == null) nullAmount else record.coin)
        viewHolder?.amount?.setText(if (record?.amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.amount, 9, 0, 8))
        viewHolder?.type?.setText(if (record?.getTypeText(context) == null) nullAmount else record.getTypeText(context))
//        viewHolder?.time?.setText(if (record?.createdTime == null) nullAmount else CommonUtil.formatDate("yyyy/MM/dd HH:mm", record.createdTime!!))
        viewHolder?.time?.setText(if (record?.createdTime == null) nullAmount else record.getDateDes(context))

    }
}