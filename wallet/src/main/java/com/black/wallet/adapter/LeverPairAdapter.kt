package com.black.wallet.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.wallet.R
import com.black.wallet.databinding.ListItemLeverPairBinding

class LeverPairAdapter(context: Context, variableId: Int, data: ArrayList<String?>?) : BaseRecycleDataBindAdapter<String?, ListItemLeverPairBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_lever_pair
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemLeverPairBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val pair = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.pair?.setText(pair?.replace("_", "/") ?: nullAmount)
    }
}