package com.black.money.view

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.*
import androidx.databinding.DataBindingUtil
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.lib.FryingSingleToast
import com.black.base.listener.OnHandlerListener
import com.black.base.model.money.Demand
import com.black.base.model.wallet.Wallet
import com.black.base.util.*
import com.black.money.R
import com.black.money.databinding.DialogDemandChangeInBinding
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

//活期宝转入
class DemandChangeInWidget(private val context: Context, private val wallet: Wallet, private val demand: Demand) {
    private val nullAmount: String = context.resources.getString(R.string.number_default)
    private val imageLoader: ImageLoader = ImageLoader(context)
    private val precision: Int = demand.precision ?: 0
    private val alertDialog: Dialog?

    private var binding: DialogDemandChangeInBinding? = null
    private var onHandlerListener: OnHandlerListener<DemandChangeInWidget>? = null

    init {
        binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_demand_change_in, null, false)
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

    private fun initViews() {
        FryingUtil.setCoinIcon(context, binding?.icon, imageLoader, demand.coinType)
        binding?.editText?.hint = String.format("请输入存入金额,最小%s", NumberUtil.formatNumberNoGroup(demand.minAmount, 0, precision))
        binding?.editText?.filters = arrayOf(NumberFilter(), PointLengthFilter(precision))
        binding?.allIn?.setOnClickListener {
            val maxInAmount: BigDecimal = if (wallet.coinAmount == null || demand.maxAmount == null) BigDecimal.ZERO else if (wallet.coinAmount!! > BigDecimal(demand.maxAmount!!)) BigDecimal(demand.maxAmount!!) else wallet.coinAmount!!
            binding?.editText?.setText(NumberUtil.formatNumberNoGroupScale(maxInAmount, RoundingMode.FLOOR, 0, precision))
        }
        binding?.coinType?.setText(String.format("存入%s", if (demand.coinType == null) nullAmount else demand.coinType))
        binding?.usable?.setText(String.format("可用 %s %s", NumberUtil.formatNumberNoGroupScale(wallet.coinAmount, RoundingMode.FLOOR, 0, precision), if (demand.coinType == null) nullAmount else demand.coinType))
        val actionDate = System.currentTimeMillis()
        val now = Calendar.getInstance()
        //        int thisHour = now.get(Calendar.HOUR_OF_DAY);
        val oneDayHour = 24 * 3600 * 1000
        //        if (thisHour >= 20) {
//            actionDate = actionDate + oneDayHour;
//        }
        val actionDateString = CommonUtil.formatTimestamp("MM月dd日", actionDate + oneDayHour)
        val comeDateString = CommonUtil.formatTimestamp("MM月dd日", actionDate + oneDayHour * 2)
        binding?.hint?.setText(String.format("%s产生收益，如20:00点后存入，%s到账", actionDateString, comeDateString))
        val color = SkinCompatResources.getColor(context, R.color.T7)
        val agreementText = "我已认真阅读并同意 <a href=\"" + UrlConfig.getUrlDemandProfile(context) + "\">《聚宝盆服务协议》</a>"
        val agreementTextSpanned: Spanned?
        agreementTextSpanned = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(agreementText, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(agreementText)
        }
        binding?.demandAgreementCheck?.text = agreementTextSpanned
        binding?.demandAgreementCheck?.movementMethod = BlackLinkMovementMethod(BlackLinkClickListener("聚宝盆服务协议"))
        binding?.demandAgreementCheck?.setLinkTextColor(color)
        binding?.demandAgreementCheck?.isChecked = true
        binding?.btnCancel?.setOnClickListener {
            if (onHandlerListener != null) {
                onHandlerListener!!.onCancel(this@DemandChangeInWidget)
            }
        }
        binding?.btnBuyConfirm?.setOnClickListener(View.OnClickListener {
            if (onHandlerListener != null) {
                if (binding?.demandAgreementCheck?.isChecked != true) {
                    FryingUtil.showToast(context, "存入聚宝盆，请阅读并同意《聚宝盆服务协议》", FryingSingleToast.ERROR)
                } else {
                    var amount = amount
                    amount = amount ?: 0.toDouble()
                    val min: Double = demand.minAmount ?: 0.0
                    val max: Double = demand.maxAmount ?: 0.0
                    if (amount < min) {
                        FryingUtil.showToast(context, String.format("数量不能小于单笔最小存币额%s %s", NumberUtil.formatNumberNoGroup(min, 0, precision), if (demand.coinType == null) nullAmount else demand.coinType))
                        return@OnClickListener
                    }
//                    if (amount > max) {
//                        FryingUtil.showToast(context, String.format("数量不能大于单笔最大存币额%s %s", CommonUtil.formatNumberNoGroup(max, 0, precision), if (demand.coinType == null) nullAmount else demand.coinType))
//                        return@OnClickListener
//                    }
                    onHandlerListener!!.onConfirm(this@DemandChangeInWidget)
                }
            }
        })
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

    fun setOnHandlerListener(onHandlerListener: OnHandlerListener<DemandChangeInWidget>?) {
        this.onHandlerListener = onHandlerListener
    }
}