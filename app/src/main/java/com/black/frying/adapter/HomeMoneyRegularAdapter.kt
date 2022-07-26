package com.black.frying.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.MoneyHomeConfigRegular
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemRegularHomeBinding

class HomeMoneyRegularAdapter(context: Context, variableId: Int, data: ArrayList<MoneyHomeConfigRegular?>?) : BaseRecycleDataBindAdapter<MoneyHomeConfigRegular?, ListItemRegularHomeBinding>(context, variableId, data) {
    private var imageLoader: ImageLoader? = null

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_regular_home
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemRegularHomeBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val regular = getItem(position)
        val viewHolder = holder.dataBing
        FryingUtil.setCoinIcon(context, viewHolder?.icon, imageLoader, regular?.coinType)
        viewHolder?.coinType?.setText(if (regular?.coinType == null) nullAmount else regular.coinType)
        viewHolder?.rate?.setText(String.format("%s%%", if (regular?.annualrate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(regular.annualrate!! * 100, 2)))
    }

}