package com.black.frying.contract.biz.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.fbsex.exchange.databinding.FuturesLayoutOrderPriceTypeBinding

class FuturesOrderPriceTypeView  @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes)  {
    var bind :FuturesLayoutOrderPriceTypeBinding
        init {
            bind = FuturesLayoutOrderPriceTypeBinding.inflate(LayoutInflater.from(context),this,true)
    }

    fun changeDisplay(text :String){
        bind.orderType.text = text
    }
}