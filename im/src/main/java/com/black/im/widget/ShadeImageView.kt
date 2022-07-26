package com.black.im.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import com.black.im.util.ScreenUtil.getPxByDp

@SuppressLint("AppCompatCustomView")
open class ShadeImageView : ImageView {
    companion object {
        private val sRoundBitmapArray: SparseArray<Bitmap?> = SparseArray()
    }

    private val mShadePaint = Paint()
    private var mRoundBitmap: Bitmap? = null
    var radius = getPxByDp(5)

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        setLayerType(View.LAYER_TYPE_HARDWARE, null)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mShadePaint.color = Color.RED
        mShadePaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
        mRoundBitmap = sRoundBitmapArray[measuredWidth + radius]
        if (mRoundBitmap == null) {
            mRoundBitmap = roundBitmap
            sRoundBitmapArray.put(measuredWidth + radius, mRoundBitmap)
        }
        canvas.drawBitmap(mRoundBitmap!!, 0f, 0f, mShadePaint)
    }

    /**
     * 获取圆角矩形图片方法
     *
     * @return Bitmap
     */
    private val roundBitmap: Bitmap
        get() {
            val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(output)
            val color = Color.parseColor("#cfd3d8")
            val rect = Rect(0, 0, measuredWidth, measuredHeight)
            val rectF = RectF(rect)
            val paint = Paint()
            paint.isAntiAlias = true
            canvas.drawARGB(0, 0, 0, 0)
            paint.color = color
            canvas.drawRoundRect(rectF, radius.toFloat(), radius.toFloat(), paint)
            return output
        }
}