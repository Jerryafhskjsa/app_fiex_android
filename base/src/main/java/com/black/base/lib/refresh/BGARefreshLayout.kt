package com.black.base.lib.refresh

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.AbsListView
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.NestedScrollingParentHelper
import androidx.core.view.ViewCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.black.base.R
import com.black.base.lib.refresh.BGARefreshScrollingUtil.isAbsListViewToBottom
import com.black.base.lib.refresh.BGARefreshScrollingUtil.isAbsListViewToTop
import com.black.base.lib.refresh.BGARefreshScrollingUtil.isRecyclerViewToBottom
import com.black.base.lib.refresh.BGARefreshScrollingUtil.isScrollViewOrWebViewToTop
import com.black.base.lib.refresh.BGARefreshScrollingUtil.isScrollViewToBottom
import com.black.base.lib.refresh.BGARefreshScrollingUtil.isStickyNavLayoutToTop
import com.black.base.lib.refresh.BGARefreshScrollingUtil.isWebViewToBottom
import com.black.base.lib.refresh.BGARefreshScrollingUtil.scrollToBottom
import com.black.base.util.FryingUtil.printError
import com.nineoldandroids.animation.ValueAnimator
import kotlin.math.abs
import kotlin.math.min

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com 创建时间:15/5/21 22:35
 * 描述:下拉刷新、上拉加载更多、可添加自定义（固定、可滑动）头部控件（例如慕课网app顶部的广告位）
 */
