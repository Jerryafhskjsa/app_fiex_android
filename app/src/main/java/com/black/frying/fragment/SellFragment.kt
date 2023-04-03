package com.black.frying.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.WalletApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.*
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.frying.adapter.ThirdAdapters
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.fbsex.exchange.BR
import com.fbsex.exchange.R
import com.black.wallet.databinding.FragmentFinancialRechargeRecordBinding
import java.util.*

class SellFragment : BaseFragment(), OnItemClickListener, QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener {
    private var wallet: Wallet? = null

    private var binding: FragmentFinancialRechargeRecordBinding? = null
    private var layout: View? = null

    private var adapter: ThirdAdapters? = null
    private var currentPage = 1
    private var total = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        wallet = arguments?.getParcelable(ConstData.WALLET)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_financial_recharge_record, container, false)
        layout = binding?.root

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = ThirdAdapters(mContext!!, BR.listItemFinancialRecordModel, null)
        adapter?.setOnItemClickListener(this)
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

        getList(true)
        return layout
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val financialRecord = adapter?.getItem(position)
    }

    override fun onRefresh() {
        currentPage = 1
        getList(false)
    }

    override fun onLoad() {
        if (total > adapter?.count!!) {
            currentPage++
            getList(false)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count!!
    }

    private fun getList(isShowLoading: Boolean){
        WalletApiServiceHelper.getDepositOrderList(mContext, isShowLoading,"S",1,10,object : NormalCallback<HttpRequestResultData<PagingData<payOrder?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<payOrder?>?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val list = returnData.data?.items
                    total = returnData.data?.total ?: 0
                    showData(list)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun showData(dataList: ArrayList<payOrder?>?) {
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
        if (currentPage == 1) {
            adapter?.data = dataList
        } else {
            adapter?.addAll(dataList)
        }
        adapter?.notifyDataSetChanged()
    }

}