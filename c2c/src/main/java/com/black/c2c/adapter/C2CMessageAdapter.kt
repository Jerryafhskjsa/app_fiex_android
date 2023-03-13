package com.black.c2c.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.c2c.C2CMessage
import com.black.base.model.c2c.C2COrder
import com.black.base.util.TimeUtil
import com.black.c2c.R
import com.black.c2c.databinding.ListC2cMessageBinding
import skin.support.content.res.SkinCompatResources

class C2CMessageAdapter(context: Context, variableId: Int, data: ArrayList<C2CMessage?>?) : BaseRecycleDataBindAdapter<C2CMessage?, ListC2cMessageBinding?>(context, variableId, data) {
    private var c1 = 0
    private var t5: Int = 0

    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, R.color.C1)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
    }

    override fun getResourceId(): Int {
        return R.layout.list_c2c_message
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListC2cMessageBinding?>, position: Int) {
        super.onBindViewHolder(holder, position)
        val c2COrder = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.time?.setText(TimeUtil.getTime(c2COrder?.time))
        viewHolder?.message?.setText(c2COrder?.note)
    }

}