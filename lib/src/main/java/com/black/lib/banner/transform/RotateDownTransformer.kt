package com.black.lib.banner.transform

import android.view.View
import androidx.viewpager.widget.ViewPager
import com.nineoldandroids.view.ViewHelper

class RotateDownTransformer : ViewPager.PageTransformer {
    companion object {
        private const val ROT_MOD = -15f
    }

    override fun transformPage(page: View, position: Float) {
        val width = page.width.toFloat()
        val height = page.height.toFloat()
        val rotation = ROT_MOD * position * -1.25f
        ViewHelper.setPivotX(page, width * 0.5f)
        ViewHelper.setPivotY(page, height)
        ViewHelper.setRotation(page, rotation)
    }
}