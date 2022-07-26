package com.black.lib.banner.transform

import android.view.View
import androidx.viewpager.widget.ViewPager
import com.nineoldandroids.view.ViewHelper

class RotateUpTransformer : ViewPager.PageTransformer {
    companion object {
        private const val ROT_MOD = -15f
    }

    override fun transformPage(page: View, position: Float) {
        val width = page.width.toFloat()
        val rotation = ROT_MOD * position
        ViewHelper.setPivotX(page, width * 0.5f)
        ViewHelper.setPivotY(page, 0f)
        ViewHelper.setTranslationX(page, 0f)
        ViewHelper.setRotation(page, rotation)
    }
}