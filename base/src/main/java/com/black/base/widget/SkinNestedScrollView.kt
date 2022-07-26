package com.black.base.widget

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.DrawableRes
import androidx.core.widget.NestedScrollView
import skin.support.widget.SkinCompatBackgroundHelper
import skin.support.widget.SkinCompatSupportable

class SkinNestedScrollView : NestedScrollView, SkinCompatSupportable {
    private var mBackgroundTintHelper: SkinCompatBackgroundHelper?

    constructor(context: Context) : super(context) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mBackgroundTintHelper = SkinCompatBackgroundHelper(this)
        mBackgroundTintHelper?.loadFromAttributes(attrs, defStyleAttr)
    }

    override fun setBackgroundResource(@DrawableRes resId: Int) {
        super.setBackgroundResource(resId)
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper?.onSetBackgroundResource(resId)
        }
    }

    override fun applySkin() {
        if (mBackgroundTintHelper != null) {
            mBackgroundTintHelper?.applySkin()
        }
    }
}