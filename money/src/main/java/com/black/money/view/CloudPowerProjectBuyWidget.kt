package com.black.money.view

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.text.Editable
import android.text.Html
import android.text.Spanned
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.lib.FryingSingleToast
import com.black.base.listener.OnHandlerListener
import com.black.base.model.money.CloudPowerProject
import com.black.base.model.wallet.Wallet
import com.black.base.util.*
import com.black.money.R
import com.black.money.databinding.DialogCloudPowerProjectBuyBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.math.RoundingMode

class CloudPowerProjectBuyWidget(private val context: Context, private val wallet: Wallet, private val project: CloudPowerProject) {
    private val nullAmount: String = context.resources.getString(R.string.number_default)
    private val imageLoader: ImageLoader = ImageLoader(context)
    private val price: Double = project.price ?: 0.0
    private val electricityBill: Double = project.electricityBill ?: 0.0
    private val days: Int = project.day ?: 0
    private val binding: DialogCloudPowerProjectBuyBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_cloud_power_project_buy, null, false)

    private val alertDialog: Dialog?
    private var totalPay = 0.0
    private var onHandlerListener: OnHandlerListener<CloudPowerProjectBuyWidget>? = null

    init {
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
        alertDialog.setContentView(binding.root, layoutParams)
        alertDialog.setCancelable(false)
        initViews()
    }

    fun initViews() {
        FryingUtil.setCoinIcon(context, binding.icon, imageLoader, project.interestCoinType)
        binding.coinType.text = String.format("购买算力代币 %s", if (project.distributionCoinType == null) nullAmount else project.distributionCoinType)
        binding.price.text = String.format("1 %s = %s %s",
                if (project.distributionCoinType == null) nullAmount else project.distributionCoinType,
                NumberUtil.formatNumberDynamicScaleNoGroup(price * days, 9, 0, 8),
                if (wallet.coinType == null) nullAmount else wallet.coinType)
        binding.editText.filters = arrayOf(NumberFilter(), PointLengthFilter(8))
        binding.editText.hint = String.format("%s %s 起购",
                NumberUtil.formatNumberDynamicScaleNoGroup(project.buyMinNum, 9, 0, 8),
                if (project.distributionCoinType == null) nullAmount else project.distributionCoinType)
        binding.editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                val amount = amount
                totalPay = if (amount == null) {
                    0.0
                } else {
                    amount * price * days
                }
                refreshTotalPay()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding.useAmount?.text = String.format("可用 %s %s", NumberUtil.formatNumberNoGroup(wallet.coinAmount, RoundingMode.FLOOR, 0, 8), if (wallet.coinType == null) nullAmount else wallet.coinType)
        refreshTotalPay()
        binding.hint?.setText(String.format("1 %s = 1T算力，代币作为收益凭证，可自由交易\n单价 = 每天算力成本*挖矿周期", if (project.distributionCoinType == null) nullAmount else project.distributionCoinType))
        val color = SkinCompatResources.getColor(context, R.color.T7)
        val agreementText = "我已认真阅读并同意 <a href=\"" + UrlConfig.getUrlCloudPowerProfile(context) + "\">《云算力服务协议》</a>"
        val agreementTextSpanned: Spanned?
        agreementTextSpanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(agreementText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(agreementText)
        }
        binding.demandAgreementCheck?.text = agreementTextSpanned
        binding.demandAgreementCheck?.movementMethod = BlackLinkMovementMethod(BlackLinkClickListener("云算力服务协议"))
        binding.demandAgreementCheck?.setLinkTextColor(color)
        binding.demandAgreementCheck?.isChecked = true
        binding.btnCancel?.setOnClickListener {
            if (onHandlerListener != null) {
                onHandlerListener?.onCancel(this@CloudPowerProjectBuyWidget)
            }
        }
        binding.btnBuyConfirm?.setOnClickListener {
            if (onHandlerListener != null) {
                if (!binding.demandAgreementCheck.isChecked) {
                    FryingUtil.showToast(context, "购买算力代币，请阅读并同意《云算力服务协议》", FryingSingleToast.ERROR)
                } else {
                    var amount = amount
                    amount = amount ?: 0.0
                    onHandlerListener?.onConfirm(this@CloudPowerProjectBuyWidget)
                }
            }
        }
    }

    val amount: Double?
        get() = CommonUtil.parseDouble(binding.editText?.text.toString())

    val amountText: String
        get() = binding.editText?.text.toString()

    private fun refreshTotalPay() {
        binding.totalPay?.text = String.format("%s %s", NumberUtil.formatNumberNoGroup(totalPay, 0, 8), if (project.coinType == null) nullAmount else project.coinType)
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

    fun setOnHandlerListener(onHandlerListener: OnHandlerListener<CloudPowerProjectBuyWidget>?): CloudPowerProjectBuyWidget {
        this.onHandlerListener = onHandlerListener
        return this
    }
}