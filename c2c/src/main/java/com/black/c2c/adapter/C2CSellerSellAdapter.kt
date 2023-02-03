package com.black.c2c.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.C2CPayment
import com.black.base.model.c2c.C2CSeller
import com.black.c2c.R
import com.black.c2c.databinding.ListItemC2cSellerSellBinding
import com.black.util.NumberUtil

class C2CSellerSellAdapter(context: Context, variableId: Int, data: ArrayList<C2CMainAD?>?) : BaseRecycleDataBindAdapter<C2CMainAD?, ListItemC2cSellerSellBinding>(context, variableId, data) {
    private var onHandleClickListener: OnHandleClickListener? = null

    override fun getResourceId(): Int {
        return R.layout.list_item_c2c_seller_sell
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemC2cSellerSellBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val c2CSeller = getItem(position)
        val viewHolder = holder.dataBing
        viewHolder?.firstLetter?.setText(if (TextUtils.isEmpty(c2CSeller?.realName)) "?" else c2CSeller?.realName!![0].toString())
        viewHolder?.id?.setText(c2CSeller?.realName)
        viewHolder?.num1?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CSeller?.currentPrice, 8, 2, 8)))
        viewHolder?.finish?.setText(String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CSeller?.currentPrice, 8, 2, 8)))
        viewHolder?.num2?.setText("成单量 " + c2CSeller?.completedOrders.toString() + " | 成单率" + String.format("%s", NumberUtil.formatNumberDynamicScaleNoGroup(c2CSeller?.completion, 8, 2, 8)))
        val paymentTypeList = c2CSeller?.payMethods
        if (paymentTypeList != null && paymentTypeList.isNotEmpty() && paymentTypeList.contains(C2CPayment.BANK)) {
            viewHolder?.cards?.visibility = View.VISIBLE
        } else {
            viewHolder?.cards?.visibility = View.GONE
        }
        if (paymentTypeList != null && paymentTypeList.isNotEmpty() && paymentTypeList.contains(C2CPayment.ALIPAY)) {
            viewHolder?.ali?.visibility = View.VISIBLE
        } else {
            viewHolder?.ali?.visibility = View.GONE
        }
        if (paymentTypeList != null && !paymentTypeList.isEmpty() && paymentTypeList.contains(C2CPayment.WECHAT)) {
            viewHolder?.weiXin?.visibility = View.VISIBLE
        } else {
            viewHolder?.weiXin?.visibility = View.GONE
        }

        viewHolder?.num3?.setText(String.format("￥ %s - ￥ %s", NumberUtil.formatNumberNoGroup(c2CSeller?.singleLimitMin ), NumberUtil.formatNumberNoGroup(c2CSeller?.singleLimitMax )))
        viewHolder?.btnHandle?.setOnClickListener {
            if (onHandleClickListener != null) {
                onHandleClickListener!!.onHandleClick(c2CSeller)
            }
        }
    }

    fun setOnHandleClickListener(onHandleClickListener: OnHandleClickListener?) {
        this.onHandleClickListener = onHandleClickListener
    }
}