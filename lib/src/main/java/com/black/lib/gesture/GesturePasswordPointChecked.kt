package com.black.lib.gesture

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint

internal class GesturePasswordPointChecked(context: Context, private val centerColor: Int, private val outSideColor: Int) : PointDisplay(context) {
    private val innerRadius: Double
    private val outerRadius: Double
    private var centerX = 0f
    private var centerY = 0f
    protected var paint: Paint = Paint()

    init {
        paint.isAntiAlias = true
        paint.style = Paint.Style.FILL
        val metrics = context.resources.displayMetrics
        innerRadius = metrics.density * 20.toDouble()
        outerRadius = innerRadius * 2.4f
    }

    override fun init(centerX: Float, centerY: Float) {
        this.centerX = centerX
        this.centerY = centerY
    }

    override fun draw(canvas: Canvas, centerX: Float, centerY: Float) {
        paint.style = Paint.Style.STROKE
        canvas.drawCircle(centerX, centerY, outerRadius.toFloat(), paint)
        paint.style = Paint.Style.FILL
        canvas.drawCircle(centerX, centerY, innerRadius.toFloat(), paint)
    }

    public override fun clone(): PointDisplay {
        return GesturePasswordPointChecked(context, centerColor, outSideColor)
    }
}