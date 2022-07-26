package com.black.money.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.PagingData
import com.black.base.model.money.PromotionsBuy
import com.black.base.model.money.PromotionsBuyRecord
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.PromotionsBuyRecordAdapter
import com.black.money.databinding.ActivityPromotionsBuyRecordBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.util.NumberUtil

@Route(value = [RouterConstData.PROMOTIONS_BUY_RECORD], beforePath = RouterConstData.LOGIN)
class PromotionsBuyRecordActivity : BaseActivity(), QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener {
    private var binding: ActivityPromotionsBuyRecordBinding? = null

    private var promotionsBuy: PromotionsBuy? = null
    private var adapter: PromotionsBuyRecordAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        promotionsBuy = intent.getParcelableExtra(ConstData.PROMOTIONS_BUY)
        if (promotionsBuy == null || promotionsBuy!!.id == null) {
            finish()
            return
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_promotions_buy_record)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = PromotionsBuyRecordAdapter(this, BR.listItemPromotionsBuyRecordModel, promotionsBuy!!, null)
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
        return getString(R.string.promotions_buy_record)
    }

    override fun onRefresh() {
        currentPage = 1
        getPromotionsRecord(false)
    }

    private fun getPromotionsRecord(isShowLoading: Boolean) {
        MoneyApiServiceHelper.getPromotionsBuyRecord(this, isShowLoading, if (promotionsBuy == null) null else NumberUtil.formatNumberNoGroup(promotionsBuy!!.id), currentPage, 10, object : NormalCallback<HttpRequestResultData<PagingData<PromotionsBuyRecord?>?>?>() {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<PromotionsBuyRecord?>?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                    total = returnData.data?.total ?: 0
                    val list = returnData.data?.list
                    if (currentPage == 1) {
                        adapter?.data = list
                    } else {
                        adapter?.addAll(list)
                    }
                } else {
                    adapter?.data = null
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
                adapter?.notifyDataSetChanged()
            }
        })
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
}