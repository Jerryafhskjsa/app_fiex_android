package com.black.money.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.money.DemandConfig
import com.black.base.model.money.RegularConfig
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.DemandAdapter
import com.black.money.databinding.ActivityDemandHomeBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.DEMAND_HOME], beforePath = RouterConstData.LOGIN)
class DemandHomeActivity : BaseActionBarActivity(), QRefreshLayout.OnRefreshListener, OnItemClickListener, View.OnClickListener {
    private var binding: ActivityDemandHomeBinding? = null
    private var adapter: DemandAdapter? = null

    private var demandConfig: DemandConfig? = null
    private var regularConfig: RegularConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_demand_home)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = DemandAdapter(this, BR.listItemDemandModel, null)
        adapter?.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "活利宝"
    }

    override fun initToolbarViews(toolbar: Toolbar) {
        findViewById<View>(R.id.btn_record).setOnClickListener(this)
    }

    override fun onRefresh() {
        getDemandConfig()
        getRegularConfig()
    }

    override fun onResume() {
        super.onResume()
        getDemandConfig()
        getRegularConfig()
    }

    override fun onClick(v: View) {
        val bundle = Bundle()
        bundle.putInt(ConstData.MONEY_RECORD_TYPE, ConstData.TAB_DEMAND)
        if (demandConfig != null && demandConfig!!.coinTypeConf != null) {
            val demandCoins = ArrayList<String>()
            for (demand in demandConfig!!.coinTypeConf!!) {
                if (demand?.coinType != null && !demandCoins.contains(demand.coinType!!)) {
                    demandCoins.add(demand.coinType!!)
                }
            }
            bundle.putStringArrayList(ConstData.DEMAND_COINS, demandCoins)
            if (regularConfig != null && regularConfig!!.coinTypeConf != null) {
                val regularCoins = ArrayList<String>()
                for (regular in regularConfig!!.coinTypeConf!!) {
                    if (regular?.coinType != null && !regularCoins.contains(regular.coinType!!)) {
                        regularCoins.add(regular.coinType!!)
                    }
                }
                bundle.putStringArrayList(ConstData.REGULAR_COINS, regularCoins)
            }
        }
        BlackRouter.getInstance().build(RouterConstData.MONEY_RECORD).with(bundle).go(this)
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val demand = adapter?.getItem(position)
        val bundle = Bundle()
        bundle.putParcelable(ConstData.DEMAND, demand)
        if (demandConfig != null && demandConfig!!.coinTypeConf != null) {
            val demandCoins = ArrayList<String>()
            for (temp in demandConfig!!.coinTypeConf!!) {
                if (temp?.status != null && true == temp.status && temp.coinType != null && !demandCoins.contains(temp.coinType!!)) {
                    demandCoins.add(temp.coinType!!)
                }
            }
            bundle.putStringArrayList(ConstData.DEMAND_COINS, demandCoins)
        }
        BlackRouter.getInstance().build(RouterConstData.DEMAND_DETAIL).with(bundle).go(this)
    }

    private fun getDemandConfig() {
        MoneyApiServiceHelper.getDemandConfig(mContext, object : NormalCallback<HttpRequestResultData<DemandConfig?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                refreshDemandConfig(null)
                binding?.refreshLayout?.setRefreshing(false)
            }

            override fun callback(returnData: HttpRequestResultData<DemandConfig?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    refreshDemandConfig(returnData.data)
                } else {
                    refreshDemandConfig(null)
                    adapter?.notifyDataSetChanged()
                    FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun refreshDemandConfig(demandConfig: DemandConfig?) {
        this.demandConfig = demandConfig
        binding?.totalAmount?.text = if (demandConfig?.lockUsdtAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demandConfig.lockUsdtAmount, 8, 0, 2)
        binding?.rewardToday?.text = if (demandConfig?.totalNextInterestUsdtAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demandConfig.totalNextInterestUsdtAmount, 8, 0, 2)
        binding?.rewardYesterday?.text = if (demandConfig?.totalLastInterestUsdtAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demandConfig.totalLastInterestUsdtAmount, 8, 0, 2)
        binding?.rewardTotal?.text = if (demandConfig?.totalInterestUsdtAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(demandConfig.totalInterestUsdtAmount, 8, 0, 2)
        adapter?.data = demandConfig?.coinTypeConf
        adapter?.notifyDataSetChanged()
    }

    private fun getRegularConfig() {
        MoneyApiServiceHelper.getRegularConfig(mContext, object : NormalCallback<HttpRequestResultData<RegularConfig?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                refreshRegularConfig(null)
            }

            override fun callback(returnData: HttpRequestResultData<RegularConfig?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    refreshRegularConfig(returnData.data)
                } else {
                    refreshRegularConfig(null)
                }
            }
        })
    }

    private fun refreshRegularConfig(regularConfig: RegularConfig?) {
        this.regularConfig = regularConfig
    }
}