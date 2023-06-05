package com.black.frying.adapter

import android.content.Context
import android.text.TextUtils
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.future.Constants
import com.black.base.model.socket.TradeOrder
import com.black.base.model.socket.TradeOrderFiex
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemEntrustCurrentHomeBinding
import skin.support.content.res.SkinCompatResources

//委托记录
class EntrustCurrentHomeAdapter(
    context: Context,
    variableId: Int,
    data: ArrayList<TradeOrderFiex?>?
) : BaseRecycleDataBindAdapter<TradeOrderFiex?, ListItemEntrustCurrentHomeBinding>(
    context,
    variableId,
    data
) {
    private var onHandleClickListener: OnHandleClickListener? = null
    private var amountPrecision: Int = 4
    private var c1 = 0
    private var t5: Int = 0

    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, R.color.T7)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_entrust_current_home
    }

    override fun onBindViewHolder(
        holder: BaseViewHolder<ListItemEntrustCurrentHomeBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        val tradeOrder: TradeOrderFiex? = getItem(position)
        val viewHolder = holder.dataBing
        val type = StringBuilder()
        val unit1 = tradeOrder?.symbol?.split("_")?.get(0)
        val unit2 = tradeOrder?.symbol?.split("_")?.get(0)
        when {
            TextUtils.equals(tradeOrder?.orderSide, "SELL") -> {
                viewHolder?.type?.setTextColor(t5)
                type.append(getString(R.string.entrust_type_sale))
            }
            TextUtils.equals(tradeOrder?.orderSide, "BUY") -> {
                viewHolder?.type?.setTextColor(c1)
                type.append(getString(R.string.entrust_type_buy))
            }
            else -> {
                viewHolder?.type?.setTextColor(c1)
            }
        }
        viewHolder?.type?.setText(type.toString())
        viewHolder?.status?.setOnClickListener {
            tradeOrder?.let {
                onHandleClickListener?.onHandleClick(tradeOrder)
            }
        }
        viewHolder?.date?.setText(
            if (tradeOrder?.createdTime == null) nullAmount else CommonUtil.formatTimestamp(
                "yyyy/MM/dd HH:mm:ss",
                tradeOrder.createdTime!!
            )
        )
        viewHolder?.pairName?.setText(tradeOrder?.symbol)
        viewHolder?.entrustAmount?.setText(
            if (tradeOrder?.origQty == null || tradeOrder.origQty == 0.0.toString()) "0" else NumberUtil.formatNumberNoGroup(
                tradeOrder.origQty!!.toDoubleOrNull(), amountPrecision, amountPrecision
            ) + unit1
        )
        viewHolder?.dealAmount?.setText(
            if (tradeOrder?.executedQty == null || tradeOrder.executedQty == 0.0.toString()) "0" else NumberUtil.formatNumberNoGroup(
                tradeOrder.executedQty!!.toDoubleOrNull(), amountPrecision, amountPrecision
            ) + "/"
        )
        viewHolder?.priceDes?.setText(
            if (tradeOrder?.price == null || tradeOrder.price == "0") "0" else NumberUtil.formatNumberNoGroup(
                tradeOrder.price!!.toDoubleOrNull(), amountPrecision, amountPrecision
            ) + unit2
        )
        viewHolder?.priceDes1?.setText(
            if (tradeOrder?.avgPrice == null || tradeOrder.avgPrice == "0" ) "0" else NumberUtil.formatNumberNoGroup(
                tradeOrder.avgPrice!!.toDoubleOrNull(), amountPrecision, amountPrecision
            ) + unit2
        )
        viewHolder?.dealAmount?.setText(
            if (tradeOrder?.avgPrice == null || tradeOrder.avgPrice == "0" || tradeOrder.executedQty == null || tradeOrder.executedQty == 0.0.toString()) "0" else NumberUtil.formatNumberNoGroup(
                tradeOrder.avgPrice!!.toDouble() * tradeOrder.executedQty !!.toDouble(), amountPrecision, amountPrecision
            ) + "/"
        )
        viewHolder?.dealAmount1?.setText(
            if (tradeOrder?.price == null || tradeOrder.price == "0" || tradeOrder.origQty == null || tradeOrder.origQty == 0.0.toString()) "0" else NumberUtil.formatNumberNoGroup(
                tradeOrder.price!!.toDouble() * tradeOrder.origQty!!.toDouble(), amountPrecision, amountPrecision
            ) + unit2
        )
    }

    fun setAmountPrecision(amountPrecision: Int) {
        this.amountPrecision = amountPrecision
        notifyDataSetChanged()
    }

    fun setOnHandleClickListener(onHandleClickListener: OnHandleClickListener?) {
        this.onHandleClickListener = onHandleClickListener
    }

    interface OnHandleClickListener {
        fun onHandleClick(tradeOrder: TradeOrderFiex)
    }
}