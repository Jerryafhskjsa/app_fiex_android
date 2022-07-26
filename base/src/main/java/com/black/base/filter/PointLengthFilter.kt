package com.black.base.filter

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils
import android.util.Log

class PointLengthFilter(pointLength: Int) : InputFilter {
    private val pointLength: Int = if (pointLength < 0) 0 else pointLength
    override fun filter(source: CharSequence?, start: Int, end: Int, dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        // 删除等特殊字符，直接返回
        if ("" == source.toString()) {
            return null
        }
        val dValue = dest.toString() + source.toString()
        val splitArray = dValue.split(".").toTypedArray()
        if (splitArray.size > 1) {
            val dotValue = splitArray[1]
            val diff = if (TextUtils.isEmpty(dotValue)) 0 else dotValue.length - pointLength
            if (diff > 0) {
                return source?.subSequence(start, end - diff)
            }
        }
        return null
    }

}