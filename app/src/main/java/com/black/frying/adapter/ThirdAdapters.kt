package com.black.frying.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.payOrder
import com.black.wallet.R
import com.black.wallet.databinding.ListItemWalletBillBinding
import skin.support.content.res.SkinCompatResources

class ThirdAdapters (context: Context, variableId: Int, data: ArrayList<payOrder?>?) : BaseRecycleDataBindAdapter<payOrder?, ListItemWalletBillBinding>(context, variableId, data) {
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

    }

}