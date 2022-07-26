package com.black.base.lib.refresh

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import skin.support.content.res.SkinCompatResources

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com 创建时间:15/5/21 10:43 描述:慕课网下拉刷新风格控件
 */
class BGAMoocStyleRefreshView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {
    private var mXfermode: PorterDuffXfermode? = null
    /**
     * 用来画临时图像的画笔
     */
    private var mPaint: Paint? = null
    /**
     * 用来画临时图像的画布
     */
    private var mCanvas: Canvas? = null
    /**
     * 原始的图片
     */
    private var mOriginalBitmap: Bitmap? = null
    /**
     * 原始的图片宽度
     */
    private var mOriginalBitmapWidth = 0
    /**
     * 原始的图片高度
     */
    private var mOriginalBitmapHeight = 0
    /**
     * 最终生成的图片
     */
    private var mUltimateBitmap: Bitmap? = null
    /**
     * 贝塞尔曲线路径
     */
    private var mBezierPath: Path? = null
    /**
     * 贝塞尔曲线控制点x
     */
    private var mBezierControlX = 0f
    /**
     * 贝塞尔曲线控制点y
     */
    private var mBezierControlY = 0f
    /**
     * 贝塞尔曲线原始的控制点y
     */
    private var mBezierControlOriginalY = 0f
    /**
     * 当前波纹的y值
     */
    private var mWaveY = 0f
    /**
     * 波纹原始的y值
     */
    private var mWaveOriginalY = 0f
    /**
     * 贝塞尔曲线控制点x是否增加
     */
    private var mIsBezierControlXIncrease = false
    /**
     * 是否正在刷新
     */
    private var mIsRefreshing = false

    init {
        initPaint()
    }

    private fun initPaint() {
        mPaint = Paint()
        mPaint?.isAntiAlias = true
        mPaint?.isDither = true
        mPaint?.style = Paint.Style.FILL
    }

    private fun initCanvas() {
        mOriginalBitmapWidth = mOriginalBitmap?.width ?: 0
        mOriginalBitmapHeight = mOriginalBitmap?.height ?: 0
        // 初始状态值
        mWaveOriginalY = mOriginalBitmapHeight.toFloat()
        mWaveY = 1.2f * mWaveOriginalY
        mBezierControlOriginalY = 1.25f * mWaveOriginalY
        mBezierControlY = mBezierControlOriginalY
        mXfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        mBezierPath = Path()
        mCanvas = Canvas()
        mUltimateBitmap = Bitmap.createBitmap(mOriginalBitmapWidth, mOriginalBitmapHeight, Bitmap.Config.ARGB_8888)
        mCanvas?.setBitmap(mUltimateBitmap)
    }

    /**
     * 设置最终生成图片的填充颜色资源
     *
     * @param resId
     */
    fun setUltimateColor(@ColorRes resId: Int) {
        mPaint?.color = SkinCompatResources.getColor(context, resId)
    }

    /**
     * 设置原始图片资源
     *
     * @param resId
     */
    fun setOriginalImage(@DrawableRes resId: Int) {
        mOriginalBitmap = BitmapFactory.decodeResource(resources, resId)
        initCanvas()
    }

    override fun onDraw(canvas: Canvas) {
        if (mUltimateBitmap == null) {
            return
        }
        drawUltimateBitmap()
        // 将目标图绘制在当前画布上，起点为左边距，上边距的交点
        canvas.drawBitmap(mUltimateBitmap, paddingLeft.toFloat(), paddingTop.toFloat(), null)
        if (mIsRefreshing) {
            invalidate()
        }
    }

    /**
     * 绘制最终的图片
     */
    private fun drawUltimateBitmap() {
        mBezierPath?.reset()
        mUltimateBitmap?.eraseColor(Color.parseColor("#00ffffff"))
        if (mBezierControlX >= mOriginalBitmapWidth + 1.0f / 2 * mOriginalBitmapWidth) {
            mIsBezierControlXIncrease = false
        } else if (mBezierControlX <= -1.0f / 2 * mOriginalBitmapWidth) {
            mIsBezierControlXIncrease = true
        }
        mBezierControlX = if (mIsBezierControlXIncrease) mBezierControlX + 10 else mBezierControlX - 10
        if (mBezierControlY >= 0) {
            mBezierControlY -= 2f
            mWaveY -= 2f
        } else {
            mWaveY = mWaveOriginalY
            mBezierControlY = mBezierControlOriginalY
        }
        mBezierPath?.moveTo(0f, mWaveY)
        mBezierPath?.cubicTo(mBezierControlX / 2, mWaveY - (mBezierControlY - mWaveY),
                (mBezierControlX + mOriginalBitmapWidth) / 2, mBezierControlY, mOriginalBitmapWidth.toFloat(), mWaveY)
        mBezierPath?.lineTo(mOriginalBitmapWidth.toFloat(), mOriginalBitmapHeight.toFloat())
        mBezierPath?.lineTo(0f, mOriginalBitmapHeight.toFloat())
        mBezierPath?.close()
        mCanvas?.drawBitmap(mOriginalBitmap, 0f, 0f, mPaint)
        mPaint?.xfermode = mXfermode
        mCanvas?.drawPath(mBezierPath, mPaint)
        mPaint?.xfermode = null
    }

    fun startRefreshing() {
        mIsRefreshing = true
        reset()
    }

    fun stopRefreshing() {
        mIsRefreshing = false
        reset()
    }

    private fun reset() {
        mWaveY = mWaveOriginalY
        mBezierControlY = mBezierControlOriginalY
        mBezierControlX = 0f
        postInvalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        var width: Int
        var height: Int
        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize + paddingLeft + paddingRight
        } else {
            width = mOriginalBitmapWidth + paddingLeft + paddingRight
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(width, widthSize)
            }
        }
        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize + paddingTop + paddingBottom
        } else {
            height = mOriginalBitmapHeight + paddingTop + paddingBottom
            if (heightMode == MeasureSpec.AT_MOST) {
                height = Math.min(height, heightSize)
            }
        }
        setMeasuredDimension(width, height)
    }
}