package com.black.base.lib.refresh

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.View
import android.view.ViewConfiguration
import android.webkit.WebView
import android.widget.AbsListView
import android.widget.LinearLayout
import android.widget.OverScroller
import android.widget.ScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.black.base.R
import kotlin.math.abs

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com 创建时间:15/10/28 上午2:32 描述:
 */
class BGAStickyNavLayout(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private var mHeaderView: View? = null
    private var mNavView: View? = null
    private var mContentView: View? = null
    private var mDirectNormalView: View? = null
    private var mDirectAbsListView: AbsListView? = null
    private var mDirectScrollView: ScrollView? = null
    private var mDirectWebView: WebView? = null
    private var mDirectViewPager: ViewPager? = null
    private var mNestedContentView: View? = null
    private var mNestedNormalView: View? = null
    private var mNestedRecyclerView: RecyclerView? = null
    private var mNestedAbsListView: AbsListView? = null
    private var mNestedScrollView: ScrollView? = null
    private var mNestedWebView: WebView? = null
    private var mOverScroller: OverScroller? = null
    private var mVelocityTracker: VelocityTracker? = null
    private var mTouchSlop = 0
    private var mMaximumVelocity = 0
    private var mMinimumVelocity = 0
    private var mIsInControl = true
    private var mLastDispatchY = 0f
    private var mLastTouchY = 0f
    var mRefreshLayout: BGARefreshLayout? = null

    init {
        init(context)
    }

    private fun init(context: Context) {
        orientation = VERTICAL
        mOverScroller = OverScroller(context)
        val configuration = ViewConfiguration.get(context)
        mTouchSlop = configuration.scaledTouchSlop
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity
    }

    override fun setOrientation(orientation: Int) {
        if (VERTICAL == orientation) {
            super.setOrientation(VERTICAL)
        }
    }

    public override fun onFinishInflate() {
        super.onFinishInflate()
        check(childCount == 3) { BGAStickyNavLayout::class.java.simpleName + context.getString(R.string.must_three) }
        mHeaderView = getChildAt(0)
        mNavView = getChildAt(1)
        mContentView = getChildAt(2)
        when (mContentView) {
            is AbsListView -> {
                mDirectAbsListView = mContentView as AbsListView?
                mDirectAbsListView?.setOnScrollListener(mLvOnScrollListener)
            }
            is ScrollView -> {
                mDirectScrollView = mContentView as ScrollView?
            }
            is WebView -> {
                mDirectWebView = mContentView as WebView?
            }
            is ViewPager -> {
                mDirectViewPager = mContentView as ViewPager?
                mDirectViewPager?.addOnPageChangeListener(object : ViewPager.SimpleOnPageChangeListener() {
                    override fun onPageSelected(position: Int) {
                        regetNestedContentView()
                    }
                })
            }
            else -> {
                mDirectNormalView = mContentView
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChild(mContentView, widthMeasureSpec, MeasureSpec
                .makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec) - navViewHeight, MeasureSpec.EXACTLY))
    }

    override fun computeScroll() {
        if (true == mOverScroller?.computeScrollOffset()) {
            scrollTo(0, mOverScroller?.currY ?: 0)
            invalidate()
        }
    }

    fun fling(velocityY: Int) {
        mOverScroller?.fling(0, scrollY, 0, velocityY, 0, 0, 0, headerViewHeight)
        invalidate()
    }

    override fun scrollTo(x: Int, y: Int) {
        var y1 = y
        if (y1 < 0) {
            y1 = 0
        }
        val headerViewHeight = headerViewHeight
        if (y1 > headerViewHeight) {
            y1 = headerViewHeight
        }
        if (y1 != scrollY) {
            super.scrollTo(x, y1)
        }
    }

    /**
     * 获取头部视图高度，包括topMargin和bottomMargin
     *
     * @return
     */
    private val headerViewHeight: Int
        get() {
            val layoutParams = mHeaderView?.layoutParams as MarginLayoutParams
            return (mHeaderView?.measuredHeight
                    ?: 0) + layoutParams.topMargin + layoutParams.bottomMargin
        }

    /**
     * 获取导航视图的高度，包括topMargin和bottomMargin
     *
     * @return
     */
    private val navViewHeight: Int
        get() {
            val layoutParams = mNavView?.layoutParams as MarginLayoutParams
            return (mNavView?.measuredHeight
                    ?: 0) + layoutParams.topMargin + layoutParams.bottomMargin
        }// 0表示x，1表示y

    /**
     * 头部视图是否已经完全隐藏
     *
     * @return
     */
    private val isHeaderViewCompleteInvisible: Boolean
        get() { // 0表示x，1表示y
            val location = IntArray(2)
            getLocationOnScreen(location)
            val contentOnScreenTopY = location[1] + paddingTop
            mNavView?.getLocationOnScreen(location)
            val params = mNavView?.layoutParams as MarginLayoutParams
            val navViewTopOnScreenY = location[1] - params.topMargin
            return navViewTopOnScreenY == contentOnScreenTopY
        }

    private fun initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker?.recycle()
            mVelocityTracker = null
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val currentTouchY = ev.y
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> mLastDispatchY = currentTouchY
            MotionEvent.ACTION_MOVE -> {
                val differentY = currentTouchY - mLastDispatchY
                mLastDispatchY = currentTouchY
                if (isContentViewToTop && isHeaderViewCompleteInvisible) {
                    if (differentY >= 0 && !mIsInControl) {
                        mIsInControl = true
                        return resetDispatchTouchEvent(ev)
                    }
                    if (differentY <= 0 && mIsInControl) {
                        mIsInControl = false
                        return resetDispatchTouchEvent(ev)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun resetDispatchTouchEvent(ev: MotionEvent): Boolean {
        val newEvent = MotionEvent.obtain(ev)
        ev.action = MotionEvent.ACTION_CANCEL
        dispatchTouchEvent(ev)
        newEvent.action = MotionEvent.ACTION_DOWN
        return dispatchTouchEvent(newEvent)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        val currentTouchY = ev.y
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> mLastTouchY = currentTouchY
            MotionEvent.ACTION_MOVE -> {
                val differentY = currentTouchY - mLastTouchY
                if (abs(differentY) > mTouchSlop) {
                    if (!isHeaderViewCompleteInvisible
                            || isContentViewToTop && isHeaderViewCompleteInvisible && mIsInControl) {
                        mLastTouchY = currentTouchY
                        return true
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        initVelocityTrackerIfNotExists()
        mVelocityTracker?.addMovement(event)
        val currentTouchY = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (true != mOverScroller?.isFinished) {
                    mOverScroller?.abortAnimation()
                }
                mLastTouchY = currentTouchY
            }
            MotionEvent.ACTION_MOVE -> {
                val differentY = currentTouchY - mLastTouchY
                mLastTouchY = currentTouchY
                if (abs(differentY) > 0) {
                    scrollBy(0, (-differentY).toInt())
                }
            }
            MotionEvent.ACTION_CANCEL -> {
                recycleVelocityTracker()
                if (true != mOverScroller?.isFinished) {
                    mOverScroller?.abortAnimation()
                }
            }
            MotionEvent.ACTION_UP -> {
                mVelocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                val initialVelocity = mVelocityTracker?.yVelocity?.toInt() ?: 0
                if (abs(initialVelocity) > mMinimumVelocity) {
                    fling(-initialVelocity)
                }
                recycleVelocityTracker()
            }
        }
        return true
    }

    val isContentViewToTop: Boolean
        get() {
            if (mDirectNormalView != null) {
                return true
            }
            if (BGARefreshScrollingUtil.isScrollViewOrWebViewToTop(mDirectWebView)) {
                return true
            }
            if (BGARefreshScrollingUtil.isScrollViewOrWebViewToTop(mDirectScrollView)) {
                return true
            }
            if (BGARefreshScrollingUtil.isAbsListViewToTop(mDirectAbsListView)) {
                return true
            }
            return if (mDirectViewPager != null) {
                isViewPagerContentViewToTop
            } else false
        }

    private val isViewPagerContentViewToTop: Boolean
        get() {
            if (mNestedContentView == null) {
                regetNestedContentView()
            }
            if (mDirectNormalView != null) {
                return true
            }
            if (BGARefreshScrollingUtil.isScrollViewOrWebViewToTop(mNestedWebView)) {
                return true
            }
            if (BGARefreshScrollingUtil.isScrollViewOrWebViewToTop(mNestedScrollView)) {
                return true
            }
            return if (BGARefreshScrollingUtil.isAbsListViewToTop(mNestedAbsListView)) {
                true
            } else BGARefreshScrollingUtil.isRecyclerViewToTop(mNestedRecyclerView)
        }

    /**
     * 重新获取嵌套的内容视图
     */
    private fun regetNestedContentView() {
        val currentItem = mDirectViewPager?.currentItem ?: 0
        val adapter = mDirectViewPager?.adapter
        if (adapter is FragmentPagerAdapter || adapter is FragmentStatePagerAdapter) {
            val item = adapter.instantiateItem(mDirectViewPager!!, currentItem) as Fragment
            mNestedContentView = item.view
            // 清空之前的
            mNestedNormalView = null
            mNestedAbsListView = null
            mNestedRecyclerView = null
            mNestedScrollView = null
            mNestedWebView = null
            if (mNestedContentView is AbsListView) {
                mNestedAbsListView = mNestedContentView as AbsListView?
                mNestedAbsListView?.setOnScrollListener(mLvOnScrollListener)
                if (!isHeaderViewCompleteInvisible) {
                    mNestedAbsListView?.setSelection(0)
                }
            } else if (mNestedContentView is ScrollView) {
                mNestedScrollView = mNestedContentView as ScrollView?
                if (!isHeaderViewCompleteInvisible) {
                    mNestedScrollView?.scrollTo(mNestedScrollView?.scrollX ?: 0, 0)
                }
            } else if (mNestedContentView is WebView) {
                mNestedWebView = mNestedContentView as WebView?
                if (!isHeaderViewCompleteInvisible) {
                    mNestedWebView?.scrollTo(mNestedWebView?.scrollX ?: 0, 0)
                }
            } else {
                mNestedNormalView = mNestedContentView
            }
        } else {
            throw IllegalStateException(context.getString(R.string.error_message_03, BGAStickyNavLayout::class.java.simpleName, "ViewPager", "adapter", "FragmentPagerAdapter", "FragmentStatePagerAdapter"))
        }
    }

    fun setRefreshLayout(refreshLayout: BGARefreshLayout?) {
        mRefreshLayout = refreshLayout
    }

    private val mRvOnScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            if ((newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_SETTLING)
                    && mRefreshLayout != null && true == mRefreshLayout?.shouldHandleRecyclerViewLoadingMore(recyclerView)) {
                mRefreshLayout?.beginLoadingMore()
            }
        }
    }
    private val mLvOnScrollListener: AbsListView.OnScrollListener = object : AbsListView.OnScrollListener {
        override fun onScrollStateChanged(absListView: AbsListView, scrollState: Int) {
            if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) && mRefreshLayout != null && true == mRefreshLayout?.shouldHandleAbsListViewLoadingMore(absListView)) {
                mRefreshLayout?.beginLoadingMore()
            }
        }

        override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {}
    }

    fun shouldHandleLoadingMore(): Boolean {
        if (mRefreshLayout == null) {
            return false
        }
        if (mDirectNormalView != null) {
            return true
        }
        if (BGARefreshScrollingUtil.isWebViewToBottom(mDirectWebView)) {
            return true
        }
        if (BGARefreshScrollingUtil.isScrollViewToBottom(mDirectScrollView)) {
            return true
        }
        if (mDirectAbsListView != null) {
            return mRefreshLayout?.shouldHandleAbsListViewLoadingMore(mDirectAbsListView) ?: false
        }
        if (mDirectViewPager != null) {
            if (mNestedContentView == null) {
                regetNestedContentView()
            }
            if (mNestedNormalView != null) {
                return true
            }
            if (BGARefreshScrollingUtil.isWebViewToBottom(mNestedWebView)) {
                return true
            }
            if (BGARefreshScrollingUtil.isScrollViewToBottom(mNestedScrollView)) {
                return true
            }
            if (mNestedAbsListView != null) {
                return mRefreshLayout?.shouldHandleAbsListViewLoadingMore(mNestedAbsListView)
                        ?: false
            }
            if (mNestedRecyclerView != null) {
                return mRefreshLayout?.shouldHandleRecyclerViewLoadingMore(mNestedRecyclerView!!)
                        ?: false
            }
        }
        return false
    }

    fun scrollToBottom() {
        BGARefreshScrollingUtil.scrollToBottom(mDirectScrollView)
        BGARefreshScrollingUtil.scrollToBottom(mDirectAbsListView)
        if (mDirectViewPager != null) {
            if (mNestedContentView == null) {
                regetNestedContentView()
            }
            BGARefreshScrollingUtil.scrollToBottom(mNestedScrollView)
            BGARefreshScrollingUtil.scrollToBottom(mNestedRecyclerView)
            BGARefreshScrollingUtil.scrollToBottom(mNestedAbsListView)
        }
    }
}