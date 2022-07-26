package com.black.clutter.util

import android.content.Context
import android.graphics.Canvas
import com.black.lib.gesture.PointDisplay

class GesturePasswordCirclePointChecked(context: Context, private val centerColor: Int, private val outSideColor: Int, private val innerRadius: Float, private val outerRadius: Float, private val outerBorder: Float) : PointDisplay(context) {
    private var centerCirclePoint: CirclePoint? = null
    private var outSideCirclePoint: CircleBorderPoint? = null
    override fun init(centerX: Float, centerY: Float) {
        //设置半径
        centerCirclePoint = CirclePoint(context, centerColor, innerRadius)
        centerCirclePoint!!.init(centerX, centerY)
        //设置半径
        outSideCirclePoint = CircleBorderPoint(context, outSideColor, outerRadius, outerBorder)
        outSideCirclePoint!!.init(centerX, centerY)
    }

    override fun draw(canvas: Canvas, centerX: Float, centerY: Float) {
        outSideCirclePoint?.draw(canvas, centerX, centerY)
        centerCirclePoint?.draw(canvas, centerX, centerY)
    }

    override fun clone(): PointDisplay {
        return GesturePasswordCirclePointChecked(context, centerColor, outSideColor, innerRadius, outerRadius, outerBorder)
    }

}