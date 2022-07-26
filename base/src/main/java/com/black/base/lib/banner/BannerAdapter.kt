package com.black.base.lib.banner

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.black.base.presenter.Presenter
import java.util.*

class BannerAdapter<T>(private val context: Context, data: List<T>?, private val presenter: Presenter<T, View>) : PagerAdapter() {
    private var data: List<T>

    init {
        this.data = data?.let { ArrayList(it) } ?: ArrayList()
    }

    fun setData(data: List<T>?) {
        this.data = data?.let { ArrayList(it) } ?: ArrayList()
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view = presenter.getView(data[position], container)
        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}
