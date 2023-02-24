package com.black.c2c.adapter

import android.annotation.SuppressLint
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

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: BaseViewHolder<ListC2cBillsBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val c2CBills = getItem(position)
        val viewHolder = holder.dataBing
        if (c2CBills?.direction == "B") {
            viewHolder?.buy?.setText(getString(R.string.buy_02))
            viewHolder?.buy?.setTextColor(R.color.C1)
        }
        if (c2CBills?.direction == "S") {
            viewHolder?.buy?.setText(getString(R.string.sell))
            viewHolder?.buy?.setTextColor(R.color.btn_negative_hover)
        }
        viewHolder?.coinType?.setText(c2CBills?.coinType)
        viewHolder?.time?.setText(c2CBills?.createTime)
        viewHolder?.account?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.price, 8, 2, 8)))
        viewHolder?.amount?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.amount, 8, 2, 8)))
        viewHolder?.money?.setText("￥" + String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CBills?.price!! * c2CBills.amount!!, 8, 2, 8)))
        viewHolder?.status?.setText(c2CBills?.getStatusText(context))
        viewHolder?.billsNum?.setText(c2CBills?.id)
    }
}