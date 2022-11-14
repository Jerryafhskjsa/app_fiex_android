package com.black.frying.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActivity
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.PairApiServiceHelper
import com.black.base.api.TradeApiServiceHelper
import com.black.base.lib.FryingSingleToast
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.*
import com.black.base.model.filter.DateFilter
import com.black.base.model.filter.EntrustStatus
import com.black.base.model.filter.EntrustType
import com.black.base.model.socket.PairStatus
import com.black.base.model.socket.TradeOrder
import com.black.base.model.socket.TradeOrderFiex
import com.black.base.model.trade.TradeOrderHistoryResult
import com.black.base.util.*
import com.black.base.view.PairStatusPopupWindow
import com.black.frying.adapter.EntrustRecordNewAdapter
import com.black.frying.view.EntrustFilter
import com.black.frying.viewmodel.TransactionViewModel
import com.black.lib.refresh.QRefreshLayout
import com.black.lib.refresh.QRefreshLayout.OnLoadListener
import com.black.lib.refresh.QRefreshLayout.OnLoadMoreCheckListener
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.fbsex.exchange.BR
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ActivityEntrustRecordsNewBinding
import skin.support.content.res.SkinCompatResources
import java.util.*

//委托记录
@Route(value = [RouterConstData.ENTRUST_RECORDS_NEW], beforePath = RouterConstData.LOGIN)
class EntrustRecordsNewActivity : BaseActivity(), View.OnClickListener, EntrustRecordNewAdapter.OnHandleClickListener, QRefreshLayout.OnRefreshListener, OnLoadListener, OnLoadMoreCheckListener, OnItemClickListener {
    companion object {
        private const val TYPE_NEW = 0
        private const val TYPE_HIS = 1
    }

    private var currentType = 0
    private var openType = 0
    private var levelType: String? = TransactionViewModel.LEVER_TYPE_COIN
    private var routerPair: String? = null

    private var entrustType: EntrustType? = EntrustType.ALL
    private var entrustStatus = EntrustStatus.NEW
    private var dateFilter = DateFilter.ALL

    private var binding: ActivityEntrustRecordsNewBinding? = null

    private var headTitleView: TextView? = null

