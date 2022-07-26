package com.black.lib.banner.transform

import android.view.View
import androidx.viewpager.widget.ViewPager
import com.nineoldandroids.view.ViewHelper

class FlowTransformer : ViewPager.PageTransformer {
    override fun transformPage(page: View, position: Float) {
        ViewHelper.setRotationY(page, position * -30f)
    }
}
