package com.black.wallet.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.OrderBeanItem
import com.black.base.model.future.PlansBean
import com.black.base.util.FryingUtil
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.wallet.R
import com.black.wallet.databinding.ListTabContractPlanBinding
import skin.support.content.res.SkinCompatResources
import java.util.Calendar

class ContraLimitTabListAdapter(context: Context, variableId: Int, data: MutableList<OrderBeanItem>?) : BaseRecycleDataBindAdapter<OrderBeanItem, ListTabContractPlanBinding>(context, variableId, data){
    private var bgWin: Int? = null
    private var bgLose: Int? = null
    private var sideBgColor: Int? = null
    private var sideBlackColor: Int? = null
    private var bgDefault: Int? = null

    override fun resetSkinResources() {
        super.resetSkinResources()
        bgDefault = SkinCompatResources.getColor(context, R.color.T3)
        bgWin = SkinCompatResources.getColor(context, R.color.T10)
        bgLose = SkinCompatResources.getColor(context, R.color.T9)
    }

    override fun getResourceId(): Int {
        return R.layout.list_tab_contract_plan
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(
        holder: BaseViewHolder<ListTabContractPlanBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        val planData = getItem(position)
        val viewHolder = holder.dataBing
        val calendar: Calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        var sideDes: String? = null
        val unit = planData.symbol!!.split("_")[1].toString().uppercase()
        when (planData.positionSide) {
            //做多
            "LONG" -> {
                sideDes = getString(R.string.contract_see_up)
                sideBgColor = context.getColor(R.color.T17)
                sideBlackColor = context.getColor(R.color.T22)
            }
            //做空
            "SHORT" -> {
                sideDes = getString(R.string.contract_see_down)
                sideBgColor = context.getColor(R.color.T16)
                sideBlackColor = context.getColor(R.color.T21)
            }
        }
        viewHolder?.tvEntrustPrice?.text = String.format("委托价格(%s)", unit)
        viewHolder?.tvEntrustAmount?.text = String.format("数量(%s)", unit)
        viewHolder?.tvDealAmount?.text = String.format("手续费(%s)", unit)
        viewHolder?.tvProfitPrice?.text = String.format("成交均价(%s)", unit)
        viewHolder?.tvLosePrice?.text = String.format(context.getString(R.string.contract_deal_amount), unit)
        viewHolder?.tvBondAmount?.text = String.format("平仓盈亏(%s)", unit)
        viewHolder?.id?.text = planData.orderId
        //仓位描述
        viewHolder?.positionDes?.text = planData.symbol?.uppercase() + "永续"
        //方向
        viewHolder?.positionSide?.text = sideDes
        viewHolder?.positionSide?.setTextColor(sideBgColor!!)
        viewHolder?.positionSide?.setBackgroundColor(sideBlackColor!!)
        //订单类型
        viewHolder?.tvRevoke?.text =
            if (planData.state == "NEW") "新建订单（未成交）" else if (planData.state == "PARTIALLY_FILLED") "部分成交" else if (planData.state == "PARTIALLY_CANCELED") "部分撤销" else if (planData.state == "FILLED") "全部成交" else if (planData.state == "CANCELED") "已撤销" else if (planData.state == "REJECTED") "下单失败" else "已过期"
        //创建时间
        viewHolder?.tvCreateTime?.text =
            CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", planData.createdTime!!)
        viewHolder?.time?.text = String.format("$year/$month/$day $hour:$minute:$second")
        //开仓均价
        viewHolder?.tvEntrustPriceDes?.text = planData.price
        //委托数量
        viewHolder?.tvEntrustAmountDes?.text = planData.origQty.toString()
        //手续费
        viewHolder?.tvDealAmountDes?.text = "--"
        //成交均价
        viewHolder?.tvProfitPriceDes?.text = planData.avgPrice
        //成交数量
        viewHolder?.tvLosePriceDes?.text = planData.executedQty.toString()
        //平仓盈亏
        viewHolder?.tvBondAmountDes?.text = if (planData.closeProfit == null ) nullAmount else planData.closeProfit
        viewHolder?.id?.setOnClickListener {
            if (CommonUtil.copyText(
                    context,
                    if (planData.orderId == null) "" else planData.orderId
                )
            ) {
                FryingUtil.showToast(context, context.getString(R.string.copy_text_success))
            } else {
                FryingUtil.showToast(context, context.getString(R.string.copy_text_failed))
            }
        }
    }
        //撤销
        /*viewHolder?.tvRevoke?.setOnClickListener {
            FutureApiServiceHelper.cancelOrderId(
                context,
                planData.clientOrderId,
                true,
                object : Callback<HttpRequestResultBean<String>?>() {
                    override fun callback(returnData: HttpRequestResultBean<String>?) {
                        if (returnData != null) {
                            Log.d("iiiiii-->cancel profit stop by id", returnData.result.toString())
                        }
                    }
                    override fun error(type: Int, error: Any?) {
                        Log.d("iiiiii-->cancel profit stop by id--error", error.toString())
                    }
                })
        }
    }

         */

}