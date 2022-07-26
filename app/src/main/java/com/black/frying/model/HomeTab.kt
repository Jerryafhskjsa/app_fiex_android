package com.black.frying.model

import android.widget.TextView
import androidx.annotation.DrawableRes

class HomeTab(var tabName: String, @param:DrawableRes var topIconId: Int, var fragmentClass: Class<*>) {
    private var indicatorTextView: TextView? = null
    fun setIndicatorTextView(indicatorTextView: TextView?) {
        this.indicatorTextView = indicatorTextView
    }

}