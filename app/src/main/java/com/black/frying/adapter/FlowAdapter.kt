package com.black.frying.adapter

import android.content.Context
import android.util.Log
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.future.OrderBean
import com.black.base.model.future.OrderBeanItem
import com.black.base.model.future.PlansBean
import com.black.base.model.wallet.FlowBill
import com.black.base.model.wallet.Order
import com.black.base.model.wallet.WalletBill
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.FragmentFlowBinding
import com.black.wallet.databinding.FragmentOdersListBinding


class FlowAdapter(context: Context, variableId: Int, data: ArrayList<FlowBill?>?) : BaseRecycleDataBindAdapter<FlowBill?, FragmentFlowBinding>(context, variableId, data) {

    override fun getResourceId(): Int {
        return R.layout.fragment_flow
    }

    override fun onBindViewHolder(holder: BaseViewHolder<FragmentFlowBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val oderList = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.action?.setText(if (oderList?.symbol == "btc_usdt")  "BTC_USDT" + context.getString(R.string.sustainable) else "ETH_USDT")
        viewHolder?.direction?.setText(if(oderList?.positionSide == "LONG") getString(R.string.contract_buy_raise) else getString(R.string.contract_sell_raise))
        viewHolder?.amount?.setText(if (oderList?.createdTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", oderList.createdTime!!))
        viewHolder?.capitalCost?.setText((if (oderList?.cast == null) nullAmount else NumberUtil.formatNumberNoGroup(oderList.cast,8,4)) + "USDT")
    }
}