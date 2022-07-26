package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.Regular
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.money.R
import com.black.money.databinding.ListItemRegularBinding
import com.black.util.NumberUtil

class RegularAdapter(context: Context, variableId: Int, data: ArrayList<Regular?>?) : BaseRecycleDataBindAdapter<Regular?, ListItemRegularBinding>(context, variableId, data) {
    private var imageLoader: ImageLoader? = null
    private var isVisibility = true

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_regular
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemRegularBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val regular = getItem(position)
        val viewHolder = holder.dataBing
        FryingUtil.setCoinIcon(context, viewHolder?.icon, imageLoader, regular?.coinType)
        viewHolder?.coinType?.setText(if (regular?.coinType == null) nullAmount else regular.coinType)
        viewHolder?.rate?.setText(String.format("%s%%", if (regular?.annualrate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(regular.annualrate!! * 100, 2)))
        viewHolder?.lockDay?.setText(String.format("%s天", if (regular?.day == null) nullAmount else NumberUtil.formatNumberNoGroup(regular.day)))
        if (isVisibility) {
            viewHolder?.totalAmount?.setText(if (regular?.sumLockAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regular.sumLockAmount, 9, 2, 8))
            viewHolder?.rewardTotal?.setText(if (regular?.totalInterestAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regular.totalInterestAmount, 9, 2, 8))
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