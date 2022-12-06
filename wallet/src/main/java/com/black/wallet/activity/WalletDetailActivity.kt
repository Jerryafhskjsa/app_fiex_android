package com.black.wallet.activity

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.WalletApiServiceHelper
import com.black.base.lib.filter.FilterEntity
import com.black.base.lib.filter.FilterResult
import com.black.base.lib.filter.FilterWindow
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.NormalCallback
import com.black.base.model.PagingData
import com.black.base.model.filter.DateFilter
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletBill
import com.black.base.model.wallet.WalletBillType
import com.black.base.util.ConstData
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.widget.SpanTextView
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.NumberUtil
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.WalletBillAdapter
import com.black.wallet.databinding.ActivityWalletDetailBinding
import com.google.gson.Gson
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

//综合账单
@Route(value = [RouterConstData.WALLET_DETAIL])
class WalletDetailActivity : BaseActivity(),
    View.OnClickListener,
    QRefreshLayout.OnRefreshListener,
    QRefreshLayout.OnLoadListener,
    QRefreshLayout.OnLoadMoreCheckListener,
    OnItemClickListener {
    private var wallet: Wallet? = null
    private var coinType: String? = null
    private var binding: ActivityWalletDetailBinding? = null

    private var adapter: WalletBillAdapter? = null
    private var currentPage = 1
    private var total = 0
    private var hasMore = false
    private var walletBillType: List<WalletBillType?>? = null
    private var walletBillTypeMap: MutableMap<String?, String?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        wallet = intent.getParcelableExtra(ConstData.WALLET)
        coinType = wallet?.coinType
        var gson = Gson()
        if (wallet == null) { //没有传入wallet，并且也没有coinType，非法调用
            if (coinType == null) {
                finish()
                return
            }
        }
        binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet_detail)
        binding?.btnExchange?.setOnClickListener(this)
        binding?.btnWithdraw?.setOnClickListener(this)

        binding?.appBarLayout?.findViewById<SpanTextView>(R.id.action_bar_title_big)?.text = wallet?.coinType
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = WalletBillAdapter(mContext, BR.listItemWalletBillModel, null)
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
        refreshWallet()
        getBillData(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return if (wallet != null) if (wallet?.coinType == null) "" else wallet?.coinType else (if (coinType == null) "" else coinType)
    }

    override fun initToolbarViews(toolbar: Toolbar) {
        findViewById<View>(R.id.filter_layout).setOnClickListener(this)
        //        findViewById(R.id.filter_layout).setVisibility(View.GONE);
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_exchange -> {
                val bundle = Bundle()
                bundle.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_EXCHANGE)
                bundle.putParcelable(ConstData.WALLET, wallet)
                BlackRouter.getInstance().build(RouterConstData.RECHARGE).with(bundle).go(this)
            }
            R.id.btn_withdraw -> {
                val bundle = Bundle()
                bundle.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_WITHDRAW)
                bundle.putParcelable(ConstData.WALLET, wallet)
                BlackRouter.getInstance().build(RouterConstData.EXTRACT).with(bundle).go(this)
            }
            R.id.filter_layout -> {
                openFilterWindow()
            }
        }
    }

    private fun refreshWallet() {

        var usableText =  binding?.root?.findViewById<SpanTextView>(R.id.usable)
        usableText?.setText(if (wallet == null) nullAmount else NumberUtil.formatNumberNoGroup(wallet?.totalAmount , RoundingMode.FLOOR, 2, 8) + "USDT")

        var totalText =  binding?.root?.findViewById<SpanTextView>(R.id.tv_all_des)
        totalText?.setText(if (wallet == null) nullAmount else NumberUtil.formatNumberNoGroup(wallet?.coinAmount?.plus(BigDecimal(wallet?.coinFroze.toString())), RoundingMode.FLOOR, 2, 8))

        var totalCnyText =  binding?.root?.findViewById<SpanTextView>(R.id.total_cny)
        totalCnyText?.setText(if (wallet == null) nullAmount else "≈" + NumberUtil.formatNumberNoGroup(wallet?.estimatedAvailableAmountCny, RoundingMode.FLOOR, 2, 8) + "CNY")


        var usableFrozeText =  binding?.root?.findViewById<SpanTextView>(R.id.froze)
        usableFrozeText?.setText(if (wallet?.coinFroze == null) nullAmount else NumberUtil.formatNumberNoGroup(wallet?.coinFroze, 2, 8) + "USDT")

    }

    private fun getBillData(isShowLoading: Boolean) {
        if (wallet == null) {
            return
        }
        WalletApiServiceHelper.getWalletBillFiex(mContext, isShowLoading, wallet?.coinType, object : NormalCallback<HttpRequestResultData<PagingData<WalletBill?>?>?>(mContext!!) {
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

    private var dateFilter: DateFilter = DateFilter.ALL
    private var walletBillTypeFilter: WalletBillType = WalletBillType.ALL
    private fun openFilterWindow() {
        val showWindow = Runnable {
            val data: MutableList<FilterEntity<*>> = ArrayList()
            data.add(WalletBillType.getDefaultFilterEntity(mContext, walletBillType, walletBillTypeFilter))
            //                data.add(DateFilter.getDefaultFilterEntity(dateFilter));
            FilterWindow(mContext as Activity, data)
                    .show(object : FilterWindow.OnFilterSelectListener {
                        override fun onFilterSelect(filterWindow: FilterWindow?, selectResult: List<FilterResult<*>>) {
                            for (filterResult in selectResult) {
                                if (WalletBillType.KEY.equals(filterResult.key, ignoreCase = true)) {
                                    walletBillTypeFilter = filterResult.data as WalletBillType
                                } else if (DateFilter.KEY.equals(filterResult.key, ignoreCase = true)) {
                                    dateFilter = filterResult.data as DateFilter
                                }
                            }
                            currentPage = 1
                            getBillData(true)
                        }

                    })
        }
        if (walletBillType == null) {
            WalletApiServiceHelper.getWalletBillType(this, object : NormalCallback<HttpRequestResultDataList<WalletBillType?>?>(mContext!!) {

                override fun callback(returnData: HttpRequestResultDataList<WalletBillType?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        walletBillType = returnData.data
                        translateToTypeMap()
                        showWindow.run()
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        } else {
            showWindow.run()
        }
    }

    private fun translateToTypeMap() {
        walletBillTypeMap = HashMap()
        if (walletBillType == null || walletBillType!!.isEmpty()) {
            return
        }
        for (billType in walletBillType!!) {
            walletBillTypeMap!![billType?.billType] = billType?.zh
        }
        adapter?.setWalletBillTypeMap(walletBillTypeMap)
        adapter?.notifyDataSetChanged()
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

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
        val walletBill = adapter?.getItem(position)
        //点击账户详情
        val extras = Bundle()
        extras.putParcelable(ConstData.WALLET_BILL, walletBill)
        BlackRouter.getInstance().build(RouterConstData.WALLET_COIN_DETAIL).with(extras).go(this)
    }
}