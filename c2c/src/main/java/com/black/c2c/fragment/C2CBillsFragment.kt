package com.black.c2c.fragment

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.fragment.BaseFragment
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.c2c.R
import com.black.c2c.databinding.FragmentC2cBillsBinding
import com.google.android.material.tabs.TabLayout

class C2CBillsFragment: BaseFragment(), View.OnClickListener {
    companion object {
        private val TAB_TITLES = arrayOfNulls<String>(4)
        private var TAB_RECHARGE: String? = null
        private var TAB_EXTRACT: String? = null
        private var TAB_EXCHANGE: String? = null
        private var TAB_TOTAL: String? = null
    }

    private var binding: FragmentC2cBillsBinding? = null
    private var fragmentList: java.util.ArrayList<Fragment>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreate(savedInstanceState)
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_c2c_bills, container, false)
        getString(R.string.all).also {
            TAB_RECHARGE = it
            TAB_TITLES[0] = TAB_RECHARGE
        }
       getString(R.string.dispayed).also {
            TAB_EXTRACT = it
            TAB_TITLES[1] = TAB_EXTRACT
        }
        getString(R.string.payed).also {
            TAB_EXCHANGE = it
            TAB_TITLES[2] = TAB_EXCHANGE
        }

        getString(R.string.statement).also {
            TAB_TOTAL = it
            TAB_TITLES[3] = TAB_TOTAL
        }

        binding?.tabLayout?.setSelectedTabIndicatorHeight(0)
        binding?.tabLayout?.tabMode = TabLayout.MODE_FIXED
        initFragmentList()

        binding?.viewPager?.adapter =
            object : FragmentStatePagerAdapter(childFragmentManager) {
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
        return binding?.root
    }

    override fun onClick(v: View) {
        when (v.id) {
        }
    }

    private fun initFragmentList() {
        if (fragmentList == null) {
            fragmentList = java.util.ArrayList()
        }
        fragmentList?.clear()
        fragmentList?.add(C2CSomeBillsFragment().also {
            val bundle = Bundle()
            bundle.putInt(ConstData.COIN_TYPE,2)
            it.arguments = bundle


        })
        fragmentList?.add(C2CSomeBillsFragment().also {
            val bundle = Bundle()
            bundle.putInt(ConstData.COIN_TYPE,2)
            it.arguments = bundle

        })
        fragmentList?.add(C2CSomeBillsFragment().also {
            val bundle = Bundle()
            bundle.putInt(ConstData.COIN_TYPE,2)
            it.arguments = bundle

        })
        fragmentList?.add(C2CSomeBillsFragment().also {
            val bundle = Bundle()
            bundle.putInt(ConstData.COIN_TYPE,2)
            it.arguments = bundle

        })

    }
}