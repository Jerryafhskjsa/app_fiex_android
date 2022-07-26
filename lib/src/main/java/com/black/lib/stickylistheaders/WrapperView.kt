package com.black.lib.stickylistheaders

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.View
import android.view.ViewGroup

/**
 *
 * the view that wrapps a divider header and a normal list item. The listview sees this as 1 item
 *
 * @author Emil Sjölander
 */
open class WrapperView internal constructor(c: Context?) : ViewGroup(c) {
    var item: View? = null
    private var mDivider: Drawable? = null
    private var mDividerHeight = 0
    var header: View? = null
    var mItemTop = 0
    fun hasHeader(): Boolean {
        return header != null
    }

    fun update(item: View?, header: View?, divider: Drawable?, dividerHeight: Int) {
        //every wrapperview must have a list item
        if (item == null) {
            throw NullPointerException("List view item must not be null.")
        }
        //only remove the current item if it is not the same as the new item. this can happen if wrapping a recycled view
        if (this.item !== item) {
            removeView(this.item)
            this.item = item
            val parent = item.parent
            if (parent != null && parent !== this) {
                if (parent is ViewGroup) {
                    parent.removeView(item)
                }
            }
            addView(item)
        }
        //same logik as above but for the header
        if (this.header !== header) {
            if (this.header != null) {
                removeView(this.header)
            }
            this.header = header
            header?.let { addView(it) }
        }
        if (mDivider !== divider) {
            mDivider = divider
            mDividerHeight = dividerHeight
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val measuredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth,
                MeasureSpec.EXACTLY)
        var measuredHeight = 0
        //measure header or divider. when there is a header visible it acts as the divider
        if (header != null) {
            val params = header?.layoutParams
            if (params != null && params.height > 0) {
                header?.measure(childWidthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY))
            } else {
                header?.measure(childWidthMeasureSpec,
                        MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            }
            measuredHeight += (header?.measuredHeight ?: 0)
        } else if (mDivider != null && item?.visibility != View.GONE) {
            measuredHeight += mDividerHeight
        }
        //measure item
        val params = item?.layoutParams
        //enable hiding listview item,ex. toggle off items in group
        if (item?.visibility == View.GONE) {
            item?.measure(childWidthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.EXACTLY))
        } else if (params != null && params.height >= 0) {
            item?.measure(childWidthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(params.height, MeasureSpec.EXACTLY))
            measuredHeight += (item?.measuredHeight ?: 0)
        } else {
            item?.measure(childWidthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            measuredHeight += (item?.measuredHeight ?: 0)
        }
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val left = 0
        val top = 0
        val right: Int = width
        val bottom: Int = height
        when {
            header != null -> {
                val headerHeight = header?.measuredHeight ?: 0
                header?.layout(left, top, right, headerHeight)
                mItemTop = headerHeight
                item?.layout(left, headerHeight, right, bottom)
            }
            mDivider != null -> {
                mDivider?.setBounds(left, top, right, mDividerHeight)
                mItemTop = mDividerHeight
                item?.layout(left, mDividerHeight, right, bottom)
            }
            else -> {
                mItemTop = top
                item?.layout(left, top, right, bottom)
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        if (header == null && mDivider != null && item?.visibility != View.GONE) {
            // Drawable.setBounds() does not seem to work pre-honeycomb. So have
            // to do this instead
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                canvas.clipRect(0, 0, width, mDividerHeight)
            }
            mDivider?.draw(canvas)
        }
    }
}