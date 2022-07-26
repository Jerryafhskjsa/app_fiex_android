package com.black.base.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.core.widget.NestedScrollView
import skin.support.widget.SkinCompatBackgroundHelper
import skin.support.widget.SkinCompatSupportable
import java.util.*

class ObserveScrollView : NestedScrollView, SkinCompatSupportable {
    private var mBackgroundTintHelper: SkinCompatBackgroundHelper?
    private var listenerInfo: MutableList<ScrollListener?>? = null
    override fun applySkin() {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper!!.applySkin()
        }
    }

    interface ScrollListener {
        fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context!!, attrs, defStyle) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper!!.loadFromAttributes(attrs, defStyle)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper!!.loadFromAttributes(attrs, 0)
    }

    constructor(context: Context?) : super(context!!) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper!!.loadFromAttributes(null, 0)
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (listenerInfo != null) {
            for (listener in listenerInfo!!) {
                listener?.onScrollChanged(l, t, oldl, oldt)
            }
        }
    }

    fun addScrollListener(l: ScrollListener?) {
        if (listenerInfo == null) {
            listenerInfo = ArrayList()
        }
        listenerInfo!!.add(l)
    }

    override fun setBackgroundResource(@DrawableRes resId: Int) {
        super.setBackgroundResource(resId)
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper!!.onSetBackgroundResource(resId)
        }
    }
}