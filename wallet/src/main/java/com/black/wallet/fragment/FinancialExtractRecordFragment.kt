package com.black.wallet.fragment

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
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.wallet.FinancialRecord
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.FinancialRecordAdapter
import com.black.wallet.databinding.FragmentFinancialExtractRecordBinding
import java.util.*

class FinancialExtractRecordFragment : BaseFragment(), OnItemClickListener, QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener {
    private var wallet: Wallet? = null

    private var binding: FragmentFinancialExtractRecordBinding? = null
    private var layout: View? = null

    private var adapter: FinancialRecordAdapter? = null
    private var currentPage = 1
    private var total = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        wallet = arguments?.getParcelable(ConstData.WALLET)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_financial_extract_record, container, false)
        layout = binding?.root

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = FinancialRecordAdapter(mContext!!, BR.listItemFinancialRecordModel, null)
        adapter?.setInto(false)
        adapter?.setOnHandleClickListener(object : FinancialRecordAdapter.OnHandleClickListener {
            override fun onHandleClick(financialRecord: FinancialRecord?) {
                cancelFinancialRecordWithdrawData(financialRecord)
            }
        })
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

        getFinancialRecordWithdrawData(true)
        return layout
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val financialRecord = adapter?.getItem(position)
        val extras = Bundle()
        extras.putParcelable("record", financialRecord)
        BlackRouter.getInstance()
                .build(RouterConstData.WITHDRAW_DETAIL)
                .with(extras)
                .go(mContext)
    }

    override fun onRefresh() {
        currentPage = 1
        getFinancialRecordWithdrawData(false)
    }

    override fun onLoad() {
        if (total > adapter?.count!!) {
            currentPage++
            getFinancialRecordWithdrawData(false)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count!!
    }

    //获取提币记录
    private fun getFinancialRecordWithdrawData(isShowLoading: Boolean) {
        WalletApiServiceHelper.getWalletRecord(mContext, isShowLoading, currentPage, 10, total, 1, wallet!!.coinType, object : NormalCallback<HttpRequestResultData<PagingData<FinancialRecord?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                showData(null)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<FinancialRecord?>?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.total!!
                    val dataList = returnData.data?.items
                    if (dataList != null) {
                        for (record in dataList) {
                            record?.actionType = getString(R.string.wallet_bill_withdraw)
                        }
                    }
                    showData(dataList)
                } else {
                    showData(null)
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    private fun showData(dataList: ArrayList<FinancialRecord?>?) {
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
        if (currentPage == 1) {
            adapter?.data = dataList
        } else {
            adapter?.addAll(dataList)
        }
        adapter?.notifyDataSetChanged()
    }

    //撤销提币
    private fun cancelFinancialRecordWithdrawData(financialRecord: FinancialRecord?) {
        WalletApiServiceHelper.cancelWithdraw(mContext, financialRecord?.id, object : NormalCallback<HttpRequestResultString?>(mContext!!) {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    FryingUtil.showToast(mContext, getString(R.string.cancel_success))
                    if (adapter?.removeItem(financialRecord)!!) {
                        adapter?.notifyDataSetChanged()
                    }
                    getFinancialRecordWithdrawData(true)
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
                adapter?.notifyDataSetChanged()
            }
        })
    }
}