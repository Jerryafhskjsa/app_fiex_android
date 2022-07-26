package com.black.community.view

import android.app.Dialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.listener.OnHandlerListener
import com.black.base.model.community.FactionConfig
import com.black.base.model.wallet.Wallet
import com.black.community.R
import com.black.community.databinding.DialogFactionAddCoinBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import java.math.RoundingMode

class FactionAddCoinWidget(context: Context, private val wallet: Wallet, private val factionConfig: FactionConfig) {
    private val alertDialog: Dialog?

    private var binding: DialogFactionAddCoinBinding? = null

    private var onHandlerListener: OnHandlerListener<FactionAddCoinWidget>? = null

    init {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_faction_add_coin, null, false)
        alertDialog = Dialog(context, R.style.AlertDialog)
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
        alertDialog.setContentView(binding?.root!!, layoutParams)
        alertDialog.setCancelable(false)
        initViews()
    }

    fun initViews() {
        binding?.editText?.filters = arrayOf(NumberFilter(), PointLengthFilter(factionConfig.precision
                ?: 4))
        binding?.coinType?.setText(if (wallet.coinType == null) "" else wallet.coinType)
        binding?.usable?.setText(String.format("可用 %s %s", NumberUtil.formatNumberNoGroup(wallet.coinAmount, RoundingMode.FLOOR, 0, 8), if (wallet.coinType == null) "" else wallet.coinType))
        binding?.btnCancel?.setOnClickListener {
            if (onHandlerListener != null) {
                onHandlerListener!!.onCancel(this@FactionAddCoinWidget)
            }
        }
        binding?.btnBuyConfirm?.setOnClickListener {
            if (onHandlerListener != null) {
                onHandlerListener!!.onConfirm(this@FactionAddCoinWidget)
            }
        }
    }

    fun setTitle(title: String?) {
        binding?.title?.text = title
    }

    fun setAmountTitle(title: String?) {
        binding?.amountTitle?.text = title
    }

    fun setAmountHint(hint: String?) {
        binding?.editText?.hint = hint
    }

    val amount: Double?
        get() = CommonUtil.parseDouble(binding?.editText?.text.toString())

    val amountText: String
        get() = binding?.editText?.text.toString()

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

    fun setOnHandlerListener(onHandlerListener: OnHandlerListener<FactionAddCoinWidget>?) {
        this.onHandlerListener = onHandlerListener
    }
}