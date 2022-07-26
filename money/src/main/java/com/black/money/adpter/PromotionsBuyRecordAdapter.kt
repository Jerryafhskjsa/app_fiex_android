package com.black.money.adpter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.PromotionsBuy
import com.black.base.model.money.PromotionsBuyRecord
import com.black.money.R
import com.black.money.databinding.ListItemPromotionsBuyRecordBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil

class PromotionsBuyRecordAdapter(context: Context, variableId: Int, private val promotionsBuy: PromotionsBuy, data: ArrayList<PromotionsBuyRecord?>?) : BaseRecycleDataBindAdapter<PromotionsBuyRecord?, ListItemPromotionsBuyRecordBinding>(context, variableId, data) {

    override fun getResourceId(): Int {
        return R.layout.list_item_promotions_buy_record
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemPromotionsBuyRecordBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val record = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.coinType?.setText(getString(R.string.promotions_buy2, if (promotionsBuy.coinType == null) "" else promotionsBuy.coinType))
        viewHolder?.status?.setText(record?.getStatusText(context))
        viewHolder?.got?.setText(String.format("%s %s", if (record?.needAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.needAmount, 8, 2, 8), if (promotionsBuy.coinType == null) "" else promotionsBuy.coinType))
        viewHolder?.pay?.setText(String.format("%s %s", if (record?.payAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.payAmount, 8, 2, 8), if (record?.payCoin == null) "" else record.payCoin))
        viewHolder?.price?.setText(String.format("%s %s", if (record?.price == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.price, 8, 2, 8), if (record?.payCoin == null) "" else record.payCoin))
        viewHolder?.realGot?.setText(String.format("%s %s", if (record?.coinAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(record.coinAmount, 8, 2, 8), if (promotionsBuy.coinType == null) "" else promotionsBuy.coinType))
        viewHolder?.time?.setText(if (record?.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", record.createTime!!))
    }
}