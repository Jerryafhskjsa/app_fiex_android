package com.black.frying.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.MoneyHomeConfigCloud
import com.black.base.util.FryingUtil
import com.black.base.util.ImageLoader
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemCloudPowerHomeBinding

class HomeMoneyCloudAdapter(context: Context, variableId: Int, data: ArrayList<MoneyHomeConfigCloud?>?) : BaseRecycleDataBindAdapter<MoneyHomeConfigCloud?, ListItemCloudPowerHomeBinding>(context, variableId, data) {
    private var imageLoader: ImageLoader? = null

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_cloud_power_home
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemCloudPowerHomeBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val cloud = getItem(position)
        val viewHolder = holder.dataBing
        FryingUtil.setCoinIcon(context, viewHolder?.icon, imageLoader, cloud?.distributionCoinType)
        viewHolder?.coinType?.setText(if (cloud?.distributionCoinType == null) nullAmount else cloud.distributionCoinType)
        viewHolder?.powerCoin?.setText(String.format("%s 云挖矿", if (cloud?.interestCoinType == null) nullAmount else cloud.interestCoinType))
        viewHolder?.power?.setText(String.format("1%s = 1T算力", if (cloud?.distributionCoinType == null) nullAmount else cloud.distributionCoinType))
        viewHolder?.powerFee?.setText(String.format("%s %s/T/天",
                if (cloud?.price == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(cloud.price, 9, 0, 8),
                if (cloud?.coinType == null) nullAmount else cloud.coinType))
        viewHolder?.interest?.setText(String.format("%s %s/T/天",
                if (cloud?.expectedInterest == null) "0" else NumberUtil.formatNumberDynamicScaleNoGroup(cloud.expectedInterest, 9, 0, 8),
                "USDT"))

        viewHolder?.powerTotal?.setText(String.format("%sTH/S", if (cloud?.totalFinancing == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(cloud.totalFinancing, 9, 0, 8)))
        viewHolder?.cycle?.setText(String.format("%s天", if (cloud?.day == null) nullAmount else NumberUtil.formatNumberNoGroup(cloud.day)))
        viewHolder?.startTime?.setText(if (cloud?.buyStartTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", cloud.buyStartTime!!))
        viewHolder?.endTime?.setText(if (cloud?.buyEndTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", cloud.buyEndTime!!))

    }

}