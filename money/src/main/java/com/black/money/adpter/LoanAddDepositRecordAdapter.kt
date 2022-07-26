package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.LoanAddDepositRecord
import com.black.money.R
import com.black.money.databinding.ListItemLoanAddDepositRecordBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil

class LoanAddDepositRecordAdapter(context: Context, variableId: Int, data: ArrayList<LoanAddDepositRecord?>?) : BaseRecycleDataBindAdapter<LoanAddDepositRecord?, ListItemLoanAddDepositRecordBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_loan_add_deposit_record
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemLoanAddDepositRecordBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val loanRecord = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.coinType?.setText(String.format("追加 %s %s",
                if (loanRecord?.mortgageAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecord.mortgageAmount, 9, 0, 8),
                if (loanRecord?.coinType == null) nullAmount else loanRecord.coinType))
        viewHolder?.createDate?.setText(if (loanRecord?.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", loanRecord.createTime!!))
        viewHolder?.amountAfterAdd?.setText(String.format("%s %s",
                if (loanRecord?.amount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecord.amount, 9, 0, 8),
                if (loanRecord?.coinType == null) nullAmount else loanRecord.coinType))
    }
}