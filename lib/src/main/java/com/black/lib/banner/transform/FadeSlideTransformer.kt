package com.black.lib.banner.transform

import android.view.View
import androidx.viewpager.widget.ViewPager
import com.nineoldandroids.view.ViewHelper

class FadeSlideTransformer : ViewPager.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        ViewHelper.setTranslationX(page, 0f)
        if (position <= -1.0f || position >= 1.0f) {
            ViewHelper.setAlpha(page, 0.0f)
        } else if (position == 0.0f) {
            ViewHelper.setAlpha(page, 1.0f)
        } else { // position is between -1.0F & 0.0F OR 0.0F & 1.0F
            ViewHelper.setAlpha(page, 1.0f - Math.abs(position))
        }
    }
}