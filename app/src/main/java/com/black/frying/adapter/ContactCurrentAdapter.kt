package com.black.frying.adapter

import android.content.Context
import android.util.Log
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.OrderBeanItem
import com.black.util.Callback
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemContractTabPlanBinding
import skin.support.content.res.SkinCompatResources

class ContactCurrentAdapter(context: Context, data: MutableList<OrderBeanItem>?) : BaseDataTypeBindAdapter<OrderBeanItem ,ListItemContractTabPlanBinding>(context, data){
    private var bgWin: Int? = null
    private var bgLose: Int? = null
    private var bgDefault: Int? = null
    var sideBgColor: Int? = null
    var sideBlackColor: Int? = null

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
                sideBgColor = context.getColor(R.color.T17)
                sideBlackColor = context.getColor(R.color.T22)
            }
            //做空
            "SHORT" ->{
                sideDes = getString(R.string.contract_see_down)
                sideBgColor = context.getColor(R.color.T16)
                sideBlackColor = context.getColor(R.color.T21)
            }
        }
        viewHolder?.positionSide?.setTextColor(sideBgColor!!)
        viewHolder?.positionSide?.setBackgroundColor(sideBlackColor!!)
        //仓位描述
        viewHolder?.positionDes?.text = planData?.symbol.toString().uppercase()
        //方向
        viewHolder?.positionSide?.text = sideDes
        //订单类型
        viewHolder?.type?.text = getString(R.string.jihua_price)
        viewHolder?.tvDealAmountDes?.text = if (planData?.state == "NOT_TRIGGERED") "新建委托（未触发）" else if (planData?.state == "TRIGGERING")  "触发中" else if (planData?.state == "TRIGGERED") "已触发" else if (planData?.state == "USER_REVOCATION") "用户撤销" else if (planData?.state == "PLATFORM_REVOCATION")"平台撤销（拒绝）" else  "已过期"
        //创建时间
        viewHolder?.tvCreateTime?.text = String.format("HHBBXX ccaa", planData?.createdTime)
        //开仓均价
        viewHolder?.tvEntrustPriceDes?.text = ""
        //委托数量
        viewHolder?.tvEntrustAmountDes?.text = ""
        //止盈价格
        viewHolder?.tvProfitPriceDes?.text = planData?.price
        //占用保证金
        viewHolder?.tvBondAmountDes?.text = "--"
        //撤销
        viewHolder?.tvRevoke?.setOnClickListener {
            FutureApiServiceHelper.cancelPlanById(
                context,
                planData.sourceId.toString(),
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