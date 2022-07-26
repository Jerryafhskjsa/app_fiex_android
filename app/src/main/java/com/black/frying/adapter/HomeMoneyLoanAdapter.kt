package com.black.frying.adapter

import android.content.Context
import android.text.TextUtils
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.MoneyHomeConfigLoan
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemLoanHomeBinding

class HomeMoneyLoanAdapter(context: Context, variableId: Int, data: ArrayList<MoneyHomeConfigLoan?>?) : BaseRecycleDataBindAdapter<MoneyHomeConfigLoan?, ListItemLoanHomeBinding>(context, variableId, data) {
    private var imageLoader: ImageLoader? = null

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_loan_home
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemLoanHomeBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val loan = getItem(position)
        val viewHolder = holder.dataBing
        FryingUtil.setCoinIcon(context, viewHolder?.icon, imageLoader, loan?.mortgageCoinType)
        viewHolder?.coinType?.setText(String.format("抵押%s 借%s", if (loan?.mortgageCoinType == null) nullAmount else loan.mortgageCoinType, if (loan == null || loan.borrowCoinType == null) nullAmount else loan.borrowCoinType))
        val minRate = String.format("%s%%", if (loan?.minRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loan.minRate!! * 100, 2))
        val maxRate = String.format("%s%%", if (loan?.maxRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loan.maxRate!! * 100, 2))
        val rate = if (TextUtils.equals(minRate, maxRate)) minRate else if (TextUtils.equals(minRate, nullAmount)) maxRate else if (TextUtils.equals(maxRate, nullAmount)) minRate else String.format("%s - %s", minRate, maxRate)
        viewHolder?.rate?.setText(rate)
    }
}