package com.black.frying.adapter

import android.content.Context
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.model.socket.PairStatus
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemHomeQuotationDetailBinding
import skin.support.content.res.SkinCompatResources

class QuotationColletAdapter(context: Context, data: MutableList<PairStatus?>?) : BaseDataTypeBindAdapter<PairStatus?,ListItemHomeQuotationDetailBinding>(context, data){
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
        return R.layout.quotation_collet_list
    }

    override fun bindView(position: Int, holder: ViewHolder<ListItemHomeQuotationDetailBinding>?) {
        val planData = getItem(position)
        var viewHolder = holder?.dataBing
        //viewHolder?.pairNameOne?.setText(planData?.pair)
        //viewHolder?.setNameOne?.setText(planData?.setName)
    }


}