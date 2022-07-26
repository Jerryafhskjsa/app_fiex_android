package com.black.base.view

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import com.black.base.R

class ConfirmDialog(context: Context, title: String?, message: CharSequence?, confirmCallback: OnConfirmCallback?) {
    private val alertDialog: Dialog?
    private var confirmCallback: OnConfirmCallback? = null
    val titleView: TextView
    val messageView: TextView
    private var btnCancel: TextView? = null
    private var btnConfirm: TextView? = null

    fun setTitleGravity(gravity: Int) {
        titleView.gravity = gravity
    }

    fun setMessageGravity(gravity: Int) {
        messageView.gravity = gravity
    }

    fun setCancelText(cancelText: String?) {
        btnCancel?.text = cancelText
    }

    fun setConfirmText(confirmText: String?) {
        btnConfirm?.text = confirmText
    }

    fun show() {
        if (alertDialog != null && !alertDialog.isShowing) {
            alertDialog.show()
        }
    }

    fun dismiss() {
        if (alertDialog != null && alertDialog.isShowing) {
            alertDialog.dismiss()
        }
    }

    fun setConfirmCallback(confirmCallback: OnConfirmCallback?) {
        this.confirmCallback = confirmCallback
    }

    interface OnConfirmCallback {
        fun onConfirmClick(confirmDialog: ConfirmDialog)
    }

    init {
        setConfirmCallback(confirmCallback)
        val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null)
        alertDialog = Dialog(context, R.style.AlertDialog)
        //        alertDialog.setContentView(contentView);
//                new AlertDialog.Builder(mActivity).setView(contentView).create();
//        int height = display.getHeight();
        val window = alertDialog.window
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
        val display = context.resources.displayMetrics
        val layoutParams = ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        alertDialog.setContentView(contentView, layoutParams)
        //        dialog.setContentView(viewDialog, layoutParams);
        titleView = contentView.findViewById(R.id.title)
        titleView.text = title ?: ""
        messageView = contentView.findViewById(R.id.message)
        messageView.text = message ?: ""
        contentView.findViewById<TextView>(R.id.btn_resume).also { btnConfirm = it }.setOnClickListener {
            if (this@ConfirmDialog.confirmCallback != null) {
                this@ConfirmDialog.confirmCallback?.onConfirmClick(this@ConfirmDialog)
            }
        }
        contentView.findViewById<TextView>(R.id.btn_cancel).also { btnCancel = it }.setOnClickListener { dismiss() }
    }
}