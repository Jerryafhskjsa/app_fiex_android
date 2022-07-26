package com.black.lib.util

import android.view.View
import android.view.ViewGroup
import android.widget.Checkable

object ViewUtil {
    @JvmStatic
    fun setChildrenChecked(view: View?, checked: Boolean) {
        if (view is Checkable) {
            (view as Checkable).isChecked = checked
        }
        if (view is ViewGroup) {
            val childCount = view.childCount
            for (i in 0 until childCount) {
                setChildrenChecked(view.getChildAt(i), checked)
            }
        }
    }
}