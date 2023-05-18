package com.black.frying.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import com.black.base.adapter.BaseDataTypeAdapter
import com.black.frying.model.PairSearch
import com.fbsex.exchange.R
import skin.support.content.res.SkinCompatResources

class PairSearchAdapter(context: Context, data: MutableList<PairSearch?>?) : BaseDataTypeAdapter<PairSearch?>(context, data) {
    private var onSearchHandleListener: OnSearchHandleListener? = null
    private var c1 = 0
    private var t5 = 0

    override fun resetSkinResources() {
        super.resetSkinResources()
        c1 = SkinCompatResources.getColor(context, com.black.wallet.R.color.T7)
        t5 = SkinCompatResources.getColor(context, com.black.wallet.R.color.T5)
    }

    @SuppressLint("CutPasteId")
    override fun getView(position: Int, convertView1: View?, parent: ViewGroup): View? {
        var convertView = convertView1
        val pairSearch = getItem(position)
        when (getItemViewType(position)) {
            PairSearch.TITLE -> {
                convertView = View.inflate(context, R.layout.list_item_search_pair_title, null)
               // convertView.findViewById<TextView>(R.id.pair).text = if (pairSearch?.pair == null) "" else pairSearch.pair!!.replace("_", "/")
                convertView.findViewById<View>(R.id.btn_action).setOnClickListener {
                    if (onSearchHandleListener != null) {
                        onSearchHandleListener?.onDelete()
                    }
                }
            }
            PairSearch.PAIR -> {
                convertView = View.inflate(context, R.layout.list_item_search_pair, null)
                val pairView = convertView.findViewById<TextView>(R.id.pair)
                val dianJi = convertView.findViewById<View>(R.id.dianji)
                convertView.findViewById<TextView>(R.id.price).text = if (pairSearch?.currentPrice == null)"0.0" else pairSearch.currentPrice.toString()
                convertView.findViewById<TextView>(R.id.price).setTextColor(if (pairSearch?.currentPrice == null || pairSearch.currentPrice <= 0) t5 else c1)
                convertView.findViewById<TextView>(R.id.price_cny).text = if (pairSearch?.priceChangeSinceToday == null)"0.0%" else pairSearch.priceChangeSinceToday.toString() + "%"
                val price = if (pairSearch?.priceChangeSinceToday == null) 0.0 else pairSearch.priceChangeSinceToday
                convertView.findViewById<TextView>(R.id.price_cny).setTextColor(if (price!! <= 0 ) t5 else c1)
                pairView.text = if (pairSearch?.pair == null) "" else pairSearch.pair!!.replace("_", "/")
                dianJi.setOnClickListener {
                    pairSearch?.let {
                        onSearchHandleListener?.onPairClick(pairSearch)
                    }
                }
                val btnCollect = convertView.findViewById<ImageView>(R.id.btn_collect)
                val isDear = pairSearch?.is_dear
                val img = if(isDear == true){
                    context.getDrawable(R.drawable.bianzu_1)
                }else{
                    context.getDrawable(R.drawable.bianzu3)
                }
                btnCollect.setImageDrawable(img)
                btnCollect.setOnClickListener {
                    pairSearch?.let {
                        onSearchHandleListener?.onCollect(pairSearch)
                    }
                }
            }
            PairSearch.DELETE -> {
                convertView = View.inflate(context, R.layout.list_item_search_pair_delete, null)
                convertView.findViewById<View>(R.id.btn_action).setOnClickListener {
                    if (onSearchHandleListener != null) {
                        onSearchHandleListener?.onDelete()
                    }
                }
            }
        }
        return convertView
    }

    fun setOnSearchHandleListener(onSearchHandleListener: OnSearchHandleListener?) {
        this.onSearchHandleListener = onSearchHandleListener
    }

    interface OnSearchHandleListener {
        fun onDelete()
        fun onCollect(pairSearch: PairSearch)
        fun onPairClick(pairSearch: PairSearch)
    }
}