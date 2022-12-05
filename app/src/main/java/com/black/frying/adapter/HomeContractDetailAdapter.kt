package com.black.frying.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.View
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.api.FutureApiService
import com.black.base.api.FutureApiServiceHelper
import com.black.base.model.HttpRequestResultBean
import com.black.base.model.future.PositionBean
import com.black.base.model.socket.PairStatus
import com.black.base.util.CookieUtil
import com.black.util.Callback
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemContractRecordDetailBinding
import com.fbsex.exchange.databinding.ListItemHomeQuotationDetailBinding
import skin.support.SkinCompatManager
import skin.support.content.res.SkinCompatResources

class HomeContractDetailAdapter(context: Context, data: MutableList<PositionBean?>?) : BaseDataTypeBindAdapter<PositionBean?, ListItemContractRecordDetailBinding>(context, data) {
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
        return R.layout.list_item_contract_record_detail
    }

    override fun bindView(position: Int, holder: ViewHolder<ListItemContractRecordDetailBinding>?) {
        val positionData = getItem(position)
        var viewHolder = holder?.dataBing
        var sideDes:String? = null
        var bondDes:String? = null
        var positionType:String? = null
        var autoMergeBond:Boolean? = positionData?.autoMargin
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
                bondDes = positionData?.openOrderMarginFrozen
                positionType = getString(R.string.contract_all_position)
            }
        }
        //仓位描述
        viewHolder?.positionDes?.text = positionData?.symbol +positionType+positionData?.leverage+"X"
        //方向
        viewHolder?.positionSide?.text = sideDes
        //已实现盈亏
        viewHolder?.alreadyCloseProfit?.text = positionData?.realizedProfit
        //当前盈亏
        viewHolder?.profits?.text = "--"
        //当前盈亏百分比
        viewHolder?.profitsPercent?.text = "--"
        //持仓数量
        viewHolder?.positionAmount?.text = positionData?.positionSize
        //可平数量
        viewHolder?.availableCloseAmount?.text = positionData?.availableCloseSize
        //持仓均价
        viewHolder?.entryPrice?.text = positionData?.entryPrice
        //强平价格
        viewHolder?.forceClosePrice?.text = "--"
        //标记价格
        viewHolder?.flagPrice?.text = "--"
        //保证金
        viewHolder?.bondAmount?.text = bondDes
        //是否自动追加保证金开关
        viewHolder?.autoAddBond?.isChecked = autoMergeBond!!
        viewHolder?.autoAddBond?.setOnCheckedChangeListener { _, isChecked ->
            FutureApiServiceHelper.autoMargin(context,positionData?.symbol,positionData?.positionSide,isChecked,true,object : Callback<HttpRequestResultBean<String>?>() {
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
    }

}