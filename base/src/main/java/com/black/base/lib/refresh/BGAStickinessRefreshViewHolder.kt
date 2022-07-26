package com.black.base.lib.refresh

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.black.base.R

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com 创建时间:15/5/21 13:16
 * 描述:黏性下拉刷新风格、类似手机消息列表下拉刷新风格
 */
class BGAStickinessRefreshViewHolder(context: Context?, isLoadingMoreEnabled: Boolean) : BGARefreshViewHolder(context!!, isLoadingMoreEnabled) {
    private var mStickinessRefreshView: BGAStickinessRefreshView? = null
    private var mRotateImageResId = -1
    private var mStickinessColorResId = -1

    override val refreshHeaderView: View
        get() {
            if (mRefreshHeaderView == null) {
                mRefreshHeaderView = View.inflate(mContext, R.layout.view_refresh_header_stickiness, null)
                mRefreshHeaderView?.setBackgroundColor(Color.TRANSPARENT)
                if (mRefreshViewBackgroundColorRes != -1) {
                    mRefreshHeaderView?.setBackgroundResource(mRefreshViewBackgroundColorRes)
                }
                if (mRefreshViewBackgroundDrawableRes != -1) {
                    mRefreshHeaderView?.setBackgroundResource(mRefreshViewBackgroundDrawableRes)
                }
                mStickinessRefreshView = mRefreshHeaderView?.findViewById(R.id.stickinessRefreshView)
                mStickinessRefreshView?.setStickinessRefreshViewHolder(this)
                if (mRotateImageResId != -1) {
                    mStickinessRefreshView?.setRotateImage(mRotateImageResId)
                } else {
                    throw RuntimeException(
                            mContext.getString(R.string.error_message_04, BGAStickinessRefreshViewHolder::class.java.simpleName, "setRotateImage"))
                }
                if (mStickinessColorResId != -1) {
                    mStickinessRefreshView?.setStickinessColor(mStickinessColorResId)
                } else {
                    throw RuntimeException(
                            mContext.getString(R.string.error_message_05, BGAStickinessRefreshViewHolder::class.java.simpleName, "setStickinessColor"))
                }
            }
            return mRefreshHeaderView!!
        }

    /**
     * 设置旋转图片资源
     *
     * @param resId
     */
    fun setRotateImage(@DrawableRes resId: Int) {
        mRotateImageResId = resId
    }

    /**
     * 设置黏性颜色资源
     *
     * @param resId
     */
    fun setStickinessColor(@ColorRes resId: Int) {
        mStickinessColorResId = resId
    }

    override fun handleScale(scale: Float, moveYDistance: Int) {
        mStickinessRefreshView?.setMoveYDistance(moveYDistance)
    }

    override fun changeToIdle() {
        mStickinessRefreshView?.smoothToIdle()
    }

    override fun changeToPullDown() {}
    override fun changeToReleaseRefresh() {}
    override fun changeToRefreshing() {
        mStickinessRefreshView?.startRefreshing()
    }

    override fun onEndRefreshing() {
        mStickinessRefreshView?.stopRefresh()
    }

    override fun canChangeToRefreshingStatus(): Boolean {
        return mStickinessRefreshView?.canChangeToRefreshing() ?: false
    }
}