package com.black.frying.view

import android.app.Activity
import android.graphics.drawable.PaintDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.CheckedTextView
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.black.base.widget.AnalyticChart.TimeStep
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ViewMoreTimeStepSelectorBinding

class MoreTimeStepSelector(activity: Activity) : PopupWindow.OnDismissListener, View.OnClickListener {
    private val popupWindow: PopupWindow?
    private val margin: Float
    private val tabs = arrayOfNulls<CheckedTextView>(5)
    private var selectedTimeStep: TimeStep? = null
    private var onMoreTimeStepSelectorListener: OnMoreTimeStepSelectorListener? = null

    private val binding: ViewMoreTimeStepSelectorBinding

    init {
        val inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater, R.layout.view_more_time_step_selector, null, false)
        val dm = activity.resources.displayMetrics
        margin = 5 * dm.density
        popupWindow = PopupWindow(binding.root, (dm.widthPixels - 2 * margin).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        //popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener(this)
        binding.tabMin1.setOnClickListener(this)
        binding.tabMin1.tag = TimeStep.MIN_1
        binding.tabMin5.setOnClickListener(this)
        binding.tabMin5.tag = TimeStep.MIN_5
        binding.tabMin30.setOnClickListener(this)
        binding.tabMin30.tag = TimeStep.MIN_30
        binding.tab6H.setOnClickListener(this)
        binding.tab6H.tag = TimeStep.HOUR_6
        binding.tabWeek1.setOnClickListener(this)
        binding.tabWeek1.tag = TimeStep.WEEK_1
        tabs[0] = binding.tabMin1
        tabs[1] = binding.tabMin5
        tabs[2] = binding.tabMin30
        tabs[3] = binding.tab6H
        tabs[4] = binding.tabWeek1
    }

    override fun onDismiss() {
        if (onMoreTimeStepSelectorListener != null) {
            onMoreTimeStepSelectorListener!!.onSelect(selectedTimeStep)
        }
    }

    override fun onClick(view: View) {
        selectKTab(view.tag as TimeStep)
    }

    private val isShowing: Boolean
        get() = popupWindow != null && popupWindow.isShowing

    private fun dismiss() {
        if (isShowing) {
            popupWindow!!.dismiss()
        }
    }

    fun show(view: View?, timeStep: TimeStep) {
        selectedTimeStep = null
        when {
            timeStep === TimeStep.MIN_1 -> {
                binding.tabMin1.isChecked = true
                binding.tabMin5.isChecked = false
                binding.tabMin30.isChecked = false
                binding.tab6H.isChecked = false
                binding.tabWeek1.isChecked = false
            }
            TimeStep.MIN_5 === timeStep -> {
                binding.tabMin5.isChecked = true
                binding.tabMin1.isChecked = false
                binding.tabMin30.isChecked = false
                binding.tab6H.isChecked = false
                binding.tabWeek1.isChecked = false
            }
            TimeStep.MIN_30 === timeStep -> {
                binding.tabMin30.isChecked = true
                binding.tabMin1.isChecked = false
                binding.tabMin5.isChecked = false
                binding.tab6H.isChecked = false
                binding.tabWeek1.isChecked = false
            }
            TimeStep.HOUR_6 === timeStep -> {
                binding.tab6H.isChecked = true
                binding.tabMin1.isChecked = false
                binding.tabMin5.isChecked = false
                binding.tabMin30.isChecked = false
                binding.tabWeek1.isChecked = false
            }
            TimeStep.WEEK_1 === timeStep -> {
                binding.tabWeek1.isChecked = true
                binding.tabMin1.isChecked = false
                binding.tabMin5.isChecked = false
                binding.tabMin30.isChecked = false
                binding.tab6H.isChecked = false
            }
        }
        popupWindow!!.showAsDropDown(view, margin.toInt(), margin.toInt())
    }

    private fun selectKTab(timeStep: TimeStep) {
        selectedTimeStep = timeStep
        dismiss()
    }

    fun setOnMoreTimeStepSelectorListener(onMoreTimeStepSelectorListener: OnMoreTimeStepSelectorListener?) {
        this.onMoreTimeStepSelectorListener = onMoreTimeStepSelectorListener
    }

    interface OnMoreTimeStepSelectorListener {
        fun onSelect(timeStep: TimeStep?)
    }
}