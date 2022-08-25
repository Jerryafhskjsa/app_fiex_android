package com.black.frying.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.socket.TradeOrder
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemEntrustRecordNewBinding
import skin.support.content.res.SkinCompatResources

class EntrustRecordNewAdapter(context: Context, variableId: Int, data: ArrayList<TradeOrder?>?) : BaseRecycleDataBindAdapter<TradeOrder?, ListItemEntrustRecordNewBinding>(context, variableId, data) {
    companion object {
        private const val TYPE_NEW = 0
        private const val TYPE_HIS = 1
    }

    private var onHandleClickListener: OnHandleClickListener? = null
    private var amountPrecision = 4
    private var c1 = 0
    private var t5: Int = 0
    private var type = TYPE_NEW

    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, R.color.C1)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_entrust_record_new
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemEntrustRecordNewBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val tradeOrder = getItem(position)
        val viewHolder: ListItemEntrustRecordNewBinding? = holder.dataBing
        val type = StringBuilder()
        if (TextUtils.equals(tradeOrder?.direction, "ASK")) {
            viewHolder?.type?.setTextColor(t5)
            type.append(getString(R.string.entrust_type_sale))
        } else if (TextUtils.equals(tradeOrder?.direction, "BID")) {
            viewHolder?.type?.setTextColor(c1)
            type.append(getString(R.string.entrust_type_buy))
        } else {
            viewHolder?.type?.setTextColor(c1)
        }
        type.append(" ").append(if (tradeOrder?.pair == null) "" else tradeOrder.pair?.replace("_", "/"))
        viewHolder?.type?.setText(type.toString())
        viewHolder?.type?.setOnClickListener {
            onHandleClickListener?.onPairClick(tradeOrder)
        }
        viewHolder?.cancel?.setOnClickListener {
            onHandleClickListener?.onHandleClick(tradeOrder)
        }
        viewHolder?.status?.setText(tradeOrder?.getStatusDisplay(context))
        viewHolder?.date?.setText(if (this.type == TYPE_NEW)
            (if (tradeOrder?.createdTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", tradeOrder.createdTime!!))
        else (if (tradeOrder?.updateTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", tradeOrder.updateTime)))
        viewHolder?.entrustPrice?.setText(if (tradeOrder?.price == null || tradeOrder.price == 0.0) "0" else NumberUtil.formatNumberNoGroup(tradeOrder.price))
        viewHolder?.entrustAmount?.setText(if (tradeOrder?.totalAmount == null || tradeOrder.totalAmount == 0.0) "0" else NumberUtil.formatNumberNoGroup(tradeOrder.totalAmount, amountPrecision, amountPrecision))
        val dealTotalAmount: Double = if (tradeOrder?.dealAvgPrice != null && tradeOrder.dealAmount != null) tradeOrder.dealAvgPrice!! * tradeOrder.dealAmount!! else 0.toDouble()
        viewHolder?.dealTotalAmount?.setText(NumberUtil.formatNumberNoGroup(dealTotalAmount))
        viewHolder?.dealPrice?.setText(if (tradeOrder?.dealAvgPrice == null || tradeOrder.dealAvgPrice == 0.0) "0" else NumberUtil.formatNumberNoGroup(tradeOrder.dealAvgPrice))
        viewHolder?.dealAmount?.setText(if (tradeOrder?.dealAmount == null || tradeOrder.dealAmount == 0.0) "0" else NumberUtil.formatNumberNoGroup(tradeOrder.dealAmount, amountPrecision, amountPrecision))
        when (tradeOrder?.status) {
            0, 1 -> {
                //新订单，未结束的
                viewHolder?.cancel?.visibility = View.VISIBLE
                viewHolder?.status?.visibility = View.GONE
            }
            else -> {
                //已撤销或者其他情况
                viewHolder?.cancel?.visibility = View.GONE
                viewHolder?.status?.visibility = View.VISIBLE
            }
        }
    }

    fun setAmountPrecision(amountPrecision: Int) {
        this.amountPrecision = amountPrecision
        notifyDataSetChanged()
    }

    fun setOnHandleClickListener(onHandleClickListener: OnHandleClickListener?) {
        this.onHandleClickListener = onHandleClickListener
    }

    fun setType(type: Int) {
        this.type = type
    }

    interface OnHandleClickListener {
        fun onPairClick(tradeOrder: TradeOrder?)
        fun onHandleClick(tradeOrder: TradeOrder?)
    }
}