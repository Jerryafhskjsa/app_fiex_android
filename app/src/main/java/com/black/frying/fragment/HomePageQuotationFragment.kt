package com.black.frying.fragment

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter
import com.black.base.api.PairApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.QuotationSet
import com.black.base.model.socket.PairStatus
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.SocketDataContainer
import com.black.base.util.StatusBarUtil
import com.black.frying.activity.HomePageActivity
import com.black.frying.util.PairQuotationComparator
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageQuotationBinding
import com.google.android.material.tabs.TabLayout
import skin.support.content.res.SkinCompatResources
import java.util.*
import kotlin.collections.ArrayList

class HomePageQuotationFragment : BaseFragment(), View.OnClickListener {
    private var parent: HomePageActivity? = null

    private var binding: FragmentHomePageQuotationBinding? = null

    private var sets: List<QuotationSet?>? = null
    private var fragmentList: MutableList<Fragment?>? = null

    //异步获取数据
    private val handlerThread: HandlerThread? = null
    private val socketHandler: Handler? = null

    var comparator = PairQuotationComparator(PairQuotationComparator.NORMAL, PairQuotationComparator.NORMAL, PairQuotationComparator.NORMAL)

    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        parent = activity as HomePageActivity
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_page_quotation, container, false)
        binding?.setTab?.setTabTextColors(SkinCompatResources.getColor(activity, R.color.C5), SkinCompatResources.getColor(activity, R.color.C1))
        binding?.setTab?.tabMode = TabLayout.MODE_SCROLLABLE


        binding?.sortCoin?.setOnClickListener(this)
        binding?.sortPrice?.setOnClickListener(this)
        binding?.sortRange?.setOnClickListener(this)
        refreshSets()
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        val currentFragment: Fragment? = CommonUtil.getItemFromList(fragmentList, binding?.setTab?.selectedTabPosition
                ?: -1)
        if (currentFragment != null && currentFragment.isVisible) {
            currentFragment.onResume()
        }
//        SocketDataContainer.refreshDearPairs(mContext)
    }

    override fun onDestroy() {
        super.onDestroy()
        handlerThread?.quit()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.sort_coin -> {
                comparator.coinType = getNextType(comparator.coinType)
                comparator.priceType = PairQuotationComparator.NORMAL
                comparator.rangeType = PairQuotationComparator.NORMAL
            }
            R.id.sort_price -> {
                comparator.coinType = PairQuotationComparator.NORMAL
                comparator.priceType = getNextType(comparator.priceType)
                comparator.rangeType = PairQuotationComparator.NORMAL
            }
            R.id.sort_range -> {
                comparator.coinType = PairQuotationComparator.NORMAL
                comparator.priceType = PairQuotationComparator.NORMAL
                comparator.rangeType = getNextType(comparator.rangeType)
            }
        }
        updateSortIcons()
        updateFragments()
    }

    override fun doResetSkinResources() {
        if (fragmentList != null) {
            for (fragment in fragmentList!!) {
//                fragment?.resetSkinResources()
            }
        }
    }

    private fun getNextType(currentType: Int): Int {
        return when (currentType) {
            PairQuotationComparator.NORMAL -> PairQuotationComparator.UP
            PairQuotationComparator.UP -> PairQuotationComparator.DOWN
            else -> PairQuotationComparator.NORMAL
        }
    }

    private fun updateSortIcons() {
        binding?.iconSortCoin?.setImageDrawable(getIcon(comparator.coinType))
        binding?.iconSortPrice?.setImageDrawable(getIcon(comparator.priceType))
        binding?.iconSortRange?.setImageDrawable(getIcon(comparator.rangeType))
    }

    private fun getIcon(type: Int): Drawable {
        return when (type) {
            PairQuotationComparator.UP -> SkinCompatResources.getDrawable(mContext, R.drawable.icon_quotation_sort_pre_02)
            PairQuotationComparator.DOWN -> SkinCompatResources.getDrawable(mContext, R.drawable.icon_quotation_sort_pre_01)
            else -> SkinCompatResources.getDrawable(mContext, R.drawable.icon_quotation_sort_nor)
        }
    }

    private fun updateFragments() {
        if (fragmentList == null || fragmentList!!.isEmpty()) {
            return
        }
        for (fragment in fragmentList!!) {
//            fragment?.updateCompare(comparator)
        }
    }

    private fun refreshSets() {
            PairApiServiceHelper.getTradeSetsLocal(activity, false, object : Callback<ArrayList<QuotationSet?>?>() {
                override fun error(type: Int, error: Any) {
//                    refreshSets()这里接口不通目前会引起死循环调用
                }
                override fun callback(returnData: ArrayList<QuotationSet?>?) {
                    if (returnData != null) {
                        var optionalSet = QuotationSet()
                        optionalSet.coinType = getString(R.string.pair_collect)
                        optionalSet.name = getString(R.string.pair_collect)
                        returnData?.add(0,  optionalSet)
                        setSets(returnData)
                    } else {
                        refreshSets()
                    }
                }
            })
    }

    //初始化行情分组
    private fun initQuotationGroup() {
        if (sets != null && sets!!.isNotEmpty()) {
            val setSize = sets!!.size
            fragmentList = ArrayList(setSize)
            for (i in 0 until setSize) {
                val set = sets!![i]
                try {
                    fragmentList?.add(HomePageQuotationDetailFragment.newInstance(set))
                } catch (throwable: Throwable) {
                    FryingUtil.printError(throwable)
                }
            }
            binding?.quotationViewPager?.adapter = object : FragmentStatePagerAdapter(childFragmentManager) {
                override fun getItem(position: Int): Fragment{
                    return fragmentList!![position] as Fragment
                }

                override fun getCount(): Int {
                    return fragmentList!!.size
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return sets!![position]?.name
                }
            }
            binding?.setTab?.setupWithViewPager(binding?.quotationViewPager, true)
            for (i in 0 until (binding?.setTab?.tabCount ?: 0)) {
                val set = sets!![i]
                try {
                    val tab = binding?.setTab?.getTabAt(i)
                    if (tab != null) {
                        tab.setCustomView(R.layout.view_home_quotation_tab)
                        if (tab.customView != null) {
                            val textView = tab.customView!!.findViewById<View>(android.R.id.text1) as TextView
                            textView.text = set?.name
                        }
                    }
                } catch (throwable: Throwable) {
                    FryingUtil.printError(throwable)
                }
            }
            binding?.setTab?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                var textSize20 = resources.getDimensionPixelSize(R.dimen.text_size_20).toFloat()
                var textSize16 = resources.getDimensionPixelSize(R.dimen.text_size_16).toFloat()
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val view = tab.customView
                    val textView = if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                    textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize20)
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    val view = tab.customView
                    val textView = if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                    textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize16)
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }

    fun setSets(sets: List<QuotationSet?>?) {
        if (sets != null && sets.isNotEmpty()) {
            this.sets = sets
            initQuotationGroup()
        }
    }

    companion object {
        fun newSelfInstance(tab: String?): HomePageQuotationFragment {
            val args = Bundle()
            val fragment = HomePageQuotationFragment()
            fragment.arguments = args
//            fragment.set = tab
            return fragment
        }
    }

}