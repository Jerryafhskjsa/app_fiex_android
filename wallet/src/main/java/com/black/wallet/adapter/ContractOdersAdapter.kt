package com.black.wallet.adapter

import android.content.Context
import android.util.Log
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.future.OrderBean
import com.black.base.model.future.OrderBeanItem
import com.black.base.model.future.PlansBean
import com.black.base.model.wallet.Order
import com.black.base.model.wallet.WalletBill
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.wallet.R
import com.black.wallet.databinding.FragmentOdersListBinding


class ContractOdersAdapter(context: Context, variableId: Int, data: ArrayList<OrderBeanItem>?) : BaseRecycleDataBindAdapter<OrderBeanItem, FragmentOdersListBinding>(context, variableId, data) {

    override fun getResourceId(): Int {
        return R.layout.fragment_oders_list
    }

    override fun onBindViewHolder(holder: BaseViewHolder<FragmentOdersListBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val oderList = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.action?.setText(oderList?.symbol)
        viewHolder?.direction?.setText(oderList?.orderSide)
        viewHolder?.amount?.setText(oderList?.createdTime.toString())
        viewHolder?.accountType?.setText(oderList?.origQty!!)
        viewHolder?.date?.setText(oderList?.price)
        viewHolder?.profit?.setText(oderList?.closeProfit)
    }
    }

