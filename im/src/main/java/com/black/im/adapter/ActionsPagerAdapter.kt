package com.black.im.adapter

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.GridView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.black.im.R
import com.black.im.model.InputMoreActionUnit
import java.util.*

class ActionsPagerAdapter(mViewPager: ViewPager, mInputMoreList: List<InputMoreActionUnit>) : PagerAdapter() {
    companion object {
        private const val ITEM_COUNT_PER_GRID_VIEW = 4
    }

    private val mContext: Context = mViewPager.context
    private val mInputMoreList: List<InputMoreActionUnit>
    private val mViewPager: ViewPager
    private val mGridViewCount: Int
    private val actionWidth = 0
    private val actionHeight = 0

    init {
        this.mInputMoreList = ArrayList(mInputMoreList)
        this.mViewPager = mViewPager
        mGridViewCount = (mInputMoreList.size + ITEM_COUNT_PER_GRID_VIEW - 1) / ITEM_COUNT_PER_GRID_VIEW
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val end = if ((position + 1) * ITEM_COUNT_PER_GRID_VIEW > mInputMoreList.size) mInputMoreList
                .size else (position + 1) * ITEM_COUNT_PER_GRID_VIEW
        val subBaseActions = mInputMoreList.subList(position
                * ITEM_COUNT_PER_GRID_VIEW, end)
        val gridView = GridView(mContext)
        gridView.adapter = ActionsGridViewAdapter(mContext, subBaseActions)
        if (mInputMoreList.size >= 4) {
            gridView.numColumns = 4
            container.post {
                val layoutParams = mViewPager.layoutParams
                layoutParams.height = actionHeight
                mViewPager.layoutParams = layoutParams
            }
        } else {
            gridView.numColumns = mInputMoreList.size
            container.post {
                val layoutParams = mViewPager.layoutParams
                layoutParams.height = actionHeight
                mViewPager.layoutParams = layoutParams
            }
        }
        gridView.setSelector(R.color.transparent)
        gridView.horizontalSpacing = 0
        gridView.verticalSpacing = 0
        gridView.gravity = Gravity.CENTER
        gridView.tag = Integer.valueOf(position)
        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, itemPosition, _ ->
            val index = parent.tag as Int * ITEM_COUNT_PER_GRID_VIEW + itemPosition
            mInputMoreList[index].onClickListener!!.onClick(view)
        }
        container.addView(gridView)
        return gridView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) { // TODO
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun getCount(): Int {
        return mGridViewCount
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}