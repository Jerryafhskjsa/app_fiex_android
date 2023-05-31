package com.black.frying.activity

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
import com.black.frying.fragment.CostFragment
import com.black.frying.fragment.DelegationFragment
import com.black.frying.fragment.EntrustmentFragment
import com.black.frying.fragment.FlowFragment
import com.black.frying.fragment.OdersFragment
import com.black.frying.fragment.OrderHistoryFragment
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.wallet.R
import com.black.wallet.databinding.ActivitySpotBillBinding
import com.google.android.material.tabs.TabLayout

@Route(value = [RouterConstData.CONTRACT_BILL_ACTIVITY])
class ContractBillActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(3)
        private var TAB_DELEGATION: String? = null
        private var TAB_ENTRUSTMENT: String? = null
        private var TAB_ODERS: String? = null
        private var TAB_FLOW: String? = null
        private var TAB_COST: String? = null
    }

    private var binding: ActivitySpotBillBinding? = null
    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var delegationFragment: DelegationFragment? = null
    private var entrustmentFragment: EntrustmentFragment? = null
    private var orderHistoryFragment: OrderHistoryFragment? = null
    private var flowFragment: FlowFragment? = null
    private var costFragment: CostFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_spot_bill)
        binding?.jilu?.setOnClickListener(this)
        getString(R.string.historical_orders).also {
            TAB_DELEGATION = it
            TAB_TITLES[0] = TAB_DELEGATION
        }
        "历史成交".also {
            TAB_ENTRUSTMENT = it
            TAB_TITLES[1] = TAB_ENTRUSTMENT
        }
        getString(R.string.map_entrustment) .also {
            TAB_ODERS = it
            TAB_TITLES[2] = TAB_ODERS
        }

       /* getString(R.string.capital_cost).also {
            TAB_FLOW = it
            TAB_TITLES[3] = TAB_FLOW
        }
        getString(R.string.capital_flow).also {
            TAB_COST = it
            TAB_TITLES[4] = TAB_COST
        }

        */

        binding?.tabLayout?.setSelectedTabIndicatorHeight(0)
        binding?.tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE
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
                    return TAB_TITLES[position]
                }

                override fun restoreState(state: Parcelable?, loader: ClassLoader?) {

                }
            }
        binding?.tabLayout?.setupWithViewPager(binding?.viewPager, true)

        for (i in TAB_TITLES.indices) {
            try {
                val tab: TabLayout.Tab? = binding?.tabLayout?.getTabAt(i)
                if (tab != null) {
                    tab.setCustomView(R.layout.view_tab_normal)
                    if (tab.customView != null) {
                        val textView =
                            tab.customView!!.findViewById<View>(android.R.id.text1) as TextView
                        textView.text = TAB_TITLES[i]
                    }
                }
            } catch (throwable: Throwable) {
                FryingUtil.printError(throwable)
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.jilu -> {
                BlackRouter.getInstance().build(RouterConstData.FUTURE_JI_LU).go(mContext)
            }
        }
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.contract_history_record)
    }
    private fun initFragmentList() {
        if (fragmentList == null) {
            fragmentList = java.util.ArrayList()
        }
        fragmentList?.clear()
        fragmentList?.add(DelegationFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
            delegationFragment = it

        })
        fragmentList?.add(OrderHistoryFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
            orderHistoryFragment = it
        })
        fragmentList?.add(EntrustmentFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
            entrustmentFragment = it
        })
       /* fragmentList?.add(FlowFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
            flowFragment = it
        })
        fragmentList?.add(CostFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
            costFragment = it
        })

        */

    }
}