package com.black.base.lib.refreshlayout.defaultview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.black.base.R
import com.black.lib.refresh.LoadView

/**
 * Created by zhangxiaoqi on 2019/4/17.
 */
class DefaultLoadView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : LoadView(context, attrs) {
    private val tvContent: TextView
    private val progressBar: ProgressBar

    init {
        layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        tvContent = TextView(context)
        tvContent.id = View.generateViewId()
        addView(tvContent)
        val contentParams = tvContent.layoutParams as LayoutParams
        contentParams.addRule(CENTER_IN_PARENT)
        progressBar = ProgressBar(context)
        addView(progressBar)
        val density = getContext().resources.displayMetrics.density
        val params = progressBar.layoutParams as LayoutParams
        params.width = (20 * density).toInt()
        params.height = (20 * density).toInt()
        params.addRule(CENTER_IN_PARENT)
        params.rightMargin = (10 * density).toInt()
        params.addRule(LEFT_OF, tvContent.id)
        progressBar.layoutParams = params
    }

    override fun setHeight(dragDistance: Float, distanceToRefresh: Float, totalDistance: Float) {}
    override fun setRefresh() {
        tvContent.setText(R.string.loading_more)
        progressBar.visibility = View.VISIBLE
    }

    override fun setPullToRefresh() {
        progressBar.visibility = View.GONE
        tvContent.setText(R.string.push_loading_more)
    }

    override fun setReleaseToRefresh() {
        progressBar.visibility = View.GONE
        tvContent.setText(R.string.realese_loading_more)
    }
}