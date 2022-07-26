package com.black.im.video

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.View

class ReturnButton(context: Context?) : View(context) {
    var path: Path? = null
    private var size = 0
    private var center_X = 0
    private var center_Y = 0
    private var strokeWidth = 0f
    private var paint: Paint? = null

    constructor(context: Context?, size: Int) : this(context) {
        this.size = size
        center_X = size / 2
        center_Y = size / 2
        strokeWidth = size / 15f
        paint = Paint()
        paint!!.isAntiAlias = true
        paint!!.color = Color.WHITE
        paint!!.style = Paint.Style.STROKE
        paint!!.strokeWidth = strokeWidth
        path = Path()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        setMeasuredDimension(size, size / 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        path!!.moveTo(strokeWidth, strokeWidth / 2)
        path!!.lineTo(center_X.toFloat(), center_Y - strokeWidth / 2)
        path!!.lineTo(size - strokeWidth, strokeWidth / 2)
        canvas.drawPath(path!!, paint!!)
    }
}