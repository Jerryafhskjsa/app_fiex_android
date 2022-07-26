package com.black.base.lib.refresh

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.black.base.R

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com 创建时间:15/5/21 13:05 描述:类似新浪微博下拉刷新风格
 */
class BGAFryingRefreshViewHolder(context: Context?, isLoadingMoreEnabled: Boolean) : BGARefreshViewHolder(context!!, isLoadingMoreEnabled) {
    //    private LottieAnimationView headerAnimationView;
    private var loadingCircleView: ImageView? = null
    private var animation: Animation? = null
    private var mUpAnim: RotateAnimation? = null
    private var mDownAnim: RotateAnimation? = null

    /**
     * @param context
     * @param isLoadingMoreEnabled 上拉加载更多是否可用
     */
    init {
        initAnimation()
    }

    private fun initAnimation() {
        mUpAnim = RotateAnimation(0f, (-180).toFloat(), Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        mUpAnim!!.duration = 150
        mUpAnim!!.fillAfter = true
        mDownAnim = RotateAnimation((-180).toFloat(), 0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        mDownAnim!!.fillAfter = true
    }

    /**
     * 设置未满足刷新条件，提示继续往下拉的文本
     *
     * @param pullDownRefreshText
     */
    fun setPullDownRefreshText(pullDownRefreshText: String?) {}

    /**
     * 设置满足刷新条件时的文本
     *
     * @param releaseRefreshText
     */
    fun setReleaseRefreshText(releaseRefreshText: String?) {}

    /**
     * 设置正在刷新时的文本
     *
     * @param refreshingText
     */
    fun setRefreshingText(refreshingText: String?) {}

    //            headerAnimationView = mRefreshHeaderView.findViewById(R.id.animation_view);
//            headerAnimationView.setRepeatCount(Integer.MAX_VALUE);
//            if (CookieUtil.getNightMode(mContext)) {
//                headerAnimationView.setAnimation("pull_down_refresh_night.json");
//            } else {
//                headerAnimationView.setAnimation("pull_down_refresh.json");
//            }
    override val refreshHeaderView: View
        get() {
            if (mRefreshHeaderView == null) {
                mRefreshHeaderView = View.inflate(mContext, R.layout.view_refresh_header_frying, null)
                mRefreshHeaderView?.setBackgroundColor(Color.TRANSPARENT)
                if (mRefreshViewBackgroundColorRes != -1) {
                    mRefreshHeaderView?.setBackgroundResource(mRefreshViewBackgroundColorRes)
                }
                if (mRefreshViewBackgroundDrawableRes != -1) {
                    mRefreshHeaderView?.setBackgroundResource(mRefreshViewBackgroundDrawableRes)
                }
                loadingCircleView = mRefreshHeaderView?.findViewById(R.id.loading_circle)
                animation = AnimationUtils.loadAnimation(mContext, R.anim.anim_loading_circle)
                animation?.repeatCount = Animation.INFINITE
                animation?.repeatMode = Animation.RESTART
                loadingCircleView?.animation = animation
                //            headerAnimationView = mRefreshHeaderView.findViewById(R.id.animation_view);
                //            headerAnimationView.setRepeatCount(Integer.MAX_VALUE);
                //            if (CookieUtil.getNightMode(mContext)) {
                //                headerAnimationView.setAnimation("pull_down_refresh_night.json");
                //            } else {
                //                headerAnimationView.setAnimation("pull_down_refresh.json");
                //            }
            }
            return mRefreshHeaderView!!
        }

    override fun handleScale(scale: Float, moveYDistance: Int) {}
    override fun changeToIdle() {}
    override fun changeToPullDown() {}
    override fun changeToReleaseRefresh() {
        loadingCircleView!!.startAnimation(animation)
        //        headerAnimationView.playAnimation();
    }

    override fun changeToRefreshing() {}
    override fun onEndRefreshing() {
        loadingCircleView!!.clearAnimation()
        //        headerAnimationView.pauseAnimation();
    }

    override val loadMoreFooterView: View?
        get() = super.loadMoreFooterView

    fun setLoadingMoreFooterView(footerView: View) {
        mLoadMoreFooterView = footerView
    }
}