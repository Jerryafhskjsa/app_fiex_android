package com.black.base.lib.refresh

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.black.base.R

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com 创建时间:15/5/21 12:56
 * 描述:继承该抽象类实现响应的抽象方法，做出各种下拉刷新效果。参考BGANormalRefreshViewHolder、
 * BGAStickinessRefreshViewHolder、BGAMoocStyleRefreshViewHolder、
 * BGAMeiTuanRefreshViewHolder
 */
abstract class BGARefreshViewHolder(protected var mContext: Context, isLoadingMoreEnabled: Boolean) {
    /**
     * 手指移动距离与下拉刷新控件paddingTop移动距离的比值
     *
     * @return
     */
    /**
     * 手指移动距离与下拉刷新控件paddingTop移动距离的比值，默认1.8f
     */
    var paddingTopScale = PULL_DISTANCE_SCALE
        private set
    /**
     * 下拉刷新控件paddingTop的弹簧距离与下拉刷新控件高度的比值，默认0.4f
     */
    private var mSpringDistanceScale = SPRING_DISTANCE_SCALE
    /**
     * 下拉刷新上拉加载更多控件
     */
    protected var mRefreshLayout: BGARefreshLayout? = null
    /**
     * 下拉刷新控件
     */
    protected var mRefreshHeaderView: View? = null
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
     * 底部加载更多菊花drawable
     */
    protected var mFooterChrysanthemumAd: AnimationDrawable? = null
    /**
     * 正在加载更多时的文本
     */
    protected var mLodingMoreText: String
    /**
     * 是否开启加载更多功能
     */
    private var mIsLoadingMoreEnabled = true
    /**
     * 整个加载更多控件的背景颜色资源id
     */
    private var mLoadMoreBackgroundColorRes = -1
    /**
     * 整个加载更多控件的背景drawable资源id
     */
    private var mLoadMoreBackgroundDrawableRes = -1
    /**
     * 下拉刷新控件的背景颜色资源id
     */
    protected var mRefreshViewBackgroundColorRes = -1
    /**
     * 下拉刷新控件的背景drawable资源id
     */
    protected var mRefreshViewBackgroundDrawableRes = -1
    /**
     * 获取顶部未满足下拉刷新条件时回弹到初始状态、满足刷新条件时回弹到正在刷新状态、刷新完毕后回弹到初始状态的动画时间，默认为500毫秒
     *
     * @return
     */
    /**
     * 设置顶部未满足下拉刷新条件时回弹到初始状态、满足刷新条件时回弹到正在刷新状态、刷新完毕后回弹到初始状态的动画时间，默认为300毫秒
     *
     * @param topAnimDuration
     */
    /**
     * 头部控件移动动画时常
     */
    var topAnimDuration = 500

    /**
     * 设置正在加载更多时的文本
     *
     * @param loadingMoreText
     */
    fun setLoadingMoreText(loadingMoreText: String) {
        mLodingMoreText = loadingMoreText
    }

    /**
     * 设置整个加载更多控件的背景颜色资源id
     *
     * @param loadMoreBackgroundColorRes
     */
    fun setLoadMoreBackgroundColorRes(@ColorRes loadMoreBackgroundColorRes: Int) {
        mLoadMoreBackgroundColorRes = loadMoreBackgroundColorRes
    }

    /**
     * 设置整个加载更多控件的背景drawable资源id
     *
     * @param loadMoreBackgroundDrawableRes
     */
    fun setLoadMoreBackgroundDrawableRes(@DrawableRes loadMoreBackgroundDrawableRes: Int) {
        mLoadMoreBackgroundDrawableRes = loadMoreBackgroundDrawableRes
    }

    /**
     * 设置下拉刷新控件的背景颜色资源id
     *
     * @param refreshViewBackgroundColorRes
     */
    fun setRefreshViewBackgroundColorRes(@ColorRes refreshViewBackgroundColorRes: Int) {
        mRefreshViewBackgroundColorRes = refreshViewBackgroundColorRes
    }

    /**
     * 设置下拉刷新控件的背景drawable资源id
     *
     * @param refreshViewBackgroundDrawableRes
     */
    fun setRefreshViewBackgroundDrawableRes(@DrawableRes refreshViewBackgroundDrawableRes: Int) {
        mRefreshViewBackgroundDrawableRes = refreshViewBackgroundDrawableRes
    }

    /**
     * 获取上拉加载更多控件，如果不喜欢这种上拉刷新风格可重写该方法实现自定义LoadMoreFooterView
     *
     * @return
     */
    open val loadMoreFooterView: View?
        get() {
            if (!mIsLoadingMoreEnabled) {
                return null
            }
            if (mLoadMoreFooterView == null) {
                mLoadMoreFooterView = View.inflate(mContext, R.layout.view_normal_refresh_footer, null)
                mLoadMoreFooterView?.setBackgroundColor(Color.TRANSPARENT)
                if (mLoadMoreBackgroundColorRes != -1) {
                    mLoadMoreFooterView?.setBackgroundResource(mLoadMoreBackgroundColorRes)
                }
                if (mLoadMoreBackgroundDrawableRes != -1) {
                    mLoadMoreFooterView?.setBackgroundResource(mLoadMoreBackgroundDrawableRes)
                }
                mFooterStatusTv = mLoadMoreFooterView?.findViewById(R.id.tv_normal_refresh_footer_status)
                mFooterChrysanthemumIv = mLoadMoreFooterView?.findViewById(R.id.iv_normal_refresh_footer_chrysanthemum)
                mFooterChrysanthemumAd = mFooterChrysanthemumIv?.getDrawable() as AnimationDrawable
                mFooterStatusTv?.setText(mLodingMoreText)
            }
            return mLoadMoreFooterView
        }

