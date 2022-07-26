package com.black.lib.gesture

import android.content.Context
import android.graphics.Canvas


abstract class PointDisplay(protected var context: Context) : Cloneable {
    abstract fun init(centerX: Float, centerY: Float)
    abstract fun draw(canvas: Canvas, centerX: Float, centerY: Float)
    public abstract override fun clone(): PointDisplay
}
