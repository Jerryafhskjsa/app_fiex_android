package com.black.im.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import com.black.im.R
import com.black.im.util.ScreenUtil.getPxByDp

class UnreadCountTextView : AppCompatTextView {
    private val mNormalSize = getPxByDp(16)
    private var mPaint: Paint? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        mPaint = Paint()
        mPaint!!.color = resources.getColor(R.color.read_dot_bg)
        setTextColor(Color.WHITE)
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 10f)
    }

    override fun onDraw(canvas: Canvas) {
        if (text.isEmpty()) {
            // 没有字符，就在本View中心画一个小圆点
            val l = (measuredWidth - getPxByDp(7)) / 2
            val r = measuredWidth - l
            canvas.drawOval(RectF(l.toFloat(), l.toFloat(), r.toFloat(), r.toFloat()), mPaint)
        } else if (text.length == 1) {
            canvas.drawOval(RectF(0.toFloat(), 0.toFloat(), mNormalSize.toFloat(), mNormalSize.toFloat()), mPaint)
        } else if (text.length > 1) {
            canvas.drawRoundRect(RectF(0.toFloat(), 0.toFloat(), measuredWidth.toFloat(), measuredHeight.toFloat()), measuredHeight / 2.toFloat(), measuredHeight / 2.toFloat(), mPaint)
        }
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var width = mNormalSize
        val height = mNormalSize
        if (text.length > 1) {
            width = mNormalSize + getPxByDp((text.length - 1) * 10)
        }
        setMeasuredDimension(width, height)
    }
}