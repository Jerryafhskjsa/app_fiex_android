package com.black.lib.banner.anim.select

import android.view.View
import com.black.lib.banner.anim.BaseAnimator
import com.nineoldandroids.animation.ObjectAnimator

class RotateEnter : BaseAnimator() {
    init {
        duration = 200
    }

    override fun setAnimation(view: View?) {
        animatorSet.playTogether(ObjectAnimator.ofFloat(
                view, "rotation", 0f, 180f))
    }
}