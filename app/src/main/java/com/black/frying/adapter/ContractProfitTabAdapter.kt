package com.black.frying.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.ProfitsBean
import com.black.util.Callback
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemContractTabProfitBinding
import skin.support.content.res.SkinCompatResources

class ContractProfitTabAdapter(context: Context, data: MutableList<ProfitsBean?>?) : BaseDataTypeBindAdapter<ProfitsBean?, ListItemContractTabProfitBinding>(context, data){
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

    @SuppressLint("SetTextI18n")
    override fun bindView(position: Int, holder: ViewHolder<ListItemContractTabProfitBinding>?) {
        val profitData = getItem(position)
        val viewHolder = holder?.dataBing
        var sideDes:String? = null
        var bondDes:String? = null
        var positionType:String? = null
        when(profitData?.positionSide){
            //做多
            "LONG" ->{
                sideDes = getString(R.string.contract_see_up)
            }
            //做空
            "SHORT" ->{
                sideDes = getString(R.string.contract_see_down)
            }
        }
        when(profitData?.positionType){
            //逐仓
            "ISOLATED" -> {
                bondDes = profitData.isolatedMargin
                positionType = getString(R.string.contract_fiexble_position)
            }
            //全仓
            "CROSSED" -> {
                positionType = getString(R.string.contract_all_position)
            }
        }
        //仓位描述
        viewHolder?.positionDes?.text = profitData?.symbol + positionType
        //方向
        viewHolder?.positionSide?.text = sideDes
        //创建时间
        viewHolder?.tvCreateTime?.text = profitData?.createdTime.toString()
        //持仓均价
        viewHolder?.tvOpenPriceDes?.text = profitData?.entryPrice
        //预估强平价(本地计算)
        viewHolder?.tvBoomPriceDes?.text = "--"
       //数量
        viewHolder?.tvAmountDes?.text = profitData?.origQty
        //最新价(动态取)
        viewHolder?.tvNowPriceDes?.text = "--"
        //止盈价格
        viewHolder?.tvProfitPriceDes?.text = profitData?.triggerProfitPrice
        //止损价格
        viewHolder?.tvLosePriceDes?.text = profitData?.triggerStopPrice

        //撤销
        viewHolder?.tvRevoke?.setOnClickListener {
            FutureApiServiceHelper.cancelProfitStopById(
                context,
                profitData?.profitId,
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