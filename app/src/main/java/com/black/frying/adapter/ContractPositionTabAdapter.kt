package com.black.frying.adapter

import android.content.Context
import android.util.Log
import android.view.View
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.PositionBean
import com.black.util.Callback
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemContractTabPositionBinding
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal

class ContractPositionTabAdapter(context: Context, data: MutableList<PositionBean?>?) :
    BaseDataTypeBindAdapter<PositionBean?, ListItemContractTabPositionBinding>(context, data){
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
        return R.layout.list_item_contract_tab_position
    }

    override fun bindView(position: Int, holder: ViewHolder<ListItemContractTabPositionBinding>?) {
        val positionData = getItem(position)
        var viewHolder = holder?.dataBing
        var sideDes: String? = null
        var sideBgColor: Int? = null
        var bondDes: String? = null
        var positionType: String? = null
        var autoMergeBond: Boolean? = positionData?.autoMargin
        when (positionData?.positionSide) {
            //做多
            "LONG" -> {
                sideDes = getString(R.string.contract_see_up)
                sideBgColor = context.getColor(R.color.T17)
            }
            //做空
            "SHORT" -> {
                sideDes = getString(R.string.contract_see_down)
                sideBgColor = context.getColor(R.color.T16)
            }
        }
        when (positionData?.positionType) {
            //逐仓
            "ISOLATED" -> {
                bondDes = positionData?.isolatedMargin
                positionType = getString(R.string.contract_fiexble_position)
            }
            //全仓
            "CROSSED" -> {
                bondDes = positionData?.openOrderMarginFrozen
                positionType = getString(R.string.contract_all_position)
            }
        }
        //仓位描述
        viewHolder?.positionDes?.text = positionData?.symbol.toString()
            .uppercase() + " " + positionType + "." + positionData?.leverage + "X"
        //方向
        viewHolder?.positionSide?.text = sideDes
        viewHolder?.positionSide?.setBackgroundColor(sideBgColor!!)
        //已实现盈亏
        viewHolder?.alreadyCloseProfit?.text = positionData?.realizedProfit
        //当前盈亏
        viewHolder?.profits?.text = positionData?.unRealizedProfit
        //当前盈亏百分比
        viewHolder?.profitsPercent?.text = positionData?.profitRate
        //持仓数量
        viewHolder?.positionAmount?.text = positionData?.positionSize
        //可平数量
        viewHolder?.availableCloseAmount?.text = positionData?.availableCloseSize
        //持仓均价
        viewHolder?.entryPrice?.text = positionData?.entryPrice
        //强平价格>0
        if (BigDecimal(positionData?.forceStopPrice).compareTo(BigDecimal.ZERO) == 1) {
            viewHolder?.forceClosePrice?.text = positionData?.forceStopPrice
        } else {
            viewHolder?.forceClosePrice?.text = "--"
        }
        //标记价格
        viewHolder?.flagPrice?.text = positionData?.flagPrice
        //保证金
        viewHolder?.bondAmount?.text = bondDes
        //是否自动追加保证金开关
        viewHolder?.autoAddBond?.isChecked = autoMergeBond!!
        viewHolder?.autoAddBond?.setOnCheckedChangeListener { _, isChecked ->
            FutureApiServiceHelper.autoMargin(
                context,
                positionData?.symbol,
                positionData?.positionSide,
                isChecked,
                true,
                object : Callback<HttpRequestResultBean<String>?>() {
                    override fun callback(returnData: HttpRequestResultBean<String>?) {
                        if (returnData != null) {
                            Log.d("iiiiii-->autoMargin", returnData.result.toString())
                        }
                    }

                    override fun error(type: Int, error: Any?) {
                        Log.d("iiiiii-->autoMargin--error", error.toString())
                    }
                })
        }
        when (positionData?.adl) {
            0 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_0)
            }
            1 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_0)
            }
            2 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_2)
            }
            3 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_3)
            }
            4 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_4)
            }
            5 -> {
                viewHolder?.itemPositionAdl!!.setImageResource(R.drawable.icon_adl_5)
            }
        }
        viewHolder?.btnClosePosition?.setOnClickListener {

        }

    }

}