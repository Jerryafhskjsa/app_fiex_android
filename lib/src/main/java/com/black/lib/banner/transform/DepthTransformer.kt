package com.black.lib.banner.transform

import android.view.View
import androidx.viewpager.widget.ViewPager
import com.nineoldandroids.view.ViewHelper

class DepthTransformer : ViewPager.PageTransformer {
    companion object {
        private const val MIN_SCALE_DEPTH = 0.75f
    }

    override fun transformPage(page: View, position: Float) {
        val alpha: Float
        val scale: Float
        val translationX: Float
        if (position > 0 && position < 1) { // moving to the right
            alpha = 1 - position
            scale = MIN_SCALE_DEPTH + (1 - MIN_SCALE_DEPTH) * (1 - Math.abs(position))
            translationX = page.width * -position
        } else { // use default for all other cases
            alpha = 1f
            scale = 1f
            translationX = 0f
        }
        ViewHelper.setAlpha(page, alpha)
        ViewHelper.setTranslationX(page, translationX)
        ViewHelper.setScaleX(page, scale)
        ViewHelper.setScaleY(page, scale)
    }
}