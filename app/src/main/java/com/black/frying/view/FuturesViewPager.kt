package com.black.frying.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class FuturesViewPager constructor(context: Context, attrs: AttributeSet?) :
    ViewPager(context, attrs) {

    private val xDirectionEnable = false

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return xDirectionEnable && super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return xDirectionEnable && super.onTouchEvent(ev)
    }
}