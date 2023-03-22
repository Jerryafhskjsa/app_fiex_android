package com.black.wallet.adapter

import android.content.Context
import android.net.Uri
import android.util.Log
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.api.C2CApiServiceHelper
import com.black.base.model.wallet.TigerWallet
import com.black.base.model.wallet.Wallet
import com.black.base.util.ExchangeRatesUtil
import com.black.base.util.ImageLoader
import com.black.base.util.UrlConfig
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ListItemSpotAccountBinding
import com.black.wallet.util.DipPx
import com.black.wallet.util.GlideRoundTransform
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import java.math.RoundingMode

class WalletAdapter(context: Context, variableId: Int, data: ArrayList<Wallet?>?) :
    BaseRecycleDataBindAdapter<Wallet?, ListItemSpotAccountBinding>(context, variableId, data) {
    private var isVisibility: Boolean = true
    private var imageLoader: ImageLoader? = null
    private var TAG = WalletAdapter::class.java.simpleName

    init {
        imageLoader = ImageLoader(context)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_spot_account
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ListItemSpotAccountBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        val wallet = getItem(position)
        val viewHolder = holder.dataBing
        var rates = C2CApiServiceHelper.coinUsdtPrice?.usdt
        val exChange = ExchangeRatesUtil.getExchangeRatesSetting(context)?.rateCode
        viewHolder?.coinType?.setText(
            if (wallet?.coinType == null) "" else wallet.coinType.toString().uppercase()
        )
        viewHolder?.coinTypeDes?.setText(if (wallet?.coinTypeDes == null) "" else wallet.coinTypeDes)
        if (isVisibility) {
            viewHolder?.usable?.setText(
                NumberUtil.formatNumberNoGroup(
                    wallet?.totalAmount,
                    RoundingMode.FLOOR,
                    2,
                    8
                )
            )
        } else {
            viewHolder?.usable?.setText("****")
        }
        if (isVisibility) {
            viewHolder?.totalCny?.setText(
                if (wallet?.estimatedAvailableAmount == null) getString(
                    R.string.number_default
                ) else if (exChange == 0) "≈ ￥ " + NumberUtil.formatNumberDynamicScaleNoGroup(
                    wallet.estimatedAvailableAmountCny!! * rates!!,
                    10,
                    2,
                    2
                ) else {
                    rates = C2CApiServiceHelper.coinUsdtPrice?.usdtToUsd
                    "≈ $ " + NumberUtil.formatNumberDynamicScaleNoGroup(
                    wallet.estimatedAvailableAmountCny!! * rates!!,
                    10,
                    2,
                    2
                )
                }
            )
        } else {
            viewHolder?.totalCny?.setText("****")
        }
        if (wallet?.coinIconUrl != null) {
            val requestOptions = RequestOptions
                .bitmapTransform(RoundedCorners(DipPx.dip2px(context, 15f)))
            Glide.with(context)
                .load(Uri.parse(UrlConfig.getCoinIconUrl(context, wallet.coinIconUrl)))
                .apply(requestOptions)
                .into(viewHolder?.iconCoin!!)
        }
    }

    fun setVisibility(isVisibility: Boolean) {
        this.isVisibility = isVisibility
        notifyDataSetChanged()
    }
}