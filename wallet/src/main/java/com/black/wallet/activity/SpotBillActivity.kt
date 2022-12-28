package com.black.wallet.activity

import android.os.Bundle
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.activity.BaseActionBarActivity
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.router.annotation.Route
import com.black.wallet.R
import com.black.wallet.databinding.ActivitySpotBillBinding
import com.black.wallet.fragment.BillEmptyFragment
import com.black.wallet.fragment.FinancialExtractRecordFragment
import com.black.wallet.fragment.FinancialRechargeRecordFragment
import com.black.wallet.fragment.TransferBillFragment
import com.google.android.material.tabs.TabLayout

@Route(value = [RouterConstData.SPOT_BILL_ACTIVITY])
class SpotBillActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(4)
        private var TAB_RECHARGE: String? = null
        private var TAB_EXTRACT: String? = null
        private var TAB_EXCHANGE: String? = null
        private var TAB_TOTAL: String? = null
    }

    private var binding: ActivitySpotBillBinding? = null
    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var rechargeFragment: FinancialRechargeRecordFragment? = null
    private var extractFragment: FinancialExtractRecordFragment? = null
    private var transferFragment: TransferBillFragment? = null
    private var totalFragment: BillEmptyFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_spot_bill)
        getString(R.string.rechange).also {
            SpotBillActivity.TAB_RECHARGE = it
            SpotBillActivity.TAB_TITLES[0] = SpotBillActivity.TAB_RECHARGE
        }
        getString(R.string.extract).also {
            SpotBillActivity.TAB_EXTRACT = it
            SpotBillActivity.TAB_TITLES[1] = SpotBillActivity.TAB_EXTRACT
        }
        getString(R.string.exchange).also {
            SpotBillActivity.TAB_EXCHANGE = it
            SpotBillActivity.TAB_TITLES[2] = SpotBillActivity.TAB_EXCHANGE
        }

        getString(R.string.comprehensive_bill).also {
            SpotBillActivity.TAB_TOTAL = it
            SpotBillActivity.TAB_TITLES[3] = SpotBillActivity.TAB_TOTAL
        }

        binding?.tabLayout?.setSelectedTabIndicatorHeight(0)
        binding?.tabLayout?.tabMode = TabLayout.MODE_FIXED
        initFragmentList()

        binding?.viewPager?.adapter =
            object : FragmentStatePagerAdapter(supportFragmentManager) {
                override fun getItem(position: Int): Fragment {
                    return fragmentList!![position]
                }

                override fun getCount(): Int {
                    return fragmentList!!.size
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return SpotBillActivity.TAB_TITLES[position]
                }

                override fun restoreState(state: Parcelable?, loader: ClassLoader?) {

                }
            }
        binding?.tabLayout?.setupWithViewPager(binding?.viewPager, true)

        for (i in SpotBillActivity.TAB_TITLES.indices) {
            try {
                val tab: TabLayout.Tab? = binding?.tabLayout?.getTabAt(i)
                if (tab != null) {
                    tab.setCustomView(R.layout.view_tab_normal)
                    if (tab.customView != null) {
                        val textView =
                            tab.customView!!.findViewById<View>(android.R.id.text1) as TextView
                        textView.text = SpotBillActivity.TAB_TITLES[i]
                    }
                }
            } catch (throwable: Throwable) {
                FryingUtil.printError(throwable)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
        }
    }
    override fun getTitleText(): String {
        return getString(R.string.history_record)
    }
    private fun initFragmentList() {
        if (fragmentList == null) {
            fragmentList = java.util.ArrayList()
        }
        fragmentList?.clear()
        fragmentList?.add(FinancialRechargeRecordFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
            rechargeFragment = it

        })
        fragmentList?.add(FinancialExtractRecordFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
            extractFragment = it
        })
        fragmentList?.add(TransferBillFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
            transferFragment = it
        })
        fragmentList?.add(BillEmptyFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
            totalFragment = it
        })

    }
}