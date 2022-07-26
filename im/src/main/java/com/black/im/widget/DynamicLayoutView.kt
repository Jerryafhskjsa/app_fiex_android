package com.black.im.widget

import android.view.View
import android.view.ViewGroup

abstract class DynamicLayoutView<T> {
    protected var mLayout: ViewGroup? = null
    protected var mViewId = 0
    fun setLayout(layout: ViewGroup?) {
        mLayout = layout
    }

    fun setMainViewId(viewId: Int) {
        mViewId = viewId
    }

    fun addChild(child: View?, params: ViewGroup.LayoutParams?) {
        mLayout?.addView(child, params)
    }

    abstract fun parseInformation(info: T)
}