package com.black.frying.contract.biz.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.fbsex.exchange.databinding.FuturesLayoutAccountInfoBinding

class FuturesAccountInfoView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val accountInfoBinding: FuturesLayoutAccountInfoBinding

    init {
        accountInfoBinding =
            FuturesLayoutAccountInfoBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun getAccountTotalTv(): TextView {
        return accountInfoBinding.futuresAccountTotalProfit
    }
    fun getAccountTotalProfitTitle(): View {
        return accountInfoBinding.futuresAccountTotalProfitTitle
    }

    fun getCoinRateTv(): TextView {
        return accountInfoBinding.futuresCoinRate
    }
}