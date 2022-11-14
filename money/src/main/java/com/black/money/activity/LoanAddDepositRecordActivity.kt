package com.black.money.activity

import android.content.Intent
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.money.LoanAddDepositRecord
import com.black.base.model.money.LoanRecord
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.HeightDividerItemDecoration
import com.black.base.util.RouterConstData
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.LoanAddDepositRecordAdapter
import com.black.money.databinding.ActivityLoanAddDepositRecordBinding
import com.black.net.HttpRequestResult
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources

@Route(value = [RouterConstData.LOAN_ADD_DEPOSIT_RECORD], beforePath = RouterConstData.LOGIN)
class LoanAddDepositRecordActivity : BaseActionBarActivity(), QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener {
    private var loanRecord: LoanRecord? = null

    private var binding: ActivityLoanAddDepositRecordBinding? = null

    private var adapter: LoanAddDepositRecordAdapter? = null
    private var currentPage = 1
    private var total = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loanRecord = intent.getParcelableExtra(ConstData.LOAN_RECORD)
        if (loanRecord == null) {
            finish()
            return
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_loan_add_deposit_record)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration: DividerItemDecoration = HeightDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = LoanAddDepositRecordAdapter(this, BR.listItemLoanAddDepositRecordModel, null)
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
        getLoanRecord(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String {
        return "追加记录"
    }

    override fun onRefresh() {
        currentPage = 1
        getLoanRecord(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0) {
            currentPage++
            getLoanRecord(true)
        } else {
            binding?.refreshLayout?.setLoading(false)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onRefresh()
    }

    private fun getLoanRecord(isShowLoading: Boolean) {
        MoneyApiServiceHelper.getLoanAddDepositRecord(this, isShowLoading, loanRecord?.id, currentPage, 10, object : NormalCallback<HttpRequestResultData<PagingData<LoanAddDepositRecord?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<LoanAddDepositRecord?>?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData?.data != null && returnData.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                    total = returnData.data?.totalCount ?: 0
                    if (currentPage == 1) {
                        adapter?.data = returnData.data?.list
                    } else {
                        adapter?.addAll(returnData.data?.list)
                    }
                    adapter?.notifyDataSetChanged()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) getString(R.string.error_data) else returnData.msg)
                }
            }
        })
    }
}