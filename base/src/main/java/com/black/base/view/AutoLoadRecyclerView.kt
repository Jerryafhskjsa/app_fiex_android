package com.black.base.view

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * @author zhangxiaohui
 * create at 2018/12/29
 */
class AutoLoadRecyclerView : RecyclerView {
    private val PAGE_SIZE = 15
    private val AUTO_LOAD_POS = 5
    private var repeatLoad = 0
    private var lastItemCount = PAGE_SIZE + 1
    private var emptyView: EmptyViewFraLayout? = null

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {}

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

    private fun checkIfEmpty() {
        if (this@AutoLoadRecyclerView.parent is EmptyViewFraLayout) {
            emptyView = this@AutoLoadRecyclerView.parent as EmptyViewFraLayout
        }
        if (emptyView != null) { // && getAdapter() != null
            val emptyViewVisible = adapter?.itemCount == 0 || adapter == null
            emptyView?.setViewStatus(if (emptyViewVisible) EmptyViewFraLayout.STATUS.NO_DATA else EmptyViewFraLayout.STATUS.SUCCESS)
            //            emptyView.setVisibility(emptyViewVisible ? VISIBLE : GONE);
//            setVisibility(emptyViewVisible ? GONE : VISIBLE);
        }
    }

    //设置没有内容时，提示用户的空布局
//    public void setEmptyView(View emptyView) {
//        this.emptyView = emptyView;
//        checkIfEmpty();
//    }
    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter = getAdapter()
        oldAdapter?.unregisterAdapterDataObserver(observer)
        super.setAdapter(adapter)
        adapter?.registerAdapterDataObserver(observer)
        checkIfEmpty()
    }

    fun setOnListner(autoLoadMoreListner: AutoLoadMoreListner) {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState != SCROLL_STATE_IDLE) { //滚动状态不加载
                    return
                }
                val layoutManager = recyclerView.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    val linearManager = layoutManager
                    //获取最后一个可见view的位置
                    val lastItemPosition = linearManager.findLastVisibleItemPosition()
                    if (linearManager.itemCount == PAGE_SIZE) { //此行重置下拉刷新后可以正常进行分布上拉加载
                        lastItemCount = PAGE_SIZE + 1
                        repeatLoad = 0
                    }
                    if (linearManager.itemCount < PAGE_SIZE) { //总条数小于一页的数量不再请求下一页
                        return
                    }
                    //上一次总条数与最新获取的总条数相同不再请求，请求多一次以解决第一页返回PAGE_SIZE数量
                    if (linearManager.itemCount - 1 == lastItemCount && repeatLoad > 1) {
                        return
                    }
                    repeatLoad++
                    lastItemCount = lastItemPosition
                    if (lastItemPosition >= linearManager.itemCount - AUTO_LOAD_POS) {
                        autoLoadMoreListner.autoLoadMoreData()
                    }
                }
            }
        })
    }

    interface AutoLoadMoreListner {
        fun autoLoadMoreData()
    }
}