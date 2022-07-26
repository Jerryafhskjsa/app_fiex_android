package com.black.base.lib

import android.app.Activity
import android.app.Dialog
import android.view.*
import android.widget.TextView
import com.black.base.R

class PopupMessageWindow(activity: Activity?, title: String?, message: String?) {
    private var alertDialog: Dialog? = null
    fun show() {
        if (alertDialog != null && !alertDialog!!.isShowing) {
            alertDialog!!.show()
        }
    }

    init {
        if (activity != null) {
            val contentView = LayoutInflater.from(activity).inflate(R.layout.dialog_popup_message, null)
            alertDialog = Dialog(activity, R.style.AlertDialog)
            val window = alertDialog?.window
            if (window != null) {
                val params = window.attributes
                //设置背景昏暗度
                params.dimAmount = 0.2f
                params.gravity = Gravity.BOTTOM
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                params.height = WindowManager.LayoutParams.WRAP_CONTENT
                //设置dialog动画
                window.setWindowAnimations(R.style.anim_bottom_in_out)
                window.attributes = params
            }
            //设置dialog的宽高为屏幕的宽高
            val display = activity.resources.displayMetrics
            val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
            alertDialog?.setContentView(contentView, layoutParams)
            //        dialog.setContentView(viewDialog, layoutParams);
            contentView.findViewById<View>(R.id.btn_resume).setOnClickListener { alertDialog?.dismiss() }
            val titleView = contentView.findViewById<TextView>(R.id.title)
            titleView.text = title ?: ""
            val messageView = contentView.findViewById<TextView>(R.id.message)
            messageView.text = message ?: ""
        }
    }
}