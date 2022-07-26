package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.LoanRecord
import com.black.money.R
import com.black.money.databinding.ListItemLoanRecordBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil

class LoanRecordAdapter(context: Context, variableId: Int, data: ArrayList<LoanRecord?>?) : BaseRecycleDataBindAdapter<LoanRecord?, ListItemLoanRecordBinding>(context, variableId, data) {
    private var onLoanRecordHandleListener: OnLoanRecordHandleListener? = null

    override fun getResourceId(): Int {
        return R.layout.list_item_loan_record
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemLoanRecordBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val loanRecord = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.coinType?.setText(String.format("借入 %s %s",
                if (loanRecord?.borrowAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecord.borrowAmount, 9, 0, 8),
                if (loanRecord?.borrowCoinType == null) nullAmount else loanRecord.borrowCoinType))
        viewHolder?.action?.setOnClickListener {
            loanRecord?.let {
                onLoanRecordHandleListener?.onLoanRecordHandle(loanRecord)
            }
        }
        viewHolder?.createDate?.setText(if (loanRecord?.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", loanRecord.createTime!!))
        viewHolder?.interest?.setText(String.format("%s %s",
                if (loanRecord?.interest == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecord.interest, 9, 0, 8),
                if (loanRecord?.borrowCoinType == null) nullAmount else loanRecord.borrowCoinType))
        viewHolder?.riskRate?.setText(String.format("%s%%", if (loanRecord?.riskRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loanRecord.riskRate!! * 100, 2)))
        viewHolder?.lastBackDate?.setText(if (loanRecord?.expireTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", loanRecord.expireTime!!))
        viewHolder?.backDate?.setText(if (loanRecord?.repaymentTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", loanRecord.repaymentTime!!))
        viewHolder?.status?.setText(if (loanRecord == null) nullAmount else loanRecord.getStatusText(context))
    }

    fun setOnLoanRecordHandleListener(onLoanRecordHandleListener: OnLoanRecordHandleListener) {
        this.onLoanRecordHandleListener = onLoanRecordHandleListener
    }

    interface OnLoanRecordHandleListener {
        fun onLoanRecordHandle(loanRecord: LoanRecord)
    }
}