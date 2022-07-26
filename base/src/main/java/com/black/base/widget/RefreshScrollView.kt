package com.black.base.widget

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import com.airbnb.lottie.LottieAnimationView
import com.black.base.R
import com.black.base.util.CookieUtil.getNightMode

class RefreshScrollView : NestedScrollView {
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    public override fun onFinishInflate() {
        super.onFinishInflate()
        val child = getChildAt(0)
        if (child is LinearLayout) {
            val linearLayout = child
            if (linearLayout.orientation == LinearLayout.VERTICAL) {
                linearLayout.addView(headerView, 0)
                initLoadMoreFooterView()
                linearLayout.addView(mLoadMoreFooterView)
            }
        }
    }

    var mRefreshHeaderView: View? = null
    var headerAnimationView: LottieAnimationView? = null
    /**
     * 下拉刷新控件的高度
     */
    private var mRefreshHeaderViewHeight = 0
    /**
     * 整个头部控件最小的paddingTop
     */
    private var mMinWholeHeaderViewPaddingTop = 0
    /**
     * 整个头部控件最大的paddingTop
     */
    private var mMaxWholeHeaderViewPaddingTop = 0

    private val headerView: View?
        private get() {
            if (mRefreshHeaderView == null) {
                mRefreshHeaderView = View.inflate(context, R.layout.view_refresh_header_frying, null)
                mRefreshHeaderView?.setBackgroundColor(Color.TRANSPARENT)
                headerAnimationView = mRefreshHeaderView?.findViewById(R.id.animation_view)
                headerAnimationView?.setRepeatCount(Int.MAX_VALUE)
                if (getNightMode(context)) {
                    headerAnimationView?.setAnimation("pull_down_refresh_night.json")
                } else {
                    headerAnimationView?.setAnimation("pull_down_refresh.json")
                }
            }
            mRefreshHeaderView!!.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            mRefreshHeaderViewHeight = refreshHeaderViewHeight
            mMinWholeHeaderViewPaddingTop = -mRefreshHeaderViewHeight
            mMaxWholeHeaderViewPaddingTop = (mRefreshHeaderViewHeight * 0.4).toInt()
            mRefreshHeaderView!!.setPadding(0, mMinWholeHeaderViewPaddingTop, 0, 0)
            return mRefreshHeaderView
        }

    // 测量下拉刷新控件的高度
    val refreshHeaderViewHeight: Int
        get() {
            if (mRefreshHeaderView != null) { // 测量下拉刷新控件的高度
                mRefreshHeaderView!!.measure(0, 0)
                return mRefreshHeaderView!!.measuredHeight
            }
            return 0
        }

    /**
     * 上拉加载更多控件
     */
    protected var mLoadMoreFooterView: View? = null
    /**
     * 底部加载更多提示控件
     */
    protected var mFooterStatusTv: TextView? = null
    /**
     * 底部加载更多菊花控件
     */
    protected var mFooterChrysanthemumIv: ImageView? = null
    /**
     * 上拉加载更多控件的高度
     */
    private var mLoadMoreFooterViewHeight = 0

    /**
     * 获取上拉加载更多控件，如果不喜欢这种上拉刷新风格可重写该方法实现自定义LoadMoreFooterView
     *
     * @return
     */
    val loadMoreFooterView: View?
        get() {
            if (mLoadMoreFooterView == null) {
                mLoadMoreFooterView = View.inflate(context, R.layout.view_normal_refresh_footer, null)
                mLoadMoreFooterView?.setBackgroundColor(Color.TRANSPARENT)
                mFooterStatusTv = mLoadMoreFooterView?.findViewById(R.id.tv_normal_refresh_footer_status)
                mFooterChrysanthemumIv = mLoadMoreFooterView?.findViewById(R.id.iv_normal_refresh_footer_chrysanthemum)
                mFooterStatusTv?.setText(R.string.loading)
            }
            return mLoadMoreFooterView
        }

    /**
     * 初始化上拉加载更多控件
     *
     * @return
     */
    private fun initLoadMoreFooterView() {
        mLoadMoreFooterView = loadMoreFooterView
        if (mLoadMoreFooterView != null) { // 测量上拉加载更多控件的高度
            mLoadMoreFooterView!!.measure(0, 0)
            mLoadMoreFooterViewHeight = mLoadMoreFooterView!!.measuredHeight
            mLoadMoreFooterView!!.visibility = View.GONE
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        return super.onTouchEvent(ev)
    }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return super.onStartNestedScroll(child, target, axes, type)
    }

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
        return super.onStartNestedScroll(child, target, nestedScrollAxes)
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return super.startNestedScroll(axes)
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return super.startNestedScroll(axes, type)
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int): Boolean {
        return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow, type)
    }

    override fun dispatchNestedScroll(dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, offsetInWindow: IntArray?): Boolean {
        return super.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        super.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun onNestedScrollAccepted(child: View, target: View, nestedScrollAxes: Int) {
        super.onNestedScrollAccepted(child, target, nestedScrollAxes)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        super.onNestedPreScroll(target, dx, dy, consumed)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        super.onNestedPreScroll(target, dx, dy, consumed, type)
    }
}