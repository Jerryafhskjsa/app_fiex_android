package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.LoanConfigSub
import com.black.money.R
import com.black.money.databinding.ListItemLoanConfigSubBinding
import com.black.util.NumberUtil

class LoanConfigSubAdapter(context: Context, variableId: Int, data: ArrayList<LoanConfigSub?>?) : BaseRecycleDataBindAdapter<LoanConfigSub?, ListItemLoanConfigSubBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_loan_config_sub
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemLoanConfigSubBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val loanConfigSub = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.subCoinType?.setText(if (loanConfigSub?.borrowCoinType == null) nullAmount else loanConfigSub.borrowCoinType)
        viewHolder?.loanRate?.setText(String.format("%s%%", if (loanConfigSub?.borrowingMortgageScale == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loanConfigSub.borrowingMortgageScale!! * 100, 2)))
        viewHolder?.explodeWarnScale?.setText(String.format("%s%%", if (loanConfigSub?.warnScale == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loanConfigSub.warnScale!! * 100, 2)))
        viewHolder?.explodeScale?.setText(String.format("%s%%", if (loanConfigSub?.burstScale == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loanConfigSub.burstScale!! * 100, 2)))
    }
}