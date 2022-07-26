package com.black.base.widget

import android.graphics.Color
import kotlin.math.sqrt

/**
 * Created by Administrator on 2014/12/12.
 */
object Colors {
    fun isLight(color: Int): Boolean {
        return sqrt(Color.red(color) * Color.red(color) * .241 + Color.green(color) * Color.green(color) * .691 + Color.blue(color) * Color.blue(color) * .068) > 130
    }
}