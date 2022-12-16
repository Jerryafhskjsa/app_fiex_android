package com.black.wallet.adapter

import android.content.Context
import android.net.Uri
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.wallet.TigerWallet
import com.black.base.util.ExchangeRatesUtil
import com.black.base.util.ImageLoader
import com.black.base.util.UrlConfig
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ListItemSpotAccountBinding
import com.black.wallet.databinding.ListSpotAccountBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.math.RoundingMode

class ContractAdapter(context: Context, variableId: Int, data: ArrayList<TigerWallet?>?) : BaseRecycleDataBindAdapter<TigerWallet?, ListSpotAccountBinding>(context, variableId, data) {
    private var isVisibility: Boolean = true
    private var imageLoader: ImageLoader? = null
    private var TAG = WalletAdapter::class.java.simpleName
    init {
        imageLoader = ImageLoader(context)
    }
    override fun getResourceId(): Int {
        return R.layout.list_spot_account
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ListSpotAccountBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        val wallet = getItem(position)
        val viewHolder = holder.dataBing
        val exChange = ExchangeRatesUtil.getExchangeRatesSetting(context)?.rateCode
            viewHolder?.coinType?.setText(if (wallet?.coinType == null) "" else wallet.coinType)
            viewHolder?.coinTypeDes?.setText(if (wallet?.coinTypeDes == null) "" else wallet.coinTypeDes)
            if (isVisibility) {
                viewHolder?.moneyTotal?.setText(
                    NumberUtil.formatNumberNoGroup(
                        wallet?.totalAmount,
                        RoundingMode.FLOOR,
                        2,
                        8
                    )
                )
                viewHolder?.profit?.setText(
                    NumberUtil.formatNumberNoGroup(
                        wallet?.profit,
                        RoundingMode.FLOOR,
                        2,
                        8
                    )
                )
                viewHolder?.margin?.setText(
                    NumberUtil.formatNumberNoGroup(
                        wallet?.crossedMargin,
                        RoundingMode.FLOOR,
                        2,
                        8
                    )
                )
                viewHolder?.experienceGold?.setText(
                    NumberUtil.formatNumberNoGroup(
                        wallet?.experienceGold,
                        RoundingMode.FLOOR,
                        2,
                        8
                    )
                )
                viewHolder?.balance?.setText(
                    NumberUtil.formatNumberNoGroup(
                        wallet?.coinAmount,
                        RoundingMode.FLOOR,
                        2,
                        8
                    )
                )
                viewHolder?.deduction?.setText(
                    NumberUtil.formatNumberNoGroup(
                        wallet?.deduction,
                        RoundingMode.FLOOR,
                        2,
                        8
                    )
                )
            } else {
                viewHolder?.moneyTotal?.setText("****")
                viewHolder?.profit?.setText("****")
                viewHolder?.margin?.setText("****")
                viewHolder?.experienceGold?.setText("****")
                viewHolder?.balance?.setText("****")
                viewHolder?.deduction?.setText("****")
            }
            if (wallet?.coinIconUrl != null) {
                Glide.with(context)
                    .load(Uri.parse(UrlConfig.getCoinIconUrl(context, wallet?.coinIconUrl)))
                    .apply(RequestOptions().error(R.drawable.icon_coin_default))
                    .into(viewHolder?.iconCoin!!)
            }

    }
    fun setVisibility(isVisibility: Boolean) {
        this.isVisibility = isVisibility
        notifyDataSetChanged()
    }
}