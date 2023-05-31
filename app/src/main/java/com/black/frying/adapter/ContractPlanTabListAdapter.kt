package com.black.frying.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.PlansBean
import com.black.base.util.FryingUtil
import com.black.frying.service.FutureService
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.wallet.R
import com.black.wallet.databinding.ListTabContractPlanBinding
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import java.util.Calendar

class ContractPlanTabListAdapter(context: Context, variableId: Int, data: MutableList<PlansBean?>?) : BaseRecycleDataBindAdapter<PlansBean?, ListTabContractPlanBinding>(context, variableId, data){
    private var bgWin: Int? = null
    private var bgLose: Int? = null
    private var sideBgColor: Int? = null
    private var sideBlackColor: Int? = null
    private var num: String? = null
    private var bgDefault: Int? = null
    private var bgGray: Int? = null

    override fun resetSkinResources() {
        super.resetSkinResources()
        bgGray = SkinCompatResources.getColor(context, R.color.T2)
        bgDefault = SkinCompatResources.getColor(context, R.color.T13)
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
        var sideDes: String? = null
        val calendar: Calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val unit = planData?.symbol!!.split("_")[1].toString().uppercase()
        val contractSize = FutureService.getContractSize(planData.symbol)?: BigDecimal(0.0001)
        val num2 = BigDecimal(planData.origQty.toString()).minus(contractSize.minus(BigDecimal(planData.price)))
        when (planData.orderSide) {
            //做多
            "BUY" -> {
                sideBgColor = context.getColor(R.color.T17)
                sideBlackColor = context.getColor(R.color.T22)
                when (planData.positionSide) {
                    "LONG" -> {
                        num = "≥" + planData.stopPrice
                        sideDes = getString(R.string.contract_buy_raise)
                    }
                    //做空
                    "SHORT" -> {
                        num = "≤" + planData.stopPrice
                        sideDes = getString(R.string.contract_buy_fall)
                    }
                }
            }
            "SELL" -> {
                sideBgColor = context.getColor(R.color.T16)
                sideBlackColor = context.getColor(R.color.T21)
                when(planData.positionSide) {
                    "LONG" -> {
                        num = "≥" + planData.stopPrice
                        sideDes = getString(R.string.contract_sell_raise)
                    }
                    //做空
                    "SHORT" -> {
                        num = "≤" + planData.stopPrice
                        sideDes = getString(R.string.contract_sell_fall)
                    }
                }
            }
        }
        viewHolder?.one?.visibility = View.GONE
        viewHolder?.two?.visibility = View.GONE
        viewHolder?.youla?.visibility = View.GONE
        viewHolder?.tvEntrustPrice?.text = String.format("委托价格(%s)", unit)
        viewHolder?.tvEntrustAmount?.text = String.format("数量(%s)", unit)
        viewHolder?.tvDealAmount?.text = String.format("触发价格(%s)", unit)
        viewHolder?.tvProfitPrice?.text = String.format("保证金(%s)", unit)
        viewHolder?.tvLosePrice?.text = String.format(context.getString(R.string.contract_deal_amount), unit)
        viewHolder?.tvBondAmount?.text = String.format("平仓盈亏(%s)", unit)
        viewHolder?.id?.text = planData.entrustId
        //仓位描述
        viewHolder?.positionDes?.text = planData.symbol?.uppercase() + "永续"
        //方向
        viewHolder?.positionSide?.text = sideDes
        viewHolder?.positionSide?.setTextColor(sideBgColor!!)
        viewHolder?.positionSide?.setBackgroundColor(sideBlackColor!!)
        //订单类型
        viewHolder?.tvRevoke?.text =
            if (planData.state == "NOT_TRIGGERED") "新建订单（未成交）" else if (planData.state == "TRIGGERING") "触发中" else if (planData.state == "TRIGGERED") "已触发" else if (planData.state == "USER_REVOCATION") "用户撤销" else if (planData.state == "PLATFORM_REVOCATION") "平台撤销(拒绝)"  else "已过期"
        //创建时间
        viewHolder?.tvCreateTime?.text =
            CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", planData.createdTime!!)
        viewHolder?.tvCreateTime?.text =
            CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", planData.createdTime!!)
        viewHolder?.time?.text = String.format("$year/$month/$day $hour:$minute:$second")
        //开仓均价
        viewHolder?.tvEntrustPriceDes?.text = planData.price
        //委托数量
        viewHolder?.tvEntrustAmountDes?.text = num2.toString()
        //手续费
        viewHolder?.tvDealAmountDes?.text = num
        //成交均价
        viewHolder?.tvProfitPriceDes?.text = "--"
        //成交数量
        viewHolder?.tvLosePriceDes?.text = planData.origQty.toString()
        //平仓盈亏
        viewHolder?.tvBondAmountDes?.text = "--"
        viewHolder?.id?.setOnClickListener {
            if (CommonUtil.copyText(
                    context,
                    if (planData.entrustId == null) "" else planData.entrustId
                )
            ) {
                FryingUtil.showToast(context, context.getString(R.string.copy_text_success))
            } else {
                FryingUtil.showToast(context, context.getString(R.string.copy_text_failed))
            }
        }
    //撤销
        if (planData.state == "NOT_TRIGGERED")
        {
            viewHolder?.tvRevoke?.setTextColor(bgDefault!!)
            viewHolder?.tvRevoke?.setOnClickListener {
                FutureApiServiceHelper.cancelPlanById(
                    context,
                    planData.entrustId,
                    true,
                    object : Callback<HttpRequestResultBean<String>?>() {
                        override fun callback(returnData: HttpRequestResultBean<String>?) {
                            if (returnData != null) {
                                viewHolder.tvRevoke.isClickable = false
                                viewHolder.tvRevoke.setText("用户撤销")
                                viewHolder.tvRevoke.setTextColor(bgGray!!)
                                Log.d("iiiiii-->cancel profit stop by id", returnData.result.toString())
                            }
                        }
                        override fun error(type: Int, error: Any?) {
                            Log.d("iiiiii-->cancel profit stop by id--error", error.toString())
                        }
                    })
            }
        }
        else{
            viewHolder?.tvRevoke?.setTextColor(bgGray!!)
        }
}



}