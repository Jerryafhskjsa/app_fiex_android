package com.black.frying.activity

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.PairApiServiceHelper
import com.black.base.model.socket.PairStatus
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.SocketDataContainer
import com.black.frying.adapter.HomeMainRiseFallAdapter
import com.black.frying.adapter.HomeQuotationDetailAdapter
import com.black.frying.fragment.HomePageQuotationDetailFragment
import com.black.frying.util.PairQuotationComparator
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.wallet.R
import com.black.wallet.databinding.ActivitySpotBillBinding
import com.black.wallet.fragment.BillEmptyFragment
import com.black.wallet.fragment.FinancialExtractRecordFragment
import com.black.wallet.fragment.FinancialRechargeRecordFragment
import com.black.wallet.fragment.TransferBillFragment
import com.fbsex.exchange.databinding.ActivityPairCollectBinding
import com.google.android.material.tabs.TabLayout
import skin.support.content.res.SkinCompatResources
import java.util.HashMap

@Route(value = [RouterConstData.PAIR_COLLECT])
class PairCollectActivity: BaseActionBarActivity(), View.OnClickListener {
    companion object{
        var TAB_TITLE = ArrayList<String>(2)
        var TAB_SPOT = null
        var TAB_FUTURES = null
    }

    private var binding: ActivityPairCollectBinding? = null
    private var fragmentList: java.util.ArrayList<Fragment>? = null
    private var rechargeFragment: FinancialRechargeRecordFragment? = null
    private var extractFragment: FinancialExtractRecordFragment? = null
    private var adapter: HomeMainRiseFallAdapter? = null
    private var transferFragment: TransferBillFragment? = null
    private var totalFragment: BillEmptyFragment? = null
    private val dataMap: MutableMap<String?, PairStatus?> = HashMap()
    private val dataList = ArrayList<PairStatus?>()
    private var gettingPairsData: Boolean? = false
    private var comparator = PairQuotationComparator(
        PairQuotationComparator.NORMAL,
        PairQuotationComparator.NORMAL,
        PairQuotationComparator.NORMAL,
        PairQuotationComparator.NORMAL
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, com.fbsex.exchange.R.layout.activity_pair_collect)
        binding?.appBarLayout?.findViewById<TextView>(R.id.text_action_bar_right)?.text = getString(R.string.baocun)
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(mContext, com.fbsex.exchange.R.color.L1)
        drawable.alpha = (0xff * 0.3).toInt()
        binding?.listView?.divider = drawable
        binding?.listView?.dividerHeight = 1
       // adapter = HomeMainRiseFallAdapter(mContext, dataList)
        binding?.listView?.adapter = adapter
        //binding?.listView?.onItemClickListener = mContext!!
        binding!!.mainTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab) {
             when(tab.position) {
                 0 -> {spot()}
                 1 -> {futures()}
             }
        }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        /*val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(mContext, com.fbsex.exchange.R.color.B2)
        binding!!.riseFallListView.divider = drawable
        binding!!.riseFallListView.dividerHeight = 15
        adapter = HomeMainRiseFallAdapter(mContext, 0, null)
        binding!!.riseFallListView.adapter = adapter

         */
    }

    override fun onClick(v: View) {
        when (v.id) {
        }
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.bianji_zixuan)
    }

    private fun spot() {
        SocketDataContainer.getPairsWithSet(
            mContext,
            getString(com.black.base.R.string.pair_collect),
            object : Callback<ArrayList<PairStatus?>?>() {
                override fun error(type: Int, error: Any) {
                    gettingPairsData = false
                }

                override fun callback(returnData: ArrayList<PairStatus?>?) {
                    if (returnData == null) {
                        gettingPairsData = false
                        return
                    }
                    synchronized(dataMap) {
                        synchronized(dataList) {
                            dataMap.clear()
                            dataList.clear()
                            dataList.addAll(returnData)
                            for (pairStatus in returnData) {
                                pairStatus?.pair?.let {
                                    dataMap[it] = pairStatus
                                }
                            }
                            mContext.run {
                                adapter?.data = dataList
                                adapter?.sortData(comparator)
                                adapter?.notifyDataSetChanged()
                                gettingPairsData = false
                            }
                        }
                    }
                    gettingPairsData = false
                }
            })
    }
    private fun futures(){
        val pairStatusType =
            ConstData.PairStatusType.FUTURE_DEAR

        SocketDataContainer.getFuturesPairsWithSet(
            mContext,
            pairStatusType,
            object : Callback<ArrayList<PairStatus?>?>() {
                override fun error(type: Int, error: Any) {
                    gettingPairsData = false
                }

                override fun callback(returnData: ArrayList<PairStatus?>?) {
                    if (returnData == null) {
                        gettingPairsData = false
                        return
                    }
                    synchronized(dataMap) {
                        synchronized(dataList) {
                            dataMap.clear()
                            dataList.clear()
                            dataList.addAll(returnData)
                            for (pairStatus in returnData) {
                                pairStatus?.pair?.let {
                                    dataMap[it] = pairStatus
                                }
                            }
                            mContext.run {
                                adapter?.data = dataList
                                adapter?.sortData(comparator)
                                adapter?.notifyDataSetChanged()
                                gettingPairsData = false
                            }
                        }
                    }
                    gettingPairsData = false
                }
            })
    }

}