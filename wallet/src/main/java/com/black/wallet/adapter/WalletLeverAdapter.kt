package com.black.wallet.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.wallet.WalletLever
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ListItemWalletLeverBinding

class WalletLeverAdapter(context: Context, variableId: Int, data: ArrayList<WalletLever?>?) : BaseRecycleDataBindAdapter<WalletLever?, ListItemWalletLeverBinding>(context, variableId, data) {
    private var isVisibility: Boolean = true
    override fun getResourceId(): Int {
        return R.layout.list_item_wallet_lever
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemWalletLeverBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val wallet = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.pair?.setText(if (wallet?.pair == null) "" else wallet.pair!!.replace("_", "/"))
        viewHolder?.coinType?.setText(if (wallet?.coinType == null) "" else wallet.coinType)
        if (isVisibility) {
            viewHolder?.usable?.setText(NumberUtil.formatNumberNoGroup(wallet?.coinAmount, 2, 8))
        } else {
            viewHolder?.usable?.setText("****")
        }
        if (isVisibility) {
            viewHolder?.borrow?.setText(NumberUtil.formatNumberNoGroup(wallet?.coinBorrow, 2, 8))
        } else {
            viewHolder?.borrow?.setText("****")
        }
        if (isVisibility) {
            viewHolder?.froze?.setText(NumberUtil.formatNumberNoGroup(wallet?.coinFroze, 2, 8))
        } else {
            viewHolder?.froze?.setText("****")
        }
        if (isVisibility) {
            viewHolder?.totalCny?.setText(if (wallet?.totalAmountCny == null) getString(R.string.number_default) else NumberUtil.formatNumberDynamicScaleNoGroup(wallet.totalAmountCny, 10, 2, 2))
        } else {
            viewHolder?.totalCny?.setText("****")
        }
        if (isVisibility) {
            viewHolder?.setUsable?.setText(NumberUtil.formatNumberNoGroup(wallet?.afterCoinAmount, 2, 8))
        } else {
            viewHolder?.setUsable?.setText("****")
        }
        if (isVisibility) {
            viewHolder?.setBorrow?.setText(NumberUtil.formatNumberNoGroup(wallet?.afterCoinBorrow, 2, 8))
        } else {
            viewHolder?.setBorrow?.setText("****")
        }
        if (isVisibility) {
            viewHolder?.setFroze?.setText(NumberUtil.formatNumberNoGroup(wallet?.afterCoinFroze, 2, 8))
        } else {
            viewHolder?.setFroze?.setText("****")
        }
        if (isVisibility) {
            viewHolder?.setTotalCny?.setText(if (wallet?.afterTotalAmountCny == null) getString(R.string.number_default) else NumberUtil.formatNumberDynamicScaleNoGroup(wallet.afterTotalAmountCny, 10, 2, 2))
        } else {
            viewHolder?.setTotalCny?.setText("****")
        }
    }

    fun setVisibility(isVisibility: Boolean) {
        this.isVisibility = isVisibility
        notifyDataSetChanged()
    }
}