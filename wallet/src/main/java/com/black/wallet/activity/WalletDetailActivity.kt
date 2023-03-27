package com.black.wallet.activity

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.C2CApiServiceHelper
import com.black.base.api.PairApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.lib.filter.FilterEntity
import com.black.base.lib.filter.FilterResult
import com.black.base.lib.filter.FilterWindow
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.*
import com.black.base.model.filter.DateFilter
import com.black.base.model.socket.PairStatus
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletBill
import com.black.base.model.wallet.WalletBillType
import com.black.base.util.*
import com.black.base.widget.SpanTextView
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.NumberUtil
import com.black.wallet.BR
import com.black.wallet.R
import com.black.wallet.adapter.QuotationAdapter
import com.black.wallet.adapter.WalletBillAdapter
import com.black.wallet.databinding.ActivityWalletDetailBinding
import com.black.wallet.util.DipPx
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

//现货币种详情
@Route(value = [RouterConstData.WALLET_DETAIL])
class WalletDetailActivity : BaseActivity(),
    View.OnClickListener,
    QRefreshLayout.OnRefreshListener,
    QRefreshLayout.OnLoadListener,
    QRefreshLayout.OnLoadMoreCheckListener,
    OnItemClickListener, AdapterView.OnItemClickListener {
    private var wallet: Wallet? = null
    private var coinType: String? = null
    private var binding: ActivityWalletDetailBinding? = null
    private var rate = C2CApiServiceHelper?.coinUsdtPrice?.usdtToUsd
    private var sets: ArrayList<QuotationSet?>? = null
    private var fragmentList: MutableList<Fragment?>? = null
    private var adapter: WalletBillAdapter? = null
    private var currentPage = 1
    private var total = 0
    private var hasMore = false
    private var walletBillType: List<WalletBillType?>? = null
    private var walletBillTypeMap: MutableMap<String?, String?>? = null
    private var adapter2: QuotationAdapter? = null
    private val dataList = ArrayList<PairStatus?>()

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
        binding?.transaction?.setOnClickListener(this)

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
        binding?.recyclerView?.layoutManager = layoutManager
        val drawable2 = ColorDrawable()
        drawable2.color = SkinCompatResources.getColor(mContext, R.color.C2)
        drawable2.alpha = (0xff * 0.3).toInt()
        binding?.listView?.divider = drawable2
        binding?.listView?.dividerHeight = 1
        adapter2 = QuotationAdapter(mContext, dataList)
        binding?.listView?.adapter = adapter2
        binding?.listView?.onItemClickListener = this
        binding?.refreshLayout?.setRefreshHolder(RefreshHolderFrying(this))
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.refreshLayout?.setOnLoadListener(this)
        binding?.refreshLayout?.setOnLoadMoreCheckListener(this)
        refreshWallet()
        refreshSets()
        getBillData(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun getTitleText(): String? {
        return if (wallet != null) if (wallet?.coinType == null) "" else wallet?.coinType else (if (coinType == null) "" else coinType)
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
            R.id.transaction -> {
                BlackRouter.getInstance().build(RouterConstData.TRANSACTION)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .go(this)
            }
        }
    }

    private fun refreshWallet() {


        val totalText =  binding?.root?.findViewById<SpanTextView>(R.id.tv_all_des)
        totalText?.setText(if (wallet == null) nullAmount else NumberUtil.formatNumberNoGroup(wallet?.coinAmount?.plus(BigDecimal(wallet?.coinFroze.toString())), RoundingMode.FLOOR, 2, 8) + wallet?.coinType)

        val totalCnyText =  binding?.root?.findViewById<SpanTextView>(R.id.total_cny)
        totalCnyText?.setText(if (wallet == null) nullAmount else "≈" + NumberUtil.formatNumberDynamicScaleNoGroup(
            rate!! * (wallet?.estimatedAvailableAmountCny!!),
            10,
            2,
            2
        ) + "USD")

        val able =  binding?.root?.findViewById<SpanTextView>(R.id.able)
        able?.setText(if (wallet == null) nullAmount else  "≈" + NumberUtil.formatNumberNoGroup(wallet?.estimatedAvailableAmountCny?.toDouble() , RoundingMode.FLOOR, 2, 8) + "USDT")

        val unable =  binding?.root?.findViewById<SpanTextView>(R.id.unable)
        unable?.setText(if (wallet == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(
            wallet?.coinFroze,
            10,
            2,
            2
        ) + wallet?.coinType)
        /*if (wallet?.coinIconUrl != null) {
            var requestOptions = RequestOptions
                .bitmapTransform(RoundedCorners(DipPx.dip2px(mContext, 15f)))

            Glide.with(mContext)
                .load(Uri.parse(UrlConfig.getCoinIconUrl(mContext, wallet?.coinIconUrl)))
                .apply(requestOptions)
                .into(binding?.root?.findViewById(R.id.icon_coin))
        }*/
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
    private fun refreshSets() {

            PairApiServiceHelper.getTradeSetsLocal(mContext, false, object : Callback<ArrayList<QuotationSet?>?>() {
                override fun error(type: Int, error: Any) {
                }
                override fun callback(returnData: ArrayList<QuotationSet?>?) {
                    if (returnData != null) {
                        var setData = ArrayList<QuotationSet?>()
                        setData?.addAll(returnData)
                        var optionalSet = QuotationSet()
                        setData?.add(0,  optionalSet)
                        if (setData != null && setData?.isNotEmpty()) {
                            sets = setData
                            initQuotationGroup()
                        }
                    }
                }
            })


    }
    private fun initQuotationGroup() {
        if (sets != null && sets!!.isNotEmpty()) {
            val setSize = sets!!.size
            if (fragmentList != null) {
                return
            }
            fragmentList = ArrayList(setSize)
            for (i in 0 until setSize) {
                val set = sets!![i]
                try {

                } catch (throwable: Throwable) {
                    FryingUtil.printError(throwable)
                }
            }

        }
    }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        mContext?.let {
            val pairStatus = adapter2?.getItem(position)
                CookieUtil.setCurrentPair(it, pairStatus?.pair)
                sendPairChangedBroadcast(SocketUtil.COMMAND_PAIR_CHANGED)
                val bundle = Bundle()
                bundle.putString(ConstData.PAIR, pairStatus?.pair)
                BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).with(bundle)
                    .go(it)
            }


    }
}