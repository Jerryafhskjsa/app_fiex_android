package com.black.frying.view

import android.app.Activity
import android.graphics.drawable.PaintDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.black.base.widget.AnalyticChart
import com.black.frying.fragment.LIMIT
import com.black.frying.fragment.MARKET
import com.black.frying.fragment.PLAN
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListPositionSideBinding

//指标选择器
class PositionSideSelector(activity: Activity) : PopupWindow.OnDismissListener, View.OnClickListener {
    private val popupWindow: PopupWindow?
    private val margin: Float
    private var onKLineQuotaSelectorListener: OnKLineQuotaSelectorListener? = null
    private var type: String? = null

    private val binding: ListPositionSideBinding

    init {
        val inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater, R.layout.list_position_side, null, false)
        val dm = activity.resources.displayMetrics
        margin = 5 * dm.density
        popupWindow = PopupWindow(binding?.root, (dm.widthPixels - 2 * margin).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener(this)
        binding.limit.setOnClickListener(this)
        binding.plan.setOnClickListener(this)
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
                binding.plan.isChecked = true
                binding.limit.isChecked = false
                binding.market.isChecked = true
            }
            R.id.plan -> {
                binding.limit.isChecked = true
                binding.plan.isChecked = false
                binding.market.isChecked = true
            }
            R.id.market -> {
                binding.plan.isChecked = true
                binding.market.isChecked = false
                binding.limit.isChecked = true
            }

        }
        getType()
        dismiss()
    }

    private fun getType() {
        var type = LIMIT
        if (!binding.limit.isChecked) {
            type = LIMIT
        }
        if (!binding.plan.isChecked) {
            type = PLAN
        }
        if (!binding.market.isChecked) {
            type = MARKET
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
        if (type == LIMIT) {
            binding.limit.isChecked = false
        }
        else if (type == PLAN) {
            binding.plan.isChecked = false
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