package com.black.c2c.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.c2c.C2COrder
import com.black.c2c.R
import com.black.c2c.databinding.ListItemC2cOrderBinding
import com.black.util.CommonUtil
import skin.support.content.res.SkinCompatResources

class C2COrderAdapter(context: Context, variableId: Int, data: ArrayList<C2COrder?>?) : BaseRecycleDataBindAdapter<C2COrder?, ListItemC2cOrderBinding>(context, variableId, data) {
    private var c1 = 0
    private var t5: Int = 0

    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, R.color.C1)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_c2c_order
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemC2cOrderBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val c2COrder = getItem(position)
        val viewHolder = holder.dataBing
        val color: Int = if (C2COrder.ORDER_BUY == c2COrder?.direction) c1 else t5
        viewHolder?.direction?.setText(String.format("%s%s", c2COrder?.getDirectionDisplay(context), c2COrder?.coinType))
        viewHolder?.direction?.setTextColor(color)
        viewHolder?.status?.setText(c2COrder?.getStatusDisplay(context))
        viewHolder?.date?.setText(if (c2COrder?.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", c2COrder.createTime!!))
        viewHolder?.amountTitle?.setText(getString(R.string.c2c_order_amount, c2COrder?.coinType))
        viewHolder?.amount?.setText(c2COrder?.getAmountDisplay(context))
        viewHolder?.money?.setText(c2COrder?.getTotalMoneyDisplay(context))
        viewHolder?.name?.setText(c2COrder?.merchantName)
    }

}