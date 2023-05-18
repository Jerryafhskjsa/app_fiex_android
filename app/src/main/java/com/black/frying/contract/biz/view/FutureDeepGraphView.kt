package com.black.frying.contract.biz.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.fbsex.exchange.databinding.FuturesLayoutDeepGraphBinding

class FutureDeepGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    lateinit var futuresLayoutDeepGraphBinding: FuturesLayoutDeepGraphBinding

    init {
        futuresLayoutDeepGraphBinding = FuturesLayoutDeepGraphBinding.inflate(LayoutInflater.from(context),this,true)
    }
}