package com.black.base.widget

import android.content.Context
import android.util.TypedValue

/**
 * Created by Zhukai on 2014/5/29 0029.
 */
internal object Density {
    fun dp2px(context: Context, dp: Float): Int {
        val r = context.resources
        val px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.displayMetrics)
        return Math.round(px)
    }
}