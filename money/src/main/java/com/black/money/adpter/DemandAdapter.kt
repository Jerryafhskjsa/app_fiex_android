package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.Demand
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.money.R
import com.black.money.databinding.ListItemDemandBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil

class DemandAdapter(context: Context, variableId: Int, data: ArrayList<Demand?>?) : BaseRecycleDataBindAdapter<Demand?, ListItemDemandBinding>(context, variableId, data) {
    private var imageLoader: ImageLoader? = null
    private var isVisibility = true

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_demand
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemDemandBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val demand = getItem(position)
        val viewHolder = holder.dataBing
        FryingUtil.setCoinIcon(context, viewHolder?.icon, imageLoader, demand?.coinType)
        viewHolder?.coinType?.setText(if (demand?.coinType == null) nullAmount else demand.coinType)
        val demandRate = if (demand == null) null else CommonUtil.getItemFromList(demand.rateConfDto, 0)
        viewHolder?.rate?.setText(String.format("%s%%", if (demandRate?.rate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(demandRate.rate!! * 100, 2)))

        viewHolder?.totalAmountTitle?.setText(String.format("存入总金额(%s)", if (demand?.coinType == null) nullAmount else demand.coinType))
        viewHolder?.rewardTotalTitle?.setText(String.format("累计收益(%s)", if (demand?.distributionCoinType == null) nullAmount else demand.distributionCoinType))
        if (isVisibility) {
            viewHolder?.totalAmount?.setText(if (demand?.lockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand.lockAmount, 9, 2, 8))
            viewHolder?.rewardTotal?.setText(if (demand?.totalInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demand.totalInterestAmount, 9, 2, 8))
        } else {
            viewHolder?.totalAmount?.setText("****")
            viewHolder?.rewardTotal?.setText("****")
        }
    }

    fun setVisibility(isVisibility: Boolean) {
        this.isVisibility = isVisibility
        notifyDataSetChanged()
    }
}