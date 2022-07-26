package com.black.money.activity

import android.graphics.Typeface
import android.os.Bundle
import android.os.Parcelable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.AbsoluteSizeSpan
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.model.HttpRequestResultData
import com.black.base.model.money.CloudPowerPersonHold
import com.black.base.util.RouterConstData
import com.black.money.R
import com.black.money.databinding.ActivityCloudPowerRecordBinding
import com.black.money.fragment.CloudPowerBuyRecordFragment
import com.black.money.fragment.CloudPowerHoldFragment
import com.black.money.fragment.CloudPowerRewardFragment
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.google.android.material.tabs.TabLayout
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.CLOUD_POWER_RECORD], beforePath = RouterConstData.LOGIN)
class CloudPowerRecordActivity : BaseActionBarActivity() {
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(3) //标题
    }

    private var binding: ActivityCloudPowerRecordBinding? = null

    private var fragmentList: MutableList<Fragment>? = null

    private var holdFragment = CloudPowerHoldFragment()
    private var buyRecordFragment = CloudPowerBuyRecordFragment()
    private var rewardFragment = CloudPowerRewardFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TAB_TITLES[0] = "持仓算力"
        TAB_TITLES[1] = "申购记录"
        TAB_TITLES[2] = "收益记录"

        binding = DataBindingUtil.setContentView(this, R.layout.activity_cloud_power_record)

        showBtcIncome(null)
        binding?.tabLayout?.setTabTextColors(SkinCompatResources.getColor(this, R.color.T2), SkinCompatResources.getColor(this, R.color.C1))
        binding?.tabLayout?.setSelectedTabIndicatorHeight(0)
        binding?.tabLayout?.tabMode = TabLayout.MODE_FIXED
        for (i in TAB_TITLES.indices) {
            val tab = binding?.tabLayout?.newTab()?.setText(TAB_TITLES[i])
            tab?.setCustomView(R.layout.view_tab_normal)
            val textView = tab?.customView!!.findViewById<View>(android.R.id.text1) as TextView
            textView.text = TAB_TITLES[i]
            binding?.tabLayout?.addTab(tab)
        }
        binding?.tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val view = tab.customView
                val textView = if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                if (textView != null) {
                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD)
                    textView.postInvalidate()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val view = tab.customView
                val textView = if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                if (textView != null) {
                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL)
                    textView.postInvalidate()
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        initFragmentList()
        binding?.viewPager?.adapter = object : FragmentStatePagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                return fragmentList!![position]
            }

            override fun getCount(): Int {
                return fragmentList!!.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return TAB_TITLES[position]
            }

            override fun restoreState(state: Parcelable?, loader: ClassLoader?) {}
        }
        CommonUtil.joinTabLayoutViewPager(binding?.tabLayout, binding?.viewPager)
    }

    override fun isStatusBarDark(): Boolean {
        return false
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_left_back_text
    }

    override fun initActionBarView(view: View) {
        view.setBackgroundColor(SkinCompatResources.getColor(this, R.color.C1))
        val btnBack = view.findViewById<ImageButton>(R.id.action_bar_back)
        btnBack.setImageDrawable(SkinCompatResources.getDrawable(this, R.drawable.btn_back_white))
    }

    override fun onResume() {
        super.onResume()
        cloudPowerBtcIncome
        cloudPowerPersonHold
    }

    private fun initFragmentList() {
        if (fragmentList == null) {
            fragmentList = ArrayList()
        }
        fragmentList!!.clear()
        fragmentList!!.add(CloudPowerHoldFragment().also { holdFragment = it })
        fragmentList!!.add(CloudPowerBuyRecordFragment().also { buyRecordFragment = it })
        fragmentList!!.add(CloudPowerRewardFragment().also { rewardFragment = it })
    }

    private val cloudPowerBtcIncome: Unit
        get() {
            MoneyApiServiceHelper.getCloudPowerBtcIncome(this, object : NormalCallback<HttpRequestResultData<Double?>?>() {
                override fun error(type: Int, error: Any?) {
                    showBtcIncome(null)
                }

                override fun callback(returnData: HttpRequestResultData<Double?>?) {
                    if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                        showBtcIncome(returnData.data)
                    } else {
                        showBtcIncome(null)
                    }
                }
            })
        }

    private val cloudPowerPersonHold: Unit
        get() {
            MoneyApiServiceHelper.getCloudPowerPersonHold(this, object : NormalCallback<HttpRequestResultData<CloudPowerPersonHold?>?>() {
                override fun error(type: Int, error: Any?) {
                    showPersonHold(null)
                }

                override fun callback(returnData: HttpRequestResultData<CloudPowerPersonHold?>?) {
                    if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                        showPersonHold(returnData.data)
                    } else {
                        showPersonHold(null)
                    }
                }
            })
        }

    private fun showBtcIncome(btcIncome: Double?) {
        binding?.btcIncome?.text = String.format("理论收益：%s BTC/T/天，数据来自 BTC.COM", if (btcIncome == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(btcIncome, 9, 0, 8))
    }

    private fun showPersonHold(personHold: CloudPowerPersonHold?) {
        val holdAmount = if (personHold?.totalHoldCoinAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(personHold.totalHoldCoinAmount, 9, 0, 8)
        val holdAmountUnit = " TH/S"
        val holeAmountString = holdAmount + holdAmountUnit
        val holdSpan = SpannableStringBuilder(holeAmountString)
        holdSpan.setSpan(AbsoluteSizeSpan(16, true), holdAmount.length, holeAmountString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding?.hold?.text = holdSpan
        val interestAmount = if (personHold?.totalInterestAmount == null) nullAmount else "+" + NumberUtil.formatNumberDynamicScaleNoGroup(personHold.totalInterestAmount, 9, 0, 8)
        val interestAmountUnit = " BTC"
        val interestAmountString = interestAmount + interestAmountUnit
        val interestSpan = SpannableStringBuilder(interestAmountString)
        interestSpan.setSpan(AbsoluteSizeSpan(16, true), interestAmount.length, interestAmountString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        binding?.interest?.text = interestSpan
    }
}