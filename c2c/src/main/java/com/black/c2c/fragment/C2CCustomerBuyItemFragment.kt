package com.black.c2c.fragment

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
import com.black.base.model.HttpRequestResultData
import com.black.base.model.PagingData
import com.black.base.model.c2c.C2COrder
import com.black.base.model.c2c.C2CSeller
import com.black.base.model.c2c.C2CSupportCoin
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.c2c.BR
import com.black.c2c.R
import com.black.c2c.adapter.C2CSellerBuyAdapter
import com.black.c2c.adapter.OnHandleClickListener
import com.black.c2c.databinding.FragmentC2cCustomerBuyItemBinding
import com.black.c2c.util.C2CHandleHelper
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.net.HttpRequestResult
import skin.support.content.res.SkinCompatResources

class C2CCustomerBuyItemFragment : BaseFragment(), OnHandleClickListener, QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener {
    private var binding: FragmentC2cCustomerBuyItemBinding? = null

    private var adapter: C2CSellerBuyAdapter? = null
    private var currentPage = 1
    private var total = 0
    var isHasGotData = false
        private set
    private var supportCoin: C2CSupportCoin? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bundle = arguments
        if (bundle != null) {
            supportCoin = bundle.getParcelable(ConstData.C2C_SUPPORT_COIN)
        }
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_c2c_customer_buy_item, container, false)
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
        adapter?.setOnHandleClickListener(this)
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
        getSellerList(false)
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        getUserInfo(null)
        getSellerList(false)
    }

    override fun doResetSkinResources() {
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.3 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter?.resetSkinResources()
        adapter?.notifyDataSetChanged()
    }

    override fun onHandleClick(c2CSeller: C2CSeller?) {
        //买入USDT
        fryingHelper.checkUserAndDoing(Runnable {
            if (supportCoin == null || mContext == null) {
                return@Runnable
            }
            mContext?.let {
                val userInfo = CookieUtil.getUserInfo(it)
                C2CHandleHelper(it, it, it.fryingHelper, userInfo, c2CSeller, C2COrder.ORDER_BUY, supportCoin!!).handle()
            }
        }, C2C_INDEX)
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0
    }

    override fun onRefresh() {
        currentPage = 1
        getSellerList(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0) {
            currentPage += 1
            getSellerList(true)
        }
    }

    private fun onRefreshEnd() {
        binding?.refreshLayout?.setRefreshing(false)
        binding?.refreshLayout?.setLoading(false)
    }

    fun getSellerList(isShowLoading: Boolean) {
        isHasGotData = true
        C2CApiServiceHelper.getC2CSellerList(activity, isShowLoading, if (supportCoin == null) null else supportCoin!!.coinType, C2COrder.ORDER_BUY, currentPage, 10, object : NormalCallback<HttpRequestResultData<PagingData<C2CSeller?>?>?>() {
            override fun error(type: Int, error: Any) {
                onRefreshEnd()
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<C2CSeller?>?>?) {
                onRefreshEnd()
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.totalCount ?: 0
                    if (currentPage == 1) {
                        adapter?.data = returnData.data?.list
                    } else {
                        adapter?.addAll(returnData.data?.list)
                    }
                    adapter?.notifyDataSetChanged()
                } else {
                    FryingUtil.showToast(activity, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
}