package com.black.c2c.adapter

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.C2CPayment
import com.black.base.model.c2c.C2CSeller
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.c2c.R
import com.black.c2c.databinding.ListItemC2cSellerBuyBinding
import com.black.router.BlackRouter
import com.black.util.NumberUtil

class C2CSellerBuyAdapter(context: Context, variableId: Int, data: ArrayList<C2CMainAD?>?) : BaseRecycleDataBindAdapter<C2CMainAD?, ListItemC2cSellerBuyBinding>(context, variableId, data) {
    override fun getResourceId(): Int {
        return R.layout.list_item_c2c_seller_buy
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemC2cSellerBuyBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val c2CSeller = getItem(position)
        val viewHolder = holder.dataBing
        if ( c2CSeller?.direction == "B") {
            viewHolder?.btnHandle?.visibility = View.VISIBLE
            viewHolder?.btnHandleSell?.visibility = View.GONE
            if (c2CSeller.canCreateOrderForQueryUser == true) {
                viewHolder?.btnHandle?.isEnabled = true
                viewHolder?.btnHandle?.setText(getString(R.string.buy_02))
            } else  {
                viewHolder?.btnHandle?.isEnabled = false
                viewHolder?.btnHandle?.setText("当前用户不可下此广告的订单")
            }
        }
        else{
            viewHolder?.btnHandleSell?.visibility = View.VISIBLE
            viewHolder?.btnHandle?.visibility = View.GONE
            if (c2CSeller?.canCreateOrderForQueryUser == true) {
                viewHolder?.btnHandle?.setText(getString(R.string.sell))
                viewHolder?.btnHandleSell?.isEnabled = true
            } else {
                viewHolder?.btnHandleSell?.isEnabled = false
                viewHolder?.btnHandleSell?.setText("当前用户不可下此广告的订单")
            }
        }
        viewHolder?.firstLetter?.setText(if (TextUtils.isEmpty(c2CSeller?.realName)) "?" else c2CSeller?.realName!![0].toString())
        viewHolder?.id?.setText(c2CSeller?.realName)
        viewHolder?.num2?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CSeller?.totalAmount, 8, 2, 8)) + c2CSeller?.coinType)
        viewHolder?.num1?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CSeller?.priceParam, 8, 2, 8)))
        viewHolder?.finish?.setText("成单量 " + c2CSeller?.completedOrders.toString() + " | 成单率" + String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CSeller?.completion, 8, 2, 8)) + "%")
        val paymentTypeList = c2CSeller?.payMethods
        if (paymentTypeList != null && paymentTypeList == "[0]") {
            viewHolder?.cards?.visibility = View.VISIBLE
            viewHolder?.ali?.visibility = View.GONE
            viewHolder?.weiXin?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList== "[1]") {
            viewHolder?.ali?.visibility = View.VISIBLE
            viewHolder?.cards?.visibility = View.GONE
            viewHolder?.weiXin?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList== "[2]") {
            viewHolder?.weiXin?.visibility = View.VISIBLE
            viewHolder?.ali?.visibility = View.GONE
            viewHolder?.cards?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList == "[1,2]") {
            viewHolder?.ali?.visibility = View.VISIBLE
            viewHolder?.cards?.visibility = View.GONE
            viewHolder?.weiXin?.visibility = View.VISIBLE
        }
        if (paymentTypeList != null && paymentTypeList== "[0,1]") {
            viewHolder?.ali?.visibility = View.VISIBLE
            viewHolder?.cards?.visibility = View.VISIBLE
            viewHolder?.weiXin?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList== "[0,2]") {
            viewHolder?.weiXin?.visibility = View.VISIBLE
            viewHolder?.ali?.visibility = View.GONE
            viewHolder?.cards?.visibility = View.VISIBLE
        }
        if (paymentTypeList != null && paymentTypeList== "[0,1,2]") {
            viewHolder?.weiXin?.visibility = View.VISIBLE
            viewHolder?.ali?.visibility = View.VISIBLE
            viewHolder?.cards?.visibility = View.VISIBLE
        }

        viewHolder?.num3?.setText(String.format("￥ %s - ￥ %s", NumberUtil.formatNumberNoGroup(c2CSeller?.singleLimitMin ), NumberUtil.formatNumberNoGroup(c2CSeller?.singleLimitMax )))
        viewHolder?.btnHandle?.setOnClickListener { v ->
            val extras = Bundle()
            extras.putParcelable(ConstData.C2C_AD, c2CSeller)
            extras.putString(ConstData.PAIR,c2CSeller?.id)
            extras.putString(ConstData.REAL_NAME, c2CSeller?.payMethods)
            BlackRouter.getInstance().build(RouterConstData.C2C_BUY).with(extras).go(context)
        }
        viewHolder?.btnHandleSell?.setOnClickListener { v ->
            val bundle = Bundle()
            bundle.putParcelable(ConstData.C2C_LIST, c2CSeller)
            bundle.putString(ConstData.PAIR,c2CSeller?.id)
            bundle.putString(ConstData.REAL_NAME, c2CSeller?.payMethods)
            BlackRouter.getInstance().build(RouterConstData.C2C_SELL).with(bundle).go(context)
        }
    }


}

