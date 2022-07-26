package com.black.base.lib.refresh

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.AnimationDrawable
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import android.widget.TextView
import com.black.base.R

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com 创建时间:15/5/21 13:05 描述:类似新浪微博下拉刷新风格
 */
class BGANormalRefreshViewHolder(context: Context, isLoadingMoreEnabled: Boolean) : BGARefreshViewHolder(context, isLoadingMoreEnabled) {
    private var mHeaderStatusTv: TextView? = null
    private var mHeaderArrowIv: ImageView? = null
    private var mHeaderChrysanthemumIv: ImageView? = null
    private var mHeaderChrysanthemumAd: AnimationDrawable? = null
    private var mUpAnim: RotateAnimation? = null
    private var mDownAnim: RotateAnimation? = null
    private var mPullDownRefreshText: String
    private var mReleaseRefreshText: String
    private var mRefreshingText: String

    /**
     * @param context
     * @param isLoadingMoreEnabled 上拉加载更多是否可用
     */
    init {
        mPullDownRefreshText = context.getString(R.string.pull_down_refresh)
        mReleaseRefreshText = context.getString(R.string.release_update)
        mRefreshingText = context.getString(R.string.loading)
        initAnimation()
    }

    private fun initAnimation() {
        mUpAnim = RotateAnimation(0f, (-180).toFloat(), Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        mUpAnim?.duration = 150
        mUpAnim?.fillAfter = true
        mDownAnim = RotateAnimation((-180).toFloat(), 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        mDownAnim?.fillAfter = true
    }

    /**
     * 设置未满足刷新条件，提示继续往下拉的文本
     *
     * @param pullDownRefreshText
     */
    fun setPullDownRefreshText(pullDownRefreshText: String) {
        mPullDownRefreshText = pullDownRefreshText
    }

    /**
     * 设置满足刷新条件时的文本
     *
     * @param releaseRefreshText
     */
    fun setReleaseRefreshText(releaseRefreshText: String) {
        mReleaseRefreshText = releaseRefreshText
    }

    /**
     * 设置正在刷新时的文本
     *
     * @param refreshingText
     */
    fun setRefreshingText(refreshingText: String) {
        mRefreshingText = refreshingText
    }

    override val refreshHeaderView: View
        get() {
            if (mRefreshHeaderView == null) {
                mRefreshHeaderView = View.inflate(mContext, R.layout.view_refresh_header_normal, null)
                mRefreshHeaderView?.setBackgroundColor(Color.TRANSPARENT)
                if (mRefreshViewBackgroundColorRes != -1) {
                    mRefreshHeaderView?.setBackgroundResource(mRefreshViewBackgroundColorRes)
                }
                if (mRefreshViewBackgroundDrawableRes != -1) {
                    mRefreshHeaderView?.setBackgroundResource(mRefreshViewBackgroundDrawableRes)
                }
                mHeaderStatusTv = mRefreshHeaderView?.findViewById(R.id.tv_normal_refresh_header_status)
                mHeaderArrowIv = mRefreshHeaderView?.findViewById(R.id.iv_normal_refresh_header_arrow)
                mHeaderChrysanthemumIv = mRefreshHeaderView?.findViewById(R.id.iv_normal_refresh_header_chrysanthemum)
                mHeaderChrysanthemumAd = mHeaderChrysanthemumIv?.drawable as AnimationDrawable
                mHeaderStatusTv?.text = mPullDownRefreshText
            }
            return mRefreshHeaderView!!
        }

    override fun handleScale(scale: Float, moveYDistance: Int) {}
    override fun changeToIdle() {}
    override fun changeToPullDown() {
        mHeaderStatusTv?.text = mPullDownRefreshText
        mHeaderChrysanthemumIv?.visibility = View.INVISIBLE
        mHeaderChrysanthemumAd?.stop()
        mHeaderArrowIv?.visibility = View.VISIBLE
        mDownAnim?.duration = 150
        mHeaderArrowIv?.startAnimation(mDownAnim)
    }

    override fun changeToReleaseRefresh() {
        mHeaderStatusTv?.text = mReleaseRefreshText
        mHeaderChrysanthemumIv?.visibility = View.INVISIBLE
        mHeaderChrysanthemumAd?.stop()
        mHeaderArrowIv?.visibility = View.VISIBLE
        mHeaderArrowIv?.startAnimation(mUpAnim)
    }

    override fun changeToRefreshing() {
        mHeaderStatusTv?.text = mRefreshingText
        // 必须把动画清空才能隐藏成功
        mHeaderArrowIv?.clearAnimation()
        mHeaderArrowIv?.visibility = View.INVISIBLE
        mHeaderChrysanthemumIv?.visibility = View.VISIBLE
        mHeaderChrysanthemumAd?.start()
    }

    override fun onEndRefreshing() {
        mHeaderStatusTv?.text = mPullDownRefreshText
        mHeaderChrysanthemumIv?.visibility = View.INVISIBLE
        mHeaderChrysanthemumAd?.stop()
        mHeaderArrowIv?.visibility = View.VISIBLE
        mDownAnim?.duration = 0
        mHeaderArrowIv?.startAnimation(mDownAnim)
    }
}