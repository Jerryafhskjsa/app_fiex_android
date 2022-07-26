package com.black.base.lib.banner

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.black.base.R
import com.black.lib.banner.widget.banner.BaseIndicaorBanner

open class SimpleImageBanner @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : BaseIndicaorBanner<Int?, SimpleImageBanner?>(context, attrs, defStyle) {
    protected var colorDrawable: ColorDrawable = ColorDrawable(Color.parseColor("#555555"))
    override fun onTitleSlect(tv: TextView?, position: Int) {
        tv?.text = null
    }

    override fun onCreateItemView(position: Int): View {
        val inflate = View.inflate(mContext, R.layout.adapter_simple_image, null)
        val iv = inflate.findViewById<ImageView>(R.id.iv)
        val item = list!![position]!!
        val itemWidth = dm.widthPixels
        val itemHeight = (itemWidth * 360 * 1.0f / 640).toInt()
        iv.scaleType = ImageView.ScaleType.CENTER_CROP
        iv.layoutParams = LinearLayout.LayoutParams(itemWidth, itemHeight)
        if (item > 0) {
            iv.setImageResource(item)
        } else {
            iv.setImageDrawable(colorDrawable)
        }
        return inflate
    }

}