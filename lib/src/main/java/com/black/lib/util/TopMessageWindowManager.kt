package com.black.lib.util

import android.R
import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

class TopMessageWindowManager {
    companion object {
        private const val ANIMATION_DURATION = 300
        private const val SHOW_DURATION = 2000
    }

    private val mWindow: PopupWindow?
    private var mContentView: View? = null
    private var mContentHeight = 0
    private var mViewDismissListener: ViewDismissListener? = null

    init {
        mWindow = PopupWindow(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT)
        mWindow.isFocusable = false
        val dw = ColorDrawable(0x00000000)
        mWindow.setBackgroundDrawable(dw)
    }

    // 显示popupWindow
    fun show(contentView: View?, viewHeight: Int) {
        if (contentView == null) {
            return
        }
        mContentHeight = viewHeight
        mContentView = contentView
        mWindow!!.contentView = mContentView
        mWindow.height = mContentHeight
        mWindow.showAtLocation(mContentView!!.rootView, Gravity.TOP, 0, offsetY)
        mWindow.setOnDismissListener {
            if (mViewDismissListener != null) {
                mViewDismissListener!!.onViewDismiss()
            }
        }
        startAnimation(mContentView, 0, mContentHeight, object : AnimationEndListener {
            override fun onEnd() {
                hideDelayed(mContentView!!, SHOW_DURATION)
            }
        })
    }

    fun show(contentView: View?) {
        if (contentView == null) {
            return
        }
        show(contentView, getActionBarHeight(contentView.context))
    }

    fun hide() {
        if (!isShow) {
            return
        }
        startAnimation(mContentView, mContentHeight, 0, object : AnimationEndListener {
            override fun onEnd() {
                dismiss()
            }
        })
    }

    private fun hideDelayed(view: View, duration: Int) {
        Handler().postDelayed({
            if (view === mContentView) {
                hide()
            }
        }, duration.toLong())
    }

    fun dismiss() {
        mWindow?.dismiss()
    }

    val isShow: Boolean
        get() = mWindow?.isShowing ?: false

    fun setViewDismissListener(listenner: ViewDismissListener?) {
        mViewDismissListener = listenner
    }

    private fun getActionBarHeight(context: Context): Int {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.textAppearanceLarge, typedValue, true)
        val attribute = intArrayOf(R.attr.actionBarSize)
        val array = context.obtainStyledAttributes(typedValue.resourceId, attribute)
        val height = array.getDimensionPixelSize(0 /* index */, -1 /* default size */)
        array.recycle()
        return height
    }

    private fun startAnimation(view: View?, start: Int, end: Int, listenner: AnimationEndListener?) {
        if (view == null) {
            return
        }
        val animator = ValueAnimator.ofInt(start, end)
        animator.interpolator = FastOutSlowInInterpolator()
        animator.duration = ANIMATION_DURATION.toLong()
        animator.addUpdateListener { animation -> setViewHight(view, animation.animatedValue as Int) }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) { // do nothing
            }

            override fun onAnimationEnd(animation: Animator) {
                listenner?.onEnd()
            }

            override fun onAnimationCancel(animation: Animator) { // do nothing
            }

            override fun onAnimationRepeat(animation: Animator) { // do nothing
            }
        })
        animator.start()
    }

    private fun setViewHight(view: View?, height: Int) {
        if (view != null) {
            val params = view.layoutParams
            if (params != null) {
                params.height = height
                view.layoutParams = params
            }
        }
    }

    private val offsetY: Int
        get() {
            var result = 0
            if (mContentView != null) {
                val resourceId = mContentView!!.context.resources.getIdentifier("status_bar_height", "dimen", "android")
                if (resourceId > 0) {
                    result = mContentView!!.context.resources.getDimensionPixelSize(resourceId)
                }
            }
            return result
        }

    internal interface AnimationEndListener {
        fun onEnd()
    }

    interface ViewDismissListener {
        fun onViewDismiss()
    }
}