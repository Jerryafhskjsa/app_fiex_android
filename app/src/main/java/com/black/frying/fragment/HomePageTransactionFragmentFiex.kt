package com.black.frying.fragment

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.CheckedTextView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.PairApiServiceHelper
import com.black.base.api.TradeApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.*
import com.black.base.model.clutter.HomeTickers
import com.black.base.model.socket.*
import com.black.base.model.trade.TradeOrderResult
import com.black.base.model.user.UserBalance
import com.black.base.model.wallet.CoinInfoType
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletLeverDetail
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.view.AlertMessageDialog
import com.black.base.view.DeepControllerWindow
import com.black.base.view.PairStatusPopupWindow
import com.black.base.view.PairStatusPopupWindow.OnPairStatusSelectListener
import com.black.frying.activity.HomePageActivity
import com.black.frying.adapter.EntrustCurrentHomeAdapter
import com.black.frying.view.TransactionDeepViewBinding
import com.black.frying.view.TransactionDeepViewBinding.OnTransactionDeepListener
import com.black.frying.view.TransactionMorePopup
import com.black.frying.view.TransactionMorePopup.OnTransactionMoreClickListener
import com.black.frying.viewmodel.TransactionViewModel
import com.black.frying.viewmodel.TransactionViewModel.OnTransactionModelListener
import com.black.im.util.IMHelper
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpCookieUtil
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.BR
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageTransactionFiexBinding
import io.reactivex.Observable
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

