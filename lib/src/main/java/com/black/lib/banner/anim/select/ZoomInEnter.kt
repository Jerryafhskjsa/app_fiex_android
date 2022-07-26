package com.black.lib.banner.anim.select

import android.view.View
import com.black.lib.banner.anim.BaseAnimator
import com.nineoldandroids.animation.ObjectAnimator

class ZoomInEnter : BaseAnimator() {
    init {
        duration = 200
    }

    override fun setAnimation(view: View?) {
        animatorSet.playTogether(ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.5f),
                ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.5f))
    }
}