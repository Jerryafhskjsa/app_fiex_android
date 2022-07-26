package com.black.base.filter

import android.text.InputFilter
import android.text.Spanned
import android.text.TextUtils

class NumberFilter : InputFilter {
    override fun filter(source: CharSequence?, start: Int, end: Int,
                        dest: Spanned, dstart: Int, dend: Int): CharSequence? {
        if (source == null || "" == source.toString()) {
            return ""
        }
        val resultString = dest.toString() + source
        if (TextUtils.isEmpty(resultString) || resultString[0] == '.') {
            return ""
        } else if (resultString.matches(Regex("^\\d+(\\.)|\\d+(\\.\\d+)?$"))) {
            return source
        }
        return ""
    }
}