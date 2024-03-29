package com.black.c2c.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.api.C2CApiServiceHelper
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.*
import com.black.base.model.c2c.C2CMainAD
import com.black.base.model.c2c.SellerAD
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.c2c.BR
import com.black.c2c.R
import com.black.c2c.adapter.C2CSellerBuyAdapter
import com.black.c2c.databinding.FragmentC2cCustomerBuyItemBinding
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.net.HttpRequestResult
import skin.support.content.res.SkinCompatResources
import java.util.Collections.addAll


class C2CSADFragment : BaseFragment(),  QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener
{
    private var binding: FragmentC2cCustomerBuyItemBinding? = null
    private var layout: View? = null
    private var adapter: C2CSellerBuyAdapter? = null
    private var coinType: String? = null
    private var direction: String? = null
    private var dataList: ArrayList<C2CMainAD?>? = ArrayList()
    private var sellList: ArrayList<C2CMainAD?>? = ArrayList()
    private var currentPage = 1
    private var total = 0

     override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_c2c_customer_buy_item, container, false)
        layout = binding?.root
        coinType = arguments?.getString(ConstData.COIN_TYPE).toString()
        direction = arguments?.getString(ConstData.COIN_INFO).toString()
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.3 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = C2CSellerBuyAdapter(mContext!!, BR.listItemC2CSallerBuyModel, null)
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
        binding?.refreshLayout?.setRefreshing(true)
        getC2CAD(false)
        return layout
    }

    override fun onLoadMoreCheck(): Boolean {
        return adapter?.count!! > (adapter?.count ?: 0)
    }

    override fun onRefresh() {
        currentPage = 1
        getC2CAD(false)
    }
    override fun onLoad() {
        if (adapter?.count!! > (adapter?.count ?: 0)) {
            currentPage += 1
            getC2CAD(true)
        }
        else{
            binding?.refreshLayout?.setLoading(false)
        }
    }

    private fun onRefreshEnd() {
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
    }


    private fun showData(dataList: ArrayList<C2CMainAD?>?) {
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
        if (currentPage == 1) {
            adapter?.data = dataList
        } else {
            adapter?.addAll(dataList)
        }
        adapter?.notifyDataSetChanged()
    }

    private fun getC2CAD(isShowLoading: Boolean) {
        val merchantId = arguments?.getInt(ConstData.PAIR)
        val name = arguments?.getString(ConstData.BIRTH)
        C2CApiServiceHelper.getC2CSellerAD(mContext, isShowLoading, merchantId,  object : NormalCallback<HttpRequestResultData<SellerAD<C2CMainAD?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                onRefreshEnd()
                showData(null)
                super.error(type, error)
            }

            @SuppressLint("SetTextI18n")
            override fun callback(returnData: HttpRequestResultData<SellerAD<C2CMainAD?>?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    val data = returnData.data
                    dataList = data?.buy
                    sellList = data?.sell
                    dataList = dataList?.plus(sellList!!) as ArrayList<C2CMainAD?>?
                    /*for (i in 0..dataList!!.size){
                        dataList!![i]?.realName = name
                    }*/
                    showData(dataList)
                } else {
                    showData(null)
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

}