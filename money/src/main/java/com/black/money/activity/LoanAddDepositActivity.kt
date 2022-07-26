package com.black.money.activity

import android.app.Activity
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.model.HttpRequestResultString
import com.black.base.model.money.LoanRecord
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.view.ConfirmDialog
import com.black.base.view.ConfirmDialog.OnConfirmCallback
import com.black.money.R
import com.black.money.databinding.ActivityLoanAddDepositBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

@Route(value = [RouterConstData.LOAN_ADD_DEPOSIT], beforePath = RouterConstData.LOGIN)
class LoanAddDepositActivity : BaseActionBarActivity(), View.OnClickListener {
    private var loanRecord: LoanRecord? = null
    private var precision = 0

    private var binding: ActivityLoanAddDepositBinding? = null

    private var currentWallet: Wallet? = null
    private val watcher: TextWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            checkClickable()
        }

        override fun afterTextChanged(s: Editable) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loanRecord = intent.getParcelableExtra(ConstData.LOAN_RECORD)
        if (loanRecord == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_loan_add_deposit)
        binding?.addAmount?.addTextChangedListener(watcher)
        binding?.addAmount?.filters = arrayOf(NumberFilter(), PointLengthFilter(loanRecord!!.precision))
        binding?.coinType?.text = loanRecord!!.mortgageCoinType
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
        binding?.btnResume?.setOnClickListener(this)
        wallet
        checkClickable()
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "追加保证金"
    }

    override fun onClick(v: View) {
        if (v.id == R.id.btn_resume) {
            val mortgageAmountText = binding?.addAmount?.text.toString().trim { it <= ' ' }
            val mortgageAmount = CommonUtil.parseDouble(mortgageAmountText)
            if (currentWallet != null && currentWallet!!.coinAmount != null && mortgageAmount != null && currentWallet!!.coinAmount!! >= BigDecimal(mortgageAmount)) {
                ConfirmDialog(this,
                        "提示",
                        String.format("确定增加 %s %s 保证金吗？",
                                mortgageAmountText,
                                if (loanRecord == null || loanRecord!!.mortgageCoinType == null) nullAmount else loanRecord!!.mortgageCoinType),
                        object : OnConfirmCallback {
                            override fun onConfirmClick(confirmDialog: ConfirmDialog) {
                                MoneyApiServiceHelper.addLoanDeposit(mContext, loanRecord!!.id, mortgageAmountText, object : NormalCallback<HttpRequestResultString?>() {
                                    override fun error(type: Int, error: Any?) {
                                        super.error(type, error)
                                        if (type == ConstData.ERROR_MISS_MONEY_PASSWORD) {
                                            confirmDialog.dismiss()
                                        }
                                    }

                                    override fun callback(returnData: HttpRequestResultString?) {
                                        confirmDialog.dismiss()
                                        if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                                            FryingUtil.showToast(mContext, "追加保证金成功")
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

    private fun checkClickable() {
        val mortgageAmountText = binding?.addAmount?.text.toString().trim { it <= ' ' }
        val mortgageAmount = CommonUtil.parseDouble(mortgageAmountText)
        binding?.btnResume?.isEnabled = mortgageAmount != null
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
                        if (wallet != null && TextUtils.equals(wallet.coinType, loanRecord!!.mortgageCoinType)) {
                            currentWallet = wallet
                            binding?.usableAmount?.text = String.format("%s %s",
                                    NumberUtil.formatNumberNoGroup(wallet.coinAmount, RoundingMode.FLOOR, 0, 8),
                                    if (loanRecord == null || loanRecord!!.mortgageCoinType == null) nullAmount else loanRecord!!.mortgageCoinType)
                            return
                        }
                    }
                }
            })
        }
}