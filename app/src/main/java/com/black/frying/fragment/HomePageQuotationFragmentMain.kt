package com.black.frying.fragment

import android.app.Activity
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.size
import androidx.databinding.DataBindingUtil
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import com.black.base.fragment.BaseFragment
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.StatusBarUtil
import com.black.frying.activity.HomePageActivity
import com.black.router.BlackRouter
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageQuotationMainBinding
import com.google.android.material.tabs.TabLayout
import java.util.*
import kotlin.collections.ArrayList

class HomePageQuotationFragmentMain : BaseFragment(), View.OnClickListener {
    private var parent: HomePageActivity? = null
    private var binding: FragmentHomePageQuotationMainBinding? = null
    private var fragmentList: MutableList<Fragment>? = null
    private var currentTabPosition:Int = 0
    private var tabSets: List<String?>? = null

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        parent = activity as HomePageActivity
    }
    override fun onStop() {
        super.onStop()
        currentTabPosition = binding?.marketTopTab?.selectedTabPosition!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_page_quotation_main, container, false)
        StatusBarUtil.addStatusBarPadding(binding?.root)
        binding?.btnSearch?.setOnClickListener(this)
        initQuotationGroup()
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_search -> BlackRouter.getInstance().build(RouterConstData.DEAR_PAIR_SEARCH).go(mContext)
        }
    }

    override fun doResetSkinResources() {
        if (fragmentList != null) {
            for (fragment in fragmentList!!) {
//                fragment?.resetSkinResources()
            }
        }
    }

    //初始化行情分组
    private fun initQuotationGroup() {
        tabSets = listOf(getString(R.string.pair_collect),getString(R.string.spot), getString(R.string.futures))
        if (tabSets != null && tabSets!!.isNotEmpty()) {
            val setSize = tabSets!!.size
            fragmentList = ArrayList(setSize)
            for (i in 0 until setSize) {
                val set = tabSets!![i]
                try {
                    when(i){
                        0 -> {fragmentList?.add(HomePageQuotationFragment.newSelfInstance(set))
                        }
                        1 -> {fragmentList?.add(HomePageQuotationFragment.newSelfInstance(set))
                        }
                        2 -> {fragmentList?.add(HomePageQuotationFragment.newSelfInstance(set))
                        }
                    }

                } catch (throwable: Throwable) {
                    FryingUtil.printError(throwable)
                }
            }
            binding?.quotationMainViewPager?.adapter = object : FragmentPagerAdapter(childFragmentManager) {
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
            binding?.marketTopTab?.setupWithViewPager(binding?.quotationMainViewPager, true)
            binding?.marketTopTab?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                var textSize14 = resources.getDimensionPixelSize(R.dimen.text_size_18).toFloat()
                var textSize12 = resources.getDimensionPixelSize(R.dimen.text_size_12).toFloat()
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val view = tab.customView
                    val textView = if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                    textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize14)
                    binding?.quotationMainViewPager?.currentItem = tab.position
                    val currentFragment = CommonUtil.getItemFromList(fragmentList, currentTabPosition ) as HomePageQuotationFragment
                    if (currentFragment != null && currentFragment.isVisible) {
//                        currentFragment.onResume()
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    val view = tab.customView
                    val textView = if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                    textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize12)
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }



}