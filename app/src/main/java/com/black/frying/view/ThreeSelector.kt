package com.black.frying.view

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ListSheZhiBinding

//指标选择器
class ThreeSelector(activity: Activity) : PopupWindow.OnDismissListener, View.OnClickListener {
    private val popupWindow: PopupWindow?
    private val margin: Float
    private var onKLineQuotaSelectorListener: OnKLineQuotaSelectorListener? = null
    private var type: String? = null
    private val binding: ListSheZhiBinding

    init {
        val inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater, R.layout.list_she_zhi, null, false)
        val dm = activity.resources.displayMetrics
        margin = 5 * dm.density
        popupWindow = PopupWindow(binding?.root, (dm.widthPixels - 2 * margin).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener(this)
        binding.futureMsg.setOnClickListener(this)
        binding.countMechain.setOnClickListener(this)
        binding.likeSetting.setOnClickListener(this)
        binding.futureRate.setOnClickListener(this)
    }

    override fun onDismiss() {
        if (onKLineQuotaSelectorListener != null) {
            onKLineQuotaSelectorListener!!.onSelect(type)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.future_msg -> {
                binding.countMechain.isChecked = true
                binding.futureMsg.isChecked = false
                binding.likeSetting.isChecked = true
                binding.futureRate.isChecked = true
            }
            R.id.count_mechain -> {
                binding.futureMsg.isChecked = true
                binding.countMechain.isChecked = false
                binding.likeSetting.isChecked = true
                binding.futureRate.isChecked = true
            }
            R.id.like_setting -> {
                binding.countMechain.isChecked = true
                binding.likeSetting.isChecked = false
                binding.futureMsg.isChecked = true
                binding.futureRate.isChecked = true
            }
            R.id.future_rate -> {
                binding.countMechain.isChecked = true
                binding.futureRate.isChecked = false
                binding.likeSetting.isChecked = true
                binding.futureMsg.isChecked = true
            }

        }
        getType()
    }

    private fun getType() {
        var type = "0"
        if (!binding.futureMsg.isChecked) {
            type = "0"
        }
        if (!binding.countMechain.isChecked) {
            type = "1"
        }
        if (!binding.likeSetting.isChecked) {
            type = "2"
        }
        if (!binding.futureRate.isChecked) {
            type = "3"
        }
        this.type = type
        dismiss()
    }

    private val isShowing: Boolean
        get() = popupWindow != null && popupWindow.isShowing

    private fun dismiss() {
        if (isShowing) {
            popupWindow!!.dismiss()
        }
    }

    fun show(view: View?, type: String?) {
        this.type = null
        refreshView(type)
        popupWindow!!.showAsDropDown(view, margin.toInt(), margin.toInt())
    }

    private fun refreshView(type: String?) {
        binding.countMechain.isChecked = true
        binding.futureRate.isChecked = true
        binding.likeSetting.isChecked = true
        binding.futureMsg.isChecked = true
    }

    fun setOnKLineQuotaSelectorListener(onKLineQuotaSelectorListener: OnKLineQuotaSelectorListener?) {
        this.onKLineQuotaSelectorListener = onKLineQuotaSelectorListener
    }

    interface OnKLineQuotaSelectorListener {
        fun onSelect(type: String?)
    }
}