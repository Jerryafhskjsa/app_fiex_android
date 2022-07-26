package com.black.base.lib.refresh

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.view.ViewCompat
import com.nineoldandroids.animation.Animator
import com.nineoldandroids.animation.ValueAnimator
import skin.support.content.res.SkinCompatResources

/**
 * 作者:王浩 邮件:bingoogolapple@gmail.com 创建时间:15/5/21 22:34 描述:黏性下拉刷新控件
 */
class BGAStickinessRefreshView @JvmOverloads constructor(context: Context?, attrs: AttributeSet? = null, defStyle: Int = 0) : View(context, attrs, defStyle) {
    companion object {
        private val TAG = BGAStickinessRefreshView::class.java.simpleName
    }

    private var mStickinessRefreshViewHolder: BGAStickinessRefreshViewHolder? = null
    private var mTopBound: RectF? = null
    private var mBottomBound: RectF? = null
    private var mRotateDrawableBound: Rect? = null
    private var mCenterPoint: Point? = null
    private var mPaint: Paint? = null
    private var mPath: Path? = null
    private var mRotateDrawable: Drawable? = null
    /**
     * 旋转图片的大小
     */
    private var mRotateDrawableSize = 0
    private var mMaxBottomHeight = 0
    private var mCurrentBottomHeight = 0
    /**
     * 是否正在旋转
     */
    private var mIsRotating = false
    private var mIsRefreshing = false
    /**
     * 当前旋转角度
     */
    private var mCurrentDegree = 0
    private var mEdge = 0
    private var mTopSize = 0

    init {
        initBounds()
        initPaint()
        initSize()
    }

    private fun initBounds() {
        mTopBound = RectF()
        mBottomBound = RectF()
        mRotateDrawableBound = Rect()
        mCenterPoint = Point()
    }

    private fun initPaint() {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPath = Path()
    }

    private fun initSize() {
        mEdge = BGARefreshLayout.dp2px(context, 5)
        mRotateDrawableSize = BGARefreshLayout.dp2px(context, 30)
        mTopSize = mRotateDrawableSize + 2 * mEdge
        mMaxBottomHeight = (2.4f * mRotateDrawableSize).toInt()
    }

    fun setStickinessColor(@ColorRes resId: Int) {
        mPaint?.color = SkinCompatResources.getColor(context, resId)
    }

    fun setRotateImage(@DrawableRes resId: Int) {
        mRotateDrawable = SkinCompatResources.getDrawable(context, resId)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = mTopSize + paddingLeft + paddingRight
        val height = mTopSize + paddingTop + paddingBottom + mMaxBottomHeight
        setMeasuredDimension(width, height)
        measureDraw()
    }

    private fun measureDraw() {
        mCenterPoint?.x = measuredWidth / 2
        mCenterPoint?.y = measuredHeight / 2
        mTopBound?.left = (mCenterPoint?.x ?: 0) - mTopSize / 2.toFloat()
        mTopBound?.right = (mTopBound?.left ?: 0f) + mTopSize
        mTopBound?.bottom = measuredHeight - paddingBottom - mCurrentBottomHeight.toFloat()
        mTopBound?.top = (mTopBound?.bottom ?: 0f) - mTopSize
        var scale = 1.0f - mCurrentBottomHeight * 1.0f / mMaxBottomHeight
        scale = Math.min(Math.max(scale, 0.2f), 1.0f)
        val mBottomSize = (mTopSize * scale).toInt()
        mBottomBound?.left = (mCenterPoint?.x ?: 0) - mBottomSize / 2.toFloat()
        mBottomBound?.right = (mBottomBound?.left ?: 0f) + mBottomSize
        mBottomBound?.bottom = (mTopBound?.bottom ?: 0f) + mCurrentBottomHeight
        mBottomBound?.top = (mBottomBound?.bottom ?: 0f) - mBottomSize
    }

