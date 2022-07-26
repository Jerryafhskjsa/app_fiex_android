package com.black.base.util

import com.black.base.view.SwipeLayout

class SwipeLayoutManager private constructor() {
    var currentLayout //用来记录当前打开的SwipeLayout
            : SwipeLayout? = null
        private set

    fun setSwipeLayout(layout: SwipeLayout?) {
        currentLayout = layout
    }

    /**
     * 清空当前所记录的已经打开的layout
     */
    fun clearCurrentLayout() {
        currentLayout = null
    }

    /**
     * 关闭当前已经打开的SwipeLayout
     */
    fun closeCurrentLayout() {
        if (currentLayout != null) {
            currentLayout!!.close()
        }
    }

    /**
     * 判断当前是否应该能够滑动，如果没有打开的，则可以滑动。
     * 如果有打开的，则判断打开的layout和当前按下的layout是否是同一个,是同一个，可以滑动
     *
     * @return
     */
    fun isShouldSwipe(swipeLayout: SwipeLayout): Boolean {
        return if (currentLayout == null) {
            //说明当前木有打开的layout
            true
        } else {
            //说明有打开的layout
            //判断打开的layout和当前按下的layout是否是同一个
            currentLayout === swipeLayout
        }
    }

    companion object {
        val instance = SwipeLayoutManager()
    }
}