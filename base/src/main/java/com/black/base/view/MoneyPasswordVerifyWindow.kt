package com.black.base.view

import android.app.Activity
import android.graphics.drawable.PaintDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.black.base.R
import com.black.base.util.ConstData
import com.black.base.util.RouterConstData
import com.black.router.BlackRouter
import skin.support.content.res.SkinCompatResources

//资金密码输入框
class MoneyPasswordVerifyWindow(protected var activity: Activity, onMoneyPasswordListener: OnMoneyPasswordListener?) : PopupWindow.OnDismissListener, View.OnClickListener {
    protected var inflater: LayoutInflater
    private val popupWindow: PopupWindow?
    private val onMoneyPasswordListener: OnMoneyPasswordListener?
    private val titleView: TextView
    private val moneyPasswordEditText: EditText
    private val missSwitch: SwitchCompat
    private var dismissType = 0

    init {
        inflater = LayoutInflater.from(activity)
        this.onMoneyPasswordListener = onMoneyPasswordListener
        val dm = activity.resources.displayMetrics
        val contentView = inflater.inflate(R.layout.view_money_password_input, null)
        popupWindow = PopupWindow(contentView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(PaintDrawable())
        popupWindow.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        popupWindow.animationStyle = R.style.anim_bottom_in_out
        popupWindow.setOnDismissListener(this)
        titleView = contentView.findViewById(R.id.title)
        moneyPasswordEditText = contentView.findViewById(R.id.money_password)
        missSwitch = contentView.findViewById(R.id.miss)
        missSwitch.trackDrawable = SkinCompatResources.getDrawable(activity, R.drawable.bg_switch_track)
        missSwitch.thumbDrawable = SkinCompatResources.getDrawable(activity, R.drawable.icon_switch_thumb)
        contentView.findViewById<View>(R.id.forget_money_password).setOnClickListener(this)
        contentView.findViewById<View>(R.id.btn_commit).setOnClickListener(this)
    }

    override fun onDismiss() {
        val lp = activity.window.attributes
        lp.alpha = 1f
        activity.window.attributes = lp
        onMoneyPasswordListener?.onDismiss(this, dismissType)
    }

    override fun onClick(v: View) {
        val id = v.id
        if (id == R.id.forget_money_password) {
            val bundle = Bundle()
            bundle.putInt(ConstData.MONEY_PASSWORD_TYPE, ConstData.MONEY_PASSWORDR_RESET)
            BlackRouter.getInstance().build(RouterConstData.MONEY_PASSWORD).with(bundle).go(activity)
        } else if (id == R.id.btn_commit) {
            onMoneyPasswordListener?.onReturn(this, moneyPasswordEditText.text.toString(), missSwitch.isChecked)
        }
    }

    val isShowing: Boolean
        get() = popupWindow != null && popupWindow.isShowing

    fun show() {
        popupWindow!!.showAtLocation(activity.window.decorView, Gravity.BOTTOM, 0, 0)
        val lp = activity.window.attributes
        lp.alpha = 0.6f
        activity.window.attributes = lp
    }

    fun dismiss() {
        dismissType = 1
        if (isShowing) {
            popupWindow!!.dismiss()
        }
    }

    interface OnMoneyPasswordListener {
        fun onReturn(window: MoneyPasswordVerifyWindow, moneyPassword: String?, timeMill: Boolean)
        fun onDismiss(window: MoneyPasswordVerifyWindow, dismissType: Int)
    }
}