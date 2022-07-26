package com.black.clutter.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.black.lib.gesture.PointDisplay

//六边形
class HexagonPoint internal constructor(context: Context, private val color: Int, private val radius: Double) : PointDisplay(context) {
    private val paint: Paint = Paint()
    private var polygon: Polygon? = null

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = color
    }

    override fun init(centerX: Float, centerY: Float) {
        polygon = Polygon(centerX, centerY, radius, 6)
        polygon?.initGraphics()
    }

    override fun draw(canvas: Canvas, centerX: Float, centerY: Float) {
        if (polygon?.atPosition(centerX, centerY) != true) {
            polygon?.initGraphics()
        }
        canvas.save()
        val path = polygon!!.path
        if (path != null) {
            canvas.drawPath(path, paint)
        }
        canvas.restore()
    }

    override fun clone(): PointDisplay {
        return HexagonPoint(context, color, radius)
    }
}