//首页交易
@Route(
    value = [RouterConstData.TRANSACTION],
    fragmentParentPath = RouterConstData.HOME_PAGE,
    fragmentIndex = 2
)
class HomePageTransactionFragmentFiex : BaseFragment(),
    View.OnClickListener,
    OnSeekBarChangeListener,
    EntrustCurrentHomeAdapter.OnHandleClickListener,
    OnItemClickListener,
    OnTransactionMoreClickListener,
    OnTransactionModelListener,
    OnTransactionDeepListener {
    companion object {
        private var TAG = HomePageTransactionFragmentFiex::class.java.simpleName
    }

    private var colorWin = 0
    private var colorLost = 0
    private var colorT3 = 0

    private var transactionType = 1 //1 买入 2卖出
    private var tabType = ConstData.TAB_COIN

    private var countProgressBuy: Drawable? = null
    private var countProgressSale: Drawable? = null
    private var currentOrderType: String? = "LIMIT"
    private var inputNumber: Boolean? = false//是否手动输入数量
    private var isDear: Boolean? = null

    private var adapter: EntrustCurrentHomeAdapter? = null

    private var isShowAll = false

    /**
     * 卖出使用 当前币种
     */
    private var currentWallet: Wallet? = null

    /**
     * 买入使用 交易区币种
     */
    private var currentEstimatedWallet: Wallet? = null

    /**
     * 买入可使用资产
     */
    private var currentBalanceBuy: UserBalance? = null

    /**
     * 卖出可使用资产
     */
    private var currentBalanceSell: UserBalance? = null

    private var layout: View? = null
    private var binding: FragmentHomePageTransactionFiexBinding? = null
    private var viewModel: TransactionViewModel? = null
    private var deepViewBinding: TransactionDeepViewBinding? = null

    /**
     * 用户资产
     */
    private var userBalance: ArrayList<UserBalance?>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val start = System.currentTimeMillis()
        if (layout != null) {
            return layout
        }
        if ((mContext as HomePageActivity).transactionIndex != -1) {
            transactionType = (mContext as HomePageActivity).transactionIndex
        }
        if ((mContext as HomePageActivity).transactionTabType != -1) {
            tabType = (mContext as HomePageActivity).transactionTabType
        }
        colorWin = SkinCompatResources.getColor(mContext, R.color.T7)
        colorLost = SkinCompatResources.getColor(mContext, R.color.T5)
        colorT3 = SkinCompatResources.getColor(mContext, R.color.T3)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home_page_transaction_fiex,
            container,
            false
        )
        layout = binding?.root
        StatusBarUtil.addStatusBarPadding(layout)
        viewModel = TransactionViewModel(mContext!!, this)
        binding!!.refreshLayout.setRefreshHolder(RefreshHolderFrying(activity!!))
        binding!!.refreshLayout.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                binding!!.refreshLayout.postDelayed(
                    { binding!!.refreshLayout.setRefreshing(false) },
                    300
                )
            }

        })
        deepViewBinding = TransactionDeepViewBinding(
            mContext!!,
            viewModel!!,
            binding!!.fragmentHomePageTransactionHeader1
        )
        deepViewBinding?.setOnTransactionDeepListener(this)

        binding?.actionBarLayout?.btnTransactionMemu?.setOnClickListener(this)
        binding?.actionBarLayout?.headCharts?.setOnClickListener(this)
        binding?.actionBarLayout?.headTransactionMore?.setOnClickListener(this)
        binding?.actionBarLayout?.riskInfo?.setOnClickListener(this)
        binding?.actionBarLayout?.leverHandle?.setOnClickListener(this)
        binding?.actionBarLayout?.leverLayout?.visibility =
            if (tabType == ConstData.TAB_LEVER) View.VISIBLE else View.GONE
        binding?.actionBarLayout?.imgCollect?.setOnClickListener(this)
        initHeader1()
        initHeader2()
        val layoutManager = LinearLayoutManager(mContext)
        layoutManager.orientation = RecyclerView.VERTICAL
        layoutManager.isSmoothScrollbarEnabled = true
        binding!!.recyclerView.layoutManager = layoutManager
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(context, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding!!.recyclerView.addItemDecoration(decoration)
        adapter = EntrustCurrentHomeAdapter(mContext!!, BR.listItemEntrustCurrentHomeModel, null)
        adapter?.setOnHandleClickListener(this)
        adapter?.setOnItemClickListener(this)
        binding!!.recyclerView.adapter = adapter
        binding!!.recyclerView.isNestedScrollingEnabled = false
        binding!!.recyclerView.setEmptyView(binding?.emptyView?.root)
        binding!!.recyclerView.isNestedScrollingEnabled = false
        binding!!.recyclerView.setHasFixedSize(true)
        binding!!.recyclerView.isFocusable = false
        binding!!.fragmentHomePageTransactionHeader2.showAllCheckbox.setOnClickListener(this)
        return layout
    }

    override fun getViewModel(): TransactionViewModel? {
        return viewModel
    }

    override fun onResume() {
        super.onResume()
        viewModel?.setTabType(tabType)
        viewModel?.getCurrentUserBalance(ConstData.BalanceType.SPOT)
        viewModel?.getCurrentPairDepth(50)
        viewModel?.getCurrentPairDeal(50)
        viewModel?.onResume()
        getTradeOrderCurrent()
        updateDear(isDear)
        initTicker()
    }

    fun initTicker() {
        PairApiServiceHelper.getSymbolTicker(
            viewModel?.getCurrentPair().toString(),
            mContext,
            object : Callback<HttpRequestResultData<HomeTickers?>?>() {
                override fun error(type: Int, error: Any?) {
                    Log.d("ttt--->ticker---",error.toString())
                }

                override fun callback(returnData: HttpRequestResultData<HomeTickers?>?) {
                    var homeTicker = returnData?.data
                    Log.d("ttt--->ticker---",homeTicker.toString())
                    updatePriceSince(homeTicker!!.r)
                }

            })
    }

    private fun updateDear(dear: Boolean?) {
        if (dear == null) {
            viewModel!!.checkDearPair()
                ?.subscribe(HttpCallbackSimple(mContext, false, object : Callback<Boolean>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(dearResult: Boolean) {
                        isDear = dearResult
                        if (isDear!!) {
                            binding?.actionBarLayout?.imgCollect?.setImageDrawable(
                                mContext?.getDrawable(
                                    R.drawable.btn_collect_dis
                                )
                            )
                        } else {
                            binding?.actionBarLayout?.imgCollect?.setImageDrawable(
                                mContext?.getDrawable(
                                    R.drawable.btn_collect_default
                                )
                            )
                        }
                    }
                }))
        } else {
            isDear = dear
            if (isDear!!) {
                binding?.actionBarLayout?.imgCollect?.setImageDrawable(mContext?.getDrawable(R.drawable.btn_collect_dis))
            } else {
                binding?.actionBarLayout?.imgCollect?.setImageDrawable(mContext?.getDrawable(R.drawable.btn_collect_default))
            }
        }
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
//        TradeOrder tradeOrder = adapter.getItem(position);
//        Bundle bundle = new Bundle();
//        bundle.putParcelable(ConstData.TRADE_ORDER, tradeOrder);
//        BlackRouter.getInstance().build(RouterConstData.ENTRUST_DETAIL).with(bundle).go(this);
    }

    //买卖功能
    private fun initHeader1() {
        binding!!.fragmentHomePageTransactionHeader1.linOrderType.setOnClickListener(this)
        binding!!.fragmentHomePageTransactionHeader1.btnBuy.setOnClickListener(this)
        binding!!.fragmentHomePageTransactionHeader1.btnSale.setOnClickListener(this)
        binding!!.fragmentHomePageTransactionHeader1.price.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                computeTotal()
                computePriceCNY()
                refreshSubmitButton()
            }

            override fun afterTextChanged(s: Editable) {
                binding!!.fragmentHomePageTransactionHeader1.price.setSelection(s.toString().length)
            }
        })
        binding!!.fragmentHomePageTransactionHeader1.transactionQuota.filters =
            arrayOf(NumberFilter(), PointLengthFilter(4))
        binding!!.fragmentHomePageTransactionHeader1.transactionQuota.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                inputNumber = true
                computeTotal()
                refreshSubmitButton()
                val count = CommonUtil.parseDouble(
                    binding!!.fragmentHomePageTransactionHeader1.transactionQuota.text.toString()
                        .trim { it <= ' ' })
                if (count != null) {
                    val max: BigDecimal? = getMaxAmount()
                    if (max != null) {
                        var countB = count?.let { BigDecimal(it) }
                        var progress = (countB?.divide(max, 2, BigDecimal.ROUND_HALF_DOWN))?.times(
                            BigDecimal(100)
                        )
                        binding!!.fragmentHomePageTransactionHeader1.countBar.progress =
                            progress?.toInt()!!
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                inputNumber = false
                binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setSelection(s.toString().length)
            }
        })
        countProgressBuy =
            SkinCompatResources.getDrawable(mContext, R.drawable.bg_transaction_progress_bar_buy)
        countProgressSale =
            SkinCompatResources.getDrawable(mContext, R.drawable.bg_transaction_progress_bar_sale)
        binding!!.fragmentHomePageTransactionHeader1.priceSub.setOnClickListener(this)
        binding!!.fragmentHomePageTransactionHeader1.priceAdd.setOnClickListener(this)
        binding!!.fragmentHomePageTransactionHeader1.amountAdd.setOnClickListener(this)
        binding!!.fragmentHomePageTransactionHeader1.amountSub.setOnClickListener(this)
        binding!!.fragmentHomePageTransactionHeader1.useable.setText(getString(R.string.number_default))
        binding!!.fragmentHomePageTransactionHeader1.useableUnit.setText(getString(R.string.number_default))
        binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText(getString(R.string.number_default))
        binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(getString(R.string.number_default))
        binding!!.fragmentHomePageTransactionHeader1.countBar.setOnSeekBarChangeListener(this)
        binding!!.fragmentHomePageTransactionHeader1.btnHandle.setOnClickListener(this)
        viewModel?.setCurrentPairorderType(currentOrderType)
        refreshOrderType(currentOrderType)
        deepViewBinding!!.init()
    }

    private fun initHeader2() {
        binding!!.fragmentHomePageTransactionHeader2.totalCurrent.setOnClickListener(this)
    }

    private fun initView() {
        clearInput()
        refreshUsable()
        refreshTransactionHardViews()
        refreshSubmitButton()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.show_all_checkbox -> {
                var checkBox: CheckBox = v as CheckBox
                Log.d("sss--->", checkBox.isChecked.toString())
                isShowAll = checkBox.isChecked
                getTradeOrderCurrent()
            }
            R.id.tab_transaction_coin -> {
                if (tabType != ConstData.TAB_COIN) {
                    changeTabType(ConstData.TAB_COIN)
                }
            }
            R.id.tab_transaction_lever -> {
                if (tabType != ConstData.TAB_LEVER) {
                    changeTabType(ConstData.TAB_LEVER)
                }
            }
            R.id.risk_info -> {
                if (mContext != null && tabType == ConstData.TAB_LEVER && TextUtils.equals(
                        viewModel?.getCurrentPair(),
                        leverDetail?.pair
                    ) && leverDetail?.burstRate != null && leverDetail?.burstRate != BigDecimal.ZERO
                ) {
                    leverDetail?.run {
                        AlertMessageDialog(
                            mContext!!,
                            "爆仓说明",
                            String.format(
                                "当前风险率达到%s%%时，系统将强制回收您当前账户内的所有借贷资产。",
                                NumberUtil.formatNumberNoGroupHardScale(
                                    leverDetail?.burstRate!! * BigDecimal(100), 2
                                )
                            )
                        )
                            .show()
                    }
                }
            }
            R.id.img_collect -> {
                viewModel!!.toggleDearPair(isDear!!)
                    ?.subscribe(
                        HttpCallbackSimple(
                            mContext,
                            true,
                            object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                                override fun callback(result: HttpRequestResultString?) {
                                    if (result != null && result.code == HttpRequestResult.SUCCESS) {
                                        isDear = !isDear!!
                                        updateDear(isDear)
                                        val showMsg =
                                            if (isDear!!) getString(R.string.pair_collect_add_ok) else getString(
                                                R.string.pair_collect_cancel_ok
                                            )
                                        FryingUtil.showToast(mContext, showMsg)
                                    } else {
                                        FryingUtil.showToast(
                                            mContext,
                                            if (result == null) "null" else result.msg
                                        )
                                    }
                                }
                            })
                    )
            }
            R.id.lever_handle -> {
                if (tabType == ConstData.TAB_LEVER) {
                    val pair = viewModel?.getCurrentPair()
                    pair?.run {
                        val bundle = Bundle()
                        bundle.putString(ConstData.PAIR, pair)
                        BlackRouter.getInstance().build(RouterConstData.WALLET_LEVER_DETAIL)
                            .with(bundle).go(mContext)
                    }
                }
            }
            R.id.lin_order_type -> {
                DeepControllerWindow(mContext as Activity,
                    getString(R.string.select_order_type),
                    currentOrderType,
                    viewModel?.getCurrentPairOrderTypeList() as List<String?>?,
                    object : DeepControllerWindow.OnReturnListener<String?> {
                        override fun onReturn(
                            window: DeepControllerWindow<String?>,
                            item: String?
                        ) {
                            refreshOrderType(item)
                            currentOrderType = item
                            viewModel?.setCurrentPairorderType(item)
                            if (currentOrderType.equals("LIMIT")) {
                                binding?.fragmentHomePageTransactionHeader1?.relVolume?.visibility =
                                    View.VISIBLE
                            } else if (currentOrderType.equals("MARKET")) {
                                binding?.fragmentHomePageTransactionHeader1?.relVolume?.visibility =
                                    View.GONE
                            }
                        }
                    }).show()
            }
            R.id.tab_transaction_c2c -> BlackRouter.getInstance().build(RouterConstData.C2C_NEW)
                .go(mContext)
            R.id.head_charts -> if (mContext != null && !TextUtils.isEmpty(
                    CookieUtil.getCurrentPair(
                        mContext!!
                    )
                )
            ) {
                val bundle = Bundle()
                bundle.putString(ConstData.PAIR, viewModel?.getCurrentPair())
                BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).with(bundle)
                    .go(mContext)
            }
            R.id.head_transaction_more -> viewModel!!.checkDearPair()
                ?.subscribe(HttpCallbackSimple(mContext, true, object : Callback<Boolean>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(returnData: Boolean) {
//                            WalletApiServiceHelper.getCoinInfo(mContext, viewModel!!.getCoinType(), object : Callback<CoinInfo?>() {
//                                override fun error(type: Int, error: Any) {
//                                    CommonUtil.checkActivityAndRunOnUI(mContext) {
//                                        TransactionMorePopup(mContext!!, returnData
//                                                ?: false, null).setOnTransactionMoreClickListener(this@HomePageTransactionFragmentFiex).show(v)
//                                    }
//                                }
//
//                                override fun callback(coinInfo: CoinInfo?) {
//                                    CommonUtil.checkActivityAndRunOnUI(mContext) {
//                                        TransactionMorePopup(mContext!!, returnData
//                                                ?: false, coinInfo?.groupId).setOnTransactionMoreClickListener(this@HomePageTransactionFragmentFiex).show(v)
//                                    }
//                                }
//                            })
                    }
                }))
            R.id.btn_transaction_memu -> mContext?.let {
                PairApiServiceHelper.getTradeSetsLocal(
                    it,
                    true,
                    object : Callback<ArrayList<QuotationSet?>?>() {
                        override fun callback(returnData: ArrayList<QuotationSet?>?) {
                            if (returnData != null) {
                                PairStatusPopupWindow.getInstance(
                                    it,
                                    PairStatusPopupWindow.TYPE_TRANSACTION,
                                    returnData
                                )
                                    .show(object : OnPairStatusSelectListener {
                                        override fun onPairStatusSelected(pairStatus: PairStatus?) {
                                            if (pairStatus == null) {
                                                return
                                            }
                                            //交易对切换
                                            if (!TextUtils.equals(
                                                    viewModel?.getCurrentPair(),
                                                    pairStatus.pair
                                                )
                                            ) { //清空价格，数量
                                                onPairStatusChanged(pairStatus)
                                            }
                                        }
                                    })
                            }
                        }

                        override fun error(type: Int, error: Any?) {
                        }
                    })
            }
            R.id.total_current ->  //全部委托
                mContext?.let {
                    if (CookieUtil.getUserInfo(it) == null) {
                        BlackRouter.getInstance().build(RouterConstData.LOGIN).go(it)
                    } else if (!TextUtils.isEmpty(viewModel?.getCurrentPair())) {
                        val extras = Bundle()
                        extras.putInt(ConstData.OPEN_TYPE, 0)
                        extras.putString(ConstData.PAIR, viewModel?.getCurrentPair())
                        extras.putString(
                            ConstData.LEVEL_TYPE,
                            if (tabType == ConstData.TAB_LEVER) TransactionViewModel.LEVER_TYPE_LEVER else TransactionViewModel.LEVER_TYPE_COIN
                        )
                        BlackRouter.getInstance().build(RouterConstData.ENTRUST_RECORDS_NEW)
                            .with(extras).go(it)
                    }
                }
            R.id.price_sub -> {
                var currentInputPrice = CommonUtil.parseDouble(
                    binding!!.fragmentHomePageTransactionHeader1.price.text.toString()
                        .trim { it <= ' ' })
                currentInputPrice = currentInputPrice ?: 0.toDouble()
                val onUnitPrice: Double = getOnUnitPrice()
                if (currentInputPrice > 0) {
                    currentInputPrice -= onUnitPrice
                    currentInputPrice = max(currentInputPrice, 0.0)
                    binding!!.fragmentHomePageTransactionHeader1.price.setText(
                        String.format(
                            "%." + viewModel!!.getPrecision() + "f",
                            currentInputPrice
                        )
                    )
                }
            }
            R.id.price_add -> {
                var currentInputPrice = CommonUtil.parseDouble(
                    binding!!.fragmentHomePageTransactionHeader1.price.text.toString()
                        .trim { it <= ' ' })
                currentInputPrice = currentInputPrice ?: 0.toDouble()
                val onUnitPrice: Double = getOnUnitPrice()
                currentInputPrice += onUnitPrice
                binding!!.fragmentHomePageTransactionHeader1.price.setText(
                    String.format(
                        "%." + viewModel!!.getPrecision() + "f",
                        currentInputPrice
                    )
                )
            }
            R.id.amount_add -> {
                var currentInputAmount = CommonUtil.parseDouble(
                    binding!!.fragmentHomePageTransactionHeader1.transactionQuota.text.toString()
                        .trim { it <= ' ' })
                currentInputAmount = currentInputAmount ?: 0.toDouble()
                val onUnitAmount: Double = getOnUnitAmount()
                currentInputAmount += onUnitAmount
                binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText(
                    String.format(
                        "%." + viewModel!!.getAmountLength() + "f",
                        currentInputAmount
                    )
                )
            }
            R.id.amount_sub -> {
                var currentInputAmount = CommonUtil.parseDouble(
                    binding!!.fragmentHomePageTransactionHeader1.transactionQuota.text.toString()
                        .trim { it <= ' ' })
                currentInputAmount = currentInputAmount ?: 0.toDouble()
                val onUnitAmount: Double = getOnUnitAmount()
                if (currentInputAmount > 0) {
                    currentInputAmount -= onUnitAmount
                    currentInputAmount = max(currentInputAmount, 0.0)
                    binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText(
                        String.format(
                            "%." + viewModel!!.getAmountLength() + "f",
                            currentInputAmount
                        )
                    )
                }
            }
            R.id.btn_buy -> context?.let {
                if (CookieUtil.getUserInfo(it) == null) {
                    fryingHelper.checkUserAndDoing(Runnable { }, TRADE_INDEX)
                } else {
                    if (transactionType != 1) {
                        transactionType = 1
                        //                    refreshSeekBar();
                        initView()
                    }
                }
            }
            R.id.btn_sale -> context?.let {
                if (CookieUtil.getUserInfo(it) == null) {
                    fryingHelper.checkUserAndDoing(Runnable { }, TRADE_INDEX)
                } else {
                    if (transactionType != 2) {
                        transactionType = 2
                        //                    refreshSeekBar();
                        initView()
                    }
                }
            }
            R.id.btn_handle -> mContext?.let {
                if (CookieUtil.getUserInfo(it) == null) {
                    //未登录，请求登陆
                    fryingHelper.checkUserAndDoing(Runnable { }, TRADE_INDEX)
                } else {
                    if (viewModel!!.getCoinType() != null) {
                        WalletApiServiceHelper.getCoinInfo(
                            mContext,
                            viewModel!!.getCoinType(),
                            object : Callback<CoinInfoType?>() {
                                override fun callback(returnData: CoinInfoType?) {
                                    if (returnData != null) {
                                        var coinInfo = returnData.config?.get(0)?.coinConfigVO
                                        if (coinInfo?.supportTrade != null && true == coinInfo.supportTrade) {
                                            if (transactionType == 1) { //买入
                                                createOrder("BUY")
                                            } else if (transactionType == 2) { //卖出
                                                createOrder("SELL")
                                            }
                                        } else {
                                            FryingUtil.showToast(
                                                mContext,
                                                getString(
                                                    R.string.alert_trade_not_support,
                                                    viewModel!!.getCoinType()
                                                )
                                            )
                                        }
                                    } else {
                                        FryingUtil.showToast(
                                            mContext,
                                            getString(
                                                R.string.alert_trade_not_support,
                                                viewModel!!.getCoinType()
                                            )
                                        )
                                    }
                                }

                                override fun error(type: Int, error: Any) {
                                    FryingUtil.showToast(mContext, error.toString())
                                }
                            })
                    }
                }
            }
        }
    }

    private fun changeTabType(tabType: Int) {
        this.tabType = tabType
        binding?.actionBarLayout?.leverLayout?.visibility =
            if (tabType == ConstData.TAB_LEVER) View.VISIBLE else View.GONE
        viewModel!!.setTabType(tabType)
        viewModel!!.changePairSocket()
        adapter?.setAmountPrecision(viewModel!!.getAmountLength())
        resetPriceLength()
        resetAmountLength()
        refreshCurrentWallet()
        refreshTransactionHardViews()
        refreshUsable()
        refreshData()
    }

    override fun onPairDeal(value: PairDeal) {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            updateCurrentPairPrice(value.p)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        //显示滑块选择的数量
        binding!!.fragmentHomePageTransactionHeader1.countProgress.progress = progress
        val amountPercent = progress.toDouble() / seekBar.max
        val max: BigDecimal? = getMaxAmount()
        if (!inputNumber!!) {
            if (max == null || max == BigDecimal.ZERO) {
                binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText("0.00")
            } else {
                binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText(
                    NumberUtil.formatNumberNoGroup(
                        max * BigDecimal(amountPercent),
                        RoundingMode.FLOOR,
                        0,
                        viewModel!!.getAmountLength()
                    )
                )
            }
        }
        onCountProgressClick(progress * 5 / 100)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}

    override fun onStopTrackingTouch(seekBar: SeekBar?) {}

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        when {
            isVisibleToUser -> {
                //setCurrentPairStatus(currentPairStatus);
            }
        }
    }

    override fun doResetSkinResources() {
        colorWin = SkinCompatResources.getColor(mContext, R.color.T7)
        colorLost = SkinCompatResources.getColor(mContext, R.color.T5)
        colorT3 = SkinCompatResources.getColor(mContext, R.color.T3)
        val decoration = DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL)
        val drawable = SkinCompatResources.getDrawable(context, R.drawable.divider_list_item_l1)
        drawable.alpha = (0.6 * 255).toInt()
        decoration.setDrawable(drawable)
        binding!!.recyclerView.addItemDecoration(decoration)
        binding!!.fragmentHomePageTransactionHeader1.price.resetRes()
        binding!!.fragmentHomePageTransactionHeader1.transactionQuota.resetRes()
        countProgressBuy =
            SkinCompatResources.getDrawable(mContext, R.drawable.bg_transaction_progress_bar_buy)
        countProgressSale =
            SkinCompatResources.getDrawable(mContext, R.drawable.bg_transaction_progress_bar_sale)
        //        countProgress.setEnabled(false);
        deepViewBinding!!.doResetSkinResources()
    }

    private fun onCountProgressClick(type: Int) {
        when (type) {
            0 -> {
                binding!!.fragmentHomePageTransactionHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountTwenty.isChecked = false
                binding!!.fragmentHomePageTransactionHeader1.amountFourty.isChecked = false
                binding!!.fragmentHomePageTransactionHeader1.amountSixty.isChecked = false
                binding!!.fragmentHomePageTransactionHeader1.amountEighty.isChecked = false
                binding!!.fragmentHomePageTransactionHeader1.amountAll.isChecked = false
            }
            1 -> {
                binding!!.fragmentHomePageTransactionHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountTwenty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountFourty.isChecked = false
                binding!!.fragmentHomePageTransactionHeader1.amountSixty.isChecked = false
                binding!!.fragmentHomePageTransactionHeader1.amountEighty.isChecked = false
                binding!!.fragmentHomePageTransactionHeader1.amountAll.isChecked = false
            }
            2 -> {
                binding!!.fragmentHomePageTransactionHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountTwenty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountFourty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountSixty.isChecked = false
                binding!!.fragmentHomePageTransactionHeader1.amountEighty.isChecked = false
                binding!!.fragmentHomePageTransactionHeader1.amountAll.isChecked = false
            }
            3 -> {
                binding!!.fragmentHomePageTransactionHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountTwenty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountFourty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountSixty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountEighty.isChecked = false
                binding!!.fragmentHomePageTransactionHeader1.amountAll.isChecked = false
            }
            4 -> {
                binding!!.fragmentHomePageTransactionHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountTwenty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountFourty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountSixty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountEighty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountAll.isChecked = false
            }
            5 -> {
                binding!!.fragmentHomePageTransactionHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountTwenty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountFourty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountSixty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountEighty.isChecked = true
                binding!!.fragmentHomePageTransactionHeader1.amountAll.isChecked = true
            }
        }
    }

    //处理点击，撤销订单
    override fun onHandleClick(tradeOrder: TradeOrderFiex) {
        //新订单可以撤销
        cancelTradeOrder(tradeOrder)
    }

    //计算最大交易数量
    private fun getMaxAmount(): BigDecimal? {
        if (transactionType == 1) {
            val usable = currentBalanceSell?.availableBalance
            val price =
                CommonUtil.parseDouble(binding!!.fragmentHomePageTransactionHeader1.price.text.toString())
            return if (usable == null || price == null || price == 0.0) null else BigDecimal(usable).divide(
                BigDecimal(price),
                2,
                BigDecimal.ROUND_HALF_DOWN
            )
        } else if (transactionType == 2) {
            return currentBalanceBuy?.availableBalance?.toBigDecimal()
        }
        return null
    }


    private fun resetAmountLength() {
        binding!!.fragmentHomePageTransactionHeader1.transactionQuota.filters =
            arrayOf(NumberFilter(), PointLengthFilter(viewModel!!.getAmountLength()))
    }

    private fun resetPriceLength() {
        binding!!.fragmentHomePageTransactionHeader1.price.filters = arrayOf(
            NumberFilter(), PointLengthFilter(
                viewModel?.getPrecision()
                    ?: 8
            )
        )
    }

    fun setTransactionType(type: Int) {
        if (type == 1 || type == 2) {
            transactionType = type
        }
    }

    fun setTransactionTabType(tabType: Int) {
        if (tabType == ConstData.TAB_COIN || tabType == ConstData.TAB_LEVER) {
            if (this.tabType != tabType) {
                this.tabType = tabType
            }
        }
    }

    //获取一个单位的价格，根据深度计算
    private fun getOnUnitPrice(): Double {
        return 10.0.pow(-(viewModel?.getPrecision() ?: 6).toDouble())
    }

    //获取一个单位的数量
    private fun getOnUnitAmount(): Double {
        return 10.0.pow(-(viewModel?.getAmountLength() ?: 6).toDouble())
    }

    //计算总额
    private fun computeTotal() {
        val price = CommonUtil.parseDouble(
            binding!!.fragmentHomePageTransactionHeader1.price.text.toString().trim { it <= ' ' })
        val count = CommonUtil.parseDouble(
            binding!!.fragmentHomePageTransactionHeader1.transactionQuota.text.toString()
                .trim { it <= ' ' })
        if (price != null) {
            if (count != null && (count != 0.0)) {
                if (currentOrderType.equals("LIMIT")) {
                    binding!!.fragmentHomePageTransactionHeader1.tradeValue.setText(
                        NumberUtil.formatNumberNoGroup(
                            price * count,
                            RoundingMode.FLOOR,
                            viewModel!!.getAmountLength(),
                            viewModel!!.getAmountLength()
                        ) + viewModel!!.getSetName()
                    )
                }
            } else { //只有价格
                if (transactionType == 1) {
                    binding!!.fragmentHomePageTransactionHeader1.actionType.setText(R.string.buy_usable)
                    binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(viewModel!!.getCoinType())
                    if (price > 0 && currentBalanceSell != null) {
                        //总的钱数除以输入价格
                        binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText(
                            NumberUtil.formatNumberNoGroup(
                                currentBalanceSell?.availableBalance!!.toDouble()
                                    .div(price.toDouble()),
                                RoundingMode.FLOOR,
                                viewModel!!.getAmountLength(),
                                viewModel!!.getAmountLength()
                            )
                        )
                    } else {
                        binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText("0.0")
                    }
                } else if (transactionType == 2) {
                    binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(viewModel!!.getSetName())
                    binding!!.fragmentHomePageTransactionHeader1.actionType.setText(R.string.sale_usable)
                    if (price > 0 && currentBalanceBuy != null) {
                        //总的钱数乘以输入价格
                        binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText(
                            NumberUtil.formatNumberNoGroup(
                                currentBalanceBuy?.availableBalance!!.toDouble() * price.toDouble(),
                                RoundingMode.FLOOR,
                                viewModel!!.getAmountLength(),
                                viewModel!!.getAmountLength()
                            )
                        )
                    } else {
                        binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText("0.0")
                    }
                }
                if (currentOrderType.equals("LIMIT")) {
                    if (price != null && count != null) {
                        binding!!.fragmentHomePageTransactionHeader1.tradeValue.setText(
                            NumberUtil.formatNumberNoGroup(
                                price * count!!,
                                RoundingMode.FLOOR,
                                viewModel!!.getAmountLength(),
                                viewModel!!.getAmountLength()
                            ) + viewModel!!.getSetName()
                        )
                    }
                }
            }
        } else {
            if (transactionType == 1) {
                binding!!.fragmentHomePageTransactionHeader1.actionType.setText(R.string.buy_usable)
                binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText("0.0")
                binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(viewModel!!.getCoinType())
            } else if (transactionType == 2) {
                binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(viewModel!!.getSetName())
                binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText("0.0")
                binding!!.fragmentHomePageTransactionHeader1.actionType.setText(R.string.sale_usable)
            }
        }
    }

    //计算当前输入价格CNY
    private fun computePriceCNY() {
        val price = CommonUtil.parseDouble(
            binding!!.fragmentHomePageTransactionHeader1.price.text.toString().trim { it <= ' ' })
        if (price != null && price > 0 && viewModel!!.getCurrentPriceCNY() != null && viewModel!!.getCurrentPrice() != 0.0) {
            binding!!.fragmentHomePageTransactionHeader1.priceCny.setText(
                "≈" + NumberUtil.formatNumberNoGroup(
                    viewModel!!.getCurrentPriceCNY()!! * price / viewModel!!.getCurrentPrice(),
                    4,
                    4
                )
            )
        } else {
            binding!!.fragmentHomePageTransactionHeader1.priceCny.setText(
                "≈" + NumberUtil.formatNumberNoGroup(
                    0.0f,
                    4,
                    4
                )
            )
        }
    }

    private fun clearInput() {
        binding!!.fragmentHomePageTransactionHeader1.price.setText("")
        binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText("")
        binding!!.fragmentHomePageTransactionHeader1.countBar.progress = 0
        binding!!.fragmentHomePageTransactionHeader1.countProgress.progress = 0
        binding!!.fragmentHomePageTransactionHeader1.amountTwenty.isChecked = false
        binding!!.fragmentHomePageTransactionHeader1.amountFourty.isChecked = false
        binding!!.fragmentHomePageTransactionHeader1.amountSixty.isChecked = false
        binding!!.fragmentHomePageTransactionHeader1.amountEighty.isChecked = false
        binding!!.fragmentHomePageTransactionHeader1.amountAll.isChecked = false
        refreshSubmitButton()
    }

    //交易对切换，刷新控件，刷新交易对信息，刷新深度，刷新数据,通知交易对改变
    fun onPairStatusChanged(pairStatus: PairStatus) {
        clearInput()
        deepViewBinding!!.clearASKTradeOrders()
        deepViewBinding!!.clearBIDTradeOrders()
        mContext?.let {
            if (tabType == ConstData.TAB_LEVER) {
                CookieUtil.setCurrentPairLever(it, pairStatus.pair)
            } else {
                CookieUtil.setCurrentPair(it, pairStatus.pair)
            }
        }
        viewModel!!.getCurrentPairStatus(pairStatus.pair)
        viewModel!!.changePairSocket()
        adapter?.setAmountPrecision(viewModel!!.getAmountLength())
        resetPriceLength()
        resetAmountLength()
        refreshCurrentWallet()
        refreshTransactionHardViews()
        refreshUsable()
        refreshData()
        currentOrderType = "LIMIT"
        viewModel?.setCurrentPairorderType(currentOrderType)
        refreshOrderType(currentOrderType)
    }

    private fun refreshOrderType(type: String?) {
        var typeDes: String? = null
        when (type) {
            "MARKET" -> {
                typeDes = getString(R.string.order_type_market)
                binding!!.fragmentHomePageTransactionHeader1?.linPrice.visibility = View.GONE
                binding!!.fragmentHomePageTransactionHeader1?.linPrinceCny.visibility = View.GONE
            }
            "LIMIT" -> {
                typeDes = getString(R.string.order_type_limit)
                binding!!.fragmentHomePageTransactionHeader1?.linPrice.visibility = View.VISIBLE
                binding!!.fragmentHomePageTransactionHeader1?.linPrinceCny.visibility = View.VISIBLE
            }
        }
        binding!!.fragmentHomePageTransactionHeader1?.orderType.text = typeDes
    }

    private fun refreshSubmitButton() {
        val userInfo = if (mContext == null) null else CookieUtil.getUserInfo(mContext!!)
        if (userInfo == null) {
            binding!!.fragmentHomePageTransactionHeader1.btnHandle.isEnabled = true
        } else {
            val price = binding!!.fragmentHomePageTransactionHeader1.price.text.toString()
            val count =
                binding!!.fragmentHomePageTransactionHeader1.transactionQuota.text.toString()
            if (currentOrderType.equals("LIMIT")) {
                binding!!.fragmentHomePageTransactionHeader1.btnHandle.isEnabled =
                    !(TextUtils.isEmpty(price) || TextUtils.isEmpty(count))
            } else if (currentOrderType.equals("MARKET")) {
                binding!!.fragmentHomePageTransactionHeader1.btnHandle.isEnabled =
                    !TextUtils.isEmpty(count)
            }
        }
    }

    //刷新交易区控件
    private fun refreshTransactionHardViews() {
        if (transactionType == 1) {
            binding!!.fragmentHomePageTransactionHeader1.btnBuy.isChecked = true
            binding!!.fragmentHomePageTransactionHeader1.btnSale.isChecked = false
            binding!!.fragmentHomePageTransactionHeader1.countProgress.progressDrawable =
                countProgressBuy
            binding!!.fragmentHomePageTransactionHeader1.amountZero.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.amountTwenty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.amountFourty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.amountSixty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.amountEighty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.amountAll.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.btnHandle.background =
                SkinCompatResources.getDrawable(activity, R.drawable.btn_t7)
        } else if (transactionType == 2) {
            binding!!.fragmentHomePageTransactionHeader1.btnBuy.isChecked = false
            binding!!.fragmentHomePageTransactionHeader1.btnSale.isChecked = true
            binding!!.fragmentHomePageTransactionHeader1.countProgress.progressDrawable =
                countProgressSale
            binding!!.fragmentHomePageTransactionHeader1.amountZero.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.amountTwenty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.amountFourty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.amountSixty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.amountEighty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.amountAll.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.btnHandle.background =
                SkinCompatResources.getDrawable(activity, R.drawable.btn_t5)
        }
        if (!TextUtils.isEmpty(viewModel!!.getCurrentPair())) {
            binding!!.actionBarLayout.actionBarTitle.setText(viewModel!!.getCoinType())
            binding!!.actionBarLayout.pairSetName.setText("/" + viewModel!!.getSetName())
            binding!!.fragmentHomePageTransactionHeader1.deepPriceP.text =
                getString(R.string.brackets, viewModel!!.getSetName())
            binding!!.fragmentHomePageTransactionHeader1.deepAmountName.text =
                getString(R.string.brackets, viewModel!!.getCoinType())
            if (transactionType == 1) {
                binding!!.fragmentHomePageTransactionHeader1.useableUnit.setText(viewModel!!.getSetName())
                binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(viewModel!!.getCoinType())
                binding!!.fragmentHomePageTransactionHeader1.useableFreezUnit.setText(viewModel!!.getSetName())
                binding!!.fragmentHomePageTransactionHeader1.btnHandle.setText(
                    resources.getString(R.string.buy).toString() + viewModel!!.getCoinType()
                )
            } else if (transactionType == 2) {
                binding!!.fragmentHomePageTransactionHeader1.useableUnit.setText(viewModel!!.getCoinType())
                binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(viewModel!!.getSetName())
                binding!!.fragmentHomePageTransactionHeader1.useableFreezUnit.setText(viewModel!!.getCoinType())
                binding!!.fragmentHomePageTransactionHeader1.btnHandle.setText(
                    resources.getString(R.string.sale).toString() + viewModel!!.getCoinType()
                )
            }
        }
        if (mContext == null || CookieUtil.getUserInfo(mContext!!) == null) {
            binding!!.fragmentHomePageTransactionHeader1.btnHandle.setText(R.string.login)
        }
        refreshCurrentWallet()
    }

    private fun refreshDeepView() {
        var deep = viewModel!!.getPrecisionDeep(viewModel!!.getPrecision())
        binding!!.fragmentHomePageTransactionHeader1.deep.setText(
            getString(
                R.string.point_count,
                deep?.deep ?: ""
            )
        )
    }

    private fun onDeepChoose() {
        //深度选择之后重新拉取数据
        refreshDeepView()
        viewModel!!.getAllOrder()
    }

    private fun refreshData() {
        viewModel!!.getAllOrder()
        viewModel?.getCurrentPairDepth(50)
        viewModel?.getCurrentPairDeal(1)
        initTicker()
    }

    //刷新当前钱包
    private fun refreshCurrentWallet() {
        currentWallet = null
        currentEstimatedWallet = null
        viewModel!!.getCurrentWallet(tabType)
    }

    private fun refreshUsable() {
        activity?.runOnUiThread {
            //买入
            if (transactionType == 1) {
                if (currentBalanceSell != null) {
                    binding!!.fragmentHomePageTransactionHeader1.useable.setText(
                        NumberUtil.formatNumberNoGroup(
                            currentBalanceSell?.availableBalance?.toDoubleOrNull(),
                            RoundingMode.FLOOR,
                            0,
                            8
                        )
                    )
                    binding!!.fragmentHomePageTransactionHeader1.freezAmount.setText(
                        NumberUtil.formatNumberNoGroup(
                            currentBalanceSell?.freeze?.toDoubleOrNull(),
                            RoundingMode.FLOOR,
                            0,
                            8
                        )
                    )
                } else {
                    binding!!.fragmentHomePageTransactionHeader1.useable.setText("0.0")
                }
                binding!!.fragmentHomePageTransactionHeader1.actionType.setText(R.string.buy_usable)
                binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText("0.0")
            } else if (transactionType == 2) {
                if (currentBalanceBuy != null) {
                    binding!!.fragmentHomePageTransactionHeader1.useable.setText(
                        NumberUtil.formatNumberNoGroup(
                            currentBalanceBuy?.availableBalance?.toDoubleOrNull(),
                            RoundingMode.FLOOR,
                            0,
                            8
                        )
                    )
                    binding!!.fragmentHomePageTransactionHeader1.freezAmount.setText(
                        NumberUtil.formatNumberNoGroup(
                            currentBalanceBuy?.freeze?.toDoubleOrNull(),
                            RoundingMode.FLOOR,
                            0,
                            8
                        )
                    )
                } else {
                    binding!!.fragmentHomePageTransactionHeader1.useable.setText("0.0")
                }
                binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText("0.0")
                binding!!.fragmentHomePageTransactionHeader1.actionType.setText(R.string.sale_usable)
            }
        }
    }

    //当前委托
    private fun getTradeOrderCurrent() {

        if (mContext != null && CookieUtil.getUserInfo(mContext!!) != null) {
//            mContext?.runOnUiThread { }
//            val orderState = 1
            TradeApiServiceHelper.getTradeOrderRecordFiex(
                activity,
                viewModel!!.getCurrentPair(),
                null,
                null,
                null,
                false,
                object : NormalCallback<HttpRequestResultData<TradeOrderResult?>?>(mContext!!) {
                    override fun error(type: Int, error: Any?) {
                        Log.d(TAG, "getTradeOrderCurrent error")
                        showCurrentOrderList(null)
                    }

                    override fun callback(returnData: HttpRequestResultData<TradeOrderResult?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            Log.d(
                                TAG,
                                "getTradeOrderCurrent data.size = " + returnData.data?.items?.size
                            )
                            showCurrentOrderList(returnData.data?.items)
                        }
                    }
                })
        }
    }

    //显示当前委托
    private fun showCurrentOrderList(data: ArrayList<TradeOrderFiex?>?) {
        adapter?.data = data
        adapter?.notifyDataSetChanged()
    }

    //更新当前委托
    private fun updateCurrentOrderList(tradeOrder: TradeOrderFiex?) {
        var data = adapter?.data
        if (data != null) {
            for (i in data?.indices!!) {
                var originData = data[i]
                if (tradeOrder?.orderId.equals(originData?.orderId)) {
                    originData?.executedQty = tradeOrder?.dealQty
                    adapter?.updateItem(i, originData)
                    adapter?.notifyItemChanged(i)
                    if (tradeOrder?.executedQty?.toDouble() == originData?.origQty?.toDouble()) {//订单完全成交，更新列表
                        getTradeOrderCurrent()
                    }
                }
            }
        } else {
            getTradeOrderCurrent()
        }
    }

    //下单
    private fun createOrder(direction: String) {
        var price: String? =
            binding!!.fragmentHomePageTransactionHeader1.price.text.toString().trim { it <= ' ' }
        val priceDouble = CommonUtil.parseDouble(price)
        if (currentOrderType.equals("LIMIT")) {
            if (priceDouble == null || priceDouble == 0.0) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_price))
                return
            }
            val currentPrice =
                CommonUtil.parseDouble(binding!!.fragmentHomePageTransactionHeader1.currentPrice.text.toString())
            if (currentPrice != null && currentPrice != 0.0) {
                if ("SELL" != direction && java.lang.Double.compare(
                        priceDouble,
                        currentPrice * 0.8
                    ) < 0
                ) {
                    FryingUtil.showToast(mContext, getString(R.string.trade_sale_over_price))
                    return
                }
                if ("BUY" == direction && java.lang.Double.compare(
                        priceDouble,
                        currentPrice * 1.2
                    ) > 0
                ) {
                    FryingUtil.showToast(mContext, getString(R.string.trade_buy_over_price))
                    return
                }
            }
        }
        val totalAmount =
            binding!!.fragmentHomePageTransactionHeader1.transactionQuota.text.toString()
                .trim { it <= ' ' }
        val totalAmountDouble = CommonUtil.parseDouble(totalAmount)
        if (totalAmountDouble == null || totalAmountDouble == 0.0) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_count))
            return
        }
        val tradeType = currentOrderType
        if (currentOrderType.equals("MARKET")) {
            price = null
        }
        val createRunnable = Runnable {
            TradeApiServiceHelper.createTradeOrder(
                mContext,
                viewModel!!.getCurrentPair(),
                direction,
                totalAmount,
                price,
                tradeType,
                object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                    override fun callback(returnData: HttpRequestResultString?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            binding!!.fragmentHomePageTransactionHeader1.price.setText("")
                            binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText("")
                            binding!!.fragmentHomePageTransactionHeader1.tradeValue.setText(
                                NumberUtil.formatNumberNoGroup(
                                    0,
                                    RoundingMode.FLOOR,
                                    viewModel!!.getAmountLength(),
                                    viewModel!!.getAmountLength()
                                ) + viewModel!!.getSetName()
                            )
//                        viewModel!!.getWalletLeverDetail()
                            viewModel!!.getCurrentUserBalance(ConstData.BalanceType.SPOT)
                            withTimerGetCurrentTradeOrder()
                            FryingUtil.showToast(mContext, getString(R.string.trade_success))
                        } else {
                            FryingUtil.showToast(
                                mContext,
                                if (returnData == null) "null" else returnData.msg
                            )
                        }
                    }

                    override fun error(type: Int, error: Any?) {
                        FryingUtil.showToast(mContext, error.toString())
                    }
                })
        }
        createRunnable.run()
    }

    fun withTimerGetCurrentTradeOrder() {
        var count = 0
        var timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                getTradeOrderCurrent()
                count++
                if (count >= 3) {
                    timer.cancel()
                }
            }
        }, Date(), 1000)
    }

    //撤销新单
    private fun cancelTradeOrder(tradeOrder: TradeOrderFiex) {
        TradeApiServiceHelper.cancelTradeOrderFiex(
            mContext,
            tradeOrder.orderId,
            object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        adapter?.removeItem(tradeOrder)
                        adapter?.notifyDataSetChanged()
                    } else {
                        FryingUtil.showToast(
                            mContext,
                            if (returnData == null) "null" else returnData.msg
                        )
                    }
                }
            })
    }

    //更新涨跌幅
    override fun onPairQuotation(pairQuo: PairQuotation) {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            updatePriceSince(pairQuo.r)
        }
    }

    override fun onPairStatusInit(pairStatus: PairStatus?) {
        clearInput()
        binding!!.fragmentHomePageTransactionHeader1.price.filters = arrayOf(NumberFilter(),
            pairStatus?.precision?.let { PointLengthFilter(it) })
        resetPriceLength()
        resetAmountLength()
        //清空当前委托
        if (mContext == null || CookieUtil.getUserInfo(mContext!!) == null) {
            showCurrentOrderList(null)
        }
        refreshTransactionHardViews()
        refreshSubmitButton()
        refreshData()
        if (!TextUtils.isEmpty(pairStatus?.pair)) {
            binding!!.actionBarLayout.actionBarTitle.setText(viewModel!!.getCoinType())
            binding!!.actionBarLayout.pairSetName.setText("/" + viewModel!!.getSetName())
        }
        adapter?.setAmountPrecision(viewModel!!.getAmountLength())
        resetAmountLength()
        resetPriceLength()
        if (pairStatus?.supportingPrecisionList != null) {
            onDeepChoose()
        }
        currentOrderType = "LIMIT"
        viewModel?.setCurrentPairorderType(currentOrderType)
    }

    override fun onUserBalanceChanged(userBalance: UserBalance?) {
        Log.d(TAG, "onUserBalanceChanged,coin = " + userBalance?.coin)
        if (userBalance?.coin.equals(currentBalanceBuy?.coin)) {
            currentBalanceBuy = userBalance
        }
        if (userBalance?.coin.equals(currentBalanceSell?.coin)) {
            currentBalanceSell = userBalance
        }
        refreshUsable()
    }

    override fun onUserTradeOrderChanged(userTradeOrder: TradeOrderFiex?) {
        Log.d(TAG, "onUserTradeOrderChanged,executedQty = " + userTradeOrder?.executedQty)
        updateCurrentOrderList(userTradeOrder)
    }

    //用户信息被修改，刷新委托信息和钱包
    override fun onUserInfoChanged() {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            getTradeOrderCurrent()
            refreshCurrentWallet()
        }
    }

    override fun onTradeOrder(
        pair: String?,
        bidOrderList: List<TradeOrder?>?,
        askOrderList: List<TradeOrder?>?
    ) {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            deepViewBinding!!.showBIDTradeOrders(pair, bidOrderList)
            deepViewBinding!!.showASKTradeOrders(pair, askOrderList)
        }
    }

    override fun onTradePairInfo(pairStatus: PairStatus?) {

    }

    override fun onWallet(observable: Observable<Pair<Wallet?, Wallet?>>?) {
        observable?.subscribe(
            HttpCallbackSimple(
                mContext,
                false,
                object : NormalCallback<Pair<Wallet?, Wallet?>?>(mContext!!) {
                    override fun callback(returnData: Pair<Wallet?, Wallet?>?) {
                        if (returnData != null) {
                            currentWallet = returnData.first
                            currentEstimatedWallet = returnData.second
                        }
                        refreshUsable()
                    }

                    override fun error(type: Int, error: Any?) {
                        refreshUsable()
                    }
                })
        )
    }

    override fun getUserBalanceCallback(): Callback<Pair<UserBalance?, UserBalance?>> {
        return object : Callback<Pair<UserBalance?, UserBalance?>>() {
            override fun callback(returnData: Pair<UserBalance?, UserBalance?>?) {
                if (returnData != null) {
                    currentBalanceBuy = returnData.first
                    currentBalanceSell = returnData.second
                }
                refreshUsable()
            }

            override fun error(type: Int, error: Any?) {
//                if (currentWallet != null && currentEstimatedWallet != null
//                    && TextUtils.equals(currentWallet?.coinType, viewModel!!.getCoinType()) && TextUtils.equals(currentEstimatedWallet?.coinType, viewModel!!.getSetName())) {
//                    //如果当前资产数据符合当前交易对，在错误情况下不清空资产数据
//                } else {
//                    currentWallet = null
//                    currentEstimatedWallet = null
//                    refreshUsable()
//                }
            }

        }
    }

    override fun getWalletCallback(): Callback<Pair<Wallet?, Wallet?>> {
        return object : Callback<Pair<Wallet?, Wallet?>>() {
            override fun callback(returnData: Pair<Wallet?, Wallet?>?) {
                if (returnData != null) {
                    currentWallet = returnData.first
                    currentEstimatedWallet = returnData.second
                }
                refreshUsable()
            }

            override fun error(type: Int, error: Any?) {
                if (currentWallet != null && currentEstimatedWallet != null
                    && TextUtils.equals(
                        currentWallet?.coinType,
                        viewModel!!.getCoinType()
                    ) && TextUtils.equals(
                        currentEstimatedWallet?.coinType,
                        viewModel!!.getSetName()
                    )
                ) {
                    //如果当前资产数据符合当前交易对，在错误情况下不清空资产数据
                } else {
                    currentWallet = null
                    currentEstimatedWallet = null
                    refreshUsable()
                }
            }

        }
    }

    private var leverDetail: WalletLeverDetail? = null
    override fun onWalletLeverDetail(leverDetail: WalletLeverDetail?) {
        if (tabType == ConstData.TAB_LEVER && TextUtils.equals(
                viewModel?.getCurrentPair(),
                leverDetail?.pair
            )
        ) {
            this.leverDetail = leverDetail
            CommonUtil.checkActivityAndRunOnUI(mContext) {
                val checkRiskRate =
                    leverDetail?.riskRate == null || leverDetail.riskRate == BigDecimal.ZERO
                binding?.actionBarLayout?.risk?.setText(
                    String.format(
                        "%s%s",
                        if (checkRiskRate) nullAmount else if (leverDetail?.riskRate!! > BigDecimal(
                                2
                            )
                        ) ">200.00" else NumberUtil.formatNumberNoGroupHardScale(
                            leverDetail?.riskRate!! * BigDecimal(
                                100
                            ), 2
                        ),
                        if (checkRiskRate) nullAmount else "%"
                    )
                )
                if (leverDetail?.riskRate == null || leverDetail.riskRate == BigDecimal.ZERO || leverDetail?.riskRate!! >= BigDecimal(
                        2
                    )
                ) {
                    binding?.actionBarLayout?.explodePrice?.setText(
                        String.format(
                            "%s%s",
                            nullAmount,
                            nullAmount
                        )
                    )
                } else {
                    val checkExplodePrice =
                        leverDetail?.burstPrice == null || leverDetail.burstPrice == BigDecimal.ZERO
                    binding?.actionBarLayout?.explodePrice?.setText(
                        String.format(
                            "%s%s",
                            if (checkExplodePrice) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(
                                leverDetail?.burstPrice,
                                9,
                                0,
                                viewModel?.getPrecision()!!
                            ),
                            if (checkExplodePrice || leverDetail?.afterCoinType == null) nullAmount else leverDetail.afterCoinType
                        )
                    )
                }
            }
        } else {
            this.leverDetail = null
        }
    }

    override fun onLeverPairConfigCheck(hasLeverConfig: Boolean) {
    }

    override fun onRechargeClick(transactionMorePopup: TransactionMorePopup) {
        fryingHelper.checkUserAndDoing(Runnable {
            val bundle = Bundle()
            bundle.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_EXCHANGE)
            bundle.putString(ConstData.COIN_TYPE, viewModel!!.getCoinType())
            BlackRouter.getInstance().build(RouterConstData.RECHARGE).with(bundle).go(mFragment)
        }, TRADE_INDEX)
    }

    override fun onExtractClick(transactionMorePopup: TransactionMorePopup) {
        fryingHelper.checkUserAndDoing(Runnable {
            val bundle = Bundle()
            bundle.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_WITHDRAW)
            bundle.putString(ConstData.COIN_TYPE, viewModel!!.getCoinType())
            BlackRouter.getInstance().build(RouterConstData.EXTRACT).with(bundle).go(mFragment)
        }, TRADE_INDEX)
    }

    override fun onBillClick(transactionMorePopup: TransactionMorePopup) {
        fryingHelper.checkUserAndDoing(Runnable {
            //点击账户详情
            val extras = Bundle()
            extras.putString(ConstData.ROUTER_COIN_TYPE, viewModel!!.getCoinType())
            BlackRouter.getInstance().build(RouterConstData.WALLET_DETAIL).with(extras)
                .go(mFragment)
        }, TRADE_INDEX)
    }

    override fun onCollectClick(
        transactionMorePopup: TransactionMorePopup,
        btnCollect: CheckedTextView
    ) {
        viewModel!!.toggleDearPair(btnCollect.isChecked)
            ?.subscribe(
                HttpCallbackSimple(
                    mContext,
                    true,
                    object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                        override fun callback(result: HttpRequestResultString?) {
                            if (result != null && result.code == HttpRequestResult.SUCCESS) {
                                isDear = !isDear!!
                                updateDear(isDear)
                                val showMsg =
                                    if (isDear!!) getString(R.string.pair_collect_add_ok) else getString(
                                        R.string.pair_collect_cancel_ok
                                    )
                                FryingUtil.showToast(mContext, showMsg)
                            } else {
                                FryingUtil.showToast(
                                    mContext,
                                    if (result == null) "null" else result.msg
                                )
                            }
                        }
                    })
            )
    }

    override fun onChatRoomClick(transactionMorePopup: TransactionMorePopup, chatRoomId: String?) {
        fryingHelper.checkUserAndDoing(Runnable {
            transactionMorePopup.dismiss()
            viewModel!!.checkIntoChatRoom()
                ?.subscribe(
                    HttpCallbackSimple(
                        mContext,
                        true,
                        object : NormalCallback<HttpRequestResultString?>(mContext!!) {
                            override fun callback(returnData: HttpRequestResultString?) =
                                if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                                    intoChatRoom(chatRoomId)
                                } else {
                                    FryingUtil.showToast(
                                        mContext,
                                        if (returnData?.msg == null) "null" else returnData.msg
                                    )
                                }
                        })
                )
        }, 0)
    }

    override fun onTradeOrderFastClick(tradeOrder: TradeOrder) {
        if (currentOrderType.equals("MARKET")) {
            return
        }
        binding!!.fragmentHomePageTransactionHeader1.price.setText(tradeOrder.formattedPrice)
        val scaleAnim = AnimationUtils.loadAnimation(mContext, R.anim.transaction_price_anim)
        binding!!.fragmentHomePageTransactionHeader1.price.startAnimation(scaleAnim)
//        val amount = binding!!.fragmentHomePageTransactionHeader1.transactionQuota.text.toString().toDouble()
//        val price = CommonUtil.parseDouble(tradeOrder.formattedPrice) ?: 0.0
//        var usableAmount = 0.0
//        if (transactionType == 1) {
//            usableAmount = if (currentEstimatedWallet != null && price != null && price != 0.0) {
//                min(amount, currentEstimatedWallet?.coinAmount!!.toDouble() / price)
//            } else {
//                0.0
//            }
//        } else if (transactionType == 2) {
//            usableAmount = if (currentWallet != null) {
//                min(amount, currentWallet?.coinAmount!!.toDouble())
//            } else {
//                0.0
//            }
//        }
//        if (usableAmount != 0.0) {
//            binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText(NumberUtil.formatNumberNoGroup(usableAmount, RoundingMode.FLOOR, 0, viewModel!!.getAmountLength()))
//        } else {
//            binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText("0.0")
//        }
    }

    override fun onDeepChanged(deep: Deep) {
        onDeepChoose()
    }

    override fun onUserBanlance(observable: Observable<HttpRequestResultDataList<UserBalance?>?>?) {
        observable!!.subscribe(
            HttpCallbackSimple(
                mContext,
                false,
                object : Callback<HttpRequestResultDataList<UserBalance?>?>() {
                    override fun callback(returnData: HttpRequestResultDataList<UserBalance?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            Log.d(TAG, "onUserBanlance succ")
                            userBalance = returnData.data
                        } else {
                            Log.d(TAG, "onUserBanlance data null or fail")
                        }
                    }

                    override fun error(type: Int, error: Any?) {
                        Log.d(TAG, "onUserBanlance error")
                    }
                })
        )
    }

    private fun updateCurrentPair(pairStatus: PairStatus) {
        val color =
            if (pairStatus.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) colorT3 else if (pairStatus.priceChangeSinceToday!! > 0) colorWin else colorLost
        val exChangeRates = ExchangeRatesUtil.getExchangeRatesSetting(mContext!!)?.rateCode
        if (exChangeRates == 0) {
            binding!!.fragmentHomePageTransactionHeader1.currentPrice.setText(pairStatus.currentPriceFormat)
            binding!!.fragmentHomePageTransactionHeader1.currentPriceCny.setText(
                String.format(
                    "≈ %s CNY",
                    pairStatus.currentPriceCNYFormat
                )
            )
        } else {
            binding!!.fragmentHomePageTransactionHeader1.currentPrice.setText(pairStatus.currentPriceFormat)
            binding!!.fragmentHomePageTransactionHeader1.currentPriceCny.setText(
                String.format(
                    "≈ %s USD",
                    pairStatus.currentPriceFormat
                )
            )
        }
        binding!!.fragmentHomePageTransactionHeader1.currentPrice.setTextColor(color)
        computePriceCNY()
    }

    private fun updateCurrentPairPrice(price: String?) {
        val exChangeRates = ExchangeRatesUtil.getExchangeRatesSetting(mContext!!)?.rateCode
        if (exChangeRates == 1) {
            binding!!.fragmentHomePageTransactionHeader1.currentPrice.setText(price)
//        binding!!.fragmentHomePageTransactionHeader1.currentPriceCny.setText(String.format("≈ %s", price))
            if (price != null && price.toDouble() > 0 && viewModel!!.getCurrentPriceCNY() != null && viewModel!!.getCurrentPrice() != 0.0) {
                binding!!.fragmentHomePageTransactionHeader1.currentPriceCny.setText(
                    "≈" + NumberUtil.formatNumberNoGroup(
                        viewModel!!.getCurrentPrice(),
                        4,
                        4
                    ) + "$"
                )
            } else {
                binding!!.fragmentHomePageTransactionHeader1.currentPriceCny.setText(
                    "≈" + NumberUtil.formatNumberNoGroup(
                        0.0f,
                        4,
                        4
                    ) + "$"
                )
            }
        } else {
            binding!!.fragmentHomePageTransactionHeader1.currentPrice.setText(price)
//        binding!!.fragmentHomePageTransactionHeader1.currentPriceCny.setText(String.format("≈ %s", price))
            if (price != null && price.toDouble() > 0 && viewModel!!.getCurrentPriceCNY() != null && viewModel!!.getCurrentPrice() != 0.0) {
                binding!!.fragmentHomePageTransactionHeader1.currentPriceCny.setText(
                    "≈" + NumberUtil.formatNumberNoGroup(
                        viewModel!!.getCurrentPriceCNY()!! * price.toDouble() / viewModel!!.getCurrentPrice(),
                        4,
                        4
                    ) + "CNY"
                )
            } else {
                binding!!.fragmentHomePageTransactionHeader1.currentPriceCny.setText(
                    "≈" + NumberUtil.formatNumberNoGroup(
                        0.0f,
                        4,
                        4
                    ) + "CNY"
                )
            }
        }
    }

    //更新涨跌幅
    private fun updatePriceSince(since: String?) {
        val since = since?.toDouble()
        var background: Drawable?
        var color: Int?
        val styleChange = StyleChangeUtil.getStyleChangeSetting(mContext!!)?.styleCode
        if (since != null && styleChange == 1) {
            if (since > 0) {//涨
                background = mContext?.getDrawable(R.drawable.trans_raise_bg_corner)
                color = mContext?.getColor(R.color.T10)
            } else if (since < 0) {
                background = mContext?.getDrawable(R.drawable.trans_fall_bg_corner)
                color = mContext?.getColor(R.color.T9)
            } else {
                background = mContext?.getDrawable(R.drawable.trans_default_bg_corner)
                color = mContext?.getColor(R.color.B3)
            }
            Log.d(tag, "priceSince0 = $since")
            var result = NumberUtil.formatNumber2(since?.times(100)) + "%"
            Log.d(tag, "priceSince1 = $result")
            binding!!.actionBarLayout.currentPriceSince.setText(result)
            binding!!.actionBarLayout.currentPriceSince.background = background
            binding!!.actionBarLayout.currentPriceSince.setTextColor(color!!)
        }
        if (since != null && styleChange == 0) {
            if (since < 0) {
                background = mContext?.getDrawable(R.drawable.trans_raise_bg_corner)
                color = mContext?.getColor(R.color.T10)
            } else if (since > 0) {
                background = mContext?.getDrawable(R.drawable.trans_fall_bg_corner)
                color = mContext?.getColor(R.color.T9)
            } else {
                background = mContext?.getDrawable(R.drawable.trans_default_bg_corner)
                color = mContext?.getColor(R.color.B3)
            }
            Log.d(tag, "priceSince0 = $since")
            var result = NumberUtil.formatNumber2(since?.times(100)) + "%"
            Log.d(tag, "priceSince1 = $result")
            binding!!.actionBarLayout.currentPriceSince.setText(result)
            binding!!.actionBarLayout.currentPriceSince.background = background
            binding!!.actionBarLayout.currentPriceSince.setTextColor(color!!)
        }
    }

    private fun intoChatRoom(chatRoomId: String?) {
        if (chatRoomId == null || mContext == null) {
            return
        }
        val userInfo = CookieUtil.getUserInfo(mContext!!)
        if (userInfo == null) {
            FryingUtil.showToast(mContext, "请先登录系统")
        } else {
            val userIdHeader = IMHelper.getUserIdHeader(mContext!!)
            val userId = userInfo.id
            val groupName = "FBSexer"
            val bundle = Bundle()
            bundle.putString(ConstData.IM_GROUP_ID, chatRoomId)
            bundle.putString(ConstData.IM_GROUP_NAME, groupName)
            IMHelper.startWithIMGroupActivity(
                mContext!!,
                mContext,
                userIdHeader + userId,
                chatRoomId,
                RouterConstData.PUBLIC_CHAT_GROUP,
                bundle,
                null,
                null
            )
        }
    }
}