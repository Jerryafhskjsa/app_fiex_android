package com.black.frying.view

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.black.frying.fragment.LIMIT
import com.black.frying.fragment.MARKET
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListPositionTypeBinding

//指标选择器
class PositionTypeSeletor(activity: Activity) : PopupWindow.OnDismissListener, View.OnClickListener {
    private val popupWindow: PopupWindow?
    private val margin: Float
    private var onKLineQuotaSelectorListener: OnKLineQuotaSelectorListener? = null
    private var type: String? = null

    private val binding: ListPositionTypeBinding

    init {
        val inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater, R.layout.list_position_type, null, false)
        val dm = activity.resources.displayMetrics
        margin = 5 * dm.density
        popupWindow = PopupWindow(binding?.root, (dm.widthPixels - 2 * margin).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener(this)
        binding.limit.setOnClickListener(this)
        binding.market.setOnClickListener(this)
    }

    override fun onDismiss() {
        if (onKLineQuotaSelectorListener != null) {
            onKLineQuotaSelectorListener!!.onSelect(type)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.limit -> {
                binding.limit.isChecked = false
                binding.market.isChecked = true
            }
            R.id.plan -> {
                binding.limit.isChecked = true
                binding.market.isChecked = true
            }
            R.id.market -> {
                binding.market.isChecked = false
                binding.limit.isChecked = true
            }

        }
        getType()
        dismiss()
    }

    private fun getType() {
        var type = "CROSSED"
        if (!binding.limit.isChecked) {
            type = "CROSSED"
        }
        if (!binding.market.isChecked) {
            type = "ISOLATED"
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

    fun show(view: View?, type: String) {
        this.type = null
        refreshView(type)
        popupWindow!!.showAsDropDown(view, margin.toInt(), margin.toInt())
    }

    private fun refreshView(type: String) {
        if (type == "CROSSED") {
            binding.limit.isChecked = false
        }
        else {
            binding.market.isChecked = false
        }
    }

    fun setOnKLineQuotaSelectorListener(onKLineQuotaSelectorListener: OnKLineQuotaSelectorListener?) {
        this.onKLineQuotaSelectorListener = onKLineQuotaSelectorListener
    }

    interface OnKLineQuotaSelectorListener {
        fun onSelect(type: String?)
    }
}