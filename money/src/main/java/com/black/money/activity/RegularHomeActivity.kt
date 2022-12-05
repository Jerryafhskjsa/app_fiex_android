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
import com.black.money.adpter.RegularAdapter
import com.black.money.databinding.ActivityRegularHomeBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.NumberUtil
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.REGULAR_HOME], beforePath = RouterConstData.LOGIN)
class RegularHomeActivity : BaseActionBarActivity(), QRefreshLayout.OnRefreshListener, OnItemClickListener, View.OnClickListener {
    private var binding: ActivityRegularHomeBinding? = null
    private var adapter: RegularAdapter? = null

    private var demandConfig: DemandConfig? = null
    private var regularConfig: RegularConfig? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_regular_home)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = RegularAdapter(this, BR.listItemRegularModel, null)
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
        return "定利宝"
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
        bundle.putInt(ConstData.MONEY_RECORD_TYPE, ConstData.TAB_REGULAR)
        if (demandConfig != null && demandConfig!!.coinTypeConf != null) {
            val demandCoins = ArrayList<String>()
            for (demand in demandConfig!!.coinTypeConf!!) {
                demand?.coinType?.let {
                    if (!demandCoins.contains(it)) {
                        demandCoins.add(it)
                    }
                }
            }
            bundle.putStringArrayList(ConstData.DEMAND_COINS, demandCoins)
            if (regularConfig != null && regularConfig!!.coinTypeConf != null) {
                val regularCoins = ArrayList<String>()
                for (regular in regularConfig!!.coinTypeConf!!) {
                    regular?.coinType?.let {
                        if (!regularCoins.contains(it)) {
                            regularCoins.add(it)
                        }
                    }
                }
                bundle.putStringArrayList(ConstData.REGULAR_COINS, regularCoins)
            }
        }
        BlackRouter.getInstance().build(RouterConstData.MONEY_RECORD).with(bundle).go(this)
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val regular = adapter?.getItem(position)
        val bundle = Bundle()
        bundle.putParcelable(ConstData.REGULAR, regular)
        if (regularConfig != null && regularConfig!!.coinTypeConf != null) {
            val regularCoins = ArrayList<String>()
            for (temp in regularConfig!!.coinTypeConf!!) {
                temp?.coinType?.let {
                    if (temp.status != null && temp.status != 0 && !regularCoins.contains(it)) {
                        regularCoins.add(it)
                    }
                }
            }
            bundle.putStringArrayList(ConstData.REGULAR_COINS, regularCoins)
        }
        BlackRouter.getInstance().build(RouterConstData.REGULAR_DETAIL).with(bundle).go(this)
    }

    private fun getDemandConfig() {
        MoneyApiServiceHelper.getDemandConfig(mContext, object : NormalCallback<HttpRequestResultData<DemandConfig?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                refreshDemandConfig(null)
            }

            override fun callback(returnData: HttpRequestResultData<DemandConfig?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    refreshDemandConfig(returnData.data)
                } else {
                    refreshDemandConfig(null)
                    FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun refreshDemandConfig(demandConfig: DemandConfig?) {
        this.demandConfig = demandConfig
    }

    private fun getRegularConfig() {
        MoneyApiServiceHelper.getRegularConfig(mContext, object : NormalCallback<HttpRequestResultData<RegularConfig?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                refreshRegularConfig(null)
                binding?.refreshLayout?.setRefreshing(false)
            }

            override fun callback(returnData: HttpRequestResultData<RegularConfig?>?) {
                binding?.refreshLayout?.setRefreshing(false)
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
        binding?.totalAmount?.text = if (regularConfig?.lockUsdtAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regularConfig.lockUsdtAmount, 8, 0, 2)
        binding?.rewardTotal?.text = if (regularConfig?.totalInterestUsdtAmount == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(regularConfig.totalInterestUsdtAmount, 8, 0, 2)
        adapter?.data = regularConfig?.coinTypeConf
        adapter?.notifyDataSetChanged()
    }
}