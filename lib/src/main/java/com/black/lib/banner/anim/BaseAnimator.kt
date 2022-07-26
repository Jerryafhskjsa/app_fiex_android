package com.black.lib.banner.anim

import android.view.View
import android.view.animation.Interpolator
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.AnimatorSet
import com.nineoldandroids.view.ViewHelper

abstract class BaseAnimator {
    companion object {
        fun reset(view: View?) {
            ViewHelper.setAlpha(view, 1f)
            ViewHelper.setScaleX(view, 1f)
            ViewHelper.setScaleY(view, 1f)
            ViewHelper.setTranslationX(view, 0f)
            ViewHelper.setTranslationY(view, 0f)
            ViewHelper.setRotation(view, 0f)
            ViewHelper.setRotationY(view, 0f)
            ViewHelper.setRotationX(view, 0f)
        }
    }

    protected var duration: Long = 500
    protected var animatorSet = AnimatorSet()
    private var interpolator: Interpolator? = null
    private var delay: Long = 0
    private var listener: AnimatorListener? = null
    abstract fun setAnimation(view: View?)
    protected fun start(view: View?) {
        reset(view)
        setAnimation(view)
        animatorSet.duration = duration
        if (interpolator != null) {
            animatorSet.setInterpolator(interpolator)
        }
        if (delay > 0) {
            animatorSet.startDelay = delay
        }
        if (listener != null) {
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animator: Animator) {
                    listener?.onAnimationStart(animator)
                }

                override fun onAnimationRepeat(animator: Animator) {
                    listener?.onAnimationRepeat(animator)
                }

                override fun onAnimationEnd(animator: Animator) {
                    listener?.onAnimationEnd(animator)
                }

                override fun onAnimationCancel(animator: Animator) {
                    listener?.onAnimationCancel(animator)
                }
            })
        }
        animatorSet.start()
    }

    fun duration(duration: Long): BaseAnimator {
        this.duration = duration
        return this
    }

    fun delay(delay: Long): BaseAnimator {
        this.delay = delay
        return this
    }

    fun interpolator(interpolator: Interpolator?): BaseAnimator {
        this.interpolator = interpolator
        return this
    }

    fun listener(listener: AnimatorListener?): BaseAnimator {
        this.listener = listener
        return this
    }

    fun playOn(view: View?) {
        start(view)
    }

    interface AnimatorListener {
        fun onAnimationStart(animator: Animator)
        fun onAnimationRepeat(animator: Animator)
        fun onAnimationEnd(animator: Animator)
        fun onAnimationCancel(animator: Animator)
    }
}