package com.black.wallet.adapter

import android.content.Context
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
        viewHolder?.action?.setText(getWalletBillTypeText(walletBill))
        viewHolder?.date?.setText(if (walletBill?.businessTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", walletBill.businessTime!!))
        viewHolder?.amount?.setText(if (walletBill?.amount == null) nullAmount else NumberUtil.formatNumberNoGroup(walletBill.amount, 2, 8))
        if (walletBill?.amount == null || walletBill.amount!! < 0) {
            viewHolder?.action?.setTextColor(t5)
        } else {
            viewHolder?.action?.setTextColor(c1)
        }
    }

    fun setWalletBillTypeMap(walletBillTypeMap: Map<String?, String?>?) {
        this.walletBillTypeMap = walletBillTypeMap
    }

    private fun getWalletBillTypeText(walletBill: WalletBill?): String? {
        return if (walletBillTypeMap != null && walletBillTypeMap!!.isNotEmpty()) {
            walletBillTypeMap!![walletBill?.businessType]
        } else null
    }
}