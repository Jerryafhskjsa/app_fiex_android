package com.black.frying.adapter

import android.content.Context
import android.text.TextUtils
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.socket.PairStatus
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemChoosePairBinding

class PairChooseAdapter(context: Context, variableId: Int, private val currentPair: String? = null, data: List<PairStatus?>?) : BaseRecycleDataBindAdapter<PairStatus?, ListItemChoosePairBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_choose_pair
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemChoosePairBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val pairStatus = getItem(position)
        val viewHolder = holder.dataBing
        var pairName: String? = null
        if (!TextUtils.isEmpty(pairStatus?.pair)) {
            pairName = pairStatus?.pair?.replace("_", "/")
        }
        viewHolder?.pair?.text = pairName ?: ""
        viewHolder?.pair?.isChecked = TextUtils.equals(currentPair, pairName)
    }

}