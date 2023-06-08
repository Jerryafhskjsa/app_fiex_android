package com.black.frying.view

import android.app.Activity
import android.graphics.drawable.PaintDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.black.base.widget.AnalyticChart
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ViewKLineQuotaSelectorBinding

//指标选择器
class KLineQuotaSelector(activity: Activity) : PopupWindow.OnDismissListener, View.OnClickListener {
    private val popupWindow: PopupWindow?
    private val margin: Float
    private var onKLineQuotaSelectorListener: OnKLineQuotaSelectorListener? = null
    private var type: Int? = null

    private val binding: ViewKLineQuotaSelectorBinding

    init {
        val inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater, R.layout.view_k_line_quota_selector, null, false)
        val dm = activity.resources.displayMetrics
        margin = 5 * dm.density
        popupWindow = PopupWindow(binding?.root, (dm.widthPixels - 2 * margin).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener(this)
        binding.ma.setOnClickListener(this)
        binding.boll.setOnClickListener(this)
        binding.mainHidden.setOnClickListener(this)
        binding.macd.setOnClickListener(this)
        binding.kdj.setOnClickListener(this)
        binding.rsi.setOnClickListener(this)
        binding.wr.setOnClickListener(this)
        binding.subHidden.setOnClickListener(this)
    }

    override fun onDismiss() {
        if (onKLineQuotaSelectorListener != null) {
            onKLineQuotaSelectorListener!!.onSelect(type)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.ma -> {
                binding.ma?.isChecked = true
                binding.boll?.isChecked = false
                binding.mainHidden?.isChecked = true
            }
            R.id.boll -> {
                binding.ma?.isChecked = false
                binding.boll?.isChecked = true
                binding.mainHidden?.isChecked = true
            }
            R.id.main_hidden -> if (true == binding.mainHidden?.isChecked) {
                binding.ma?.isChecked = false
                binding.boll?.isChecked = false
                binding.mainHidden?.isChecked = false
            } else {
                dismiss()
                return
            }
            R.id.macd -> {
                binding.macd?.isChecked = true
                binding.kdj?.isChecked = false
                binding.rsi?.isChecked = false
                binding.wr?.isChecked = false
                binding.subHidden?.isChecked = true
            }
            R.id.kdj -> {
                binding.macd?.isChecked = false
                binding.kdj?.isChecked = true
                binding.rsi?.isChecked = false
                binding.wr?.isChecked = false
                binding.subHidden?.isChecked = true
            }
            R.id.rsi -> {
                binding.macd?.isChecked = false
                binding.kdj?.isChecked = false
                binding.rsi?.isChecked = true
                binding.wr?.isChecked = false
                binding.subHidden?.isChecked = true
            }
            R.id.wr -> {
                binding.macd?.isChecked = false
                binding.kdj?.isChecked = false
                binding.rsi?.isChecked = false
                binding.wr?.isChecked = true
                binding.subHidden?.isChecked = true
            }
            R.id.sub_hidden -> if (true == binding.subHidden?.isChecked) {
                binding.macd?.isChecked = false
                binding.kdj?.isChecked = false
                binding.rsi?.isChecked = false
                binding.wr?.isChecked = false
                binding.subHidden?.isChecked = false
            } else {
                dismiss()
                return
            }
        }
        getType()
        dismiss()
    }

    private fun getType() {
        var type = 0
        if (true == binding.ma?.isChecked) {
            type = type or AnalyticChart.MA
        }
        if (true == binding.boll?.isChecked) {
            type = type or AnalyticChart.BOLL
        }
        if (true != binding.mainHidden?.isChecked) {
            type = type or AnalyticChart.MAIN_HIDDEN
        }
        if (true == binding.macd?.isChecked) {
            type = type or AnalyticChart.MACD
        }
        if (true == binding.macd?.isChecked) {
            type = type or AnalyticChart.KDJ
        }
        if (true == binding.rsi?.isChecked) {
            type = type or AnalyticChart.RSI
        }
        if (true == binding.wr?.isChecked) {
            type = type or AnalyticChart.WR
        }
        if (true != binding.subHidden?.isChecked) {
            type = type or AnalyticChart.SUB_HIDDEN
        }
        this.type = type
    }

    private val isShowing: Boolean
        get() = popupWindow != null && popupWindow.isShowing

    private fun dismiss() {
        if (isShowing) {
            popupWindow!!.dismiss()
        }
    }

    fun show(view: View?, type: Int) {
        this.type = null
        refreshView(type)
        popupWindow!!.showAsDropDown(view, margin.toInt(), margin.toInt())
    }

    private fun refreshView(type: Int) {
        binding.ma?.isChecked = type and AnalyticChart.MA == AnalyticChart.MA
        binding.boll?.isChecked = type and AnalyticChart.BOLL == AnalyticChart.BOLL
        binding.mainHidden?.isChecked = type and AnalyticChart.MAIN_HIDDEN != AnalyticChart.MAIN_HIDDEN
        binding.macd?.isChecked = type and AnalyticChart.MACD == AnalyticChart.MACD
        binding.macd?.isChecked = type and AnalyticChart.KDJ == AnalyticChart.KDJ
        binding.rsi?.isChecked = type and AnalyticChart.RSI == AnalyticChart.RSI
        binding.wr?.isChecked = type and AnalyticChart.WR == AnalyticChart.WR
        binding.subHidden?.isChecked = type and AnalyticChart.SUB_HIDDEN != AnalyticChart.SUB_HIDDEN
    }

    fun setOnKLineQuotaSelectorListener(onKLineQuotaSelectorListener: OnKLineQuotaSelectorListener?) {
        this.onKLineQuotaSelectorListener = onKLineQuotaSelectorListener
    }

    interface OnKLineQuotaSelectorListener {
        fun onSelect(type: Int?)
    }
}