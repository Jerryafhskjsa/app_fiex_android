package com.black.clutter.util

import android.content.Context
import android.graphics.Canvas
import com.black.lib.gesture.PointDisplay

class GesturePasswordPointChecked(context: Context, private val centerColor: Int, private val outSideColor: Int, private val innerRadius: Double, private val outerRadius: Double) : PointDisplay(context) {
    private var centerHexagonPoint: HexagonPoint? = null
    private var outSideHexagonPoint: HexagonPoint? = null

    override fun init(centerX: Float, centerY: Float) {
        //设置半径
        centerHexagonPoint = HexagonPoint(context, centerColor, innerRadius)
        centerHexagonPoint!!.init(centerX, centerY)
        //设置半径
        outSideHexagonPoint = HexagonPoint(context, outSideColor, outerRadius)
        outSideHexagonPoint!!.init(centerX, centerY)
    }

    override fun draw(canvas: Canvas, centerX: Float, centerY: Float) {
        outSideHexagonPoint?.draw(canvas, centerX, centerY)
        centerHexagonPoint?.draw(canvas, centerX, centerY)
    }

    override fun clone(): PointDisplay {
        return GesturePasswordPointChecked(context, centerColor, outSideColor, innerRadius, outerRadius)
    }

}