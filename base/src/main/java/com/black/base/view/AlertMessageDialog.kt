package com.black.base.view

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.black.base.R

class AlertMessageDialog(context: Context, title: String?, message: CharSequence?) {
    private val alertDialog: Dialog?

    init {
        val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_alert_message, null)
        alertDialog = Dialog(context, R.style.AlertDialog)
        alertDialog.setContentView(contentView)
        val titleView = contentView.findViewById<TextView>(R.id.title)
        titleView.text = title ?: ""
        val messageView = contentView.findViewById<TextView>(R.id.message)
        messageView.text = message ?: ""
        contentView.findViewById<View>(R.id.btn_resume).setOnClickListener { dismiss() }
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
}