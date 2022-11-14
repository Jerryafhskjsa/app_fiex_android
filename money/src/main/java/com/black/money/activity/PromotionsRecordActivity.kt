package com.black.money.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.money.PromotionsRecord
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.PromotionsRecordAdapter
import com.black.money.databinding.ActivityPromotionsRecordBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route

@Route(value = [RouterConstData.PROMOTIONS_RECORD], beforePath = RouterConstData.LOGIN)
class PromotionsRecordActivity : BaseActivity(), QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener {
    private var binding: ActivityPromotionsRecordBinding? = null
    private var adapter: PromotionsRecordAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_promotions_record)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = PromotionsRecordAdapter(this, BR.listItemPromotionsRecordModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)
        getPromotionsRecord(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.promotions_record)
    }

    override fun onRefresh() {
        currentPage = 1
        getPromotionsRecord(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0) {
            currentPage++
            getPromotionsRecord(false)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0
    }

    private fun getPromotionsRecord(isShowLoading: Boolean) {
        MoneyApiServiceHelper.getPromotionsRecord(this, isShowLoading, currentPage, 10, object : NormalCallback<HttpRequestResultData<PagingData<PromotionsRecord?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<PromotionsRecord?>?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                    if (returnData.data?.data != null) {
                        total = returnData.data?.total ?: 0
                        val dataList = returnData.data?.data
                        if (currentPage == 1) {
                            adapter?.data = (dataList)
                        } else {
                            adapter?.addAll(dataList)
                        }
                    }
                } else {
                    if (currentPage == 1) {
                        adapter?.data = null
                    }
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
                adapter?.notifyDataSetChanged()
            }
        })
    }

}
