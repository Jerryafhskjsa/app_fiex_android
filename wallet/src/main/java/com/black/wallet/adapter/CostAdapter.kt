package com.black.wallet.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.wallet.CostBill
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.FragmentCostBinding


class CostAdapter (context: Context, variableId: Int, data: ArrayList<CostBill?>?) : BaseRecycleDataBindAdapter<CostBill?, FragmentCostBinding>(context, variableId, data) {

    override fun getResourceId(): Int {
        return R.layout.fragment_cost
    }

    override fun onBindViewHolder(holder: BaseViewHolder<FragmentCostBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val oderList = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.type?.setText(oderList?.getType(context))
        viewHolder?.direction?.setText(oderList?.getSymbol(context))
        viewHolder?.amount?.setText(if (oderList?.createdTime == null)nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", oderList.createdTime!!))
        viewHolder?.money?.setText(NumberUtil.formatNumberNoGroup(oderList?.amount,8,4))
        viewHolder?.coin?.setText(oderList?.coin)
    }
}