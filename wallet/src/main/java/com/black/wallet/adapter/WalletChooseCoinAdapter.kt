package com.black.wallet.adapter

import android.content.Context
import android.net.Uri
import android.view.View
import android.widget.SectionIndexer
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.UrlConfig
import com.black.util.CommonUtil
import com.black.wallet.R
import com.black.wallet.databinding.ListItemWalletChooseCoinBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class WalletChooseCoinAdapter(context: Context, data: ArrayList<Wallet?>?) : BaseDataTypeBindAdapter<Wallet?, ListItemWalletChooseCoinBinding>(context, data), SectionIndexer {
    override fun getItemLayoutId(): Int {
        return R.layout.list_item_wallet_choose_coin
    }

    override fun bindView(position: Int, holder: ViewHolder<ListItemWalletChooseCoinBinding>?) {
        val wallet = getItem(position)
        val lastWallet = CommonUtil.getItemFromList(data, position - 1)
        val viewHolder = holder?.dataBing
//        if (lastWallet == null || Wallet.COMPARATOR_CHOOSE_COIN.compare(wallet, lastWallet) != 0) {
//            viewHolder?.subTitle?.visibility = View.VISIBLE
//            viewHolder?.subTitle?.setText(wallet?.sortLetter.toString())
//        } else {
//            viewHolder?.subTitle?.visibility = View.GONE
//        }
        viewHolder?.coinType?.setText(if (wallet?.coinType == null) "" else wallet.coinType)
        viewHolder?.coinCount?.setText(wallet?.coinAmount.toString())
        viewHolder?.coinNameDes?.setText(wallet?.coinTypeDes)
        if(wallet?.coinIconUrl != null){
            Glide.with(context)
                .load(Uri.parse(UrlConfig.getCoinIconUrl(context,wallet?.coinIconUrl)))
                .apply(RequestOptions().error(R.drawable.icon_coin_default))
                .into(viewHolder?.imgCoin!!)
        }
    }

    override fun getSections(): Array<Any> {
        return arrayOf(ConstData.SIDE_BAR_TITLE)
    }

    override fun getPositionForSection(position: Int): Int {
        for (i in 0 until count) {
            val firstChar = getItem(i)?.sortLetter
            if (firstChar?.toInt() == position) {
                return i
            }
        }
        return -1
    }

    override fun getSectionForPosition(position: Int): Int {
        val firstChar = getItem(position)?.sortLetter
        var result = -1
        for (i in sections.indices) {
            val sors = sections[i] as String
            if (firstChar == sors[0]) {
                result = i
                break
            }
        }
        return result
    }

}