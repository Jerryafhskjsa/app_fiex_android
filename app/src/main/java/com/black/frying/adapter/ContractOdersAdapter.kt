package com.black.frying.adapter

import android.content.Context
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.future.OrderBeanItem
import com.black.frying.service.FutureService
import com.black.util.CommonUtil
import com.black.wallet.R
import com.black.wallet.databinding.FragmentOdersListBinding
import java.math.BigDecimal


class ContractOdersAdapter(context: Context, variableId: Int, data: ArrayList<OrderBeanItem>?) : BaseRecycleDataBindAdapter<OrderBeanItem, FragmentOdersListBinding>(context, variableId, data) {

    override fun getResourceId(): Int {
        return R.layout.fragment_oders_list
    }

    override fun onBindViewHolder(holder: BaseViewHolder<FragmentOdersListBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val oderList = getItem(position)
        val viewHolder = holder.dataBing
        val contractSize = FutureService.getContractSize(oderList.symbol)?: BigDecimal(0.0001)
        val num = BigDecimal(oderList.origQty.toString()).multiply(contractSize.multiply(BigDecimal(oderList.avgPrice)))
        val num2 = BigDecimal(oderList.executedQty.toString()).multiply(contractSize.multiply(BigDecimal(oderList.avgPrice)))
        viewHolder?.coin?.setText(oderList.symbol?.uppercase()  + getString(R.string.sustainable))
        viewHolder?.type?.setText(if (oderList.orderSide == "BUY") getString(R.string.contract_buy_raise) else getString(R.string.contract_sell_raise))
        viewHolder?.amount?.setText(CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", oderList.createdTime!!))
        viewHolder?.accountType?.setText((if(oderList.origQty == null) nullAmount else String.format("%.4f", num)) + "USDT")
        viewHolder?.date?.setText("--")
        viewHolder?.profit?.setText( if (oderList.executedQty == null)String.format("%.4f", num) + "USDT" else String.format("%.4f", (num - num2)) + "USDT" )
    }
    }

