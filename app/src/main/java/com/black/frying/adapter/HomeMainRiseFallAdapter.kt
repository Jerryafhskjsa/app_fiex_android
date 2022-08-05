package com.black.frying.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import com.black.base.adapter.BaseDataTypeBindAdapter
import com.black.base.model.socket.PairStatus
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListItemHomeMainRiseFallBinding
import skin.support.content.res.SkinCompatResources

class HomeMainRiseFallAdapter(context: Context, data: ArrayList<PairStatus?>?) : BaseDataTypeBindAdapter<PairStatus?, ListItemHomeMainRiseFallBinding>(context, data) {
    private var bgDefault: Drawable? = null
    private var bgWin: Drawable? = null
    private var bgLose: Drawable? = null

    override fun resetSkinResources() {
        super.resetSkinResources()
        bgDefault = SkinCompatResources.getDrawable(context, R.drawable.bg_t3_corner3)
        bgWin = SkinCompatResources.getDrawable(context, R.drawable.bg_t7_corner3)
        bgLose = SkinCompatResources.getDrawable(context, R.drawable.bg_t5_corner3)
    }

    override fun getItemLayoutId(): Int {
        return R.layout.list_item_home_main_rise_fall
    }

    override fun bindView(position: Int, holder: ViewHolder<ListItemHomeMainRiseFallBinding>?) {
        val pairStatus = getItem(position)
        pairStatus?.currentPrice = (pairStatus?.currentPrice ?: 0.0)
        pairStatus?.setCurrentPriceCNY(pairStatus.currentPriceCNY, nullAmount)
        pairStatus?.priceChangeSinceToday = pairStatus?.priceChangeSinceToday
        val viewHolder = holder?.dataBing
        val bgColor = if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) colorDefault else if (pairStatus.priceChangeSinceToday!! > 0) colorWin else colorLost
        val bg = if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) bgDefault!! else if (pairStatus.priceChangeSinceToday!! > 0) bgWin!! else bgLose!!
        viewHolder?.pairName?.setText(pairStatus?.name)
//        viewHolder?.setName?.setText(pairStatus?.setName)
        viewHolder?.price?.setText(pairStatus?.currentPriceFormat)
//        viewHolder?.priceCny?.setText("¥ " + pairStatus?.currentPriceCNYFormat)
        viewHolder?.since?.setText(pairStatus?.priceChangeSinceTodayFormat)
//        viewHolder?.since?.background = bg
        //折线图假数据
        var xdata = arrayOf("0","1","2","3","4","5","6","7","8","9")
        var ydata:IntArray = intArrayOf(0,10,20,30,40,50,60,70,80,90)
        var linedata:FloatArray = floatArrayOf(5f,10f,6f,30f,5f,62.5f,6f,2f,3f,6f)
        viewHolder?.lineCartTab?.setChartdate(xdata,ydata,linedata, Color.GREEN)
    }

}