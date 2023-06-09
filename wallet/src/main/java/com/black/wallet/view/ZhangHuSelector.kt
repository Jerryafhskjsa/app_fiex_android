package com.black.wallet.view

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.black.base.model.wallet.SupportAccount
import com.black.wallet.R
import com.black.wallet.databinding.ListTranferZhanghuBinding

//指标选择器
class ZhangHuSelector(activity: Activity) : PopupWindow.OnDismissListener, View.OnClickListener {
    private val popupWindow: PopupWindow?
    private val margin: Float
    private var onKLineQuotaSelectorListener: OnKLineQuotaSelectorListener? = null
    private var type: SupportAccount? = null
    private val binding: ListTranferZhanghuBinding
    private var list: ArrayList<SupportAccount?>? = null

    init {
        val inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater, R.layout.list_tranfer_zhanghu, null, false)
        val dm = activity.resources.displayMetrics
        margin = 5 * dm.density
        popupWindow = PopupWindow(binding?.root, (dm.widthPixels - 2 * margin).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener(this)
        binding.plan.setOnClickListener(this)
        binding.market.setOnClickListener(this)
        binding.limit.setOnClickListener(this)
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
                    this.type = list!![0]
                }

                R.id.plan -> {
                    binding.limit.isChecked = true
                    binding.plan.isChecked = false
                    binding.market.isChecked = true
                    this.type = list!![2]
                }

                R.id.market -> {
                    binding.plan.isChecked = true
                    binding.market.isChecked = false
                    binding.limit.isChecked = true
                    this.type = list!![1]
                }

            }
            dismiss()
        }




    private val isShowing: Boolean
        get() = popupWindow != null && popupWindow.isShowing

    private fun dismiss() {
        if (isShowing) {
            popupWindow!!.dismiss()
        }
    }

    fun show(view: View?, type: SupportAccount? ,data: ArrayList<SupportAccount?>) {
        this.type = null
        refreshViewType(type,data)
        popupWindow!!.showAsDropDown(view, margin.toInt(), margin.toInt())
    }

    private fun refreshViewType(type: SupportAccount? ,data: ArrayList<SupportAccount?>?) {
        list = data
        val size = data?.size
        if (size == 2){
            binding.plan.visibility = View.GONE
        }
        if (size == 1){
            binding.plan.visibility = View.GONE
            binding.two.visibility = View.GONE
        }
        for (i in data!!.indices){
            if (type == data[i]){
                this.type = type
                if (i == 0){
                    binding.limit.isChecked = false
                }
                if (i == 1){
                    binding.market.isChecked = false
                }
                if (i == 2){
                    binding.plan.isChecked = false
                }
            }
            if (i == 0){
                binding.limit.text = data[i]?.name
            }
            if (i == 1){
                binding.market.text = data[i]?.name
            }
            if (i == 2){
                binding.plan.text = data[i]?.name
            }
        }
    }

    fun setOnKLineQuotaSelectorListener(onKLineQuotaSelectorListener: OnKLineQuotaSelectorListener) {
        this.onKLineQuotaSelectorListener = onKLineQuotaSelectorListener
    }

    interface OnKLineQuotaSelectorListener {
        fun onSelect(type: SupportAccount?)
    }
}