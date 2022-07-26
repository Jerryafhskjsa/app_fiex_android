package com.black.lib.gesture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint

internal class GesturePasswordPointDefault(context: Context, private val color: Int) : PointDisplay(context) {
    private val radius: Double
    protected var paint: Paint
    private var centerX = 0f
    private var centerY = 0f

    init {
        val metrics = context.resources.displayMetrics
        radius = metrics.density * 20.toDouble()
        paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        paint.color = color
    }

    override fun init(centerX: Float, centerY: Float) {
        this.centerX = centerX
        this.centerY = centerY
    }

    override fun draw(canvas: Canvas, centerX: Float, centerY: Float) {
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, radius.toFloat(), paint)
    }

    public override fun clone(): PointDisplay {
        return GesturePasswordPointDefault(context, color)
    }
}