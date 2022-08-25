package com.black.frying.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.socket.TradeOrder
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.QuotationDetalAdapterLayoutBinding
import skin.support.content.res.SkinCompatResources

class QuotationDetailDealAdapter(context: Context, variableId: Int, data: MutableList<TradeOrder?>?) : BaseRecycleDataBindAdapter<TradeOrder?, QuotationDetalAdapterLayoutBinding>(context, variableId, data) {
    private var colorC2 = 0
    private var colorC3 = 0
    private var amountLength = 0

    override fun resetSkinResources() {
        super.resetSkinResources()
        colorC2 = SkinCompatResources.getColor(context, R.color.T7)
        colorC3 = SkinCompatResources.getColor(context, R.color.T5)
    }

    fun setAmountLength(amountLength: Int) {
        this.amountLength = amountLength
    }

    override fun onBindViewHolder(holder: BaseViewHolder<QuotationDetalAdapterLayoutBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val dealOrder = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.timeView?.setText(if (dealOrder?.createdTime == null) nullAmount else CommonUtil.formatTimestamp("HH:mm:ss",
            dealOrder.createdTime!!
        ))
        if ("B".equals(dealOrder?.tradeDealDirection, ignoreCase = true) || "BID".equals(dealOrder?.tradeDealDirection, ignoreCase = true)) {
            viewHolder?.directionView?.setText(R.string.k_buy)
            viewHolder?.directionView?.setTextColor(colorC2)
            viewHolder?.dealPriceView?.setTextColor(colorC2)
        } else if ("S".equals(dealOrder?.tradeDealDirection, ignoreCase = true) || "ASK".equals(dealOrder?.tradeDealDirection, ignoreCase = true)) {
            viewHolder?.directionView?.setText(R.string.k_sale)
            viewHolder?.directionView?.setTextColor(colorC3)
            viewHolder?.dealPriceView?.setTextColor(colorC3)
        }
        viewHolder?.dealPriceView?.setText(dealOrder?.formattedPrice)
        viewHolder?.dealAmountView?.setText(NumberUtil.formatNumberNoGroup(dealOrder?.dealAmount, amountLength, amountLength))
    }

    override fun getResourceId(): Int {
        return R.layout.quotation_detal_adapter_layout
    }
}