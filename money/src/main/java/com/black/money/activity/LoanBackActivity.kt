package com.black.money.activity

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.model.HttpRequestResultString
import com.black.base.model.money.LoanRecord
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.money.R
import com.black.money.databinding.ActivityLoanBackBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Route(value = [RouterConstData.LOAN_BACK], beforePath = RouterConstData.LOGIN)
class LoanBackActivity : BaseActionBarActivity(), View.OnClickListener {
    private var loanRecord: LoanRecord? = null
    private var precision = 0
    private var backTotalAmount: Double = 0.0
    private var currentWallet: Wallet? = null

    private var binding: ActivityLoanBackBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loanRecord = intent.getParcelableExtra(ConstData.LOAN_RECORD)
        if (loanRecord == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_loan_back)
        precision = loanRecord!!.precision
        binding?.createDate?.text = if (loanRecord == null || loanRecord!!.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", loanRecord!!.createTime!!)
        binding?.lastBackDate?.text = if (loanRecord == null || loanRecord!!.expireTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", loanRecord!!.expireTime!!)
        binding?.days?.text = String.format("%s 天", if (loanRecord == null || loanRecord!!.numberDays == null) nullAmount else NumberUtil.formatNumberNoGroup(loanRecord!!.numberDays))
        binding?.mortgageAmount?.text = String.format("%s %s",
                if (loanRecord == null || loanRecord!!.mortgageAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecord!!.mortgageAmount, 9, 0, 8),
                if (loanRecord == null || loanRecord!!.mortgageCoinType == null) nullAmount else loanRecord!!.mortgageCoinType)
        binding?.loanAmount?.text = String.format("%s %s",
                if (loanRecord == null || loanRecord!!.borrowAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecord!!.borrowAmount, 9, 0, 8),
                if (loanRecord == null || loanRecord!!.borrowCoinType == null) nullAmount else loanRecord!!.borrowCoinType)
        binding?.rate?.text = String.format("%s%%", if (loanRecord == null || loanRecord!!.rate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loanRecord!!.rate!! * 100, 2))
        binding?.interest?.text = String.format("%s %s",
                if (loanRecord == null || loanRecord!!.interest == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecord!!.interest, 9, 0, 8),
                if (loanRecord == null || loanRecord!!.borrowCoinType == null) nullAmount else loanRecord!!.borrowCoinType)
        binding?.riskRate?.text = String.format("%s%%", if (loanRecord == null || loanRecord!!.riskRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loanRecord!!.riskRate!! * 100, 2))
        var breakAmount: Double? = null
        val status = loanRecord!!.statusInt
        if (status == 1) {
            binding?.breakRateLayout?.visibility = View.VISIBLE
            binding?.breakLayout?.visibility = View.VISIBLE
            binding?.breakRate?.text = String.format("%s%%", if (loanRecord == null || loanRecord!!.defaultRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loanRecord!!.defaultRate!! * 100, 2))
            breakAmount = if (loanRecord == null || loanRecord!!.borrowAmount == null || loanRecord!!.defaultRate == null) null else loanRecord!!.borrowAmount!! * loanRecord!!.defaultRate!!
            binding?.breakAmount?.text = String.format("%s %s",
                    if (breakAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(breakAmount, 9, 0, 8),
                    if (loanRecord == null || loanRecord!!.borrowCoinType == null) nullAmount else loanRecord!!.borrowCoinType)
        } else {
            binding?.breakRateLayout?.visibility = View.GONE
            binding?.breakLayout?.visibility = View.GONE
        }
        backTotalAmount = 0.0
        backTotalAmount += (loanRecord?.borrowAmount ?: 0.0)
        backTotalAmount += (loanRecord?.interest ?: 0.0)
        backTotalAmount += breakAmount ?: 0.toDouble()

        binding?.backTotalAmount?.text = String.format("%s %s",
                NumberUtil.formatNumberDynamicScaleNoGroup(backTotalAmount, 9, 0, 8),
                if (loanRecord == null || loanRecord!!.borrowCoinType == null) nullAmount else loanRecord!!.borrowCoinType)
        findViewById<View>(R.id.btn_resume).setOnClickListener(this)
        wallet
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "还款"
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_resume) {
            if (currentWallet != null && currentWallet!!.coinAmount != null && currentWallet!!.coinAmount!! >= BigDecimal(backTotalAmount)) {
                ConfirmDialog(this,
                        "提示",
                        String.format("确定总共支付 %s %s，偿还该笔贷款吗？",
                                NumberUtil.formatNumberDynamicScaleNoGroup(backTotalAmount, 9, 0, 8),
                                if (loanRecord == null || loanRecord!!.borrowCoinType == null) nullAmount else loanRecord!!.borrowCoinType),
                        object : OnConfirmCallback {
                            override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                                MoneyApiServiceHelper.backLoan(mContext, loanRecord!!.id, object : NormalCallback<HttpRequestResultString?>() {
                                    override fun error(type: Int, error: Any?) {
                                        super.error(type, error)
                                        if (type == ConstData.ERROR_MISS_MONEY_PASSWORD) {
                                            confirmDialog.dismiss()
                                        }
                                    }

                                    override fun callback(returnData: HttpRequestResultString?) {
                                        confirmDialog.dismiss()
                                        if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                                            FryingUtil.showToast(mContext, "还贷成功")
                                            setResult(Activity.RESULT_OK)
                                            finish()
                                        } else {
                                            FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                                        }
                                    }
                                })
                            }
                        })
                        .show()
            } else {
                FryingUtil.showToast(mContext, "余额不足")
            }
        }
    }

    private val wallet: Unit
        get() {
            WalletApiServiceHelper.getWalletList(mContext, false, object : Callback<ArrayList<Wallet?>?>() {
                override fun error(type: Int, error: Any) {}
                override fun callback(returnData: ArrayList<Wallet?>?) {
                    if (returnData == null || returnData.isEmpty()) {
                        return
                    }
                    for (wallet in returnData) {
                        if (wallet != null && TextUtils.equals(wallet.coinType, loanRecord!!.borrowCoinType)) {
                            currentWallet = wallet
                            binding?.usableAmount?.text = String.format("%s %s",
                                    NumberUtil.formatNumberNoGroup(wallet.coinAmount, RoundingMode.FLOOR, 0, 8),
                                    if (loanRecord == null || loanRecord!!.borrowCoinType == null) nullAmount else loanRecord!!.borrowCoinType)
                            return
                        }
                    }
                }
            })
        }
}