    private var adapter: EntrustRecordNewAdapter? = null
    private var currentPage = 1
    private var total = 0
    private var hasMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_entrust_records_new)

        openType = intent.getIntExtra(ConstData.OPEN_TYPE, 0)
        routerPair = intent.getStringExtra(ConstData.PAIR)
        levelType = intent.getStringExtra(ConstData.LEVEL_TYPE)
        entrustType = when (levelType) {
            TransactionViewModel.LEVER_TYPE_LEVER -> {
                EntrustType.LEVER
            }
            TransactionViewModel.LEVER_TYPE_COIN -> {
                EntrustType.COIN
            }
            else -> {
                EntrustType.ALL
            }
        }

        initHeaderLayout()

        binding?.pairChooseMenu?.setOnClickListener(this)
        binding?.pairChooseMenu?.setText(if (routerPair == null) getString(R.string.all_coin) else routerPair!!.replace("_", "/"))
        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding?.recyclerView?.layoutManager = layoutManager
        val decoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(this, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding?.recyclerView?.addItemDecoration(decoration)
        adapter = EntrustRecordNewAdapter(this, BR.listItemEntrustRecordModel, null)
        adapter?.setType(currentType)
        adapter?.setOnHandleClickListener(this)
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
        getTradeOrderCurrent(true)
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun initToolbarViews(toolbar: Toolbar) {
        toolbar.setOnClickListener {
            val entrustFilter = getEntrustFilter(binding?.filterLayout)
            if (entrustFilter.isShowing) {
                entrustFilter.dismiss()
            }
        }
    }

    private fun initHeaderLayout() {
        headTitleView = findViewById(R.id.action_bar_title)
        binding?.entrustNew?.setOnClickListener(this)
        binding?.entrustHis?.setOnClickListener(this)
        if (openType == 0) {
            //交易页当前交易对订单记录
            entrustStatus = EntrustStatus.NEW
            dateFilter = DateFilter.ALL
            currentType = TYPE_NEW
        } else { //历史所有交易对订单订单记录
            entrustStatus = EntrustStatus.HIS
            dateFilter = DateFilter.DAYS_3
            currentType = TYPE_HIS
        }
        binding?.filterLayout?.setOnClickListener(this)
        refreshCurrentType(currentType)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.entrust_new -> if (TYPE_NEW != currentType) {
                currentType = TYPE_NEW
                refreshCurrentType(TYPE_NEW)
                adapter?.setType(currentType)
                adapter?.clear()
                adapter?.notifyDataSetChanged()
                getTradeOrderCurrent(true)
            }
            R.id.entrust_his -> if (TYPE_HIS != currentType) {
                currentType = TYPE_HIS
                refreshCurrentType(TYPE_HIS)
                adapter?.setType(currentType)
                adapter?.clear()
                adapter?.notifyDataSetChanged()
                getTradeOrderCurrent(true)
            }
            R.id.filter_layout -> {
                //                openFilterWindow();
                val entrustFilter = getEntrustFilter(v)
                if (entrustFilter.isShowing) {
                    entrustFilter.dismiss()
                } else {
                    entrustFilter.show()
                }
            }
            R.id.pair_choose_menu -> PairApiServiceHelper.getTradeSetsLocal(mContext, true, object : NormalCallback<ArrayList<QuotationSet?>?>(mContext!!) {
                override fun callback(returnData: ArrayList<QuotationSet?>?) {
                    val type = PairStatusPopupWindow.TYPE_ENTRUST or (if (TransactionViewModel.LEVER_TYPE_LEVER == levelType) PairStatus.LEVER_DATA else PairStatus.NORMAL_DATA)
                    PairStatusPopupWindow.getInstance(mContext as Activity, type, returnData)
                            .show(object : PairStatusPopupWindow.OnPairStatusSelectListener {
                                override fun onPairStatusSelected(pairStatus: PairStatus?) {
                                    //交易对切换
                                    setCurrentPairStatus(pairStatus)
                                }

                            })
                }
            })
        }
    }

    override fun onBackPressed() {
        if (getEntrustFilter(binding?.filterLayout).isShowing) {
            getEntrustFilter(binding?.filterLayout).dismiss()
        } else {
            super.onBackPressed()
        }
    }

    private var entrustFilter: EntrustFilter? = null

    private fun getEntrustFilter(view: View?): EntrustFilter {
        if (entrustFilter == null) {
            var coinType: String? = null
            var setName: String? = null
            if (routerPair != null) {
                val arr = routerPair!!.split("_").toTypedArray()
                if (arr.size > 1) {
                    coinType = arr[0]
                    setName = arr[1]
                }
            }
            entrustFilter = EntrustFilter(this, view!!, coinType, setName, levelType, entrustType)
            entrustFilter?.setOnEntrustFilterListener(object : EntrustFilter.OnEntrustFilterListener {
                override fun onSelected(entrustFilter: EntrustFilter, coinType: String?, set: String?, entrustType: EntrustType?) {
                    var useCinType = coinType
                    var useSet = set
                    this@EntrustRecordsNewActivity.entrustType = entrustType
                    if (TextUtils.isEmpty(useCinType) && TextUtils.isEmpty(useSet)) {
                        routerPair = null
                        entrustFilter.dismiss()
                        currentPage = 1
                        getTradeOrderCurrent(true)
                    } else if (TextUtils.isEmpty(useCinType) && !TextUtils.isEmpty(useSet)) {
                        FryingUtil.showToast(mContext, resources.getString(R.string.pair_coin_type_empty), FryingSingleToast.ERROR)
                    } else {
                        entrustFilter.dismiss()
                        useCinType = useCinType?.toUpperCase(Locale.getDefault()) ?: ""
                        useSet = useSet?.toUpperCase(Locale.getDefault()) ?: ""
                        val pair = useCinType + "_" + useSet
                        SocketDataContainer.getPairStatus(mContext, pair, object : Callback<PairStatus?>() {
                            override fun error(type: Int, error: Any) {
                                FryingUtil.showToast(mContext, resources.getString(R.string.pair_error), FryingSingleToast.ERROR)
                            }

                            override fun callback(returnData: PairStatus?) {
                                if (returnData == null) {
                                    FryingUtil.showToast(mContext, resources.getString(R.string.pair_error), FryingSingleToast.ERROR)
                                } else {
                                    this@EntrustRecordsNewActivity.routerPair = returnData.pair
                                    currentPage = 1
                                    getTradeOrderCurrent(true)
                                }
                            }
                        })
                    }
                }

            })
        }
        return entrustFilter!!
    }

    private fun refreshCurrentType(type: Int) {
        if (TYPE_HIS == type) {
            binding?.entrustNew?.isChecked = false
            binding?.entrustNew?.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_20).toFloat())
            binding?.entrustHis?.isChecked = true
            binding?.entrustHis?.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_28).toFloat())
            entrustStatus = EntrustStatus.HIS
            headTitleView?.setText(R.string.filter_entrust_his)
        } else if (TYPE_NEW == type) {
            binding?.entrustNew?.isChecked = true
            binding?.entrustNew?.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_28).toFloat())
            binding?.entrustHis?.isChecked = false
            binding?.entrustHis?.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(R.dimen.text_size_20).toFloat())
            entrustStatus = EntrustStatus.NEW
            headTitleView?.setText(R.string.filter_entrust_new)
        }
    }

    private fun setCurrentPairStatus(pairStatus: PairStatus?) {
        routerPair = pairStatus?.pair
        binding?.pairChooseMenu?.text = if (routerPair == null) getString(R.string.all_coin) else routerPair!!.replace("_", "/")
        currentPage = 1
        getTradeOrderCurrent(true)
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
//        TradeOrder tradeOrder = adapter.getItem(position);
//        Bundle bundle = new Bundle();
//        bundle.putParcelable(ConstData.TRADE_ORDER, tradeOrder);
//        BlackRouter.getInstance().build(RouterConstData.ENTRUST_DETAIL).with(bundle).go(this);
    }

    //当前委托
    private fun getTradeOrderCurrent(isShowLoading: Boolean) {
        var pair = routerPair
        if(currentType == TYPE_HIS){
            pair = null
        }
        TradeApiServiceHelper.getTradeOrderHistoryRecord(mContext, pair,  isShowLoading, object : NormalCallback<HttpRequestResultData<TradeOrderHistoryResult?>?>(mContext!!) {
            override fun error(type: Int, error: Any?) {
                super.error(type, error)
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
            }

            override fun callback(returnData: HttpRequestResultData<TradeOrderHistoryResult?>?) {
                binding?.refreshLayout?.setRefreshing(false)
                binding?.refreshLayout?.setLoading(false)
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    hasMore = returnData.data?.hasNext != null && returnData.data?.hasNext!!
                    if (currentPage == 1) {
                        adapter?.data = returnData?.data?.items
                    } else {
                        adapter?.addAll(returnData?.data?.items)
                    }
                    adapter?.notifyDataSetChanged()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    override fun onPairClick(tradeOrder: TradeOrderFiex?) {
        val bundle = Bundle()
        bundle.putInt(ConstData.HOME_FRAGMENT_INDEX, 2)
        bundle.putInt(ConstData.TRANSACTION_INDEX, 1)
        bundle.putInt(ConstData.TRANSACTION_TYPE, ConstData.TAB_LEVER)
        BlackRouter.getInstance().build(RouterConstData.TRANSACTION)
                .with(bundle)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .go(this) { routeResult, error ->
                    if (error != null) {
                        FryingUtil.printError(error)
                    }
                    if (routeResult) {
                        finish()
                    }
                }
    }

    override fun onHandleClick(tradeOrder: TradeOrderFiex?) {
//        TradeApiServiceHelper.cancelTradeOrder(mContext, tradeOrder!!.id, tradeOrder.pair, tradeOrder.direction, object : NormalCallback<HttpRequestResultString?>() {
//            override fun callback(returnData: HttpRequestResultString?) {
//                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
//                    adapter?.removeItem(tradeOrder)
//                    adapter?.notifyDataSetChanged()
//                } else {
//                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
//                }
//            }
//        })
    }

    override fun onRefresh() {
        currentPage = 1
        getTradeOrderCurrent(false)
    }

    override fun onLoad() {
        if (total > adapter?.count ?: 0 || hasMore) {
            currentPage += 1
            getTradeOrderCurrent(true)
        }
    }

    override fun onLoadMoreCheck(): Boolean {
        return total > adapter?.count ?: 0 || hasMore
    }
}