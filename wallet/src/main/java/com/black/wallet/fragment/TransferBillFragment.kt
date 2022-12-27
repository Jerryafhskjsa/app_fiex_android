package com.black.wallet.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.WalletApiService
import com.black.base.api.WalletApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.wallet.FinancialRecord
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletTransferRecord
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.FinancialRecordAdapter
import com.black.wallet.adapter.WalletTransferRecordAdapter
import com.black.wallet.databinding.FragmentFinancialRechargeRecordBinding
import java.util.*

class TransferBillFragment : BaseFragment(), OnItemClickListener, QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener {
    private var wallet: Wallet? = null

    private var binding: FragmentFinancialRechargeRecordBinding? = null
    private var layout: View? = null

    private var adapter: WalletTransferRecordAdapter? = null
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
        adapter = WalletTransferRecordAdapter(mContext!!, BR.listItemFinancialRecordModel, null)
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

        getRecord(true)
        return layout
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val financialRecord = adapter?.getItem(position)
        val extras = Bundle()
    }

    override fun onRefresh() {
        currentPage = 1
        getRecord(false)
    }

    override fun onLoad() {
        if (total > adapter?.count!!) {
            currentPage++
            getRecord(false)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count!!
    }

    //获取划转记录
    private fun getRecord(isShowLoading: Boolean) {
        ApiManager.build(mContext!!, UrlConfig.ApiType.URL_PRO).getService(WalletApiService::class.java)
            ?.getWalletTransferRecord(null,currentPage, 10,
                 null, null)
            ?.compose(RxJavaHelper.observeOnMainThread())
            ?.subscribe(HttpCallbackSimple(mContext, isShowLoading, object : NormalCallback<HttpRequestResultData<PagingData<WalletTransferRecord?>?>?>(mContext!!) {
                override fun error(type: Int, error: Any?) {
                    super.error(type, error)
                    showData(null)
                }
                override fun callback(returnData: HttpRequestResultData<PagingData<WalletTransferRecord?>?>?) {
                    if (returnData?.code != null  && returnData.code == HttpRequestResult.SUCCESS) {
                        total = returnData.data?.totalCount!!
                        val dataList = returnData.data?.records
                        showData(dataList)
                    } else {
                        FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                    }
                }
            }))
    }

    private fun showData(dataList: ArrayList<WalletTransferRecord?>?) {
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