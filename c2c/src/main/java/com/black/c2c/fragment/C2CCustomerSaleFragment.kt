package com.black.c2c.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.black.base.fragment.BaseFragment
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.c2c.R
import com.black.c2c.databinding.FragmentC2cCustomerSaleBinding
import com.google.android.material.tabs.TabLayout
import skin.support.content.res.SkinCompatResources
import java.util.*

class C2CCustomerSaleFragment : BaseFragment() {
    private var binding: FragmentC2cCustomerSaleBinding? = null
    private var supportCoins: ArrayList<C2CSupportCoin?>? = null
    private var fragmentList: MutableList<C2CCustomerSaleItemFragment>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        val bundle = arguments
        if (bundle != null) {
            supportCoins = bundle.getParcelableArrayList(ConstData.C2C_SUPPORT_COINS)
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_c2c_customer_sale, container, false)
        binding?.coinTab?.setTabTextColors(SkinCompatResources.getColor(activity, R.color.T2), SkinCompatResources.getColor(activity, R.color.C1))
        binding?.coinTab?.setSelectedTabIndicatorHeight(0)
        binding?.coinTab?.tabMode = TabLayout.MODE_SCROLLABLE
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshCoinTabs()
    }

    fun setSupportCoins(supportCoins: ArrayList<C2CSupportCoin?>?) {
        this.supportCoins = supportCoins
        refreshCoinTabs()
    }

    private fun refreshCoinTabs() {
        if (binding?.coinTab != null) {
            binding?.coinTab?.removeAllTabs()
            if (fragmentList == null) {
                fragmentList = ArrayList()
            }
            fragmentList!!.clear()
            if (supportCoins != null && supportCoins!!.isNotEmpty()) {
                val size = supportCoins!!.size
                for (i in 0 until size) {
                    val c2CSupportCoin = supportCoins!![i]
                    try {
                        val itemFragment = C2CCustomerSaleItemFragment()
                        val bundle = Bundle()
                        bundle.putParcelable(ConstData.C2C_SUPPORT_COIN, c2CSupportCoin)
                        itemFragment.arguments = bundle
                        fragmentList!!.add(itemFragment)
                    } catch (throwable: Throwable) {
                        FryingUtil.printError(throwable)
                    }
                }
            }
            binding?.viewPager?.adapter = object : FragmentPagerAdapter(childFragmentManager) {
                override fun getItem(position: Int): Fragment {
                    return fragmentList!![position]
                }

                override fun getCount(): Int {
                    return fragmentList!!.size
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return supportCoins!![position]?.coinType
                }
            }
            binding?.coinTab?.setupWithViewPager(binding?.viewPager, true)
            for (i in 0 until (binding?.coinTab?.tabCount ?: 0)) {
                val c2CSupportCoin = supportCoins!![i]
                val coinType = c2CSupportCoin?.coinType
                try {
                    val tab = binding?.coinTab?.getTabAt(i)
                    if (tab != null) {
                        tab.setCustomView(R.layout.view_c2c_one_key_buy_tab)
                        if (tab.customView != null) {
                            val textView = tab.customView!!.findViewById<View>(android.R.id.text1) as TextView
                            textView.text = coinType
                        }
                    }
                } catch (throwable: Throwable) {
                    FryingUtil.printError(throwable)
                }
            }
        }
    }
}