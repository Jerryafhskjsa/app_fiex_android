package com.black.base.adapter

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.black.util.CommonUtil
import java.util.*

class BaseViewPagerAdapter(viewList: List<View>?) : PagerAdapter() {
    private var viewList: List<View>
    fun setViewList(viewList: List<View>?) {
        this.viewList = viewList ?: ArrayList()
    }

    fun getView(position: Int): View? {
        return CommonUtil.getItemFromList(viewList, position)
    }

    override fun getCount(): Int {
        return viewList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = viewList[position]
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(viewList[position])
    }

    init {
        this.viewList = viewList ?: ArrayList()
    }
}