    public override fun onDraw(canvas: Canvas) {
        if (mRotateDrawable == null) {
            return
        }
        mPath?.reset()
        mTopBound?.round(mRotateDrawableBound)
        mRotateDrawable?.bounds = mRotateDrawableBound
        if (mIsRotating) {
            mPath?.addOval(mTopBound, Path.Direction.CW)
            canvas.drawPath(mPath, mPaint)
            canvas.save()
            canvas.rotate(mCurrentDegree.toFloat(), mRotateDrawable?.bounds?.centerX()?.toFloat()
                    ?: 0f, mRotateDrawable?.bounds?.centerY()?.toFloat() ?: 0f)
            mRotateDrawable?.draw(canvas)
            canvas.restore()
        } else { // 移动到drawable左边缘的中间那个点
            mPath?.moveTo(mTopBound?.left ?: 0f, (mTopBound?.top ?: 0f) + mTopSize / 2)
            // 从drawable左边缘的中间那个点开始画半圆
            mPath?.arcTo(mTopBound, 180f, 180f)
            // 二阶贝塞尔曲线，第一个是控制点，第二个是终点
            // mPath.quadTo(mTopBound.right - mTopSize / 8, mTopBound.bottom,
            // mBottomBound.right, mBottomBound.bottom - mBottomBound.height() /
            // 2);
            // mCurrentBottomHeight 0 到 mMaxBottomHeight
            // scale 0.2 到 1
            val scale = Math.max(mCurrentBottomHeight * 1.0f / mMaxBottomHeight, 0.2f)
            val bottomControlXOffset = mTopSize * ((3 + Math.pow(scale.toDouble(), 7.0).toFloat() * 16) / 32)
            val bottomControlY = (mTopBound?.bottom ?: 0f) / 2 + (mCenterPoint?.y ?: 0) / 2
            // 三阶贝塞尔曲线，前两个是控制点，最后一个点是终点
            mPath?.cubicTo((mTopBound?.right ?: 0f) - mTopSize / 8, (mTopBound?.bottom
                    ?: 0f), (mTopBound?.right ?: 0f) - bottomControlXOffset,
                    bottomControlY, mBottomBound?.right ?: 0f, (mBottomBound?.bottom
                    ?: 0f) - (mBottomBound?.height() ?: 0f) / 2)
            mPath?.arcTo(mBottomBound, 0f, 180f)
            // mPath.quadTo(mTopBound.left + mTopSize / 8, mTopBound.bottom,
            // mTopBound.left, mTopBound.bottom - mTopSize / 2);
            mPath?.cubicTo((mTopBound?.left
                    ?: 0f) + bottomControlXOffset, bottomControlY, (mTopBound?.left
                    ?: 0f) + mTopSize / 8,
                    mTopBound?.bottom ?: 0f, mTopBound?.left ?: 0f, (mTopBound?.bottom
                    ?: 0f) - mTopSize / 2)
            canvas.drawPath(mPath, mPaint)
            mRotateDrawable?.draw(canvas)
        }
    }

    fun setMoveYDistance(moveYDistance: Int) {
        val bottomHeight = moveYDistance - mTopSize - paddingBottom - paddingTop
        mCurrentBottomHeight = if (bottomHeight > 0) {
            bottomHeight
        } else {
            0
        }
        postInvalidate()
    }

    /**
     * 是否能切换到正在刷新状态
     *
     * @return
     */
    fun canChangeToRefreshing(): Boolean {
        return mCurrentBottomHeight >= mMaxBottomHeight * 0.98f
    }

    fun startRefreshing() {
        val animator = ValueAnimator.ofInt(mCurrentBottomHeight, 0)
        animator.duration = mStickinessRefreshViewHolder?.topAnimDuration?.toLong() ?: 0
        animator.addUpdateListener { animation ->
            mCurrentBottomHeight = animation.animatedValue as Int
            postInvalidate()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                mIsRefreshing = true
                if (mCurrentBottomHeight != 0) {
                    mStickinessRefreshViewHolder?.startChangeWholeHeaderViewPaddingTop(mCurrentBottomHeight)
                } else {
                    mStickinessRefreshViewHolder?.startChangeWholeHeaderViewPaddingTop(-(mTopSize + paddingTop + paddingBottom))
                }
            }

            override fun onAnimationEnd(animation: Animator) {
                mIsRotating = true
                startRotating()
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }

    private fun startRotating() {
        ViewCompat.postOnAnimation(this) {
            mCurrentDegree += 10
            if (mCurrentDegree > 360) {
                mCurrentDegree = 0
            }
            if (mIsRefreshing) {
                startRotating()
            }
            postInvalidate()
        }
    }

    fun stopRefresh() {
        mIsRotating = true
        mIsRefreshing = false
        postInvalidate()
    }

    fun smoothToIdle() {
        val animator = ValueAnimator.ofInt(mCurrentBottomHeight, 0)
        animator.duration = mStickinessRefreshViewHolder?.topAnimDuration?.toLong() ?: 0
        animator.addUpdateListener { animation ->
            mCurrentBottomHeight = animation.animatedValue as Int
            postInvalidate()
        }
        animator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationEnd(animation: Animator) {
                mIsRotating = false
            }

            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
        animator.start()
    }

    fun setStickinessRefreshViewHolder(stickinessRefreshViewHolder: BGAStickinessRefreshViewHolder?) {
        mStickinessRefreshViewHolder = stickinessRefreshViewHolder
    }
}