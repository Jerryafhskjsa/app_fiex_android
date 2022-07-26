package com.black.frying.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.MoneyHomeConfigDemand
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemDemandHomeBinding

class HomeMoneyDemandAdapter(context: Context, variableId: Int, data: ArrayList<MoneyHomeConfigDemand?>?) : BaseRecycleDataBindAdapter<MoneyHomeConfigDemand?, ListItemDemandHomeBinding>(context, variableId, data) {
    private var imageLoader: ImageLoader? = null

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_demand_home
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemDemandHomeBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val demand = getItem(position)
        val viewHolder= holder.dataBing
        FryingUtil.setCoinIcon(context, viewHolder?.icon, imageLoader, demand?.coinType)
        viewHolder?.coinType?.setText(if (demand?.coinType == null) nullAmount else demand.coinType)
        viewHolder?.rate?.setText(String.format("%s%%", if (demand?.rate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(demand.rate!! * 100, 2)))

    }
}