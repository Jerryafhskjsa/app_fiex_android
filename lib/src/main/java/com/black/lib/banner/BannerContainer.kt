package com.black.lib.banner

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.viewpager.widget.ViewPager
import com.black.lib.banner.widget.loopviewpager.FixedSpeedScroller
import com.black.util.CommonUtil
import me.crosswall.lib.coverflow.core.PagerContainer

class BannerContainer : PagerContainer {
    private val VIEWPAGER_SWITCH_DURING: Long = 5000 //轮播时间
    private val mHandler = Handler()
    private val scrollCommand = ScrollCommand()

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        onDetached()
    }

    private fun setScrollSpeed() {
        try {
            val mScroller = ViewPager::class.java.getDeclaredField("mScroller")
            mScroller.isAccessible = true
            val interpolator = AccelerateDecelerateInterpolator()
            val myScroller = FixedSpeedScroller(context, interpolator, 450)
            mScroller[viewPager] = myScroller
        } catch (e: Exception) {
            CommonUtil.printError(context, e)
        }
    }

    fun startScroll() {
        setScrollSpeed()
        mHandler.removeCallbacksAndMessages(null)
        mHandler.postDelayed(scrollCommand, VIEWPAGER_SWITCH_DURING)
    }

    fun goOnScroll() {
        val position = viewPager.currentItem
        viewPager.setCurrentItem(position + 1, true)
    }

    fun stopScroll() {
        mHandler.removeCallbacksAndMessages(null)
    }

    protected fun onDetached() {
        stopScroll()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility != View.VISIBLE) {
            stopScroll()
        } else {
            startScroll()
        }
    }

    internal inner class ScrollCommand : Runnable {
        override fun run() {
            goOnScroll()
            mHandler.postDelayed(this, VIEWPAGER_SWITCH_DURING)
        }
    }
}