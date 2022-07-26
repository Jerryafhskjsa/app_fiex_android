package com.black.money.adpter

import android.content.Context
import android.graphics.drawable.Drawable
import com.black.base.adapter.BaseRecycleDataBindAdapter
import com.black.base.adapter.interfaces.BaseViewHolder
import com.black.base.model.money.SupportCoin
import com.black.money.R
import com.black.money.databinding.ListItemPromotionsBuyCoinBinding
import skin.support.content.res.SkinCompatResources

class PromotionsBuyCoinAdapter(context: Context, variableId: Int, data: ArrayList<SupportCoin?>?) : BaseRecycleDataBindAdapter<SupportCoin?, ListItemPromotionsBuyCoinBinding>(context, variableId, data) {
    private var bgDef: Drawable? = null
    private var bgDis: Drawable? = null
    private var defColor = 0
    private var disColor = 0
    private var checkedIndex = 0

    override fun resetSkinResources() {
        super.resetSkinResources()
        bgDef = SkinCompatResources.getDrawable(context, R.drawable.bg_border_c1_corner_2)
        bgDis = SkinCompatResources.getDrawable(context, R.drawable.bg_b1_corner_2)
        defColor = SkinCompatResources.getColor(context, R.color.C1)
        disColor = SkinCompatResources.getColor(context, R.color.T1)
    }

    override fun getResourceId(): Int {
        return R.layout.list_item_promotions_buy_coin
    }

    override fun onBindViewHolder(holder: BaseViewHolder<ListItemPromotionsBuyCoinBinding>, position: Int) {
        super.onBindViewHolder(holder, position)
        val supportCoin = getItem(position)
        val viewHolder = holder.dataBing
        if (viewHolder?.coinType != null) {
            viewHolder?.coinType.text = if (supportCoin?.coin == null) "" else supportCoin.coin
            if (checkedIndex != position) {
                viewHolder?.coinType.background = bgDis
                viewHolder?.coinType.setTextColor(disColor)
            } else {
                viewHolder?.coinType.background = bgDef
                viewHolder?.coinType.setTextColor(defColor)
            }
        }
    }


    fun getCheckedIndex(): Int {
        return checkedIndex
    }

    fun setCheckedIndex(checkedIndex: Int) {
        this.checkedIndex = checkedIndex
    }
}