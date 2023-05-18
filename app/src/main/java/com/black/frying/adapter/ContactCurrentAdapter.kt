package com.black.frying.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.OrderBeanItem
import com.black.util.Callback
import com.black.util.CommonUtil
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

    @SuppressLint("SetTextI18n")
    override fun bindView(position: Int, holder: ViewHolder<ListItemContractTabPlanBinding>?) {
        val planData = getItem(position)
        var viewHolder = holder?.dataBing
        var sideDes:String? = null
        var bondDes:String? = null
        var positionType:String? = null
        val unit = planData.symbol!!.split("_")[1].toString().uppercase()
        when(planData.positionSide){
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
        viewHolder?.tvEntrustPrice?.setText(getString(R.string.price) +"(" + unit + ")")
        viewHolder?.typeOne?.setText( getString((R.string.bao_amount)) + "(" + unit + ")")
        viewHolder?.tvDealAmount?.setText(getString(R.string.type))
        viewHolder?.tvProfitPrice?.setText(getString((R.string.junjia)) + "(" + unit + ")")
        viewHolder?.tvEntrustAmount?.setText(getString((R.string.contract_bond)))
        viewHolder?.tvBondAmount?.setText(getString((R.string.contract_with_limit)) + "(" + unit + ")")

        //仓位描述
        viewHolder?.positionDes?.text = planData.symbol.toString().uppercase()
        //方向
        viewHolder?.positionSide?.text = sideDes
        //订单类型
        viewHolder?.type?.text = planData.origQty.toString() + "/" + planData.executedQty
        viewHolder?.tvDealAmountDes?.text = "限价"
        //创建时间
        viewHolder?.tvCreateTime?.text = CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm", planData.createdTime!!)
        //成交均价
        viewHolder?.tvEntrustPriceDes?.text = planData.price
        //保证金
        viewHolder?.tvEntrustAmountDes?.text = planData.marginFrozen
        //止盈价格
        viewHolder?.tvProfitPriceDes?.text = planData.avgPrice
        //zhiyinzhisun
        viewHolder?.tvBondAmountDes?.text = planData.triggerProfitPrice.toString() + "/" + planData.triggerProfitPrice
        //撤销
        viewHolder?.tvRevoke?.setOnClickListener {
            FutureApiServiceHelper.cancelOrderId(
                context,
                planData.sourceId.toString(),
                true,
                object : Callback<HttpRequestResultBean<String>?>() {
                    override fun callback(returnData: HttpRequestResultBean<String>?) {
                        if (returnData != null) {
                            //Log.d("iiiiii-->cancel profit stop by id", returnData.result.toString())
                        }
                    }
                    override fun error(type: Int, error: Any?) {
                        //Log.d("iiiiii-->cancel profit stop by id--error", error.toString())
                    }
                })
        }
    }

}