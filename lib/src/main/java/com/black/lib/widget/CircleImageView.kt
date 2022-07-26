package com.black.lib.widget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.DrawableRes

/**
 * 圆形图片控件
 */
class CircleImageView(context: Context?, attrs: AttributeSet?) : ImageView(context, attrs) {
    companion object {
        private val BITMAP_CONFIG = Bitmap.Config.ARGB_8888
        private const val COLORDRAWABLE_DIMENSION = 2
    }

    private var mBitmap: Bitmap? = null
    private var mBitmapShader: BitmapShader? = null
    private var mPaint: Paint? = null

    init {
        init()
    }

    override fun onDraw(canvas: Canvas) {
        if (mBitmap == null || mBitmapShader == null) {
            return
        }
        if (mBitmap?.height == 0 || mBitmap?.width == 0) return
        updateBitmapShader()
        mPaint?.shader = mBitmapShader
        canvas.drawCircle(width / 2.0f, height / 2.0f, Math.min(width / 2.0f, height / 2.0f), mPaint)
    }

    private fun init() {
        if (mBitmap == null) return
        mBitmapShader = BitmapShader(mBitmap!!, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        mPaint = Paint()
        mPaint?.isAntiAlias = true
    }

    override fun setImageBitmap(bm: Bitmap?) {
        super.setImageBitmap(bm)
        mBitmap = bm
        init()
    }

    override fun setImageDrawable(drawable: Drawable) {
        super.setImageDrawable(drawable)
        mBitmap = getBitmapFromDrawable(drawable)
        init()
    }

    override fun setImageResource(@DrawableRes resId: Int) {
        super.setImageResource(resId)
        mBitmap = getBitmapFromDrawable(drawable)
        init()
    }

    override fun setImageURI(uri: Uri) {
        super.setImageURI(uri)
        mBitmap = if (uri != null) getBitmapFromDrawable(drawable) else null
        init()
    }

    private fun getBitmapFromDrawable(drawable: Drawable?): Bitmap? {
        if (drawable == null) {
            return null
        }
        return if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else try {
            val bitmap: Bitmap = if (drawable is ColorDrawable) {
                Bitmap.createBitmap(COLORDRAWABLE_DIMENSION, COLORDRAWABLE_DIMENSION, BITMAP_CONFIG)
            } else {
                Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, BITMAP_CONFIG)
            }
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (e: Exception) {
            null
        }
    }

    private fun updateBitmapShader() {
        if (mBitmap == null) return
        val canvasSize = Math.min(width, height)
        if (canvasSize == 0) return
        if (canvasSize != mBitmap?.width || canvasSize != mBitmap?.height) {
            val matrix = Matrix()
            val scale = canvasSize.toFloat() / mBitmap?.width!!.toFloat()
            matrix.setScale(scale, scale)
            mBitmapShader?.setLocalMatrix(matrix)
        }
    }
}