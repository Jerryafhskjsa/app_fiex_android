package com.black.c2c.activity

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.C2CApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.c2c.C2COrder
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.c2c.BR
import com.black.c2c.R
import com.black.c2c.adapter.C2COrderAdapter
import com.black.c2c.databinding.ActivityC2cOrderBinding
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.google.gson.Gson
import skin.support.content.res.SkinCompatResources

//c2c订单
@Route(value = [RouterConstData.C2C_ORDER], beforePath = RouterConstData.LOGIN)
class C2COrderActivity : BaseActivity(), View.OnClickListener, QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener, OnItemClickListener {
    private var binding: ActivityC2cOrderBinding? = null

    private var adapter: C2COrderAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_c2c_order)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = C2COrderAdapter(this, BR.listItemC2COrderModel, null)
        adapter?.setOnItemClickListener(this)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        //解决数据加载不完的问题
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        //解决数据加载完成后, 没有停留在顶部的问题
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return getString(R.string.c2c_order_record)
    }

    override fun initToolbarViews(toolbar: Toolbar) {
        val bnFilter = toolbar.findViewById<View>(R.id.filter_layout)
        if (bnFilter != null) {
            bnFilter.setOnClickListener(this)
            bnFilter.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        currentPage = 1
        getC2COrderList(true)
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.filter_layout) {
        }
    }

    override fun onRefresh() {
        currentPage = 1
        getC2COrderList(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0) {
            currentPage++
            getC2COrderList(true)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        //查看详情
        val c2COrder = adapter?.getItem(position)
        val bundle = Bundle()
        bundle.putString(ConstData.C2C_ORDER_ID, c2COrder?.id.toString())
        bundle.putString(ConstData.C2C_DIRECTION, c2COrder?.direction)
        bundle.putString(ConstData.C2C_ORDER_DATA, Gson().toJson(c2COrder))
        BlackRouter.getInstance().build(RouterConstData.C2C_ORDER_DETAIL)
                .with(bundle).go(mContext)
    }

    private fun getC2COrderList(isShowLoading: Boolean) {
        C2CApiServiceHelper.getC2COrderList(this, isShowLoading, null, null, currentPage, 10, object : NormalCallback<HttpRequestResultData<PagingData<C2COrder?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                super.error(type, error)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<C2COrder?>?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.totalCount ?: 0
                    if (currentPage == 1) {
                        adapter?.data = returnData.data?.list
                    } else {
                        adapter?.addAll(returnData.data?.list)
                    }
                    adapter?.notifyDataSetChanged()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }
}