package com.black.frying.adapter

import android.content.Context
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.model.future.ProfitsBean
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemContractTabProfitBinding
import skin.support.content.res.SkinCompatResources

class ContractProfitTabAdapter(context: Context, data: MutableList<ProfitsBean?>?) : BaseDataTypeBindAdapter<ProfitsBean?, ListItemContractTabProfitBinding>(context, data) {
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
        return R.layout.list_item_contract_tab_profit
    }

    override fun bindView(position: Int, holder: ViewHolder<ListItemContractTabProfitBinding>?) {
        val positionData = getItem(position)
        var viewHolder = holder?.dataBing
        var sideDes:String? = null
        var bondDes:String? = null
        var positionType:String? = null
        when(positionData?.positionSide){
            //做多
            "LONG" ->{
                sideDes = getString(R.string.contract_see_up)
            }
            //做空
            "SHORT" ->{
                sideDes = getString(R.string.contract_see_down)
            }
        }
        when(positionData?.positionType){
            //逐仓
            "ISOLATED" -> {
                bondDes = positionData?.isolatedMargin
                positionType = getString(R.string.contract_fiexble_position)
            }
            //全仓
            "CROSSED" -> {
                positionType = getString(R.string.contract_all_position)
            }
        }
        //仓位描述
//        viewHolder?.positionDes?.text = positionData?.symbol +positionType+positionData?.leverage+"X"
        //方向
        viewHolder?.positionSide?.text = sideDes
        //已实现盈亏
//        viewHolder?.alreadyCloseProfit?.text = positionData?.realizedProfit
        //当前盈亏
        viewHolder?.profits?.text = "--"
        //当前盈亏百分比
        viewHolder?.profitsPercent?.text = "--"
        //持仓数量
        viewHolder?.positionAmount?.text = positionData?.positionSize
        //可平数量
//        viewHolder?.availableCloseAmount?.text = positionData?.availableCloseSize
        //持仓均价
        viewHolder?.entryPrice?.text = positionData?.entryPrice
        //强平价格
        viewHolder?.forceClosePrice?.text = "--"
        //标记价格
        viewHolder?.flagPrice?.text = "--"
        //保证金
        viewHolder?.bondAmount?.text = bondDes
    }

}