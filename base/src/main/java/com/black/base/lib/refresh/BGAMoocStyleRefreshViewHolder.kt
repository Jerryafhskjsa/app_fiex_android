package com.black.base.lib.refresh

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat
import com.black.base.R

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com 创建时间:15/5/21 13:16 描述:慕课网下拉刷新风格
 */
class BGAMoocStyleRefreshViewHolder(context: Context?, isLoadingMoreEnabled: Boolean) : BGARefreshViewHolder(context!!, isLoadingMoreEnabled) {
    private var mMoocRefreshView: BGAMoocStyleRefreshView? = null
    private var mUltimateColorResId = -1
    private var mOriginalImageResId = -1
    override val refreshHeaderView: View
        get() {
            if (mRefreshHeaderView == null) {
                mRefreshHeaderView = View.inflate(mContext, R.layout.view_refresh_header_mooc_style, null)
                mRefreshHeaderView?.setBackgroundColor(Color.TRANSPARENT)
                if (mRefreshViewBackgroundColorRes != -1) {
                    mRefreshHeaderView?.setBackgroundResource(mRefreshViewBackgroundColorRes)
                }
                if (mRefreshViewBackgroundDrawableRes != -1) {
                    mRefreshHeaderView?.setBackgroundResource(mRefreshViewBackgroundDrawableRes)
                }
                mMoocRefreshView = mRefreshHeaderView?.findViewById(R.id.moocView)
                if (mOriginalImageResId != -1) {
                    mMoocRefreshView?.setOriginalImage(mOriginalImageResId)
                } else {
                    throw RuntimeException(
                            mContext.getString(R.string.error_message_01, BGAMoocStyleRefreshViewHolder::class.java.simpleName, "setOriginalImage"))
                }
                if (mUltimateColorResId != -1) {
                    mMoocRefreshView?.setUltimateColor(mUltimateColorResId)
                } else {
                    throw RuntimeException(mContext.getString(R.string.error_message_02, BGAMoocStyleRefreshViewHolder::class.java.simpleName, "setUltimateColor"))
                }
            }
            return mRefreshHeaderView!!
        }

    /**
     * 设置原始的图片资源
     *
     * @param resId
     */
    fun setOriginalImage(@DrawableRes resId: Int) {
        mOriginalImageResId = resId
    }

    /**
     * 设置最终生成图片的填充颜色资源
     *
     * @param resId
     */
    fun setUltimateColor(@ColorRes resId: Int) {
        mUltimateColorResId = resId
    }

    override fun handleScale(scale: Float, moveYDistance: Int) {
        var value = scale
        value = 0.6f + 0.4f * value
        ViewCompat.setScaleX(mMoocRefreshView, value)
        ViewCompat.setScaleY(mMoocRefreshView, value)
    }

    override fun changeToIdle() {}
    override fun changeToPullDown() {}
    override fun changeToReleaseRefresh() {}
    override fun changeToRefreshing() {
        mMoocRefreshView?.startRefreshing()
    }

    override fun onEndRefreshing() {
        mMoocRefreshView?.stopRefreshing()
    }
}