package com.black.lib.banner

import android.view.View
import androidx.viewpager.widget.ViewPager

class ZoomPageTransformer : ViewPager.PageTransformer {
    companion object {
        private const val MAX_SCALE = 1.0f
        private const val MIN_SCALE = 0.85f //0.85f
        private const val MIN_ALPHA = 0.3f
        private const val TAG = "PageTransformer"
    }

    override fun transformPage(view: View, position: Float) {
        //setScaleY只支持api11以上
        when {
            position < -1 -> {
                view.scaleX = MIN_SCALE
                view.scaleY = MIN_SCALE
                view.alpha = MIN_ALPHA //左边的左边的Page
            }
            position <= 1 -> {
                val scaleFactor = MIN_SCALE + (1 - Math.abs(position)) * (MAX_SCALE - MIN_SCALE)
                if (position > 0) {
                    view.translationX = -scaleFactor
                } else if (position < 0) {
                    view.translationX = scaleFactor
                }
                view.scaleY = scaleFactor
                view.scaleX = scaleFactor
                // float alpha = 1f -  Math.abs(position) * (1 - );
                val alpha = MIN_ALPHA + (1 - MIN_ALPHA) * (1 - Math.abs(position))
                view.alpha = alpha
            }
            else -> {
                // (1,+Infinity]
                view.scaleX = MIN_SCALE
                view.scaleY = MIN_SCALE
                view.alpha = MIN_ALPHA
            }
        }
    }
}