    /**
     * 获取头部下拉刷新控件
     *
     * @return
     */
    abstract val refreshHeaderView: View?

    /**
     * 下拉刷新控件可见时，处理上下拉进度
     *
     * @param scale         下拉过程0 到 1，回弹过程1 到 0，没有加上弹簧距离移动时的比例
     * @param moveYDistance 整个下拉刷新控件paddingTop变化的值，如果有弹簧距离，会大于整个下拉刷新控件的高度
     */
    abstract fun handleScale(scale: Float, moveYDistance: Int)

    /**
     * 进入到未处理下拉刷新状态
     */
    abstract fun changeToIdle()

    /**
     * 进入下拉状态
     */
    abstract fun changeToPullDown()

    /**
     * 进入释放刷新状态
     */
    abstract fun changeToReleaseRefresh()

    /**
     * 进入正在刷新状态
     */
    abstract fun changeToRefreshing()

    /**
     * 结束下拉刷新
     */
    abstract fun onEndRefreshing()

    /**
     * 设置手指移动距离与下拉刷新控件paddingTop移动距离的比值
     *
     * @param pullDistanceScale
     */
    fun setPullDistanceScale(pullDistanceScale: Float) {
        paddingTopScale = pullDistanceScale
    }

    /**
     * 下拉刷新控件paddingTop的弹簧距离与下拉刷新控件高度的比值
     *
     * @return
     */
    /**
     * 设置下拉刷新控件paddingTop的弹簧距离与下拉刷新控件高度的比值，不能小于0，如果刷新控件比较高，建议将该值设置小一些
     *
     * @param springDistanceScale
     */
    var springDistanceScale: Float
        get() = mSpringDistanceScale
        set(springDistanceScale) {
            if (springDistanceScale < 0) {
                throw RuntimeException(mContext.getString(R.string.error_message_06, "paddingTop", "springDistanceScale"))
            }
            mSpringDistanceScale = springDistanceScale
        }

    /**
     * 是处于能够进入刷新状态
     *
     * @return
     */
    open fun canChangeToRefreshingStatus(): Boolean {
        return false
    }

    /**
     * 进入加载更多状态
     */
    fun changeToLoadingMore() {
        if (mIsLoadingMoreEnabled && mFooterChrysanthemumAd != null) {
            mFooterChrysanthemumAd?.start()
        }
    }

    /**
     * 结束上拉加载更多
     */
    fun onEndLoadingMore() {
        if (mIsLoadingMoreEnabled && mFooterChrysanthemumAd != null) {
            mFooterChrysanthemumAd?.stop()
        }
    }// 测量下拉刷新控件的高度

    /**
     * 获取下拉刷新控件的高度，如果初始化时的高度和最后展开的最大高度不一致，需重写该方法返回最大高度
     *
     * @return
     */
    val refreshHeaderViewHeight: Int
        get() {
            if (mRefreshHeaderView != null) { // 测量下拉刷新控件的高度
                mRefreshHeaderView?.measure(0, 0)
                return mRefreshHeaderView?.measuredHeight ?: 0
            }
            return 0
        }

    /**
     * 改变整个下拉刷新头部控件移动一定的距离（带动画），自定义刷新控件进入刷新状态前后的高度有变化时可以使用该方法（
     * 参考BGAStickinessRefreshView）
     *
     * @param distance
     */
    fun startChangeWholeHeaderViewPaddingTop(distance: Int) {
        mRefreshLayout?.startChangeWholeHeaderViewPaddingTop(distance)
    }

    /**
     * 设置下拉刷新上拉加载更多控件，
     * 该方法是设置BGARefreshViewHolder给BGARefreshLayout时由BGARefreshLayout调用
     *
     * @param refreshLayout
     */
    fun setRefreshLayout(refreshLayout: BGARefreshLayout?) {
        mRefreshLayout = refreshLayout
    }

    companion object {
        /**
         * 手指移动距离与下拉刷新控件paddingTop移动距离的比值
         */
        private const val PULL_DISTANCE_SCALE = 1.8f
        /**
         * 下拉刷新控件paddingTop的弹簧距离与下拉刷新控件高度的比值
         */
        private const val SPRING_DISTANCE_SCALE = 0.4f
    }

    /**
     * @param context
     * @param isLoadingMoreEnabled 上拉加载更多是否可用
     */
    init {
        mLodingMoreText = mContext.getString(R.string.loading)
        mIsLoadingMoreEnabled = isLoadingMoreEnabled
    }
}