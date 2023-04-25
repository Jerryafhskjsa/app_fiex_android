package com.black.frying.contract.biz.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.black.util.NumberUtils
import com.fbsex.exchange.databinding.FuturesLayoutPriceEditTextBinding
import java.math.BigDecimal
import kotlin.math.pow

class FuturesPriceEditTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
    var binding: FuturesLayoutPriceEditTextBinding

    var  onInputChange :OnInputChange?=null
    init {
        binding =
            FuturesLayoutPriceEditTextBinding.inflate(LayoutInflater.from(context), this, true)

        binding.priceAdd.setOnClickListener {
            if (!binding.price.isEnabled){
                return@setOnClickListener
            }
            // add
            var text = binding.price.text.toString()
            if (text.contains("%")){
                text = ""
            }
            val s = if (text.isEmpty()) {
                NumberUtils.formatRoundDown(getUnitPrice(), 2, precision).toString()
            } else {
                NumberUtils.toBigDecimal(text.toString()).add(getUnitPrice()).toString()
            }
            binding.price.setText(s)
            onInputChange?.onAdd(s)
        }
        binding.priceSub.setOnClickListener {
            if (!binding.price.isEnabled){
                return@setOnClickListener
            }
            //subtraction
            var text = binding.price.text.toString()
            if (text.contains("%")){
                text = ""
            }
            val s = if (text.isEmpty()) {
                NumberUtils.formatRoundDown(getUnitPrice(), 2, precision).toString()
            } else {
                var subtract = NumberUtils.toBigDecimal(text.toString()).subtract(getUnitPrice())
                subtract = subtract.max(BigDecimal.ZERO)
                subtract.toString()
            }
            binding.price.setText(s)
            onInputChange?.onSub(s)
        }

        binding.price.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onInputChange?.onInput(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    //先设置 精度
    var precision: Int = 1

    private fun getUnitPrice(): BigDecimal {
        return 10.0.pow(-(precision)).toBigDecimal()
    }

    fun setHint(hint :String){
        binding.price.hint = hint
    }

    fun setText( text:String){
        binding.price.setText(text)
    }
    fun setEnable(enable: Boolean) {
        binding.price.isEnabled = enable
    }
}

interface OnInputChange {
    fun onAdd(price: String)
    fun onSub(price: String)
    fun onInput(price: String)
}