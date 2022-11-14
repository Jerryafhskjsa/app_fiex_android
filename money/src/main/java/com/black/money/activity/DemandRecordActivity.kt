package com.black.money.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.money.DemandRecord
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.DemandRecordAdapter
import com.black.money.databinding.ActivityDemandRecordBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources

//聚宝盆收益记录
@Route(value = [RouterConstData.DEMAND_RECORD], beforePath = RouterConstData.LOGIN)
class DemandRecordActivity : BaseActivity(), QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener {
    private var binding: ActivityDemandRecordBinding? = null
    private var adapter: DemandRecordAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_demand_record)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = DemandRecordAdapter(this, BR.listItemDemanRecordModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)
        getDemandRecord(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "收益记录"
    }

    override fun onRefresh() {
        getDemandRecord(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0) {
            currentPage += 1
            getDemandRecord(true)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0
    }

    private fun getDemandRecord(isShowLoading: Boolean) {
        MoneyApiServiceHelper.getDemandRewardRecord(this, currentPage, 10, isShowLoading, object : NormalCallback<HttpRequestResultData<PagingData<DemandRecord?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<DemandRecord?>?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.total ?: 0
                    if (currentPage == 1) {
                        adapter?.data = returnData.data?.data
                    } else {
                        adapter?.addAll(returnData.data?.data)
                    }
                    adapter?.notifyDataSetChanged()
                } else {
                    adapter?.data = null
                    adapter?.notifyDataSetChanged()
                    FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                }
            }
        })
    }
}