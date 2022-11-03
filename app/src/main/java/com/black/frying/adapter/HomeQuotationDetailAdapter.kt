package com.black.frying.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.model.socket.PairStatus
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemHomeQuotationDetailBinding
import skin.support.content.res.SkinCompatResources

class HomeQuotationDetailAdapter(context: Context, data: MutableList<PairStatus?>?) : BaseDataTypeBindAdapter<PairStatus?, ListItemHomeQuotationDetailBinding>(context, data) {
    private var bgWin: Int? = null
    private var bgLose: Int? = null
    private var bgDefault: Int? = null

    override fun resetSkinResources() {
        super.resetSkinResources()
        bgDefault = SkinCompatResources.getColor(context, R.color.T3)
        bgWin = SkinCompatResources.getColor(context, R.color.T10)
        bgLose = SkinCompatResources.getColor(context, R.color.T9)
    }

    override fun getItemLayoutId(): Int {
        return R.layout.list_item_home_quotation_detail
    }

    override fun bindView(position: Int, holder: ViewHolder<ListItemHomeQuotationDetailBinding>?) {
        val pairStatus = getItem(position)

        pairStatus?.currentPrice = (pairStatus?.currentPrice ?: 0.0)
        pairStatus?.setCurrentPriceCNY(pairStatus.currentPriceCNY, nullAmount)
        pairStatus?.priceChangeSinceToday = (pairStatus?.priceChangeSinceToday)

        val viewHolder = holder?.dataBing
        val color = if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) bgDefault!! else if (pairStatus.priceChangeSinceToday!! > 0) bgWin!! else bgLose!!
        if (pairStatus?.isHighRisk != null && true == pairStatus.isHighRisk) {
            viewHolder?.stView?.visibility = View.VISIBLE
        } else {
            viewHolder?.stView?.visibility = View.GONE
        }
        viewHolder?.pairName?.setText(pairStatus?.name)
        viewHolder?.setName?.setText(pairStatus?.setName)
        viewHolder?.volume24?.setText(context.getString(R.string.volumn_24, pairStatus?.tradeAmountFormat
                ?: "0.00"))
        viewHolder?.price?.setText(pairStatus?.currentPriceFormat)
        viewHolder?.priceCny?.setText(String.format("≈ %s", pairStatus?.currentPriceCNYFormat))
        viewHolder?.since?.setText(pairStatus?.priceChangeSinceTodayFormat)
        viewHolder?.since?.setTextColor(color)
    }

}