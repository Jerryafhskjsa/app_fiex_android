package com.black.frying.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import cn.jiguang.dy.Protocol.mContext
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.model.socket.PairStatus
import com.black.base.util.ExchangeRatesUtil
import com.black.base.util.StyleChangeUtil
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
        var styleChange = StyleChangeUtil.getStyleChangeSetting(context)?.styleCode
        if (styleChange == 1){
            val color = if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) bgDefault!! else if (pairStatus.priceChangeSinceToday!! > 0 ) bgWin!! else bgLose!!
            viewHolder?.sinceColor?.setBackgroundColor(color)
            viewHolder?.price?.setTextColor(color)
        }
        if (styleChange == 0){
            val color = if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) bgDefault!! else if (pairStatus.priceChangeSinceToday!! < 0 ) bgWin!! else bgLose!!
            viewHolder?.sinceColor?.setBackgroundColor(color)
            viewHolder?.price?.setTextColor(color)
        }
        val exChangeRates = ExchangeRatesUtil.getExchangeRatesSetting(context)?.rateCode
        if (exChangeRates == 0)
        {
            viewHolder?.price?.setText(pairStatus?.currentPriceFormat)
            viewHolder?.cny?.setText(String.format("%sCNY", pairStatus?.currentPriceCNYFormat))
        }
        else{
            viewHolder?.price?.setText(pairStatus?.currentPriceFormat)
            viewHolder?.cny?.setText(String.format("%sUSD", pairStatus?.currentPriceFormat))
        }
        viewHolder?.pairName?.setText(pairStatus?.name?.uppercase())
        viewHolder?.setName?.setText(pairStatus?.setName?.uppercase())
        viewHolder?.volume24?.setText(context.getString(R.string.volumn_24, pairStatus?.tradeAmountFormat
                ?: "0.00"))
        viewHolder?.since?.setText(pairStatus?.priceChangeSinceTodayFormat)
    }

}