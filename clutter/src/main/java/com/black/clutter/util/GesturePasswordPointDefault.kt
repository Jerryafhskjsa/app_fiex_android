package com.black.clutter.util

import android.content.Context
import android.graphics.Canvas
import com.black.lib.gesture.PointDisplay

class GesturePasswordPointDefault(context: Context, private val color: Int, private val radius: Double) : PointDisplay(context) {
    private var hexagonPoint: HexagonPoint? = null

    override fun init(centerX: Float, centerY: Float) {
        //设置半径
        hexagonPoint = HexagonPoint(context, color, radius)
        hexagonPoint!!.init(centerX, centerY)
    }

    override fun draw(canvas: Canvas, centerX: Float, centerY: Float) {
        hexagonPoint?.draw(canvas, centerX, centerY)
    }

    override fun clone(): PointDisplay {
        return GesturePasswordPointDefault(context, color, radius)
    }

}