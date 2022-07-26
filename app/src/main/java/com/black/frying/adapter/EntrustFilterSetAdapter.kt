package com.black.frying.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.frying.model.EntrustFilterSet
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemEntrustFilterSetBinding

class EntrustFilterSetAdapter(context: Context, variableId: Int, data: ArrayList<EntrustFilterSet?>?) : BaseRecycleDataBindAdapter<EntrustFilterSet?, ListItemEntrustFilterSetBinding>(context, variableId, data) {
    private var checkedSet: EntrustFilterSet? = null

    override fun getResourceId(): Int {
        return R.layout.list_item_entrust_filter_set
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemEntrustFilterSetBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val set = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.set?.text = set?.set
        viewHolder?.set?.isChecked = set?.isChecked ?: false
    }

    fun check(position: Int) {
        val newSet = CommonUtil.getItemFromList(data, position)
        if (newSet != null) {
            newSet.isChecked = true
        }
        if (!CommonUtil.equals(newSet, checkedSet)) {
            if (checkedSet != null) {
                checkedSet?.isChecked = false
            }
            checkedSet = newSet
            notifyDataSetChanged()
        }
    }
}