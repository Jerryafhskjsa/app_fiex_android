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
import com.fbsex.exchange.databinding.ListItemEntrustCurrentHomeBinding
import com.fbsex.exchange.databinding.ListItemEntrustRecordNewBinding
import skin.support.content.res.SkinCompatResources

class EntrustRecordNewAdapter(context: Context, variableId: Int, data: ArrayList<TradeOrderFiex?>?) : BaseRecycleDataBindAdapter<TradeOrderFiex?, ListItemEntrustCurrentHomeBinding>(context, variableId, data) {
    companion object {
        private const val TYPE_NEW = 0
        private const val TYPE_HIS = 1
    }

    private var onHandleClickListener: OnHandleClickListener? = null
    private var amountPrecision = 4
    private var c1 = 0
    private var t5: Int = 0
    private var b2: Int = 0
    private var type = TYPE_NEW

    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, R.color.C1)
        t5 = SkinCompatResources.getColor(context, R.color.T5)
        b2 = SkinCompatResources.getColor(context, R.color.B2)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_entrust_current_home
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemEntrustCurrentHomeBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val tradeOrder = getItem(position)
        val viewHolder: ListItemEntrustCurrentHomeBinding? = holder.dataBing
        var orderType = tradeOrder?.orderType
        var orderTypeDes:String? = null
        val unit1 = tradeOrder?.symbol?.split("_")?.get(1)
        val unit2 = tradeOrder?.symbol?.split("_")?.get(0)
        when(orderType){
            "LIMIT" -> orderTypeDes = getString(R.string.order_type_limit)
            "MARKET" -> orderTypeDes = getString(R.string.order_type_market)
        }
        val type = StringBuilder()
        //type.append(orderTypeDes)
        if (TextUtils.equals(tradeOrder?.orderSide, "SELL")) {
            viewHolder?.type?.setTextColor(t5)
            type.append(getString(R.string.entrust_type_sale))
        } else if (TextUtils.equals(tradeOrder?.orderSide, "BUY")) {
            viewHolder?.type?.setTextColor(c1)
            type.append(getString(R.string.entrust_type_buy))
        } else {
            viewHolder?.type?.setTextColor(c1)
        }
        //type.append(" ").append(if (tradeOrder?.symbol == null) "" else tradeOrder.symbol?.replace("_", "/"))
        viewHolder?.status?.setBackgroundColor(b2)
        viewHolder?.type?.setText(type.toString())
        viewHolder?.pairName?.setText(tradeOrder?.symbol)
        viewHolder?.type?.setOnClickListener {
            onHandleClickListener?.onPairClick(tradeOrder)
        }
        viewHolder?.cancel?.setOnClickListener {
            onHandleClickListener?.onHandleClick(tradeOrder)
        }
        viewHolder?.status?.setText(tradeOrder?.getStatusDisplay(context))
        viewHolder?.date?.setText(
            if (tradeOrder?.createdTime == null) nullAmount else CommonUtil.formatTimestamp(
                "yyyy/MM/dd HH:mm:ss",
                tradeOrder.createdTime!!
            )
        )
        viewHolder?.pairName?.setText(tradeOrder?.symbol)
        viewHolder?.entrustAmount?.setText(
            if (tradeOrder?.origQty == null || tradeOrder.origQty == 0.0.toString()) "0.0000" else NumberUtil.formatNumberNoGroup(
                tradeOrder.origQty!!.toDoubleOrNull(), amountPrecision, amountPrecision
            ) +  " " + unit2
        )
        viewHolder?.dealAmount?.setText(
            if (tradeOrder?.executedQty == null || tradeOrder.executedQty == 0.0.toString()) "0.0000" else NumberUtil.formatNumberNoGroup(
                tradeOrder.executedQty!!.toDoubleOrNull(), amountPrecision, amountPrecision
            ) + "/"
        )
        viewHolder?.priceDes?.setText(
            if (tradeOrder?.price == null || tradeOrder.price == "0") "0.00" else NumberUtil.formatNumberNoGroup(
                tradeOrder.price!!.toDoubleOrNull(), amountPrecision, amountPrecision
            ) +  " "  + unit1
        )
        viewHolder?.priceDes1?.setText(
            if (tradeOrder?.avgPrice == null || tradeOrder.avgPrice == "0" ) "0.00" else NumberUtil.formatNumberNoGroup(
                tradeOrder.avgPrice!!.toDoubleOrNull(), amountPrecision, amountPrecision
            )
        )
        viewHolder?.dealAmount1?.setText(
            if (tradeOrder?.avgPrice == null || tradeOrder.avgPrice == "0.00" || tradeOrder.executedQty == null || tradeOrder.executedQty == 0.0.toString()) "0" else NumberUtil.formatNumberNoGroup(
                tradeOrder.avgPrice!!.toDouble() * tradeOrder.executedQty !!.toDouble(), amountPrecision, amountPrecision
            ) + "/"
        )
        viewHolder?.entrustAmount1?.setText(
            if (tradeOrder?.price == null || tradeOrder.price == "0.00" || tradeOrder.origQty == null || tradeOrder.origQty == 0.0.toString()) "0" else NumberUtil.formatNumberNoGroup(
                tradeOrder.price!!.toDouble() * tradeOrder.origQty!!.toDouble(), amountPrecision, amountPrecision
            ) +  " "  + unit1
        )
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