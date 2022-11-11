package com.black.frying.adapter

import android.content.Context
import android.text.TextUtils
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.socket.TradeOrder
import com.black.base.model.socket.TradeOrderFiex
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemEntrustRecordNewBinding
import skin.support.content.res.SkinCompatResources

class EntrustRecordNewAdapter(context: Context, variableId: Int, data: ArrayList<TradeOrderFiex?>?) : BaseRecycleDataBindAdapter<TradeOrderFiex?, ListItemEntrustRecordNewBinding>(context, variableId, data) {
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
        var orderType = tradeOrder?.orderType
        var orderTypeDes:String? = null
        when(orderType){
            "LIMIT" -> orderTypeDes = getString(R.string.order_type_limit)
            "MARKET" -> orderTypeDes = getString(R.string.order_type_market)
        }
        val type = StringBuilder()
        type.append(orderTypeDes)
        if (TextUtils.equals(tradeOrder?.orderSide, "SELL")) {
            viewHolder?.type?.setTextColor(t5)
            type.append(getString(R.string.entrust_type_sale))
        } else if (TextUtils.equals(tradeOrder?.orderSide, "BUY")) {
            viewHolder?.type?.setTextColor(c1)
            type.append(getString(R.string.entrust_type_buy))
        } else {
            viewHolder?.type?.setTextColor(c1)
        }
        type.append(" ").append(if (tradeOrder?.symbol == null) "" else tradeOrder.symbol?.replace("_", "/"))
        viewHolder?.type?.setText(type.toString())
        viewHolder?.type?.setOnClickListener {
            onHandleClickListener?.onPairClick(tradeOrder)
        }
        viewHolder?.cancel?.setOnClickListener {
            onHandleClickListener?.onHandleClick(tradeOrder)
        }
        viewHolder?.status?.setText(tradeOrder?.getStatusDisplay(context))
        viewHolder?.date?.setText(if (tradeOrder?.createdTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", tradeOrder.createdTime!!))
        viewHolder?.entrustPrice?.setText(if (tradeOrder?.price == null) "0" else NumberUtil.formatNumberNoGroup(tradeOrder?.price?.toDouble()))
        viewHolder?.entrustAmount?.setText(if (tradeOrder?.origQty == null || tradeOrder?.origQty?.toDouble() == 0.0) "0" else NumberUtil.formatNumberNoGroup(tradeOrder?.origQty?.toDouble(), amountPrecision, amountPrecision))
        val dealTotalAmount: Double = if (tradeOrder?.avgPrice != null && tradeOrder.origQty != null) tradeOrder?.avgPrice!!.toDouble().times(tradeOrder?.origQty!!.toDouble()) else 0.toDouble()
        viewHolder?.dealTotalAmount?.setText(NumberUtil.formatNumberNoGroup(dealTotalAmount))
        viewHolder?.dealPrice?.setText(if (tradeOrder?.avgPrice == null || tradeOrder?.avgPrice?.toDouble() == 0.0) "0" else NumberUtil.formatNumberNoGroup(tradeOrder?.avgPrice?.toDouble()))
        viewHolder?.dealAmount?.setText(if (tradeOrder?.dealQty == null || tradeOrder?.dealQty?.toDouble() == 0.0) "0" else NumberUtil.formatNumberNoGroup(tradeOrder?.dealQty?.toDouble(), amountPrecision, amountPrecision))
        when (tradeOrder?.state) {
            "NEW", "PARTIALLY_FILLED" -> {
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
        fun onPairClick(tradeOrder: TradeOrderFiex?)
        fun onHandleClick(tradeOrder: TradeOrderFiex?)
    }
}