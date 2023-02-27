package com.black.c2c.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.C2CApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.C2CADData
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.c2c.C2CBills
import com.black.base.model.wallet.Wallet
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.BR
import com.black.c2c.R
import com.black.c2c.adapter.C2CBillsAdapter
import com.black.c2c.databinding.FragmentC2cSomeBillsBinding
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import java.util.ArrayList

class C2CSomeBillsFragment: BaseFragment(), OnItemClickListener, QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener {
    private var wallet: Wallet? = null

    private var binding: FragmentC2cSomeBillsBinding? = null
    private var layout: View? = null
    private var status: Int? = null
    private var adapter: C2CBillsAdapter? = null
    private var currentPage = 1
    private var total = 0
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        wallet = arguments?.getParcelable(ConstData.WALLET)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_c2c_some_bills, container, false)
        layout = binding?.root
        status = arguments?.getInt(ConstData.COIN_TYPE)
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        adapter = C2CBillsAdapter(mContext!!, BR.listItemC2COrderListModel, null)
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

        getC2CADData(true)
        return layout
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val financialRecord = adapter?.getItem(position)
        val extras = Bundle()
        val id = financialRecord?.id
        extras.putString("id", id)
        BlackRouter.getInstance()
            .build(RouterConstData.C2C_BUY_CONFRIM)
            .with(extras)
            .go(mContext)
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > (adapter?.count ?: 0)
    }

    override fun onRefresh() {
        currentPage = 1
        getC2CADData(false)
    }

    override fun onLoad() {
        if (total > (adapter?.count ?: 0)) {
            currentPage += 1
            getC2CADData(true)
        }
        else{
            binding?.refreshLayout?.setLoading(false)
        }
    }

    private fun onRefreshEnd() {
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
    }


    private fun getC2CADData(isShowLoading: Boolean) {
        C2CApiServiceHelper.getC2COL(mContext, isShowLoading,null,null,null,null,null,null,null,status,  object : NormalCallback<HttpRequestResultData<C2CADData<C2CBills?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                onRefreshEnd()
                showData(null)
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<C2CADData<C2CBills?>?>?) {
                onRefreshEnd()
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.total ?: 0
                    var dataList = returnData.data?.data
                    showData(dataList)
                } else {
                    showData(null)
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
    private fun showData(dataList: ArrayList<C2CBills?>?) {
        onRefreshEnd()
        if (currentPage == 1) {
            adapter?.data = dataList
        } else {
            adapter?.addAll(dataList)
        }
        adapter?.notifyDataSetChanged()
    }

}