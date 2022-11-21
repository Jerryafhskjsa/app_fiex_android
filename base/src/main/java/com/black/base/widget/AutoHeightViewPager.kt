package com.black.base.widget

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import androidx.viewpager.widget.ViewPager

class AutoHeightViewPager : ViewPager {
    private var mCurPosition = 0
    private var mHeight = 0
    private val mChildrenViews: HashMap<Int, View> = LinkedHashMap()

    constructor(context: Context?) : super(context!!) {}
    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMeasureSpec = heightMeasureSpec
        if (mChildrenViews.size > mCurPosition) {
            val child = mChildrenViews[mCurPosition]
            if (child != null) {
                child.measure(
                    widthMeasureSpec,
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                mHeight = child.measuredHeight
            }
        }
        if (mHeight != 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY)
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    fun updateHeight(current: Int) {
        mCurPosition = current
        if (mChildrenViews.size > current) {
            var layoutParams = layoutParams
            if (layoutParams == null) {
                layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeight)
            } else {
                layoutParams.height = mHeight
            }
            setLayoutParams(layoutParams)
        }
    }

    fun setViewPosition(view: View, position: Int) {
        mChildrenViews[position] = view
    }


    companion object {
        const val POSITION = "position"
    }
}