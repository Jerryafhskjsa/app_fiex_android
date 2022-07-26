package com.black.frying.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.model.socket.PairStatus
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemHomeQuotationDetailBinding
import skin.support.content.res.SkinCompatResources

class HomeQuotationDetailAdapter(context: Context, data: MutableList<PairStatus?>?) : BaseDataTypeBindAdapter<PairStatus?, ListItemHomeQuotationDetailBinding>(context, data) {
    private var bgWin: Drawable? = null
    private var bgLose: Drawable? = null
    private var bgDefault: Drawable? = null

    override fun resetSkinResources() {
        super.resetSkinResources()
        bgDefault = SkinCompatResources.getDrawable(context, R.drawable.bg_t3_corner3)
        bgWin = SkinCompatResources.getDrawable(context, R.drawable.bg_t7_corner3)
        bgLose = SkinCompatResources.getDrawable(context, R.drawable.bg_t5_corner3)
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
        val bg = if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) bgDefault!! else if (pairStatus.priceChangeSinceToday!! > 0) bgWin!! else bgLose!!
        if (pairStatus?.isHighRisk != null && true == pairStatus.isHighRisk) {
            viewHolder?.stView?.visibility = View.VISIBLE
        } else {
            viewHolder?.stView?.visibility = View.GONE
        }
        viewHolder?.pairName?.setText(pairStatus?.name)
        viewHolder?.setName?.setText(pairStatus?.setName)
        viewHolder?.volume24?.setText(context.getString(R.string.volumn_24, pairStatus?.totalAmountFromat
                ?: "0.00"))
        viewHolder?.price?.setText(pairStatus?.currentPriceFormat)
        viewHolder?.priceCny?.setText(String.format("¥ %s", pairStatus?.currentPriceCNYFormat))
        viewHolder?.since?.setText(pairStatus?.priceChangeSinceTodayFormat)
        viewHolder?.since?.background = bg
    }

}