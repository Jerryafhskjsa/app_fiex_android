package com.black.money.activity

import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.money.LoanRecord
import com.black.base.model.money.LoanRecordDetail
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.money.R
import com.black.money.databinding.ActivityLoanRecordDetailBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil

@Route(value = [RouterConstData.LOAN_ORDER_DETAIL], beforePath = RouterConstData.LOGIN)
class LoanRecordDetailActivity : BaseActionBarActivity(), View.OnClickListener {
    private var record: LoanRecord? = null

    private var binding: ActivityLoanRecordDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        record = intent.getParcelableExtra(ConstData.LOAN_RECORD)
        if (record == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_loan_record_detail)
        detail
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        val status = record!!.statusInt
        return if (status == 12) "爆仓详情" else if (status == 11) "还款详情" else "详情"
    }

    override fun onClick(v: View) {}
    private val detail: Unit
        get() {
            MoneyApiServiceHelper.getLoanRecordDetail(this, record!!.id, object : NormalCallback<HttpRequestResultData<LoanRecordDetail?>?>() {
                override fun callback(returnData: HttpRequestResultData<LoanRecordDetail?>?) {
                    if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                        showLoanRecordDetail(returnData.data)
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }

    private fun showLoanRecordDetail(loanRecordDetail: LoanRecordDetail?) {
        val status = loanRecordDetail?.statusInt ?: 0
        binding?.createDate?.text = if (loanRecordDetail?.createTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", loanRecordDetail.createTime!!)
        binding?.days?.text = String.format("%s 天", if (loanRecordDetail?.numberDays == null) nullAmount else NumberUtil.formatNumberNoGroup(loanRecordDetail.numberDays))
        binding?.rate?.text = String.format("%s%%", if (loanRecordDetail?.rate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loanRecordDetail.rate!! * 100, 2))
        binding?.mortgageAmount?.text = String.format("%s %s",
                if (loanRecordDetail?.mortgageAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecordDetail.mortgageAmount, 9, 0, 8),
                if (loanRecordDetail?.mortgageCoinType == null) nullAmount else loanRecordDetail.mortgageCoinType)
        binding?.loanAmount?.text = String.format("%s %s",
                if (loanRecordDetail?.borrowAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecordDetail.borrowAmount, 9, 0, 8),
                if (loanRecordDetail?.borrowCoinType == null) nullAmount else loanRecordDetail.borrowCoinType)
        if (loanRecordDetail?.endTime != null) {
            binding?.backDateLayout?.visibility = View.VISIBLE
            binding?.backDate?.text = if (loanRecordDetail.endTime == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd HH:mm:ss", loanRecordDetail.endTime!!)
        } else {
            binding?.backDateLayout?.visibility = View.GONE
        }
        binding?.interest?.text = String.format("%s %s",
                if (loanRecordDetail?.interest == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecordDetail.interest, 9, 0, 8),
                if (loanRecordDetail?.borrowCoinType == null) nullAmount else loanRecordDetail.borrowCoinType)
        if (status == 12) {
            binding?.backDateLayout?.visibility = View.VISIBLE
            binding?.backDateTitle?.text = "爆仓时间"
            binding?.explodeMortgagePriceLayout?.visibility = View.VISIBLE
            binding?.explodeMortgagePrice?.text = String.format("%s USDT", if (loanRecordDetail!!.burstMortgagePrice == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecordDetail.burstMortgagePrice, 9, 0, 8))
            binding?.explodeLoanPriceLayout?.visibility = View.VISIBLE
            binding?.explodeLoanPrice?.text = String.format("%s USDT", if (loanRecordDetail.burstBorrowPrice == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecordDetail.burstBorrowPrice, 9, 0, 8))
            binding?.explodeRateLayout?.visibility = View.VISIBLE
            binding?.explodeRate?.text = String.format("%s%%", if (loanRecordDetail.burstRiskRate == null) "0.00" else NumberUtil.formatNumberNoGroupHardScale(loanRecordDetail.burstRiskRate!! * 100, 2))
            binding?.explodeLayout?.visibility = View.VISIBLE
            binding?.explodeAmount?.text = String.format("%s %s",
                    if (loanRecordDetail.burstAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecordDetail.burstAmount, 9, 0, 8),
                    if (loanRecordDetail.mortgageCoinType == null) nullAmount else loanRecordDetail.mortgageCoinType)
            binding?.returnAmountLayout?.visibility = View.VISIBLE
            binding?.returnAmount?.text = String.format("%s %s",
                    if (loanRecordDetail.returnAmount == null) "0" else NumberUtil.formatNumberDynamicScaleNoGroup(loanRecordDetail.returnAmount, 9, 0, 8),
                    if (loanRecordDetail.mortgageCoinType == null) nullAmount else loanRecordDetail.mortgageCoinType)
            //            binding?.statusLayout?.setVisibility(View.VISIBLE);
//            binding?.status?.setText("爆仓");
        } else if (status == 11 || status == 4 || status == 5) {
            binding?.backDateLayout?.visibility = View.VISIBLE
            binding?.backDateTitle?.text = "还款时间"
            binding?.statusLayout?.visibility = View.VISIBLE
            binding?.status?.text = if (status == 4) "违约还款" else if (status == 5) "逾期还款" else "正常还款"
            if (status == 4) {
                binding?.breakLayout?.visibility = View.VISIBLE
                binding?.breakAmount?.text = String.format("%s %s",
                        NumberUtil.formatNumberDynamicScaleNoGroup(loanRecordDetail!!.defaultAmount, 9, 0, 8),
                        if (loanRecordDetail.borrowCoinType == null) nullAmount else loanRecordDetail.borrowCoinType)
                binding?.breakRateLayout?.visibility = View.VISIBLE
                binding?.breakRate?.text = String.format("%s%%", if (loanRecordDetail.defaultRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loanRecordDetail.defaultRate!! * 100, 2))
            }
        } else {
            binding?.interestTitle?.text = "已产生利息"
            binding?.riskRateLayout?.visibility = View.VISIBLE
            binding?.riskRate?.text = String.format("%s%%", if (loanRecordDetail?.burstRiskRate == null) nullAmount else NumberUtil.formatNumberNoGroupHardScale(loanRecordDetail.burstRiskRate!! * 100, 2))
        }
    }
}