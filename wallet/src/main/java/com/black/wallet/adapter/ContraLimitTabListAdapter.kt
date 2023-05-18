package com.black.wallet.adapter

import android.content.Context
import android.util.Log
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.OrderBeanItem
import com.black.base.model.future.PlansBean
import com.black.util.Callback
import com.black.wallet.R
import com.black.wallet.databinding.ListTabContractPlanBinding
import skin.support.content.res.SkinCompatResources

    class ContraLimitTabListAdapter(context: Context, variableId: Int, data: MutableList<OrderBeanItem>?) : BaseRecycleDataBindAdapter<OrderBeanItem, ListTabContractPlanBinding>(context, variableId, data){
    private var bgWin: Int? = null
    private var bgLose: Int? = null
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

    override fun onBindViewHolder(
        holder: BaseViewHolder<ListTabContractPlanBinding>,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)
        val planData = getItem(position)
        val viewHolder = holder.dataBing
        var sideDes:String? = null
        var bondDes:String? = null
        val positionType:String? = null
        when(planData.positionSide){
            //做多
            "LONG" ->{
                sideDes = getString(R.string.contract_see_up)
            }
            //做空
            "SHORT" ->{
                sideDes = getString(R.string.contract_see_down)
            }
        }
        //仓位描述
        viewHolder?.positionDes?.text = planData.symbol + positionType
        //方向
        viewHolder?.positionSide?.text = sideDes
        //订单类型
        viewHolder?.tvType?.text = planData.state
        //创建时间
        viewHolder?.tvCreateTime?.text = planData.createdTime.toString()
        //开仓均价
        viewHolder?.tvEntrustPriceDes?.text = planData.price
        //委托数量
        viewHolder?.tvEntrustAmountDes?.text = planData.origQty.toString()
        //成交数量
        viewHolder?.tvDealAmountDes?.text = "--"
        //止盈价格
        viewHolder?.tvProfitPriceDes?.text = planData.triggerProfitPrice
        //止损价格
        viewHolder?.tvLosePriceDes?.text = planData.triggerStopPrice
        //占用保证金
        viewHolder?.tvBondAmountDes?.text = "--"
        //撤销
        viewHolder?.tvRevoke?.setOnClickListener {
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

}