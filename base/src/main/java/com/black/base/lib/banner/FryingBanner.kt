package com.black.base.lib.banner

import android.content.Context
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.ViewPager
import com.black.base.R
import com.black.base.presenter.Presenter
import com.black.lib.banner.BannerContainer
import com.black.util.CommonUtil
import me.crosswall.lib.coverflow.core.PagerContainer
import java.util.*

abstract class FryingBanner<T>(protected var context: Context) : Presenter<T, View>, ViewPager.OnPageChangeListener {
    companion object {
        private const val TAG = "FryingBanner"
    }

    protected var inflater: LayoutInflater = LayoutInflater.from(context)
    protected var dm: DisplayMetrics
    protected var pagerContainer: BannerContainer
    protected var pager: ViewPager
    protected var adapter: BannerAdapter<T>
    protected var indicator: LinearLayout
    protected var width = 0
    protected var height = 0
    val bannerView: PagerContainer
        get() = pagerContainer

    init {
        pagerContainer = inflater.inflate(R.layout.banner_url_image, null) as BannerContainer
        pager = pagerContainer.findViewById(R.id.banner_view_pager)
        indicator = pagerContainer.findViewById(R.id.banner_banner_indicator)
        pager.clipChildren = false
        pager.addOnPageChangeListener(this)
        adapter = BannerAdapter(context, ArrayList(), this)
        pager.adapter = adapter
        dm = context.resources.displayMetrics
    }

    open fun setData(data: List<T>?) {
        var useData = data
        useData = useData ?: ArrayList()
        pager.offscreenPageLimit = useData.size
        adapter = BannerAdapter(context, useData, this)
        pager.adapter = adapter
        pager.offscreenPageLimit = useData.size
        indicator.removeAllViews()
        addPoints(useData.size)
        pager.currentItem = 0
        selectPoint(0)
    }

    protected abstract fun addPoints(count: Int)
    protected fun selectPoint(position: Int) {
        val count = indicator.childCount
        for (i in 0 until count) {
            val pointView = indicator.getChildAt(i) as ImageView
            if (i == position) {
                onPointSelected(pointView)
            } else {
                onPointUnselected(pointView)
            }
        }
    }

    protected open fun onPointUnselected(pointView: ImageView?) {}
    protected open fun onPointSelected(pointView: ImageView?) {}
    override fun getView(model: T, view: View): View {
        val itemView = onCreateItemView(model)
        itemView.setOnClickListener {
            if (onBannerItemClickListener != null) {
                onBannerItemClickListener!!.onItemClick(model)
            }
        }
        return itemView
    }

    abstract fun onCreateItemView(model: T): View
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
    override fun onPageSelected(position: Int) {
        selectPoint(position)
    }

    override fun onPageScrollStateChanged(state: Int) {}
    fun startScroll() {
        pagerContainer.startScroll()
    }

    open fun setScale(scale: Float): FryingBanner<*>? {
        return setScale(scale, null)
    }

    fun setScale(scale: Float, parent: View?): FryingBanner<*> {
        width = dm.widthPixels
        if (parent != null) {
            val paddingTotal = parent.paddingLeft + parent.paddingRight
            val marginTotal = 0
            CommonUtil.measureView(parent)
            width = if (parent.measuredWidth == 0) width else parent.measuredWidth
            //            ViewGroup.LayoutParams params = parent.getLayoutParams();
//            if(params instanceof ViewGroup.MarginLayoutParams){
//                ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) params;
//                marginTotal = marginLayoutParams.leftMargin + marginLayoutParams.rightMargin;
//            }
            width = width - paddingTotal - marginTotal
        }
        height = (width * scale).toInt()
        val params = pager.layoutParams
        params.height = height
        pager.layoutParams = params
        var containerParams = pagerContainer.layoutParams
        if (containerParams == null) {
            containerParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)
        } else {
            containerParams.height = height
        }
        pagerContainer.layoutParams = containerParams
        return this
    }

    private var onBannerItemClickListener: OnBannerItemClickListener<T>? = null
    fun setOnBannerItemClickListener(onBannerItemClickListener: OnBannerItemClickListener<T>?) {
        this.onBannerItemClickListener = onBannerItemClickListener
    }

    interface OnBannerItemClickListener<T> {
        fun onItemClick(item: T)
    }
}