package com.black.money.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.money.CloudPowerBuyRecord
import com.black.base.util.FryingUtil
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.CloudPowerBuyRecordAdapter
import com.black.money.databinding.FragmentCloudPowerBuyRecordBinding
import com.black.net.HttpRequestResult
import skin.support.content.res.SkinCompatResources
import java.util.*

class CloudPowerBuyRecordFragment : BaseFragment(), QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener {
    private var binding: FragmentCloudPowerBuyRecordBinding? = null

    private var adapter: CloudPowerBuyRecordAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_cloud_power_buy_record, container, false)

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = CloudPowerBuyRecordAdapter(mContext!!, BR.listItemCloudPowerBuyRecordModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext!!))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)

        currentPage = 1
        getCloudPowerHoldRecord(false)
        return binding?.root
    }

    override fun onRefresh() {
        currentPage = 1
        getCloudPowerHoldRecord(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0) {
            currentPage += 1
            getCloudPowerHoldRecord(true)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0
    }

    private fun getCloudPowerHoldRecord(isShowLoading: Boolean) {
        MoneyApiServiceHelper.getCloudPowerBuyRecord(mContext, isShowLoading, currentPage, 10, object : NormalCallback<HttpRequestResultData<PagingData<CloudPowerBuyRecord?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                showDataList(null)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<CloudPowerBuyRecord?>?>?) {
                if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.total ?: 0
                    showDataList(returnData.data?.data)
                } else {
                    showDataList(null)
                    FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun showDataList(data: ArrayList<CloudPowerBuyRecord?>?) {
        if (adapter != null) {
            if (currentPage == 1) {
                adapter?.data = data
            } else {
                adapter?.addAll(data)
            }
            adapter?.notifyDataSetChanged()
            binding?.refreshLayout?.setRefreshing(false)
            binding?.refreshLayout?.setLoading(false)
        }
    }
}