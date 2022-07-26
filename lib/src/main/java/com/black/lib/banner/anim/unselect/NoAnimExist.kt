package com.black.lib.banner.anim.unselect

import android.view.View
import com.black.lib.banner.anim.BaseAnimator
import com.nineoldandroids.animation.ObjectAnimator

class NoAnimExist : BaseAnimator() {
    init {
        duration = 200
    }

    override fun setAnimation(view: View?) {
        animatorSet.playTogether(ObjectAnimator.ofFloat(view, "alpha", 1f, 1f))
    }
}