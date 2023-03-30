package com.black.frying.contract.biz.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.fbsex.exchange.R

@SuppressLint("ViewConstructor")
class FutureTitleBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    init {
        View.inflate(context, R.layout.futures_layout_title_bar, this)
    }

    val futuresTitleBar: View by lazy {
        findViewById(R.id.futuresTitleBar)
    }
    val futuresTitleBarTitle: TextView by lazy {
        findViewById(R.id.futuresTitleBarTitle)
    }

    val futuresTitleBarPriceSince: TextView by lazy {
        findViewById(R.id.futuresTitleBarPriceSince)
    }

    val futuresCollectCoin: ImageView by lazy {
        findViewById(R.id.futuresCollectCoin)
    }
    val futuresCoinCharts: View by lazy {
        findViewById(R.id.futuresCoinCharts)
    }
    val futuresTransactionMore: View by lazy {
        findViewById(R.id.futuresTransactionMore)
    }

}