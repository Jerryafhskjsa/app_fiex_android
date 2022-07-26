package com.black.clutter.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.black.lib.gesture.PointDisplay

//圆形
class CirclePoint internal constructor(context: Context, private val color: Int, private val radius: Float) : PointDisplay(context) {
    private val paint: Paint = Paint()

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = color
    }

    override fun init(centerX: Float, centerY: Float) {}
    override fun draw(canvas: Canvas, centerX: Float, centerY: Float) {
        canvas.save()
        if (radius > 0) {
            canvas.drawCircle(centerX, centerY, radius, paint)
        }
        canvas.restore()
    }

    override fun clone(): PointDisplay {
        return CirclePoint(context, color, radius)
    }
}