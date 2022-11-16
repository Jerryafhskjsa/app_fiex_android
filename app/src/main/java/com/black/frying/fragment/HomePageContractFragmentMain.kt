package com.black.frying.fragment

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.black.base.fragment.BaseFragment
import com.black.base.util.FryingUtil
import com.black.base.util.StatusBarUtil
import com.black.frying.activity.HomePageActivity
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageContractMainBinding
import com.google.android.material.tabs.TabLayout
import kotlin.collections.ArrayList

class HomePageContractFragmentMain : BaseFragment(), View.OnClickListener {
    private var parent: HomePageActivity? = null
    private var binding: FragmentHomePageContractMainBinding? = null
    private var fragmentList: MutableList<Fragment>? = null
    private var tabSets: List<String?>? = null

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        parent = activity as HomePageActivity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_page_contract_main, container, false)
        StatusBarUtil.addStatusBarPadding(binding?.root)
        initContractGroup()
        return binding?.root
    }

    override fun onClick(v: View) {
        when (v.id) {
        }
    }

    override fun doResetSkinResources() {
        if (fragmentList != null) {
            for (fragment in fragmentList!!) {
//                fragment?.resetSkinResources()
            }
        }
    }

    private fun initContractGroup() {
        tabSets = listOf(getString(R.string.usdt_base), getString(R.string.coin_base), getString(R.string.simulation_base))
        if (tabSets != null && tabSets!!.isNotEmpty()) {
            val setSize = tabSets!!.size
            fragmentList = ArrayList(setSize)
            for (i in 0 until setSize) {
                val set = tabSets!![i]
                try {
                    when(i){
                        0 -> fragmentList?.add(HomePageContractFragment.newSelfInstance(set))
                        1,2 ->fragmentList?.add(EmptyFragment())
                    }

                } catch (throwable: Throwable) {
                    FryingUtil.printError(throwable)
                }
            }
            binding?.contractMainViewPager?.adapter = object : FragmentPagerAdapter(childFragmentManager) {
                override fun getItem(position: Int): Fragment{
                    return fragmentList?.get(position) as Fragment
                }

                override fun getCount(): Int {
                    return fragmentList?.size ?: 0
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return tabSets!![position]
                }
            }
            binding?.contractTopTab?.setupWithViewPager(binding?.contractMainViewPager, true)

            binding?.contractTopTab?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }
}