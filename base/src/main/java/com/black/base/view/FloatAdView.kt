package com.black.base.view

import android.animation.ValueAnimator
import android.app.Activity
import android.graphics.Bitmap
import android.util.DisplayMetrics
import android.view.*
import android.view.View.OnTouchListener
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.black.base.R

class FloatAdView(private val activity: Activity, private var width: Int, private var height: Int) {
    private val dm: DisplayMetrics
    private var contentView: View? = null
    private var positionX //显示位置
            = 0
    private var positionY //显示位置
            = 0
    private var lottieAnimationView: LottieAnimationView? = null
    private var parent: View? = null

    init {
        dm = activity.resources.displayMetrics
        init()
    }

    private fun init() {
        val inflater = LayoutInflater.from(activity)
        contentView = inflater.inflate(R.layout.view_float_ad, null)
        lottieAnimationView = contentView?.findViewById(R.id.animation_view)
        lottieAnimationView?.setRepeatCount(ValueAnimator.INFINITE)
    }

    fun setLottieData(dataString: String?) {
        lottieAnimationView?.setAnimationFromJson(dataString, null)
    }

    fun setLottieBitmap(Bitmap: Bitmap?) {
        lottieAnimationView?.setImageBitmap(Bitmap)
    }

    fun show(parent: FrameLayout?) {
        if (parent == null) {
            return
        }
        this.parent = parent
        lottieAnimationView?.playAnimation()
        parent.addView(contentView, FrameLayout.LayoutParams(
                if (width != 0) width else FrameLayout.LayoutParams.WRAP_CONTENT,
                if (height != 0) height else FrameLayout.LayoutParams.WRAP_CONTENT))
        contentView?.setOnTouchListener(object : OnTouchListener {
            private var downX = 0f
            private var downY = 0f
            private var mActivePointerId = 0
            private var isDrag = false
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        mActivePointerId = event.getPointerId(0)
                        isDrag = false
                        downX = event.x
                        downY = event.y
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val xDistance = event.x - downX
                        val yDistance = event.y - downY
                        val screenWidth: Int = getParentContentWidth()
                        val screenHeight: Int = getParentContentHeight()
                        val width = contentView?.width ?: 0
                        val height = contentView?.height ?: 0
                        var l: Int
                        var r: Int
                        var t: Int
                        var b: Int
                        //当水平或者垂直滑动距离大于10,才算拖动事件
                        if (Math.abs(xDistance) > 10 || Math.abs(yDistance) > 10) {
                            isDrag = true
                            l = ((contentView?.left ?: 0) + xDistance).toInt()
                            r = l + width
                            t = ((contentView?.top ?: 0) + yDistance).toInt()
                            b = t + height
                            //不划出边界判断,此处应按照项目实际情况,因为本项目需求移动的位置是手机全屏,
// 所以才能这么写,如果是固定区域,要得到父控件的宽高位置后再做处理
                            if (l < 0) {
                                l = 0
                                r = l + width
                            } else if (r > screenWidth) {
                                r = screenWidth
                                l = r - width
                            }
                            if (t < 0) {
                                t = 0
                                b = t + height
                            } else if (b > screenHeight) {
                                b = screenHeight
                                t = b - height
                            }
                            positionX = l
                            positionY = t
                            setPosition()
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        val clickTime = event.eventTime - event.downTime
                        val activePointerIndex = event.findPointerIndex(mActivePointerId)
                        val x = event.getX(activePointerIndex)
                        val deltaX = downX - x
                        val y = event.getY(activePointerIndex)
                        val deltaY = downY - y
                        val moveDistance = Math.sqrt(deltaX * deltaX + deltaY * deltaY.toDouble())
                        if (clickTime <= 200 && moveDistance < 50 * dm.density) {
                            contentView?.performClick()
                        }
                        //计算合适的位置停放,吸边
                        resetPosition()
                    }
                }
                return true
            }
        })
        showDefaultPosition()
    }

    private fun showDefaultPosition() {
        contentView?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val width = getParentContentWidth()
                val height = getParentContentHeight()
                val contentWidth = contentView?.width ?: 0
                val contentHeight = contentView?.height ?: 0
                this@FloatAdView.width = contentWidth
                this@FloatAdView.height = contentHeight
                positionX = width - contentWidth
                positionY = height - contentHeight - (dm.density * 15).toInt()
                setPosition()
                contentView?.viewTreeObserver?.removeOnGlobalLayoutListener(this);
            }

        })
    }

    private fun resetPosition() {
        val width = getParentContentWidth()
        val height = getParentContentHeight()
        val contentWidth = contentView?.width ?: 0
        val contentHeight = contentView?.height ?: 0
        val leftScale = positionX.toFloat() / width
        val rightScale = (width - contentWidth - positionX).toFloat() / width
        val topScale = positionY.toFloat() / height
        val bottomScale = (height - contentHeight - positionY).toFloat() / height
        val minScale = Math.min(Math.min(leftScale, rightScale), Math.min(topScale, bottomScale))
        if (bottomScale == minScale) {
            positionY = height - contentHeight
        } else if (rightScale == minScale) {
            positionX = width - contentWidth
        } else if (topScale == minScale) {
            positionY = 0
        } else if (leftScale == minScale) {
            positionX = 0
        }
        setPosition()
    }

    private fun setPosition() {
        val contentWidth = contentView?.width ?: 0
        val contentHeight = contentView?.height ?: 0
        contentView?.layout(positionX, positionY, positionX + contentWidth, positionY + contentHeight)
        var params = contentView?.layoutParams as FrameLayout.LayoutParams?
        if (params == null) {
            params = FrameLayout.LayoutParams(
                    if (width != 0) width else FrameLayout.LayoutParams.WRAP_CONTENT,
                    if (height != 0) height else FrameLayout.LayoutParams.WRAP_CONTENT)
        }
        params.topMargin = positionY
        params.leftMargin = positionX
        contentView?.layoutParams = params
    }

    private fun getParentContentWidth(): Int {
        return if (parent == null) 0 else (parent!!.width - parent!!.paddingLeft - parent!!.paddingRight)
    }

    private fun getParentContentHeight(): Int {
        return if (parent == null) 0 else (parent!!.height - parent!!.paddingTop - parent!!.paddingBottom)
    }

    fun setOnClickListener(onClickListener: View.OnClickListener?) {
        contentView?.setOnClickListener(onClickListener)
    }

    fun close(layout: ViewGroup?) {
        if (layout == null) {
            return
        }
        layout.removeView(contentView)
    }
}