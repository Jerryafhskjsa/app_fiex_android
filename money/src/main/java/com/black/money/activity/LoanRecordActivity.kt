package com.black.money.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.MoneyApiServiceHelper
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.money.LoanRecord
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.HeightDividerItemDecoration
import com.black.base.util.RouterConstData
import com.black.base.view.DeepControllerWindow
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.money.BR
import com.black.money.R
import com.black.money.adpter.LoanRecordAdapter
import com.black.money.adpter.LoanRecordAdapter.OnLoanRecordHandleListener
import com.black.money.databinding.ActivityLoanRecordBinding
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import skin.support.content.res.SkinCompatResources
import java.util.*

@Route(value = [RouterConstData.LOAN_RECORD], beforePath = RouterConstData.LOGIN)
class LoanRecordActivity : BaseActionBarActivity(), QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener, OnLoanRecordHandleListener, OnItemClickListener {
    companion object {
        private var ADD_AMOUNT: String? = null
        private var BACK: String? = null
        private var ADD_AMOUNT_RECORD: String? = null
        private var DETAIL: String? = null
    }

    private var binding: ActivityLoanRecordBinding? = null

    private var adapter: LoanRecordAdapter? = null
    private var currentPage = 1
    private var total = 0
    private val recordActions = ArrayList<String?>()
    private var handler: Handler? = null
    private var timerCommand: TimerCommand? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ADD_AMOUNT = "追加"
        BACK = "还贷"
        ADD_AMOUNT_RECORD = "追加记录"
        DETAIL = "订单详情"

        binding = DataBindingUtil.setContentView(this, R.layout.activity_loan_record)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration: DividerItemDecoration = HeightDividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(mContext, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = LoanRecordAdapter(this, BR.listItemLoanRecordModel, null)
        adapter?.setOnLoanRecordHandleListener(this)
        adapter?.setOnItemClickListener(this)
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
        return "抵押借贷记录"
    }

    override fun onResume() {
        super.onResume()
        if (timerCommand == null) {
            timerCommand = TimerCommand()
        }
        if (handler == null) {
            handler = Handler()
        }
        handler?.postDelayed(timerCommand, 5000)
    }

    override fun onStop() {
        super.onStop()
        if (timerCommand != null && handler != null) {
            handler?.removeCallbacks(timerCommand)
            timerCommand = null
        }
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

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val loanRecord = adapter?.getItem(position)
        val bundle = Bundle()
        bundle.putParcelable(ConstData.LOAN_RECORD, loanRecord)
        BlackRouter.getInstance().build(RouterConstData.LOAN_ORDER_DETAIL).with(bundle).go(mContext)
    }

    override fun onLoanRecordHandle(loanRecord: LoanRecord) {
        recordActions.clear()
        if (loanRecord.isDoing) {
            recordActions.add(ADD_AMOUNT)
            recordActions.add(BACK)
        }
        recordActions.add(ADD_AMOUNT_RECORD)
        DeepControllerWindow(this, null, null, recordActions,
                object : DeepControllerWindow.OnReturnListener<String?> {
                    override fun onReturn(window: DeepControllerWindow<String?>, item: String?) {
                        when {
                            TextUtils.equals(ADD_AMOUNT, item) -> {
                                val bundle = Bundle()
                                bundle.putParcelable(ConstData.LOAN_RECORD, loanRecord)
                                BlackRouter.getInstance().build(RouterConstData.LOAN_ADD_DEPOSIT).withRequestCode(ConstData.LOAN_ADD_DEPOSIT).with(bundle).go(mContext)
                            }
                            TextUtils.equals(BACK, item) -> {
                                val bundle = Bundle()
                                bundle.putParcelable(ConstData.LOAN_RECORD, loanRecord)
                                BlackRouter.getInstance().build(RouterConstData.LOAN_BACK).withRequestCode(ConstData.LOAN_BACK).with(bundle).go(mContext)
                            }
                            TextUtils.equals(ADD_AMOUNT_RECORD, item) -> {
                                val bundle = Bundle()
                                bundle.putParcelable(ConstData.LOAN_RECORD, loanRecord)
                                BlackRouter.getInstance().build(RouterConstData.LOAN_ADD_DEPOSIT_RECORD).with(bundle).go(mContext)
                            }
                        }
                    }

                }).show()
    }

    private fun getLoanRecord(isShowLoading: Boolean) {
        isDataLoading = true
        MoneyApiServiceHelper.getLoanRecord(this, isShowLoading, currentPage, 30, object : NormalCallback<HttpRequestResultData<PagingData<LoanRecord?>?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                isDataLoading = false
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
            }

            override fun callback(returnData: HttpRequestResultData<PagingData<LoanRecord?>?>?) {
                isDataLoading = false
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

    var isDataLoading = false

    private inner class TimerCommand : Runnable {
        override fun run() {
            if (!isDataLoading) {
                currentPage = 1
                getLoanRecord(false)
            }
            handler?.postDelayed(this, 5)
        }
    }
}