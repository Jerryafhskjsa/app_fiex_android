package com.black.lib.banner

import android.view.View
import androidx.viewpager.widget.ViewPager
import me.crosswall.lib.coverflow.core.Utils

class BannerTransformer(scaleX: Float, scaleY: Float) : ViewPager.PageTransformer {
    companion object {
        const val TAG = "CoverTransformer"
        const val SCALE_MIN = 0.3f
        const val SCALE_MAX = 1.0f
        const val MARGIN_MIN = 0.0f
        const val MARGIN_MAX = 50.0f
    }

    var scaleX = 0.0f
    var scaleY = 0.0f
    private var pagerMargin = 0.0f
    private var spaceValue = 0.0f
    private val rotationX = 0.0f
    private var rotationY = 0.0f
    init {
        pagerMargin = pagerMargin
        spaceValue = spaceValue
        rotationY = rotationY
        this.scaleX = scaleX
        this.scaleY = scaleY
    }
    override fun transformPage(page: View, position: Float) {
        var realPagerMargin: Float
        if (scaleX != 0.0f) {
            realPagerMargin = Utils.getFloat(1.0f - Math.abs(position * scaleX), 0.3f, 1.0f)
            page.scaleX = realPagerMargin
        }
        if (scaleY != 0.0f) {
            realPagerMargin = Utils.getFloat(1.0f - Math.abs(position * scaleY), 0.3f, 1.0f)
            page.scaleY = realPagerMargin
        }
        if (pagerMargin != 0.0f) {
            realPagerMargin = position * pagerMargin
            if (spaceValue != 0.0f) {
                val realSpaceValue = Utils.getFloat(Math.abs(position * spaceValue), 0.0f, 50.0f)
                realPagerMargin += if (position > 0.0f) realSpaceValue else -realSpaceValue
            }
            page.translationX = realPagerMargin
        }
    }
}