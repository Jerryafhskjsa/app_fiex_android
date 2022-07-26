package com.black.lib.view

import android.graphics.*
import android.graphics.drawable.Drawable

class ProgressDrawable @JvmOverloads constructor(private var progressColor: Int, private var bgColor: Int, direction: Int = LEFT) : Drawable() {
    companion object {
        private const val MAX = 10000
        const val LEFT = 0
        const val RIGHT = 1
        private val DIRECTION = intArrayOf(LEFT, RIGHT)
    }

    private val mPaint: Paint
    private val mColor = 0
    private val mBorderWidth = 0
    private val mBorderRadius = 0
    private val mRect: RectF? = null
    private val mPath: Path? = null
    private var progress = 0.0
    private var direction = LEFT

    init {
        this.direction = direction
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint.style = Paint.Style.FILL
    }

    override fun draw(canvas: Canvas) {
        if (canvas == null) {
            return
        }
        val rect = bounds
        //先绘制背景
        mPaint.color = bgColor
        canvas.drawRect(rect, mPaint)
        //绘制进度条
        mPaint.color = progressColor
        if (RIGHT == direction) {
            val progressRect = Rect((rect.left + (rect.right - rect.left) * (1 - progress)).toInt(), rect.top, rect.right, rect.bottom)
            canvas.drawRect(progressRect, mPaint)
        } else {
            val progressRect = Rect(rect.left, rect.top, (rect.left + (rect.right - rect.left) * progress).toInt(), rect.bottom)
            canvas.drawRect(progressRect, mPaint)
        }
    }

    override fun setAlpha(alpha: Int) {}
    override fun setColorFilter(colorFilter: ColorFilter) {}
    override fun getOpacity(): Int {
        return PixelFormat.OPAQUE
    }

    fun setProgress(progress: Double) {
        this.progress = if (progress < 0) 0.toDouble() else if (progress > 1) 1.0 else progress
        invalidateSelf()
    }

    @Deprecated("")
    fun setProgress(progress: Int) {
        this.progress = if (progress < 0) 0.0 else if (progress > MAX) 1.0 else progress.toDouble() / MAX
        invalidateSelf()
    }

    fun setColor(progressColor: Int, bgColor: Int) {
        this.progressColor = progressColor
        this.bgColor = bgColor
        invalidateSelf()
    }
}