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
        Log.d("iiiiii","bussiness time = "+walletBill?.businessTime)
        viewHolder?.action?.setText(walletBill?.type)
        viewHolder?.date?.setText(if (walletBill?.createdTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", walletBill.createdTime!!))
        viewHolder?.amount?.setText(if (walletBill?.availableChange == null) nullAmount else NumberUtil.formatNumberNoGroup(walletBill.availableChange?.toDouble(), 2, 8))
        if (walletBill?.availableChange == null || walletBill?.availableChange?.toDouble()!! < 0) {
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
            walletBillTypeMap!![walletBill?.type]
        } else null
    }
}