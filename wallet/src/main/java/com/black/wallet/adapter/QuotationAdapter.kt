package com.black.wallet.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import cn.jiguang.dy.Protocol.mContext
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.model.socket.PairStatus
import com.black.base.util.ExchangeRatesUtil
import com.black.base.util.StyleChangeUtil
import com.black.wallet.R
import com.black.wallet.databinding.QuotationDetailBinding
import skin.support.content.res.SkinCompatResources

class QuotationAdapter(context: Context, data: MutableList<PairStatus?>?) : BaseDataTypeBindAdapter<PairStatus?, QuotationDetailBinding>(context, data) {
    private var bgWin: Int? = null
    private var bgLose: Int? = null
    private var bgDefault: Int? = null

    override fun resetSkinResources() {
        super.resetSkinResources()
        bgDefault = SkinCompatResources.getColor(context, com.black.wallet.R.color.T13)
        bgWin = SkinCompatResources.getColor(context, com.black.wallet.R.color.T10)
        bgLose = SkinCompatResources.getColor(context, com.black.wallet.R.color.T9)
    }

    override fun getItemLayoutId(): Int {
        return R.layout.quotation_detail
    }

    override fun bindView(position: Int, holder: ViewHolder<QuotationDetailBinding>?) {
        val pairStatus = getItem(position)

        pairStatus?.currentPrice = (pairStatus?.currentPrice ?: 0.0)
        pairStatus?.setCurrentPriceCNY(pairStatus.currentPriceCNY, nullAmount)
        pairStatus?.priceChangeSinceToday = (pairStatus?.priceChangeSinceToday)

        val viewHolder = holder?.dataBing
        var styleChange = StyleChangeUtil.getStyleChangeSetting(context)?.styleCode
        if (styleChange == 1){
            val color = if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) bgDefault!! else if (pairStatus.priceChangeSinceToday!! > 0 ) bgWin!! else bgLose!!
            viewHolder?.since?.setTextColor(color)
        }
        if (styleChange == 0){
            val color = if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) bgDefault!! else if (pairStatus.priceChangeSinceToday!! < 0 ) bgWin!! else bgLose!!
            viewHolder?.since?.setTextColor(color)
        }
        if (pairStatus?.isHighRisk != null && true == pairStatus.isHighRisk) {
            viewHolder?.stView?.visibility = View.VISIBLE
        } else {
            viewHolder?.stView?.visibility = View.GONE
        }
        val exChangeRates = ExchangeRatesUtil.getExchangeRatesSetting(context)?.rateCode
        if (exChangeRates == 0)
        {
            viewHolder?.price?.setText(pairStatus?.currentPriceFormat)
            viewHolder?.priceCny?.setText(String.format("≈ %sCNY", pairStatus?.currentPriceCNYFormat))
        }
        else{
            viewHolder?.price?.setText(pairStatus?.currentPriceFormat)
            viewHolder?.priceCny?.setText(String.format("≈ %sUSD", pairStatus?.currentPriceFormat))
        }
        viewHolder?.pairName?.setText(pairStatus?.name)
        viewHolder?.setName?.setText(pairStatus?.setName)
        viewHolder?.volume24?.setText(context.getString(com.black.wallet.R.string.volumn_24, pairStatus?.tradeAmountFormat
            ?: "0.00"))
        viewHolder?.since?.setText(pairStatus?.priceChangeSinceTodayFormat)
    }

}