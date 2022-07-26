package com.black.base.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EmptyRecyclerView : RecyclerView {
    private var emptyView: View? = null
    private val observer: AdapterDataObserver = object : AdapterDataObserver() {
        override fun onChanged() {
            checkIfEmpty()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            checkIfEmpty()
        }
    }

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) {}

    private fun checkIfEmpty() {
        if (emptyView != null && adapter != null) {
            val emptyViewVisible = adapter!!.itemCount == 0
            emptyView!!.visibility = if (emptyViewVisible) View.VISIBLE else View.GONE
            visibility = if (emptyViewVisible) View.GONE else View.VISIBLE
        }
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        checkIfEmpty()
    }

    //设置没有内容时，提示用户的空布局
    fun setEmptyView(emptyView: View?) {
        this.emptyView = emptyView
        checkIfEmpty()
    }

    //屏幕中最后一个可见子项的position
    val isVisBottom:
            //当前屏幕所看到的子项个数
            //当前RecyclerView的所有子项个数
            //RecyclerView的滑动状态
            Boolean
        get() {
            val layoutManager = layoutManager as LinearLayoutManager?
            //屏幕中最后一个可见子项的position
            val lastVisibleItemPosition = layoutManager!!.findLastVisibleItemPosition()
            //当前屏幕所看到的子项个数
            val visibleItemCount = layoutManager.childCount
            //当前RecyclerView的所有子项个数
            val totalItemCount = layoutManager.itemCount
            //RecyclerView的滑动状态
            val state = scrollState
            return visibleItemCount > 0 && lastVisibleItemPosition == totalItemCount - 1 && state == SCROLL_STATE_IDLE
        }

    //屏幕中最后一个可见子项的position
    val lastVisiblePosition: Int
        get() {
            val layoutManager = layoutManager as LinearLayoutManager?
            //屏幕中最后一个可见子项的position
            return layoutManager!!.findLastVisibleItemPosition()
        }

    companion object {
        private const val TAG = "EmptyRecyclerView"
    }
}