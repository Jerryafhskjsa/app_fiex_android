package com.black.clutter.util

import android.content.Context
import android.graphics.Canvas
import com.black.lib.gesture.PointDisplay

class GesturePasswordCirclePointDefault(context: Context, private val color: Int, private val radius: Float) : PointDisplay(context) {
    private var circlePoint: CirclePoint? = null
    override fun init(centerX: Float, centerY: Float) {
        //设置半径
        circlePoint = CirclePoint(context, color, radius)
        circlePoint!!.init(centerX, centerY)
    }

    override fun draw(canvas: Canvas, centerX: Float, centerY: Float) {
        circlePoint?.draw(canvas, centerX, centerY)
    }

    override fun clone(): PointDisplay {
        return GesturePasswordCirclePointDefault(context, color, radius)
    }

}