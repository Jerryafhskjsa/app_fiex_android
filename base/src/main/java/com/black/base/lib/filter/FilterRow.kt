package com.black.base.lib.filter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.black.base.R
import com.black.util.CommonUtil
import com.google.android.material.tabs.TabLayout
import skin.support.content.res.SkinCompatResources
import java.util.*

class FilterRow<T>(context: Context, data: FilterEntity<T>?) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val key: String? = data?.key
    private var data: List<T>
    private val selectItem: T?
    private val tabLayout: TabLayout?
    val contentView: View

    init {
        this.data = if (data == null) ArrayList() else data.data!!
        selectItem = data?.selectItem
        contentView = inflater.inflate(R.layout.view_filter_row, null)
        tabLayout = contentView.findViewById(R.id.tab_layout)
        tabLayout.setTabTextColors(SkinCompatResources.getColor(context, R.color.T2), SkinCompatResources.getColor(context, R.color.C1))
        tabLayout.setSelectedTabIndicatorHeight(0)
        tabLayout.tabMode = TabLayout.MODE_SCROLLABLE
        refreshViews()
    }

    fun setData(data: List<T>?) {
        this.data = data ?: ArrayList()
        refreshViews()
    }

    fun getSelectItem(): FilterResult<T> {
        val result = FilterResult<T>()
        result.key = key
        result.data = if (tabLayout != null) CommonUtil.getItemFromList(data, tabLayout.selectedTabPosition) else null
        return result
    }

    private fun refreshViews() {
        for (i in data.indices) {
            val item: T? = data[i]
            val set = item?.toString() ?: ""
            val tab = tabLayout!!.newTab().setText(set)
            tab.setCustomView(R.layout.view_filter_row_tab)
            if (tab.customView != null) {
                val textView = tab.customView!!.findViewById<View>(android.R.id.text1) as TextView
                textView.text = set
            }
            tabLayout.addTab(tab)
        }
        var selectIndex = 0
        if (selectItem != null) {
            selectIndex = data.indexOf(selectItem)
        }
        val fistTab = tabLayout!!.getTabAt(selectIndex)
        fistTab?.select()
    }
}
