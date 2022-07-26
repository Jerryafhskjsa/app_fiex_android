package com.black.lib.typeface

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.text.style.TypefaceSpan

class CustomerTypefaceSpan(private val context: Context, path: String?) : TypefaceSpan(path) {
    companion object {
        private fun apply(context: Context, paint: Paint, family: String) {
            val oldStyle: Int
            val old = paint.typeface
            oldStyle = old?.style ?: 0
            //        Typeface tf = Typeface.createFromAsset(context.getAssets(), family);
            val tf = TypefaceManager[context, family]
            tf?.let {
                val fake = oldStyle and tf.style.inv()
                if (fake and Typeface.BOLD != 0) {
                    paint.isFakeBoldText = true
                }
                if (fake and Typeface.ITALIC != 0) {
                    paint.textSkewX = -0.25f
                }
                paint.isFakeBoldText = false
                paint.typeface = tf
            }
        }
    }

    override fun updateDrawState(ds: TextPaint) {
        apply(context, ds, family)
    }

    override fun updateMeasureState(paint: TextPaint) {
        apply(context, paint, family)
    }

}