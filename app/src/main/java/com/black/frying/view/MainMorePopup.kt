package com.black.frying.view

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.PaintDrawable
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ViewMainMoreBinding

class MainMorePopup(private val activity: Activity) : PopupWindow.OnDismissListener, View.OnClickListener {
    private val popupWindow: PopupWindow?
    private val dm: DisplayMetrics
    private var onMainMoreClickListener: OnMainMoreClickListener? = null
    private val binding: ViewMainMoreBinding

    init {
        val inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater, R.layout.view_main_more, null, false)
        dm = activity.resources.displayMetrics
        popupWindow = PopupWindow(binding?.root,dm.widthPixels, WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener(this)
        binding.cancel?.setOnClickListener(this)
        binding.notify?.setOnClickListener(this)
        binding.customService?.setOnClickListener(this)
        binding.doScan?.setOnClickListener(this)
        binding.flashExchange?.setOnClickListener(this)
        binding.informations?.setOnClickListener(this)
    }

    override fun onDismiss() {
        val lp = activity.window.attributes
        lp.alpha = 1f
        activity.window.attributes = lp
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.cancel -> onMainMoreClickListener?.onCancelClick(this)
            R.id.notify -> onMainMoreClickListener?.onNotifyClick(this)
            R.id.custom_service -> onMainMoreClickListener?.onCustomServiceClick(this)
            R.id.do_scan -> onMainMoreClickListener?.onDoScanClick(this)
            R.id.flash_exchange -> onMainMoreClickListener?.onFlashExchangeClick(this)
            R.id.informations -> onMainMoreClickListener?.onInformationClick(this)
        }
    }

    private val isShowing: Boolean
        get() = popupWindow != null && popupWindow.isShowing

    fun dismiss() {
        if (isShowing) {
            popupWindow?.dismiss()
        }
    }

    fun show(view: View) {
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
        popupWindow?.showAtLocation(activity.window.decorView, Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, 0)
    }

    fun setOnMainMoreClickListener(onMainMoreClickListener: OnMainMoreClickListener?): MainMorePopup {
        this.onMainMoreClickListener = onMainMoreClickListener
        return this
    }

    interface OnMainMoreClickListener {
        fun onCancelClick(mainMorePopup: MainMorePopup)
        fun onNotifyClick(mainMorePopup: MainMorePopup)
        fun onCustomServiceClick(mainMorePopup: MainMorePopup)
        fun onDoScanClick(mainMorePopup: MainMorePopup)
        fun onFlashExchangeClick(mainMorePopup: MainMorePopup)
        fun onInformationClick(mainMorePopup: MainMorePopup)
    }
}