package com.black.wallet.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
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
import com.black.base.model.wallet.WalletBill
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.FinancialRecordAdapter
import com.black.wallet.adapter.WalletBillAdapter
import com.black.wallet.databinding.FragmentFinancialRechargeRecordBinding
import skin.support.content.res.SkinCompatResources
import java.util.*

class BillEmptyFragment : BaseFragment(), OnItemClickListener, QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener {
    private var wallet: Wallet? = null

    private var binding: FragmentFinancialRechargeRecordBinding? = null
    private var layout: View? = null

    private var adapter: WalletBillAdapter? = null
    private var hasMore = false
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
        val decoration = DividerItemDecoration(mContext!!, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext!!, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = WalletBillAdapter(mContext!!, BR.listItemWalletBillModel, null)
        adapter?.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false
        binding?.recyclerView?.layoutManager = layoutManager

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext!!))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)

        getBillData(true)
        return layout
    }



    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        //val walletBill = adapter?.getItem(position)
        //点击账户详情
        //val extras = Bundle()
        //extras.putParcelable("record", walletBill)
        //BlackRouter.getInstance().build(RouterConstData.WALLET_COIN_DETAIL).with(extras).go(this)

    }

    override fun onRefresh() {
        currentPage = 1
        getBillData(false)
    }

    override fun onLoad() {
        if (total > adapter?.count!! || hasMore) {
            currentPage += 1
            getBillData(true)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count!! || hasMore
    }
    //获取综合账单记录

    private fun getBillData(isShowLoading: Boolean) {
        WalletApiServiceHelper.getWalletBillFiex(mContext, isShowLoading, null, object : NormalCallback<HttpRequestResultData<PagingData<WalletBill?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<WalletBill?>?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.total!!
                    hasMore = returnData.data?.hasNext != null && returnData.data?.hasNext!!
                    if (currentPage == 1) {
                        adapter?.data = (returnData.data?.items)
                    } else {
                        adapter?.addAll(returnData.data?.items)
                    }
                    adapter?.notifyDataSetChanged()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }




}