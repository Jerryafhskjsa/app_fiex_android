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
import com.black.router.annotation.Route
import com.black.wallet.R
import com.black.wallet.databinding.ActivitySpotBillBinding
import com.google.android.material.tabs.TabLayout

@Route(value = [RouterConstData.FUTURE_JI_LU])
class FutureJiLu: BaseActionBarActivity(), View.OnClickListener {
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(1)
        private var TAB_DELEGATION: String? = null
        private var TAB_ENTRUSTMENT: String? = null
        private var TAB_ODERS: String? = null
        private var TAB_FLOW: String? = null
        private var TAB_COST: String? = null
    }

    private var binding: ActivitySpotBillBinding? = null
    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var odersFragment: com.black.frying.fragment.OdersFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_spot_bill)
        binding?.jilu?.visibility = View.GONE
        binding?.tabLayout?.visibility = View.GONE
        getString(R.string.limit_orders).also {
            TAB_DELEGATION = it
            TAB_TITLES[0] = TAB_DELEGATION
        }

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

            }
        }
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "账单记录"
    }
    private fun initFragmentList() {
        if (fragmentList == null) {
            fragmentList = java.util.ArrayList()
        }
        fragmentList?.clear()

        fragmentList?.add(com.black.frying.fragment.OdersFragment().also {
            val bundle = Bundle()
            it.arguments = bundle
            odersFragment = it
        })

    }
}