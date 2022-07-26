package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.PromotionsBuyFive
import com.black.base.model.money.PromotionsBuyFiveRecord
import com.black.money.R
import com.black.money.databinding.ListItemPromotionsBuyFiveRecordBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil

class PromotionsBuyFiveRecordAdapter(context: Context, variableId: Int, private val promotionsBuy: PromotionsBuyFive, data: ArrayList<PromotionsBuyFiveRecord?>?) : BaseRecycleDataBindAdapter<PromotionsBuyFiveRecord?, ListItemPromotionsBuyFiveRecordBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_promotions_buy_five_record
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemPromotionsBuyFiveRecordBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val record = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.coinType?.setText(getString(R.string.purchase_five_buy, if (promotionsBuy.coinType == null) "" else promotionsBuy.coinType))
        viewHolder?.status?.setText(record!!.getStatusText(context))
        viewHolder?.got?.setText(String.format("%s %s", if (record?.needAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.needAmount, 8, 2, 8), if (promotionsBuy.coinType == null) "" else promotionsBuy.coinType))
        viewHolder?.pay?.setText(String.format("%s %s", if (record?.payAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.payAmount, 8, 2, 8), if (record?.payCoin == null) "" else record.payCoin))
        viewHolder?.price?.setText(String.format("%s %s", if (record?.price == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.price, 8, 2, 8), if (record?.payCoin == null) "" else record.payCoin))
        viewHolder?.realGot?.setText(String.format("%s", if (record?.coinAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.coinAmount, 8, 2, 8)))
        viewHolder?.time?.setText(if (record?.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", record.createTime!!))
        viewHolder?.gotNo?.setText(record?.getGotNoDisplay(context))
    }
}