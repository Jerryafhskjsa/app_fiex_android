package com.black.wallet.activity

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.WalletApiService
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.manager.ApiManager
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.wallet.LeverBorrowRecord
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.util.RxJavaHelper
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.LeverBorrowRecordAdapter
import com.black.wallet.databinding.ActivityLeverBorrowRecordBinding
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.LEVER_BORROW_RECORD], beforePath = RouterConstData.LOGIN)
class LeverBorrowRecordActivity : BaseActionBarActivity(), QRefreshLayout.OnRefreshListener, QRefreshLayout.OnLoadListener, QRefreshLayout.OnLoadMoreCheckListener {
    private var pair: String? = null

    private var binding: ActivityLeverBorrowRecordBinding? = null

    private var adapter: LeverBorrowRecordAdapter? = null
    private var currentPage = 1
    private var total = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_lever_borrow_record)

        binding?.type?.setText(String.format("%s 逐仓账户", pair?.replace("_", "/") ?: nullAmount))

        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = LeverBorrowRecordAdapter(mContext, BR.listItemLeverBorrowRecordModel, null)
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setEmptyView(binding?.emptyView?.root)
        binding?.recyclerView?.isNestedScrollingEnabled = false
        binding?.recyclerView?.setHasFixedSize(true)
        binding?.recyclerView?.isFocusable = false

        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(mContext))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)

        getRecord(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        pair = intent.getStringExtra(ConstData.PAIR)
        return "借币记录"
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

    private fun getRecord(isShowLoading: Boolean) {
        ApiManager.build(this).getService(WalletApiService::class.java)
                ?.getLeverBorrowRecord(currentPage, 10, pair, "BORROW")
                ?.compose(RxJavaHelper.observeOnMainThread())
                ?.subscribe(HttpCallbackSimple(this, isShowLoading, object : NormalCallback<HttpRequestResultData<PagingData<LeverBorrowRecord?>?>?>(mContext!!) {

                    override fun error(type: Int, error: Any?) {
                        super.error(type, error)
                        showData(null)
                    }

                    override fun callback(returnData: HttpRequestResultData<PagingData<LeverBorrowRecord?>?>?) {
                        if (returnData?.code != null && returnData.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                            total = returnData.data?.totalCount!!
                            val dataList = returnData.data?.data
                            showData(dataList)
                        } else {
                            FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                        }
                    }

                }))
    }

    private fun showData(dataList: ArrayList<LeverBorrowRecord?>?) {
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