package com.black.frying.contract.biz.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.fbsex.exchange.databinding.FuturesLayoutMultipleSettingBinding

class FuturesMultipleSettingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    private val multipleSettingBinding: FuturesLayoutMultipleSettingBinding

    init {
        multipleSettingBinding =
            FuturesLayoutMultipleSettingBinding.inflate(LayoutInflater.from(context), this, true)
    }

    fun getMuchBtn(): View {
        return multipleSettingBinding.futuresMultipleMore
    }

    fun getLessBtn(): View {
        return multipleSettingBinding.futuresMultipleLess
    }

    fun setMuchText(text: String) {
        multipleSettingBinding.futuresMultipleMoreText.text = text
    }

    fun setLessText(text: String) {
        multipleSettingBinding.futuresMultipleLessText.text = text
    }
}