package com.black.base.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import com.black.base.R
import com.black.base.util.ConstData
import skin.support.content.res.SkinCompatResources

class SideBar : View {
    companion object {
        private val sSideBarTitle = ConstData.SIDE_BAR_TITLE
    }

    private var mChoose = -1
    private var mPaint: Paint = Paint()
    private var mTvDialog: TextView? = null
    private var mListener: OnTouchLetterChangeListener? = null
    private var mHeight = 0
    private var mWidth = 0
    private var mSingleTextHeight = 0

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    init {
        setPaint()
    }

    private fun setPaint() {
        mPaint.reset()
        mPaint.color = Color.WHITE
        mPaint.isAntiAlias = true
        mPaint.typeface = Typeface.DEFAULT_BOLD
        mPaint.textSize = 30f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mHeight = measuredHeight
        mWidth = measuredWidth
        mSingleTextHeight = mHeight / sSideBarTitle.size
    }

    override fun onDraw(canvas: Canvas) {
        for (i in sSideBarTitle.indices) {
            if (i == mChoose) {
                mPaint.color = SkinCompatResources.getColor(context, R.color.black)
                mPaint.isFakeBoldText = true
            } else {
                mPaint.color = SkinCompatResources.getColor(context, R.color.grey)
            }
            val xPos = mWidth / 2 - mPaint.measureText(sSideBarTitle[i]) / 2
            val yPos = mSingleTextHeight * i + mSingleTextHeight.toFloat()
            canvas.drawText(sSideBarTitle[i], xPos, yPos, mPaint)
            setPaint()
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val action = event.action
        val y = event.y
        val lastChoose = mChoose
        val scale = (y / height * sSideBarTitle.size).toInt()
        when (action) {
            MotionEvent.ACTION_UP -> {
                mChoose = -1
                if (mTvDialog != null) {
                    mTvDialog?.visibility = INVISIBLE
                }
            }
            else -> if (lastChoose != scale) {
                if (scale >= 0 && scale < sSideBarTitle.size) {
                    if (mListener != null) {
                        mListener?.letterChange(sSideBarTitle[scale])
                    }
                    if (mTvDialog != null) {
                        mTvDialog?.text = sSideBarTitle[scale]
                        mTvDialog?.visibility = VISIBLE
                    }
                    mChoose = scale
                }
            }
        }
        invalidate()
        return true
    }

    fun setOnTouchLetterChangeListener(onTouchLetterChangeListener: OnTouchLetterChangeListener?) {
        mListener = onTouchLetterChangeListener
    }

    interface OnTouchLetterChangeListener {
        fun letterChange(s: String?)
    }

    fun setTvDialog(tvDialog: TextView?) {
        mTvDialog = tvDialog
    }
}