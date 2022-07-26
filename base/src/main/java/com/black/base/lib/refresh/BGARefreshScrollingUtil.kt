package com.black.base.lib.refresh

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.webkit.WebView
import android.widget.AbsListView
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com 创建时间:15/10/28 上午2:26 描述:
 */
object BGARefreshScrollingUtil {
    fun isScrollViewOrWebViewToTop(view: View?): Boolean {
        return view != null && view.scrollY == 0
    }

    fun isAbsListViewToTop(absListView: AbsListView?): Boolean {
        if (absListView != null) {
            var firstChildTop = 0
            if (absListView.childCount > 0) { // 如果AdapterView的子控件数量不为0，获取第一个子控件的top
                firstChildTop = absListView.getChildAt(0).top - absListView.paddingTop
            }
            return absListView.firstVisiblePosition == 0 && firstChildTop == 0
        }
        return false
    }

    fun isRecyclerViewToTop(recyclerView: RecyclerView?): Boolean {
        if (recyclerView != null) {
            var firstChildTop = 0
            if (recyclerView.childCount > 0) { // 如果RecyclerView的子控件数量不为0，获取第一个子控件的top
// 解决item的topMargin不为0时不能触发下拉刷新
                val layoutParams = recyclerView.getChildAt(0)
                        .layoutParams as MarginLayoutParams
                firstChildTop = (recyclerView.getChildAt(0).top - layoutParams.topMargin
                        - recyclerView.paddingTop)
            }
            val manager = recyclerView.layoutManager ?: return true
            if (manager.itemCount == 0) {
                return true
            }
            if (manager is LinearLayoutManager) {
                return manager.findFirstCompletelyVisibleItemPosition() < 1 && firstChildTop == 0
            } else if (manager is StaggeredGridLayoutManager) {
                val out = manager.findFirstCompletelyVisibleItemPositions(null)
                return out[0] < 1
            }
        }
        return false
    }

    fun isStickyNavLayoutToTop(stickyNavLayout: BGAStickyNavLayout): Boolean {
        return isScrollViewOrWebViewToTop(stickyNavLayout) && stickyNavLayout.isContentViewToTop
    }

    fun isWebViewToBottom(webView: WebView?): Boolean {
        return webView != null && webView.contentHeight * webView.scale == (webView.scrollY + webView.measuredHeight).toFloat()
    }

    fun isScrollViewToBottom(scrollView: ScrollView?): Boolean {
        if (scrollView != null) {
            val scrollContentHeight = scrollView.scrollY + scrollView.measuredHeight - scrollView.paddingTop - scrollView.paddingBottom
            val realContentHeight = scrollView.getChildAt(0).measuredHeight
            return scrollContentHeight <= realContentHeight
        }
        return false
    }

    fun isScrollViewToBottom(scrollView: NestedScrollView?): Boolean {
        if (scrollView != null) {
            val scrollContentHeight = scrollView.scrollY + scrollView.measuredHeight - scrollView.paddingTop - scrollView.paddingBottom
            val realContentHeight = scrollView.getChildAt(0).measuredHeight
            return scrollContentHeight <= realContentHeight
        }
        return false
    }

    fun isAbsListViewToBottom(absListView: AbsListView?): Boolean {
        if (absListView != null && absListView.adapter != null && absListView.childCount > 0 && absListView.lastVisiblePosition == absListView.adapter.count - 1) {
            val lastChild = absListView.getChildAt(absListView.childCount - 1)
            val stickyNavLayout = getStickyNavLayout(absListView)
            return if (stickyNavLayout != null) { // 处理BGAStickyNavLayout中lastChild.getBottom() <=
                // absListView.getMeasuredHeight()失效问题
                // 0表示x，1表示y
                val location = IntArray(2)
                lastChild.getLocationOnScreen(location)
                val lastChildBottomOnScreen = location[1] + lastChild.measuredHeight
                stickyNavLayout.getLocationOnScreen(location)
                val stickyNavLayoutBottomOnScreen = location[1] + stickyNavLayout.measuredHeight
                lastChildBottomOnScreen + absListView.paddingBottom <= stickyNavLayoutBottomOnScreen
            } else {
                lastChild.bottom <= absListView.measuredHeight
            }
        }
        return false
    }

    fun isRecyclerViewToBottom(recyclerView: RecyclerView?): Boolean {
        if (recyclerView != null) {
            val manager = recyclerView.layoutManager
            if (manager == null || manager.itemCount == 0) {
                return false
            }
            if (manager is LinearLayoutManager) {
                if (manager.findLastCompletelyVisibleItemPosition() == manager.itemCount - 1) {
                    val stickyNavLayout = getStickyNavLayout(recyclerView)
                    return if (stickyNavLayout != null) { // 处理BGAStickyNavLayout中findLastCompletelyVisibleItemPosition失效问题
                        val lastChild = manager
                                .getChildAt(manager.findLastCompletelyVisibleItemPosition())
                        if (lastChild == null) {
                            true
                        } else { // 0表示x，1表示y
                            val location = IntArray(2)
                            lastChild.getLocationOnScreen(location)
                            val lastChildBottomOnScreen = location[1] + lastChild.measuredHeight
                            stickyNavLayout.getLocationOnScreen(location)
                            val stickyNavLayoutBottomOnScreen = location[1] + stickyNavLayout.measuredHeight
                            lastChildBottomOnScreen <= stickyNavLayoutBottomOnScreen
                        }
                    } else {
                        true
                    }
                }
            } else if (manager is StaggeredGridLayoutManager) {
                val out = manager.findLastCompletelyVisibleItemPositions(null)
                val lastPosition = manager.itemCount - 1
                for (position in out) {
                    if (position == lastPosition) {
                        return true
                    }
                }
            }
        }
        return false
    }

    fun scrollToBottom(scrollView: ScrollView?) {
        scrollView?.post { scrollView.fullScroll(ScrollView.FOCUS_DOWN) }
    }

    fun scrollToBottom(scrollView: NestedScrollView?) {
        scrollView?.post { scrollView.fullScroll(NestedScrollView.FOCUS_DOWN) }
    }

    fun scrollToBottom(absListView: AbsListView?) {
        if (absListView != null) {
            if (absListView.adapter != null && absListView.adapter.count > 0) {
                absListView.post(Runnable { absListView.setSelection(absListView.adapter.count - 1) })
            }
        }
    }

    fun scrollToBottom(recyclerView: RecyclerView?) {
        if (recyclerView != null) {
            if (recyclerView.adapter != null && recyclerView.adapter!!.itemCount > 0) {
                recyclerView.post(Runnable { recyclerView.smoothScrollToPosition(recyclerView.adapter!!.itemCount - 1) })
            }
        }
    }

    fun getStickyNavLayout(view: View): BGAStickyNavLayout? {
        var viewParent = view.parent
        while (viewParent != null) {
            if (viewParent is BGAStickyNavLayout) {
                return viewParent
            }
            viewParent = viewParent.parent
        }
        return null
    }
}