class BGARefreshLayout @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    companion object {
        private val TAG = BGARefreshLayout::class.java.simpleName
        fun dp2px(context: Context, dpValue: Int): Int {
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue.toFloat(),
                    context.resources.displayMetrics).toInt()
        }
    }

    private val mParentHelper: NestedScrollingParentHelper
    private val mChildHelper: NestedScrollingChildHelper
    private var mRefreshViewHolder: BGARefreshViewHolder? = null
    /**
     * 整个头部控件，下拉刷新控件mRefreshHeaderView和下拉刷新控件下方的自定义组件mCustomHeaderView的父控件
     */
    private var mWholeHeaderView: LinearLayout? = null
    /**
     * 下拉刷新控件
     */
    private var mRefreshHeaderView: View? = null
    /**
     * 下拉刷新控件下方的自定义控件
     */
    private var mCustomHeaderView: View? = null
    /**
     * 下拉刷新控件下方的自定义控件是否可滚动，默认不可滚动
     */
    private var mIsCustomHeaderViewScrollable = false
    /**
     * 下拉刷新控件的高度
     */
    private var mRefreshHeaderViewHeight = 0
    /**
     * 当前刷新状态
     */
    private var mCurrentRefreshStatus = RefreshStatus.IDLE
    /**
     * 上拉加载更多控件
     */
    private var mLoadMoreFooterView: View? = null
    /**
     * 上拉加载更多控件的高度
     */
    private var mLoadMoreFooterViewHeight = 0
    /**
     * 下拉刷新和上拉加载更多代理
     */
    private var mDelegate: BGARefreshLayoutDelegate? = null
    /**
     * 手指按下时，y轴方向的偏移量
     */
    private var mWholeHeaderDownY = -1
    /**
     * 整个头部控件最小的paddingTop
     */
    private var mMinWholeHeaderViewPaddingTop = 0
    /**
     * 整个头部控件最大的paddingTop
     */
    private var mMaxWholeHeaderViewPaddingTop = 0
    /**
     * 是否处于正在加载更多状态
     */
    private var mIsLoadingMore = false
    private var mAbsListView: AbsListView? = null
    private var mScrollView: ScrollView? = null
    private var mNestedScrollView: NestedScrollView? = null
    private var mNormalView: View? = null
    private var mWebView: WebView? = null
    private var mStickyNavLayout: BGAStickyNavLayout? = null
    private var mContentView: View? = null
    private var mInterceptTouchDownX = -1f
    private var mInterceptTouchDownY = -1f
    /**
     * 按下时整个头部控件的paddingTop
     */
    private var mWholeHeaderViewDownPaddingTop = 0
    /**
     * 记录开始下拉刷新时的downY
     */
    private var mRefreshDownY = -1
    /**
     * 是否已经设置内容控件滚动监听器
     */
    private var mIsInitedContentViewScrollListener = false
    /**
     * 触发上拉加载更多时是否显示加载更多控件
     */
    private var mIsShowLoadingMoreView = true
    /**
     * 下拉刷新是否可用
     */
    private var mPullDownRefreshEnable = true
    private val mHandler: Handler

    init {
        orientation = VERTICAL
        mHandler = Handler(Looper.getMainLooper())
        mParentHelper = NestedScrollingParentHelper(this)
        mChildHelper = NestedScrollingChildHelper(this)
        initWholeHeaderView()
    }

    /**
     * 初始化整个头部控件
     */
    private fun initWholeHeaderView() {
        mWholeHeaderView = LinearLayout(context)
        mWholeHeaderView!!.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        mWholeHeaderView!!.orientation = VERTICAL
        addView(mWholeHeaderView)
    }

    public override fun onFinishInflate() {
        super.onFinishInflate()
        if (childCount != 2) {
            throw RuntimeException(BGARefreshLayout::class.java.simpleName + context.getString(R.string.only_one))
        }
        mContentView = getChildAt(1)
        when (mContentView) {
            is AbsListView -> {
                mAbsListView = mContentView as AbsListView?
            }
            is ScrollView -> {
                mScrollView = mContentView as ScrollView?
            }
            is NestedScrollView -> {
                mNestedScrollView = mContentView as NestedScrollView?
            }
            is WebView -> {
                mWebView = mContentView as WebView?
            }
            is BGAStickyNavLayout -> {
                mStickyNavLayout = mContentView as BGAStickyNavLayout?
                mStickyNavLayout!!.setRefreshLayout(this)
            }
            else -> {
                mNormalView = mContentView
                // 设置为可点击，否则在空白区域无法拖动
                mNormalView!!.isClickable = true
            }
        }
    }

    fun setRefreshViewHolder(refreshViewHolder: BGARefreshViewHolder?) {
        mRefreshViewHolder = refreshViewHolder
        mRefreshViewHolder!!.setRefreshLayout(this)
        initRefreshHeaderView()
        initLoadMoreFooterView()
    }

    fun startChangeWholeHeaderViewPaddingTop(distance: Int) {
        val animator = ValueAnimator.ofInt(mWholeHeaderView!!.paddingTop,
                mWholeHeaderView!!.paddingTop - distance)
        animator.duration = mRefreshViewHolder!!.topAnimDuration.toLong()
        animator.addUpdateListener { animation ->
            val paddingTop = animation.animatedValue as Int
            mWholeHeaderView!!.setPadding(0, paddingTop, 0, 0)
        }
        animator.start()
    }

    /**
     * 初始化下拉刷新控件
     *
     * @return
     */
    private fun initRefreshHeaderView() {
        mRefreshHeaderView = mRefreshViewHolder!!.refreshHeaderView
        if (mRefreshHeaderView != null) {
            mRefreshHeaderView!!.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            mRefreshHeaderViewHeight = mRefreshViewHolder!!.refreshHeaderViewHeight
            mMinWholeHeaderViewPaddingTop = -mRefreshHeaderViewHeight
            mMaxWholeHeaderViewPaddingTop = (mRefreshHeaderViewHeight
                    * mRefreshViewHolder!!.springDistanceScale).toInt()
            mWholeHeaderView!!.setPadding(0, mMinWholeHeaderViewPaddingTop, 0, 0)
            mWholeHeaderView!!.addView(mRefreshHeaderView, 0)
        }
    }

    /**
     * 设置下拉刷新控件下方的自定义控件
     *
     * @param customHeaderView 下拉刷新控件下方的自定义控件
     * @param scrollable       是否可以滚动
     */
    fun setCustomHeaderView(customHeaderView: View?, scrollable: Boolean) {
        if (mCustomHeaderView != null && mCustomHeaderView!!.parent != null) {
            val parent = mCustomHeaderView!!.parent as ViewGroup
            parent.removeView(mCustomHeaderView)
        }
        mCustomHeaderView = customHeaderView
        if (mCustomHeaderView != null) {
            mCustomHeaderView!!.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            mWholeHeaderView!!.addView(mCustomHeaderView)
            mIsCustomHeaderViewScrollable = scrollable
        }
    }

    /**
     * 初始化上拉加载更多控件
     *
     * @return
     */
    private fun initLoadMoreFooterView() {
        mLoadMoreFooterView = mRefreshViewHolder!!.loadMoreFooterView
        if (mLoadMoreFooterView != null) { // 测量上拉加载更多控件的高度
            mLoadMoreFooterView!!.measure(0, 0)
            mLoadMoreFooterViewHeight = mLoadMoreFooterView!!.measuredHeight
            mLoadMoreFooterView!!.visibility = View.GONE
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // 被添加到窗口后再设置监听器，这样开发者就不必烦恼先初始化RefreshLayout还是先设置自定义滚动监听器
        if (!mIsInitedContentViewScrollListener && mLoadMoreFooterView != null) {
            setAbsListViewOnScrollListener()
            addView(mLoadMoreFooterView, childCount)
            mIsInitedContentViewScrollListener = true
        }
    }

    private fun setAbsListViewOnScrollListener() {
        if (mAbsListView != null) {
            try { // 通过反射获取开发者自定义的滚动监听器，并将其替换成自己的滚动监听器，触发滚动时也要通知开发者自定义的滚动监听器（非侵入式，不让开发者继承特定的控件）
// mAbsListView.getClass().getDeclaredField("mOnScrollListener")获取不到mOnScrollListener，必须通过AbsListView.class.getDeclaredField("mOnScrollListener")获取
                val field = AbsListView::class.java.getDeclaredField("mOnScrollListener")
                field.isAccessible = true
                // 开发者自定义的滚动监听器
                val onScrollListener = field[mAbsListView] as AbsListView.OnScrollListener
                mAbsListView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
                    override fun onScrollStateChanged(absListView: AbsListView, scrollState: Int) {
                        if ((scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE || scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING)
                                && shouldHandleAbsListViewLoadingMore(mAbsListView)) {
                            beginLoadingMore()
                        }
                        onScrollListener?.onScrollStateChanged(absListView, scrollState)
                    }

                    override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int,
                                          totalItemCount: Int) {
                        onScrollListener?.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount)
                    }
                })
            } catch (e: Exception) {
                printError(context, e)
            }
        }
    }

    fun shouldHandleAbsListViewLoadingMore(absListView: AbsListView?): Boolean {
        return if (mIsLoadingMore || mCurrentRefreshStatus == RefreshStatus.REFRESHING || mLoadMoreFooterView == null || mDelegate == null || absListView == null || absListView.adapter == null || absListView.adapter.count == 0) {
            false
        } else isAbsListViewToBottom(absListView)
    }

    fun shouldHandleRecyclerViewLoadingMore(recyclerView: RecyclerView): Boolean {
        return if (mIsLoadingMore || mCurrentRefreshStatus == RefreshStatus.REFRESHING || mLoadMoreFooterView == null || mDelegate == null || recyclerView.adapter == null || recyclerView.adapter?.itemCount == null || recyclerView.adapter?.itemCount == 0) {
            false
        } else isRecyclerViewToBottom(recyclerView)
    }

    /**
     * 是否满足处理刷新的条件
     *
     * @return
     */
    private fun shouldHandleLoadingMore(): Boolean {
        if (mIsLoadingMore || mCurrentRefreshStatus == RefreshStatus.REFRESHING || mLoadMoreFooterView == null || mDelegate == null) {
            return false
        }
        // 内容是普通控件，满足
        if (mNormalView != null) {
            return true
        }
        if (isWebViewToBottom(mWebView)) {
            return true
        }
        if (isScrollViewToBottom(mScrollView)) {
            return true
        }
        if (isScrollViewToBottom(mNestedScrollView)) {
            return true
        }
        if (mAbsListView != null) {
            return shouldHandleAbsListViewLoadingMore(mAbsListView)
        }
        return if (mStickyNavLayout != null) {
            mStickyNavLayout!!.shouldHandleLoadingMore()
        } else false
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                mInterceptTouchDownX = event.rawX
                mInterceptTouchDownY = event.rawY
            }
            MotionEvent.ACTION_MOVE -> if (!mIsLoadingMore && mCurrentRefreshStatus != RefreshStatus.REFRESHING) {
                if (mInterceptTouchDownX == -1f) {
                    mInterceptTouchDownX = event.rawX
                }
                if (mInterceptTouchDownY == -1f) {
                    mInterceptTouchDownY = event.rawY
                }
                val interceptTouchMoveDistanceY = (event.rawY - mInterceptTouchDownY).toInt()
                // 可以没有上拉加载更多，但是必须有下拉刷新，否则就不拦截事件
                if (abs(event.rawX - mInterceptTouchDownX) < abs(interceptTouchMoveDistanceY)
                        && mRefreshHeaderView != null) {
                    if (interceptTouchMoveDistanceY > 0 && shouldHandleRefresh()
                            || interceptTouchMoveDistanceY < 0 && shouldHandleLoadingMore()
                            || interceptTouchMoveDistanceY < 0 && !isWholeHeaderViewCompleteInvisible
                            || interceptTouchMoveDistanceY > 0 && shouldInterceptToMoveCustomHeaderViewDown()) { // ACTION_DOWN时没有消耗掉事件，子控件会处于按下状态，这里设置ACTION_CANCEL，使子控件取消按下状态
                        event.action = MotionEvent.ACTION_CANCEL
                        super.onInterceptTouchEvent(event)
                        return true
                    }
                }
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                // 重置
                mInterceptTouchDownX = -1f
                mInterceptTouchDownY = -1f
            }
        }
        return super.onInterceptTouchEvent(event)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (mIsCustomHeaderViewScrollable && !isWholeHeaderViewCompleteInvisible) {
            super.dispatchTouchEvent(ev)
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * 是否满足处理刷新的条件
     *
     * @return
     */
    private fun shouldHandleRefresh(): Boolean {
        return if (!mPullDownRefreshEnable || mIsLoadingMore || mCurrentRefreshStatus == RefreshStatus.REFRESHING || mRefreshHeaderView == null || mDelegate == null) {
            false
        } else isContentViewToTop
    }

    // 内容是普通控件，满足
    private val isContentViewToTop: Boolean
        private get() { // 内容是普通控件，满足
            if (mNormalView != null) {
                return true
            }
            if (isScrollViewOrWebViewToTop(mWebView)) {
                return true
            }
            if (isScrollViewOrWebViewToTop(mScrollView)) {
                return true
            }
            if (isScrollViewOrWebViewToTop(mNestedScrollView)) {
                return true
            }
            return if (isAbsListViewToTop(mAbsListView)) {
                true
            } else isStickyNavLayoutToTop(mStickyNavLayout!!)
        }

    private fun shouldInterceptToMoveCustomHeaderViewDown(): Boolean {
        return (isContentViewToTop && mCustomHeaderView != null && mIsCustomHeaderViewScrollable
                && !isCustomHeaderViewCompleteVisible)
    }

    private fun shouldInterceptToMoveCustomHeaderViewUp(): Boolean {
        return (isContentViewToTop && mCustomHeaderView != null && mIsCustomHeaderViewScrollable
                && !isWholeHeaderViewCompleteInvisible)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (null != mRefreshHeaderView) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mWholeHeaderDownY = event.y.toInt()
                    if (mCustomHeaderView != null) {
                        mWholeHeaderViewDownPaddingTop = mWholeHeaderView!!.paddingTop
                    }
                    if (mCustomHeaderView == null || !mIsCustomHeaderViewScrollable) {
                        mRefreshDownY = event.y.toInt()
                    }
                    if (isWholeHeaderViewCompleteInvisible) {
                        mRefreshDownY = event.y.toInt()
                        return true
                    }
                }
                MotionEvent.ACTION_MOVE -> if (checkActionMove(event.y, mRefreshDownY.toFloat()) && handleActionMove(event)) {
                    return true
                }
                MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> if (handleActionUpOrCancel(event)) {
                    return true
                }
                else -> {
                }
            }
        }
        return super.onTouchEvent(event)
    }

    private fun checkActionMove(actionY: Float, downY: Float): Boolean {
        return actionEventCallback == null || actionEventCallback!!.checkActionMove(actionY, downY)
    }// 0表示x，1表示y

    /**
     * 自定义头部控件是否已经完全显示
     *
     * @return true表示已经完全显示，false表示没有完全显示
     */
    private val isCustomHeaderViewCompleteVisible: Boolean
        private get() {
            if (mCustomHeaderView != null) { // 0表示x，1表示y
                val location = IntArray(2)
                getLocationOnScreen(location)
                val mOnScreenY = location[1]
                mCustomHeaderView!!.getLocationOnScreen(location)
                val customHeaderViewOnScreenY = location[1]
                return mOnScreenY <= customHeaderViewOnScreenY
            }
            return true
        }// 0表示x，1表示y

    /**
     * 整个头部控件是否已经完全隐藏
     *
     * @return true表示完全隐藏，false表示没有完全隐藏
     */
    private val isWholeHeaderViewCompleteInvisible: Boolean
        private get() {
            if (mCustomHeaderView != null && mIsCustomHeaderViewScrollable) { // 0表示x，1表示y
                val location = IntArray(2)
                getLocationOnScreen(location)
                val mOnScreenY = location[1]
                mWholeHeaderView!!.getLocationOnScreen(location)
                val wholeHeaderViewOnScreenY = location[1]
                return wholeHeaderViewOnScreenY + mWholeHeaderView!!.measuredHeight <= mOnScreenY
            }
            return true
        }

    /**
     * 处理手指滑动事件
     *
     * @param event
     * @return true表示自己消耗掉该事件，false表示不消耗该事件
     */
    private fun handleActionMove(event: MotionEvent): Boolean {
        if (mCurrentRefreshStatus == RefreshStatus.REFRESHING || mIsLoadingMore) {
            return false
        }
        if ((mCustomHeaderView == null || !mIsCustomHeaderViewScrollable) && mRefreshDownY == -1) {
            mRefreshDownY = event.y.toInt()
        }
        if (mCustomHeaderView != null && mIsCustomHeaderViewScrollable && isCustomHeaderViewCompleteVisible
                && mRefreshDownY == -1) {
            mRefreshDownY = event.y.toInt()
        }
        var refreshDiffY = event.y.toInt() - mRefreshDownY
        refreshDiffY = (refreshDiffY / mRefreshViewHolder!!.paddingTopScale).toInt()
        // 如果是向下拉，并且当前可见的第一个条目的索引等于0，才处理整个头部控件的padding
        if (refreshDiffY > 0 && shouldHandleRefresh() && isCustomHeaderViewCompleteVisible) {
            var paddingTop = mMinWholeHeaderViewPaddingTop + refreshDiffY
            if (paddingTop > 0 && mCurrentRefreshStatus != RefreshStatus.RELEASE_REFRESH) { // 下拉刷新控件完全显示，并且当前状态没有处于释放开始刷新状态
                mCurrentRefreshStatus = RefreshStatus.RELEASE_REFRESH
                handleRefreshStatusChanged()
                mRefreshViewHolder!!.handleScale(1.0f, refreshDiffY)
            } else if (paddingTop < 0) { // 下拉刷新控件没有完全显示，并且当前状态没有处于下拉刷新状态
                if (mCurrentRefreshStatus != RefreshStatus.PULL_DOWN) {
                    val isPreRefreshStatusNotIdle = mCurrentRefreshStatus != RefreshStatus.IDLE
                    mCurrentRefreshStatus = RefreshStatus.PULL_DOWN
                    if (isPreRefreshStatusNotIdle) {
                        handleRefreshStatusChanged()
                    }
                }
                val scale = 1 - paddingTop * 1.0f / mMinWholeHeaderViewPaddingTop
                /**
                 * 往下滑 paddingTop mMinWholeHeaderViewPaddingTop 到 0 scale 0 到 1
                 * 往上滑 paddingTop 0 到 mMinWholeHeaderViewPaddingTop scale 1 到 0
                 */
                mRefreshViewHolder!!.handleScale(scale, refreshDiffY)
            }
            paddingTop = min(paddingTop, mMaxWholeHeaderViewPaddingTop)
            mWholeHeaderView!!.setPadding(0, paddingTop, 0, 0)
            if (mRefreshViewHolder!!.canChangeToRefreshingStatus()) {
                mWholeHeaderDownY = -1
                mRefreshDownY = -1
                beginRefreshing()
            }
            return true
        }
        if (mCustomHeaderView != null && mIsCustomHeaderViewScrollable) {
            if (mWholeHeaderDownY == -1) {
                mWholeHeaderDownY = event.y.toInt()
                if (mCustomHeaderView != null) {
                    mWholeHeaderViewDownPaddingTop = mWholeHeaderView!!.paddingTop
                }
            }
            val wholeHeaderDiffY = event.y.toInt() - mWholeHeaderDownY
            if (mPullDownRefreshEnable && !isWholeHeaderViewCompleteInvisible
                    || wholeHeaderDiffY > 0 && shouldInterceptToMoveCustomHeaderViewDown()
                    || wholeHeaderDiffY < 0 && shouldInterceptToMoveCustomHeaderViewUp()) {
                var paddingTop = mWholeHeaderViewDownPaddingTop + wholeHeaderDiffY
                if (paddingTop < mMinWholeHeaderViewPaddingTop - mCustomHeaderView!!.measuredHeight) {
                    paddingTop = mMinWholeHeaderViewPaddingTop - mCustomHeaderView!!.measuredHeight
                }
                mWholeHeaderView!!.setPadding(0, paddingTop, 0, 0)
                return true
            }
        }
        return false
    }

    /**
     * 处理手指抬起事件
     *
     * @return true表示自己消耗掉该事件，false表示不消耗该事件
     */
    private fun handleActionUpOrCancel(event: MotionEvent): Boolean {
        var isReturnTrue = false
        // 如果当前头部刷新控件没有完全隐藏，则需要返回true，自己消耗ACTION_UP事件
        if ((mCustomHeaderView == null || mCustomHeaderView != null && !mIsCustomHeaderViewScrollable)
                && mWholeHeaderView!!.paddingTop != mMinWholeHeaderViewPaddingTop) {
            isReturnTrue = true
        }
        if (mCurrentRefreshStatus == RefreshStatus.PULL_DOWN || mCurrentRefreshStatus == RefreshStatus.IDLE) { // 处于下拉刷新状态，松手时隐藏下拉刷新控件
            if (mCustomHeaderView == null || mCustomHeaderView != null && mWholeHeaderView!!.paddingTop < 0 && mWholeHeaderView!!.paddingTop > mMinWholeHeaderViewPaddingTop) {
                hiddenRefreshHeaderView()
            }
            mCurrentRefreshStatus = RefreshStatus.IDLE
            handleRefreshStatusChanged()
        } else if (mCurrentRefreshStatus == RefreshStatus.RELEASE_REFRESH) { // 处于松开进入刷新状态，松手时完全显示下拉刷新控件，进入正在刷新状态
            beginRefreshing()
        }
        if (mRefreshDownY == -1) {
            mRefreshDownY = event.y.toInt()
        }
        val diffY = event.y.toInt() - mRefreshDownY
        if (shouldHandleLoadingMore() && diffY <= 0) { // 处理上拉加载更多，需要返回true，自己消耗ACTION_UP事件
            isReturnTrue = true
            beginLoadingMore()
        }
        mWholeHeaderDownY = -1
        mRefreshDownY = -1
        return isReturnTrue
    }

    /**
     * 处理下拉刷新控件状态变化
     */
    private fun handleRefreshStatusChanged() {
        when (mCurrentRefreshStatus) {
            RefreshStatus.IDLE -> mRefreshViewHolder!!.changeToIdle()
            RefreshStatus.PULL_DOWN -> mRefreshViewHolder!!.changeToPullDown()
            RefreshStatus.RELEASE_REFRESH -> mRefreshViewHolder!!.changeToReleaseRefresh()
            RefreshStatus.REFRESHING -> mRefreshViewHolder!!.changeToRefreshing()
            else -> {
            }
        }
    }

    /**
     * 切换到正在刷新状态，会调用delegate的onBGARefreshLayoutBeginRefreshing方法
     */
    private fun beginRefreshing() {
        if (mCurrentRefreshStatus != RefreshStatus.REFRESHING && mDelegate != null) {
            mCurrentRefreshStatus = RefreshStatus.REFRESHING
            changeRefreshHeaderViewToZero()
            handleRefreshStatusChanged()
            mDelegate!!.onBGARefreshLayoutBeginRefreshing(this)
        }
    }

    /**
     * 结束下拉刷新
     */
    fun endRefreshing() {
        if (mCurrentRefreshStatus == RefreshStatus.REFRESHING) {
            mCurrentRefreshStatus = RefreshStatus.IDLE
            hiddenRefreshHeaderView()
            handleRefreshStatusChanged()
            mRefreshViewHolder!!.onEndRefreshing()
        }
    }

    /**
     * 隐藏下拉刷新控件，带动画
     */
    private fun hiddenRefreshHeaderView() {
        val animator = ValueAnimator.ofInt(mWholeHeaderView!!.paddingTop, mMinWholeHeaderViewPaddingTop)
        animator.duration = mRefreshViewHolder!!.topAnimDuration.toLong()
        animator.addUpdateListener { animation ->
            val paddingTop = animation.animatedValue as Int
            mWholeHeaderView!!.setPadding(0, paddingTop, 0, 0)
        }
        animator.start()
    }

    /**
     * 设置下拉刷新控件的paddingTop到0，带动画
     */
    private fun changeRefreshHeaderViewToZero() {
        val animator = ValueAnimator.ofInt(mWholeHeaderView!!.paddingTop, 0)
        animator.duration = mRefreshViewHolder!!.topAnimDuration.toLong()
        animator.addUpdateListener { animation ->
            val paddingTop = animation.animatedValue as Int
            mWholeHeaderView!!.setPadding(0, paddingTop, 0, 0)
        }
        animator.start()
    }

    /**
     * 开始上拉加载更多，会触发delegate的onBGARefreshLayoutBeginRefreshing方法
     */
    fun beginLoadingMore() {
        if (!mIsLoadingMore && mLoadMoreFooterView != null && mDelegate != null && mDelegate!!.onBGARefreshLayoutBeginLoadingMore(this)) {
            mIsLoadingMore = true
            if (mIsShowLoadingMoreView) {
                showLoadingMoreView()
            }
        }
    }

    /**
     * 显示上拉加载更多控件
     */
    private fun showLoadingMoreView() {
        mRefreshViewHolder!!.changeToLoadingMore()
        mLoadMoreFooterView!!.visibility = View.VISIBLE
        scrollToBottom(mScrollView)
        scrollToBottom(mNestedScrollView)
        scrollToBottom(mAbsListView)
        if (mStickyNavLayout != null) {
            mStickyNavLayout!!.scrollToBottom()
        }
    }

    /**
     * 结束上拉加载更多
     */
    fun endLoadingMore() {
        if (mIsLoadingMore) {
            if (mIsShowLoadingMoreView) { // 避免WiFi环境下请求数据太快，加载更多控件一闪而过
                mHandler.postDelayed(mDelayHiddenLoadingMoreViewTask, 300)
            } else {
                mIsLoadingMore = false
            }
        }
    }

    private val mDelayHiddenLoadingMoreViewTask = Runnable {
        mIsLoadingMore = false
        mRefreshViewHolder!!.onEndLoadingMore()
        mLoadMoreFooterView!!.visibility = View.GONE
    }

    /**
     * 上拉加载更多时是否显示加载更多控件
     *
     * @param isShowLoadingMoreView
     */
    fun setIsShowLoadingMoreView(isShowLoadingMoreView: Boolean) {
        mIsShowLoadingMoreView = isShowLoadingMoreView
    }

    /**
     * 设置下拉刷新是否可用
     *
     * @param pullDownRefreshEnable
     */
    fun setPullDownRefreshEnable(pullDownRefreshEnable: Boolean) {
        mPullDownRefreshEnable = pullDownRefreshEnable
    }

    /**
     * 设置下拉刷新上拉加载更多代理
     *
     * @param delegate
     */
    fun setDelegate(delegate: BGARefreshLayoutDelegate?) {
        mDelegate = delegate
    }

    fun startNestedScroll(axes: Int, type: Int): Boolean {
        return mChildHelper.startNestedScroll(axes, type)
    }

    fun stopNestedScroll(type: Int) {
        mChildHelper.stopNestedScroll(type)
    }

    fun hasNestedScrollingParent(type: Int): Boolean {
        return mChildHelper.hasNestedScrollingParent(type)
    }

    fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int): Boolean {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow, type)
    }

    fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?, type: Int): Boolean {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow, type)
    }

    fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
    }

    fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type)
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, type)
    }

    fun onStopNestedScroll(target: View, type: Int) {
        mParentHelper.onStopNestedScroll(target, type)
        stopNestedScroll(type)
    }

    fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        val oldScrollY = scrollY
        scrollBy(0, dyUnconsumed)
        val myConsumed = scrollY - oldScrollY
        val myUnconsumed = dyUnconsumed - myConsumed
        dispatchNestedScroll(0, myConsumed, 0, myUnconsumed, null,
                type)
    }

    fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        dispatchNestedPreScroll(dx, dy, consumed, null, type)
    }

    interface BGARefreshLayoutDelegate {
        /**
         * 开始刷新
         */
        fun onBGARefreshLayoutBeginRefreshing(refreshLayout: BGARefreshLayout?)

        /**
         * 开始加载更多。由于监听了ScrollView、RecyclerView、AbsListView滚动到底部的事件，
         * 所以这里采用返回boolean来处理是否加载更多。否则使用endLoadingMore方法会失效
         *
         * @param refreshLayout
         * @return 如果要开始加载更多则返回true，否则返回false。（返回false的场景：没有网络、
         * 一共只有x页数据并且已经加载了x页数据了）
         */
        fun onBGARefreshLayoutBeginLoadingMore(refreshLayout: BGARefreshLayout?): Boolean
    }

    enum class RefreshStatus {
        IDLE, PULL_DOWN, RELEASE_REFRESH, REFRESHING
    }

    @JvmField
    var actionEventCallback: ActionEventCallback? = null

    fun setActionEventCallback(actionEventCallback: ActionEventCallback?) {
        this.actionEventCallback = actionEventCallback
    }

    interface ActionEventCallback {
        fun checkActionMove(actionY: Float, downY: Float): Boolean
    }
}