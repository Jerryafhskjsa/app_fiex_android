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
    private var type: String? = null
    private val binding: ListTranferZhanghuBinding

    init {
        val inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater, R.layout.list_tranfer_zhanghu, null, false)
        val dm = activity.resources.displayMetrics
        margin = 5 * dm.density
        popupWindow = PopupWindow(binding?.root, (dm.widthPixels - 2 * margin).toInt(), WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener(this)
    }

    override fun onDismiss() {
        if (onKLineQuotaSelectorListener != null) {
            onKLineQuotaSelectorListener!!.onSelect(type)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {

        }
        getType()
        dismiss()
    }

    private fun getType() {
        this.type = type
    }

    private val isShowing: Boolean
        get() = popupWindow != null && popupWindow.isShowing

    private fun dismiss() {
        if (isShowing) {
            popupWindow!!.dismiss()
        }
    }

    fun show(view: View?, type: String ,data: ArrayList<SupportAccount?>) {
        this.type = null
        refreshView(type)
        refreshViewType(data)
        popupWindow!!.showAsDropDown(view, margin.toInt(), margin.toInt())
    }

    private fun refreshViewType(data: ArrayList<SupportAccount?>?) {
        val size = data?.size
        if (size == 2){
            binding.plan.visibility = View.GONE
        }
        if (size == 1){
            binding.plan.visibility = View.GONE
            binding.two.visibility = View.GONE
        }
    }

    private fun refreshView(type: String) {
    }

    fun setOnKLineQuotaSelectorListener(onKLineQuotaSelectorListener: OnKLineQuotaSelectorListener) {
        this.onKLineQuotaSelectorListener = onKLineQuotaSelectorListener
    }

    interface OnKLineQuotaSelectorListener {
        fun onSelect(type: String?)
    }
}