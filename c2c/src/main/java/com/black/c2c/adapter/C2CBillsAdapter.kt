package com.black.c2c.adapter

import android.content.Context
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.c2c.C2CBills
import com.black.base.model.c2c.C2CMainAD
import com.black.c2c.R
import com.black.c2c.databinding.ListC2cBillsBinding
import com.black.c2c.databinding.ListItemC2cSellerBuyBinding
import com.black.util.NumberUtil

class C2CBillsAdapter(context: Context, variableId: Int, data: ArrayList<C2CBills?>?) : BaseRecycleDataBindAdapter<C2CBills?, ListC2cBillsBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_c2c_bills
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListC2cBillsBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val c2CBills = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.buy?.setText(c2CBills?.direction)
        viewHolder?.coinType?.setText(c2CBills?.coinType)
        viewHolder?.time?.setText(c2CBills?.createTime)
        viewHolder?.account?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.price, 8, 2, 8)))
        viewHolder?.amount?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.amount, 8, 2, 8)))
        viewHolder?.money?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.price!! * c2CBills.amount!!, 8, 2, 8)))
        viewHolder?.status?.setText(c2CBills?.status.toString())
    }
}