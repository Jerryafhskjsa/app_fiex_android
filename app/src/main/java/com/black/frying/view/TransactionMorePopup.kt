package com.black.frying.view

import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.text.TextUtils
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.CheckedTextView
import android.widget.PopupWindow
import androidx.databinding.DataBindingUtil
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ViewTransactionMoreBinding
import skin.support.content.res.SkinCompatResources

class TransactionMorePopup(private val activity: Activity, isCollect: Boolean, private val chatRoomId: String?) : PopupWindow.OnDismissListener, View.OnClickListener {
    private val popupWindow: PopupWindow?
    private val dm: DisplayMetrics
    private val margin: Float
    private var onTransactionMoreClickListener: OnTransactionMoreClickListener? = null
    private val binding: ViewTransactionMoreBinding

    init {
        val inflater = LayoutInflater.from(activity)
        binding = DataBindingUtil.inflate(inflater, R.layout.view_transaction_more, null, false)
        dm = activity.resources.displayMetrics
        margin = 5 * dm.density
        popupWindow = PopupWindow(binding?.root, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        val drawable = ColorDrawable(SkinCompatResources.getColor(activity, R.color.transparent))
        popupWindow.setBackgroundDrawable(drawable)
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.setOnDismissListener(this)
        binding.recharge?.setOnClickListener(this)
        binding.extract?.setOnClickListener(this)
        binding.bill?.setOnClickListener(this)
        binding.collect?.setOnClickListener(this)
        binding.collect?.isChecked = isCollect
        binding.chatRoom?.setOnClickListener(this)
        if (!TextUtils.isEmpty(chatRoomId)) {
            binding.chatRoom?.visibility = View.VISIBLE
        } else {
            binding.chatRoom?.visibility = View.GONE
        }
    }

    override fun onDismiss() {
        val lp = activity.window.attributes
        lp.alpha = 1f
        activity.window.attributes = lp
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.recharge -> onTransactionMoreClickListener?.onRechargeClick(this)
            R.id.extract -> onTransactionMoreClickListener?.onExtractClick(this)
            R.id.bill -> onTransactionMoreClickListener?.onBillClick(this)
            R.id.collect -> onTransactionMoreClickListener?.onCollectClick(this, binding.collect)
            R.id.chat_room -> onTransactionMoreClickListener?.onChatRoomClick(this, chatRoomId)
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
        CommonUtil.measureView(binding.root)
        val offsetX = view.width - binding.root.measuredWidth
        popupWindow?.showAsDropDown(view, offsetX, 0, Gravity.LEFT)
    }

    fun setOnTransactionMoreClickListener(onTransactionMoreClickListener: OnTransactionMoreClickListener?): TransactionMorePopup {
        this.onTransactionMoreClickListener = onTransactionMoreClickListener
        return this
    }

    interface OnTransactionMoreClickListener {
        fun onRechargeClick(transactionMorePopup: TransactionMorePopup)
        fun onExtractClick(transactionMorePopup: TransactionMorePopup)
        fun onBillClick(transactionMorePopup: TransactionMorePopup)
        fun onCollectClick(transactionMorePopup: TransactionMorePopup, btnCollect: CheckedTextView)
        fun onChatRoomClick(transactionMorePopup: TransactionMorePopup, chatRoomId: String?)
    }
}