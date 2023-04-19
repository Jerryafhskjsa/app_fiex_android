package com.black.frying.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import android.widget.TextView
import com.black.base.adapter.BaseDataTypeAdapter
import com.black.frying.model.PairSearch
import com.bumptech.glide.Glide
import com.fbsex.exchange.R

class PairSearchAdapter(context: Context, data: MutableList<PairSearch?>?) : BaseDataTypeAdapter<PairSearch?>(context, data) {
    private var onSearchHandleListener: OnSearchHandleListener? = null

    override fun getView(position: Int, convertView1: View?, parent: ViewGroup): View? {
        var convertView = convertView1
        val pairSearch = getItem(position)
        when (getItemViewType(position)) {
            PairSearch.TITLE -> convertView = View.inflate(context, R.layout.list_item_search_pair_title, null)
            PairSearch.PAIR -> {
                convertView = View.inflate(context, R.layout.list_item_search_pair, null)
                val pairView = convertView.findViewById<TextView>(R.id.pair)
                pairView.text = if (pairSearch?.pair == null) "" else pairSearch.pair!!.replace("_", "/")
                //convertView.findViewById<ImageView>(R.id.image).setImageBitmap()
               /* convertView.findViewById<ImageView>(R.id.image).let {
                    Glide.with(context)
                        .load(Uri.parse(pairSearch?.baseCoinPrecision))
                        //.apply(RequestOptions.bitmapTransform(CircleCrop()).error(R.drawable.icon_avatar))
                        .into(it)
                }*/
                convertView.findViewById<TextView>(R.id.price).text = pairSearch?.currentPrice.toString()
                convertView.findViewById<TextView>(R.id.zeng).text = pairSearch?.priceChangeSinceToday.toString()
                pairView.setOnClickListener {
                    pairSearch?.let {
                        onSearchHandleListener?.onPairClick(pairSearch)
                    }
                }
                val btnCollect = convertView.findViewById<ImageView>(R.id.btn_collect)
                var isDear = pairSearch?.is_dear
                var img = if(isDear == true){
                    context.getDrawable(R.drawable.bianzu_2)
                }else{
                    context.getDrawable(R.drawable.bianzu_1)

                }
                btnCollect.setImageDrawable(img)
                btnCollect.setOnClickListener {
                    pairSearch?.let {
                        onSearchHandleListener?.onCollect(pairSearch)
                    }
                }
            }
            PairSearch.DELETE -> {
                convertView = View.inflate(context, R.layout.list_item_search_pair_title, null)
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