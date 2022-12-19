package com.black.wallet.adapter

import android.content.Context
import android.util.Log
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.wallet.WalletBill
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.ListItemWalletBillBinding
import skin.support.content.res.SkinCompatResources

class WalletBillAdapter(context: Context, variableId: Int, data: ArrayList<WalletBill?>?) : BaseRecycleDataBindAdapter<WalletBill?, ListItemWalletBillBinding>(context, variableId, data) {
    private var c1 = 0
    private var t5 = 0
    private var walletBillTypeMap: Map<String?, String?>? = null

    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, R.color.T7)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_wallet_bill
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemWalletBillBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val walletBill = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.action?.setText(walletBill?.getType(context))
        viewHolder?.date?.setText(if (walletBill?.createdTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", walletBill.createdTime!!))
        if (walletBill == null || walletBill?.availableChange?.toDouble() != 0.0 ) {
            viewHolder?.amount?.setText(
                NumberUtil.formatNumberNoGroup(
                    walletBill?.availableChange?.toDouble(),
                    2,
                    4
                ) + walletBill?.coin
            )
            if (walletBill?.availableChange == null || walletBill.availableChange?.toDouble()!! < 0) {
                viewHolder?.action?.setTextColor(t5)
            } else {
                viewHolder?.action?.setTextColor(c1)
            }
            viewHolder?.accountType?.setText(getString(R.string.usable))
        }
        if (walletBill == null || walletBill?.frozeChange?.toDouble() != 0.0 ) {
            viewHolder?.amount?.setText(
                NumberUtil.formatNumberNoGroup(
                    walletBill?.frozeChange?.toDouble(),
                    2,
                    4
                ) + walletBill?.coin
            )
            if (walletBill?.frozeChange == null || walletBill.frozeChange?.toDouble()!! < 0) {
                viewHolder?.action?.setTextColor(t5)
            } else {
                viewHolder?.action?.setTextColor(c1)
            }
            viewHolder?.accountType?.setText(getString(R.string.freez))
        }
        if (walletBill?.availableChange?.toDouble() != 0.0 && walletBill?.frozeChange?.toDouble() !=  0.0){
            viewHolder?.accountType?.setText(getString(R.string.usable) + "/" + getString(R.string.freez))
        }
    }

    fun setWalletBillTypeMap(walletBillTypeMap: Map<String?, String?>?) {
        this.walletBillTypeMap = walletBillTypeMap
    }

    private fun getWalletBillTypeText(walletBill: WalletBill?): String? {
        return if (walletBillTypeMap != null && walletBillTypeMap!!.isNotEmpty()) {
            walletBillTypeMap!![walletBill?.type]
        } else null
    }
}