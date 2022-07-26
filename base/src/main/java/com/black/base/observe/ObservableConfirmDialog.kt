package com.black.base.observe

import android.app.Dialog
import android.content.Context
import android.view.*
import android.widget.TextView
import com.black.base.R
import io.reactivex.ObservableEmitter

class ObservableConfirmDialog(context: Context, title: String?, message: CharSequence?) : ObservableBaseDialog<ObservableConfirmDialog?>() {
    private val alertDialog: Dialog?
    val titleView: TextView
    val messageView: TextView
    private var btnCancel: TextView? = null
    private var btnConfirm: TextView? = null
    private val context: Context

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

    override fun show(): ObservableConfirmDialog { //Log.e("ObservableConfirmDialog", "show:");
        alertDialog?.show()
        return this
    }

    fun dismiss() { //Log.e("ObservableConfirmDialog", "dismiss:");
        if (alertDialog != null && alertDialog.isShowing) {
            alertDialog.dismiss()
        }
    }

    operator fun next() { //Log.e("ObservableConfirmDialog", "next:");
    }

    fun cancel() { //Log.e("ObservableConfirmDialog", "cancel:");
        dismiss()
    }

    protected override fun bindEvents(emitter: ObservableEmitter<ObservableConfirmDialog?>?) {
        if (btnConfirm != null) {
            btnConfirm?.setOnClickListener(View.OnClickListener { emitter?.onNext(this@ObservableConfirmDialog) })
        }
        if (btnCancel != null) {
            btnCancel?.setOnClickListener(View.OnClickListener {
                dismiss()
                emitter?.onComplete()
            })
        }
    }

    init {
        dialog = this
        this.context = context
        //Log.e("ObservableConfirmDialog", "init:" + 1 + Thread.currentThread());
        val contentView = LayoutInflater.from(context).inflate(R.layout.dialog_confirm, null)
        //Log.e("ObservableConfirmDialog", "init:" + 1.5);
        alertDialog = Dialog(context, R.style.AlertDialog)
        //Log.e("ObservableConfirmDialog", "init:" + 2);
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
        contentView.findViewById<TextView>(R.id.btn_resume).also { btnConfirm = it }.setOnClickListener { next() }
        contentView.findViewById<TextView>(R.id.btn_cancel).also { btnCancel = it }.setOnClickListener { cancel() }
        //Log.e("ObservableConfirmDialog", "init:" + 3);
    }
}
