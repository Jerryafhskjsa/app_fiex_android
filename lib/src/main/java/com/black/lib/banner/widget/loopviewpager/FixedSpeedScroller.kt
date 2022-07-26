package com.black.lib.banner.widget.loopviewpager

import android.content.Context
import android.view.animation.Interpolator
import android.widget.Scroller

class FixedSpeedScroller : Scroller {
    private var scrollSpeed = 450

    constructor(context: Context?) : super(context) {}
    constructor(context: Context?, interpolator: Interpolator?, scrollSpeed: Int) : super(context, interpolator) {
        this.scrollSpeed = scrollSpeed
    }

    override fun startScroll(startX: Int, startY: Int, dx: Int, dy: Int, duration: Int) {
        super.startScroll(startX, startY, dx, dy, scrollSpeed)
    }
}
