package com.black.base.adapter

import android.content.Context
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.black.base.R
import com.black.base.model.PairStatusShowPopup
import com.black.base.model.socket.PairStatus
import com.black.base.util.CookieUtil
import com.black.base.util.StyleChangeUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources

class PairStatusAdapter(
    context: Context,
    data: MutableList<PairStatusShowPopup?>?,
    private val dataType: Int
) : BaseDataTypeAdapter<PairStatusShowPopup?>(context, data) {
    private var TAG = PairStatusAdapter::class.java.simpleName
    private var colorSelect = 0
    private var colorTransparent = 0
    private val currentPair: String? =
        if (dataType == PairStatus.LEVER_DATA) CookieUtil.getCurrentPairLever(context) else CookieUtil.getCurrentPair(
            context
        )

    override fun resetSkinResources() {
        super.resetSkinResources()
        colorTransparent = SkinCompatResources.getColor(context, R.color.transparent)
        colorSelect = SkinCompatResources.getColor(context, R.color.L1_ALPHA30)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var view = convertView
        val pairStatus = getItem(position)
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_pair_status, null)
            val viewHolder = ViewHolder()
            viewHolder.pairNameView = view.findViewById(R.id.pair_name)
            viewHolder.setNameView = view.findViewById(R.id.set_name)
            viewHolder.maxLeverView = view.findViewById(R.id.max_lever)
            viewHolder.priceView = view.findViewById(R.id.price)
            viewHolder.sinceView = view.findViewById(R.id.raise_fall)
            viewHolder.hotView = view.findViewById(R.id.icon_hot)
            view.tag = viewHolder
        }
        bindView(view!!, pairStatus)
        return view
    }

    private fun bindView(convertView: View, pairStatus: PairStatusShowPopup?) {
        val viewHolder = convertView.tag as ViewHolder
        if (dataType == PairStatus.LEVER_DATA) {
            val maxMultiple =
                if (pairStatus?.leverConfEntity == null) null else pairStatus.leverConfEntity?.maxMultiple
            val max = maxMultiple?.toInt()
            viewHolder.maxLeverView?.visibility = View.VISIBLE
            viewHolder.maxLeverView?.text = String.format(
                "%sX",
                if (max == null) nullAmount else NumberUtil.formatNumberNoGroup(max)
            )
        } else {
            viewHolder.maxLeverView?.visibility = View.GONE
        }
        if (viewHolder.pairNameView != null) {
            viewHolder.pairNameView?.text = pairStatus?.name.toString().uppercase()
        }
        if (viewHolder.setNameView != null) {
            viewHolder.setNameView?.text = pairStatus?.setName.toString().uppercase()
        }
        if (viewHolder.priceView != null) {
            val color = colorDefault
            viewHolder.priceView?.text = pairStatus?.currentPriceFormat
            viewHolder.priceView?.setTextColor(color)
        }
        if (viewHolder.sinceView != null) {
            var styleChange = StyleChangeUtil.getStyleChangeSetting(context)?.styleCode
            if (styleChange == 1) {
                val color =
                    if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) colorDefault else if (pairStatus.priceChangeSinceToday!! < 0) colorWin else colorLost
                viewHolder.sinceView?.text = pairStatus?.priceChangeSinceTodayFormat
                viewHolder.sinceView?.setTextColor(color)
            }
            if (styleChange == 0) {
                val color =
                    if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) colorDefault else if (pairStatus.priceChangeSinceToday!! > 0) colorWin else colorLost
                viewHolder.sinceView?.text = pairStatus?.priceChangeSinceTodayFormat
                viewHolder.sinceView?.setTextColor(color)
            }
        }
        if (viewHolder.hotView != null) {
            var visiable: Int? = if (pairStatus?.isHot == true) {
                View.VISIBLE
            } else {
                View.GONE
            }
            if (visiable != null) {
                viewHolder.hotView?.visibility = visiable
            }
        }
        if (TextUtils.equals(currentPair, pairStatus?.pair)) {
            convertView.setBackgroundColor(colorSelect)
        } else {
            convertView.setBackgroundColor(colorTransparent)
        }
    }

    inner class ViewHolder {
        var coinIconView: ImageView? = null
        var pairNameView: TextView? = null
        var setNameView: TextView? = null
        var maxLeverView: TextView? = null
        var nameView: TextView? = null
        var hotView: ImageView? = null
        var priceView: TextView? = null
        var sinceView: TextView? = null
    }

}