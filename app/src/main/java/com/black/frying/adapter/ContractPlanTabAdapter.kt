package com.black.frying.adapter

import android.content.Context
import android.util.Log
import android.view.View
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.PlansBean
import com.black.util.Callback
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemContractTabPlanBinding
import skin.support.content.res.SkinCompatResources

class ContractPlanTabAdapter(context: Context, data: MutableList<PlansBean?>?) : BaseDataTypeBindAdapter<PlansBean?, ListItemContractTabPlanBinding>(context, data){
    private var bgWin: Int? = null
    private var bgLose: Int? = null
    private var bgDefault: Int? = null

    override fun resetSkinResources() {
        super.resetSkinResources()
        bgDefault = SkinCompatResources.getColor(context, R.color.T3)
        bgWin = SkinCompatResources.getColor(context, R.color.T10)
        bgLose = SkinCompatResources.getColor(context, R.color.T9)
    }

    override fun getItemLayoutId(): Int {
        return R.layout.list_item_contract_tab_plan
    }

    override fun bindView(position: Int, holder: ViewHolder<ListItemContractTabPlanBinding>?) {
        val planData = getItem(position)
        var viewHolder = holder?.dataBing
        var sideDes:String? = null
        var bondDes:String? = null
        var positionType:String? = null
        when(planData?.positionSide){
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
        viewHolder?.positionDes?.text = planData?.symbol +positionType
        //方向
        viewHolder?.positionSide?.text = sideDes
        //订单类型
        viewHolder?.tvType?.text = planData?.state
        //创建时间
        viewHolder?.tvCreateTime?.text = planData?.createdTime.toString()
        //开仓均价
        viewHolder?.tvEntrustPriceDes?.text = planData?.price
        //委托数量
        viewHolder?.tvEntrustAmountDes?.text = planData?.origQty
        //成交数量
        viewHolder?.tvDealAmountDes?.text = "--"
        //止盈价格
        viewHolder?.tvProfitPriceDes?.text = planData?.triggerProfitPrice
        //止损价格
        viewHolder?.tvLosePriceDes?.text = planData?.triggerStopPrice
        //占用保证金
        viewHolder?.tvBondAmountDes?.text = "--"
        //撤销
        viewHolder?.tvRevoke?.setOnClickListener {
            FutureApiServiceHelper.cancelPlanById(
                context,
                planData?.entrustId,
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