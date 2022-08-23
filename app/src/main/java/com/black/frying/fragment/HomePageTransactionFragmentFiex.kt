package com.black.frying.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import com.black.base.lib.FryingSingleToast
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.HttpRequestResultString
import com.black.base.model.PagingData
import com.black.base.model.socket.PairStatus
import com.black.base.model.socket.TradeOrder
import com.black.base.model.wallet.CoinInfo
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletLeverDetail
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.view.AlertMessageDialog
import com.black.base.view.PairStatusPopupWindow
import com.black.base.view.PairStatusPopupWindow.OnPairStatusSelectListener
import com.black.frying.activity.HomePageActivity
import com.black.frying.adapter.EntrustCurrentHomeAdapter
import com.black.frying.view.PairChoosePopup
import com.black.frying.view.PairChoosePopup.OnPairChooseListener
import com.black.frying.view.TransactionDeepViewBinding
import com.black.frying.view.TransactionDeepViewBinding.OnTransactionDeepListener
import com.black.frying.view.TransactionMorePopup
import com.black.frying.view.TransactionMorePopup.OnTransactionMoreClickListener
import com.black.frying.viewmodel.TransactionViewModel
import com.black.frying.viewmodel.TransactionViewModel.OnTransactionModelListener
import com.black.im.util.IMHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.BR
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageTransactionBinding
import com.fbsex.exchange.databinding.FragmentHomePageTransactionFiexBinding
import io.reactivex.Observable
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

//交易
@Route(value = [RouterConstData.TRANSACTION], fragmentParentPath = RouterConstData.HOME_PAGE, fragmentIndex = 2)
class HomePageTransactionFragmentFiex : BaseFragment(), View.OnClickListener, OnSeekBarChangeListener, EntrustCurrentHomeAdapter.OnHandleClickListener, OnItemClickListener, OnTransactionMoreClickListener, OnTransactionModelListener, OnTransactionDeepListener {
    companion object {
        private const val TAG = "HomePageTransactionFragmentFiex"
    }

    private var colorWin = 0
    private var colorLost = 0
    private var colorT3 = 0

    private var transactionType = 1 //1 买入 2卖出
    private var tabType = ConstData.TAB_COIN

    private var countProgressBuy: Drawable? = null
    private var countProgressSale: Drawable? = null

    private var adapter: EntrustCurrentHomeAdapter? = null

    /**
     * 卖出使用 当前币种
     */
    private var currentWallet: Wallet? = null
    /**
     * 买入使用 交易区币种
     */
    private var currentEstimatedWallet: Wallet? = null

    private var layout: View? = null
    private var binding: FragmentHomePageTransactionFiexBinding? = null
    private var viewModel: TransactionViewModel? = null
    private var deepViewBinding: TransactionDeepViewBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_page_transaction_fiex, container, false)
        layout = binding?.root
        StatusBarUtil.addStatusBarPadding(layout)
        viewModel = TransactionViewModel(mContext!!, this)
        deepViewBinding = TransactionDeepViewBinding(mContext!!, viewModel!!, binding!!.fragmentHomePageTransactionHeader1)
        deepViewBinding?.setOnTransactionDeepListener(this)

        binding?.actionBarLayout?.btnTransactionMemu?.setOnClickListener(this)
        binding?.actionBarLayout?.headCharts?.setOnClickListener(this)
        binding?.actionBarLayout?.headTransactionMore?.setOnClickListener(this)
        binding?.actionBarLayout?.riskInfo?.setOnClickListener(this)
        binding?.actionBarLayout?.leverHandle?.setOnClickListener(this)
        binding?.actionBarLayout?.leverLayout?.visibility = if (tabType == ConstData.TAB_LEVER) View.VISIBLE else View.GONE

        initHeader1()
        initHeader2()
        //点击查看全部委托
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
        return layout
    }

    override fun getViewModel(): TransactionViewModel? {
        return viewModel
    }

    override fun onResume() {
        super.onResume()
        viewModel?.setTabType(tabType)
    }

    override fun onItemClick(recyclerView: RecyclerView?, view: View, position: Int, item: Any?) {
//        TradeOrder tradeOrder = adapter.getItem(position);
//        Bundle bundle = new Bundle();
//        bundle.putParcelable(ConstData.TRADE_ORDER, tradeOrder);
//        BlackRouter.getInstance().build(RouterConstData.ENTRUST_DETAIL).with(bundle).go(this);
    }

    //买卖功能
    private fun initHeader1() {
        binding!!.fragmentHomePageTransactionHeader1.btnBuy.setOnClickListener(this)
        binding!!.fragmentHomePageTransactionHeader1.btnSale.setOnClickListener(this)
        binding!!.fragmentHomePageTransactionHeader1.price.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                computeTotal()
                computePriceCNY()
                refreshSubmitButton()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        binding!!.fragmentHomePageTransactionHeader1.transactionQuota.filters = arrayOf(NumberFilter(), PointLengthFilter(4))
        binding!!.fragmentHomePageTransactionHeader1.transactionQuota.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                computeTotal()
                refreshSubmitButton()
            }

            override fun afterTextChanged(s: Editable) {}
        })
        countProgressBuy = SkinCompatResources.getDrawable(mContext, R.drawable.bg_transaction_progress_bar_buy)
        countProgressSale = SkinCompatResources.getDrawable(mContext, R.drawable.bg_transaction_progress_bar_sale)
        binding!!.fragmentHomePageTransactionHeader1.priceSub.setOnClickListener(this)
        binding!!.fragmentHomePageTransactionHeader1.priceAdd.setOnClickListener(this)
        binding!!.fragmentHomePageTransactionHeader1.useable.setText(getString(R.string.number_default))
        binding!!.fragmentHomePageTransactionHeader1.useableUnit.setText(getString(R.string.number_default))
        binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText(getString(R.string.number_default))
        binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(getString(R.string.number_default))
        binding!!.fragmentHomePageTransactionHeader1.countBar.setOnSeekBarChangeListener(this)
        binding!!.fragmentHomePageTransactionHeader1.btnHandle.setOnClickListener(this)
        deepViewBinding!!.init()
    }

    private fun initHeader2() {
        binding!!.fragmentHomePageTransactionHeader2.totalCurrent.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
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
                if (mContext != null && tabType == ConstData.TAB_LEVER && TextUtils.equals(viewModel?.getCurrentPair(), leverDetail?.pair) && leverDetail?.burstRate != null && leverDetail?.burstRate != BigDecimal.ZERO) {
                    leverDetail?.run {
                        AlertMessageDialog(mContext!!,
                                "爆仓说明",
                                String.format("当前风险率达到%s%%时，系统将强制回收您当前账户内的所有借贷资产。", NumberUtil.formatNumberNoGroupHardScale(leverDetail?.burstRate!! * BigDecimal(100), 2)))
                                .show()
                    }
                }
            }
            R.id.lever_handle -> {
                if (tabType == ConstData.TAB_LEVER) {
                    val pair = viewModel?.getCurrentPair()
                    pair?.run {
                        val bundle = Bundle()
                        bundle.putString(ConstData.PAIR, pair)
                        BlackRouter.getInstance().build(RouterConstData.WALLET_LEVER_DETAIL).with(bundle).go(mContext)
                    }
                }
            }
            R.id.tab_transaction_c2c -> BlackRouter.getInstance().build(RouterConstData.C2C_NEW).go(mContext)
            R.id.head_charts -> if (mContext != null && !TextUtils.isEmpty(CookieUtil.getCurrentPair(mContext!!))) {
                val bundle = Bundle()
                bundle.putString(ConstData.PAIR, viewModel?.getCurrentPair())
                BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).with(bundle).go(mContext)
            }
            R.id.head_transaction_more -> viewModel!!.checkDearPair()
                    ?.subscribe(HttpCallbackSimple(mContext, true, object : Callback<Boolean>() {
                        override fun error(type: Int, error: Any) {}
                        override fun callback(returnData: Boolean) {
                            WalletApiServiceHelper.getCoinInfo(mContext, viewModel!!.getCoinType(), object : Callback<CoinInfo?>() {
                                override fun error(type: Int, error: Any) {
                                    CommonUtil.checkActivityAndRunOnUI(mContext) {
                                        TransactionMorePopup(mContext!!, returnData
                                                ?: false, null).setOnTransactionMoreClickListener(this@HomePageTransactionFragmentFiex).show(v)
                                    }
                                }

                                override fun callback(coinInfo: CoinInfo?) {
                                    CommonUtil.checkActivityAndRunOnUI(mContext) {
                                        TransactionMorePopup(mContext!!, returnData
                                                ?: false, coinInfo?.groupId).setOnTransactionMoreClickListener(this@HomePageTransactionFragmentFiex).show(v)
                                    }
                                }
                            })
                        }
                    }))
            R.id.btn_transaction_memu -> mContext?.let {
                PairApiServiceHelper.getTradeSets(it, true, object : NormalCallback<HttpRequestResultDataList<String?>?>() {
                    override fun callback(returnData: HttpRequestResultDataList<String?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            val dataType = if (tabType == ConstData.TAB_COIN) PairStatus.NORMAL_DATA else PairStatus.LEVER_DATA
                            PairStatusPopupWindow.getInstance(it, PairStatusPopupWindow.TYPE_TRANSACTION or dataType, returnData.data)
                                    .show(object : OnPairStatusSelectListener {
                                        override fun onPairStatusSelected(pairStatus: PairStatus?) {
                                            if (pairStatus == null) {
                                                return
                                            }
                                            //交易对切换
                                            if (!TextUtils.equals(viewModel?.getCurrentPair(), pairStatus.pair)) { //清空价格，数量
                                                onPairStatusChanged(pairStatus)
                                            }
                                        }
                                    })
                        }
                    }
                })
            }
            R.id.total_current ->  //全部委托
                mContext?.let {
                    if (CookieUtil.getUserInfo(it) == null) {
                        BlackRouter.getInstance().build(RouterConstData.LOGIN).go(it)
                    } else if (!TextUtils.isEmpty(viewModel?.getCurrentPair())) {
                        val extras = Bundle()
                        extras.putString(ConstData.PAIR, viewModel?.getCurrentPair())
                        extras.putString(ConstData.LEVEL_TYPE, if (tabType == ConstData.TAB_LEVER) TransactionViewModel.LEVER_TYPE_LEVER else TransactionViewModel.LEVER_TYPE_COIN)
                        BlackRouter.getInstance().build(RouterConstData.ENTRUST_RECORDS_NEW).with(extras).go(it)
                    }
                }
            R.id.price_sub -> {
                var currentInputPrice = CommonUtil.parseDouble(binding!!.fragmentHomePageTransactionHeader1.price.text.toString().trim { it <= ' ' })
                currentInputPrice = currentInputPrice ?: 0.toDouble()
                val onUnitPrice: Double = getOnUnitPrice()
                if (currentInputPrice > 0) {
                    currentInputPrice -= onUnitPrice
                    currentInputPrice = max(currentInputPrice, 0.0)
                    binding!!.fragmentHomePageTransactionHeader1.price.setText(String.format("%." + viewModel!!.getPrecision() + "f", currentInputPrice))
                }
            }
            R.id.price_add -> {
                var currentInputPrice = CommonUtil.parseDouble(binding!!.fragmentHomePageTransactionHeader1.price.text.toString().trim { it <= ' ' })
                currentInputPrice = currentInputPrice ?: 0.toDouble()
                val onUnitPrice: Double = getOnUnitPrice()
                currentInputPrice += onUnitPrice
                binding!!.fragmentHomePageTransactionHeader1.price.setText(String.format("%." + viewModel!!.getPrecision() + "f", currentInputPrice))
            }
            R.id.btn_buy -> context?.let {
                if (CookieUtil.getUserInfo(it) == null) {
                    fryingHelper.checkUserAndDoing(Runnable { }, TRADE_INDEX)
                } else {
                    if (transactionType != 1) {
                        transactionType = 1
                        //                    refreshSeekBar();
                        clearInput()
                        refreshUsable()
                        refreshTransactionHardViews()
                        refreshSubmitButton()
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
                        clearInput()
                        refreshUsable()
                        refreshTransactionHardViews()
                        refreshSubmitButton()
                    }
                }
            }
            R.id.btn_handle -> mContext?.let {
                if (CookieUtil.getUserInfo(it) == null) {
                    //未登录，请求登陆
                    fryingHelper.checkUserAndDoing(Runnable { }, TRADE_INDEX)
                } else {
                    if (viewModel!!.getCoinType() != null) {
                        WalletApiServiceHelper.getCoinInfo(mContext, viewModel!!.getCoinType(), object : Callback<CoinInfo?>() {
                            override fun callback(returnData: CoinInfo?) {
                                if (returnData != null) {
                                    if (returnData.supportTrade != null && true == returnData.supportTrade) {
                                        if (transactionType == 1) { //买入
                                            createOrder("BID")
                                        } else if (transactionType == 2) { //卖出
                                            createOrder("ASK")
                                        }
                                    } else {
                                        FryingUtil.showToast(mContext, getString(R.string.alert_trade_not_support, viewModel!!.getCoinType()))
                                    }
                                } else {
                                    FryingUtil.showToast(mContext, getString(R.string.alert_trade_not_support, viewModel!!.getCoinType()))
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
        binding?.actionBarLayout?.leverLayout?.visibility = if (tabType == ConstData.TAB_LEVER) View.VISIBLE else View.GONE
        viewModel!!.setTabType(tabType)
        viewModel!!.changePairSocket()
        viewModel!!.startListenLeverDetail()
        viewModel!!.getWalletLeverDetail()
        adapter?.setAmountPrecision(viewModel!!.getAmountLength())
        resetPriceLength()
        resetAmountLength()
        refreshCurrentWallet()
        refreshTransactionHardViews()
        refreshUsable()
        refreshData()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        //显示滑块选择的数量
        binding!!.fragmentHomePageTransactionHeader1.countProgress.progress = progress
        val amountPercent = progress.toDouble() / seekBar.max
        val max: BigDecimal? = getMaxAmount()
        if (max == null || max == BigDecimal.ZERO) {
            binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText("0.00")
        } else {
            binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText(NumberUtil.formatNumberNoGroup(max * BigDecimal(amountPercent), RoundingMode.FLOOR, 0, viewModel!!.getAmountLength()))
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
        countProgressBuy = SkinCompatResources.getDrawable(mContext, R.drawable.bg_transaction_progress_bar_buy)
        countProgressSale = SkinCompatResources.getDrawable(mContext, R.drawable.bg_transaction_progress_bar_sale)
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
    override fun onHandleClick(tradeOrder: TradeOrder) {
        //新订单可以撤销
        cancelTradeOrder(tradeOrder)
    }

    //计算最大交易数量
    private fun getMaxAmount(): BigDecimal? {
        if (transactionType == 1) {
            val usable = currentEstimatedWallet?.coinAmount
            val price = CommonUtil.parseDouble(binding!!.fragmentHomePageTransactionHeader1.price.text.toString())
            return if (usable == null || price == null || price == 0.0) null else usable / BigDecimal(price)
        } else if (transactionType == 2) {
            return currentWallet?.coinAmount
        }
        return null
    }


    private fun resetAmountLength() {
        binding!!.fragmentHomePageTransactionHeader1.transactionQuota.filters = arrayOf(NumberFilter(), PointLengthFilter(viewModel!!.getAmountLength()))
    }

    private fun resetPriceLength() {
        binding!!.fragmentHomePageTransactionHeader1.price.filters = arrayOf(NumberFilter(), PointLengthFilter(viewModel?.getPrecision()
                ?: 8))
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

    //计算总额
    private fun computeTotal() {
        val price = CommonUtil.parseDouble(binding!!.fragmentHomePageTransactionHeader1.price.text.toString().trim { it <= ' ' })
        val count = CommonUtil.parseDouble(binding!!.fragmentHomePageTransactionHeader1.transactionQuota.text.toString().trim { it <= ' ' })
        if (price != null) {
            if (count != null) {
                binding!!.fragmentHomePageTransactionHeader1.actionType.setText(R.string.trade_value)
                binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText(NumberUtil.formatNumberNoGroup(price * count, RoundingMode.FLOOR, viewModel!!.getAmountLength(), viewModel!!.getAmountLength()))
                binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(viewModel!!.getSetName())
            } else { //只有价格
                if (transactionType == 1) {
                    binding!!.fragmentHomePageTransactionHeader1.actionType.setText(R.string.buy_usable)
                    binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(viewModel!!.getCoinType())
                    if (price > 0 && currentEstimatedWallet != null) {
                        //总的钱数除以输入价格
                        binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText(NumberUtil.formatNumberNoGroup(currentEstimatedWallet?.coinAmount!! / BigDecimal(price), RoundingMode.FLOOR, viewModel!!.getAmountLength(), viewModel!!.getAmountLength()))
                    } else {
                        binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText("0.0")
                    }
                } else if (transactionType == 2) {
                    binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(viewModel!!.getSetName())
                    binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText(binding!!.fragmentHomePageTransactionHeader1.price.text.toString().trim { it <= ' ' })
                    binding!!.fragmentHomePageTransactionHeader1.actionType.setText(R.string.sale_usable)
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
        val price = CommonUtil.parseDouble(binding!!.fragmentHomePageTransactionHeader1.price.text.toString().trim { it <= ' ' })
        if (price != null && price > 0 && viewModel!!.getCurrentPriceCNY() != null && viewModel!!.getCurrentPrice() != 0.0) {
            binding!!.fragmentHomePageTransactionHeader1.priceCny.setText("≈" + NumberUtil.formatNumberNoGroup(viewModel!!.getCurrentPriceCNY()!! * price / viewModel!!.getCurrentPrice(), 4, 4))
        } else {
            binding!!.fragmentHomePageTransactionHeader1.priceCny.setText("≈" + NumberUtil.formatNumberNoGroup(0.0f, 4, 4))
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
        viewModel!!.startListenLeverDetail()
        viewModel!!.getWalletLeverDetail()
        adapter?.setAmountPrecision(viewModel!!.getAmountLength())
        resetPriceLength()
        resetAmountLength()
        refreshCurrentWallet()
        refreshTransactionHardViews()
        refreshUsable()
        refreshData()
    }

    private fun refreshSubmitButton() {
        val userInfo = if (mContext == null) null else CookieUtil.getUserInfo(mContext!!)
        if (userInfo == null) {
            binding!!.fragmentHomePageTransactionHeader1.btnHandle.isEnabled = true
        } else {
            val price = binding!!.fragmentHomePageTransactionHeader1.price.text.toString()
            val count = binding!!.fragmentHomePageTransactionHeader1.transactionQuota.text.toString()
            binding!!.fragmentHomePageTransactionHeader1.btnHandle.isEnabled = !(TextUtils.isEmpty(price) || TextUtils.isEmpty(count))
        }
    }

    //刷新交易区控件
    private fun refreshTransactionHardViews() {
        if (transactionType == 1) {
            binding!!.fragmentHomePageTransactionHeader1.btnBuy.isChecked = true
            binding!!.fragmentHomePageTransactionHeader1.btnSale.isChecked = false
            binding!!.fragmentHomePageTransactionHeader1.countProgress.progressDrawable = countProgressBuy
            binding!!.fragmentHomePageTransactionHeader1.amountZero.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.amountTwenty.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.amountFourty.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.amountSixty.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.amountEighty.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.amountAll.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageTransactionHeader1.btnHandle.background = SkinCompatResources.getDrawable(activity, R.drawable.btn_t7)
        } else if (transactionType == 2) {
            binding!!.fragmentHomePageTransactionHeader1.btnBuy.isChecked = false
            binding!!.fragmentHomePageTransactionHeader1.btnSale.isChecked = true
            binding!!.fragmentHomePageTransactionHeader1.countProgress.progressDrawable = countProgressSale
            binding!!.fragmentHomePageTransactionHeader1.amountZero.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.amountTwenty.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.amountFourty.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.amountSixty.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.amountEighty.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.amountAll.buttonDrawable = SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageTransactionHeader1.btnHandle.background = SkinCompatResources.getDrawable(activity, R.drawable.btn_t5)
        }
        if (!TextUtils.isEmpty(viewModel!!.getCurrentPair())) {
            binding!!.actionBarLayout.actionBarTitle.setText(viewModel!!.getCoinType())
            binding!!.actionBarLayout.pairSetName.setText("/"+viewModel!!.getSetName())
            if (transactionType == 1) {
                binding!!.fragmentHomePageTransactionHeader1.useableUnit.setText(viewModel!!.getSetName())
                binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(viewModel!!.getCoinType())
                binding!!.fragmentHomePageTransactionHeader1.btnHandle.setText(resources.getString(R.string.buy).toString() + viewModel!!.getCoinType())
            } else if (transactionType == 2) {
                binding!!.fragmentHomePageTransactionHeader1.useableUnit.setText(viewModel!!.getCoinType())
                binding!!.fragmentHomePageTransactionHeader1.useableBuyUnit.setText(viewModel!!.getSetName())
                binding!!.fragmentHomePageTransactionHeader1.btnHandle.setText(resources.getString(R.string.sale).toString() + viewModel!!.getCoinType())
            }
        }
        if (mContext == null || CookieUtil.getUserInfo(mContext!!) == null) {
            binding!!.fragmentHomePageTransactionHeader1.btnHandle.setText(R.string.login)
        }
        refreshCurrentWallet()
    }

    private fun refreshDeepView() {
        binding!!.fragmentHomePageTransactionHeader1.deep.setText(getString(R.string.point_count, if (viewModel!!.getPrecision() == 0) "" else viewModel!!.getPrecision().toString()))
    }

    private fun onDeepChoose() {
        //深度选择之后重新拉取数据
        refreshDeepView()
        viewModel!!.getAllOrder()
    }

    //s
    private fun refreshData() {
//        if (!getUserVisibleHint()) {
//            return;
//        }
//        viewModel!!.getTradePairInfo()
        getTradeOrderCurrent()
        viewModel!!.getAllOrder()
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
                if (currentEstimatedWallet != null) {
                    binding!!.fragmentHomePageTransactionHeader1.useable.setText(NumberUtil.formatNumberNoGroup(currentEstimatedWallet?.coinAmount, RoundingMode.FLOOR, 0, 8))
                } else {
                    binding!!.fragmentHomePageTransactionHeader1.useable.setText("0.0")
                }
                binding!!.fragmentHomePageTransactionHeader1.actionType.setText(R.string.buy_usable)
                binding!!.fragmentHomePageTransactionHeader1.useableBuy.setText("0.0")
            } else if (transactionType == 2) {
                if (currentWallet != null) {
                    binding!!.fragmentHomePageTransactionHeader1.useable.setText(NumberUtil.formatNumberNoGroup(currentWallet?.coinAmount, RoundingMode.FLOOR, 0, 8))
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
            mContext?.runOnUiThread { }
            val levelType = if (tabType == ConstData.TAB_LEVER) TransactionViewModel.LEVER_TYPE_LEVER else TransactionViewModel.LEVER_TYPE_COIN
            TradeApiServiceHelper.getTradeOrderRecord(activity, viewModel!!.getCurrentPair(), false, 1, 20, false, null, null, levelType, false, object : NormalCallback<HttpRequestResultData<PagingData<TradeOrder?>?>?>() {
                override fun error(type: Int, error: Any) {
                    showCurrentOrderList(null)
                }

                override fun callback(returnData: HttpRequestResultData<PagingData<TradeOrder?>?>?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        showCurrentOrderList(returnData.data?.data)
                    }
                }
            })
        }
    }

    //显示当前委托
    private fun showCurrentOrderList(data: ArrayList<TradeOrder?>?) {
        adapter?.data = data
        adapter?.notifyDataSetChanged()
    }

    //下单
    private fun createOrder(direction: String) {
        val price = binding!!.fragmentHomePageTransactionHeader1.price.text.toString().trim { it <= ' ' }
        val priceDouble = CommonUtil.parseDouble(price)
        if (priceDouble == null || priceDouble == 0.0) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_price))
            return
        }
        val currentPrice = CommonUtil.parseDouble(binding!!.fragmentHomePageTransactionHeader1.currentPrice.text.toString())
        if (currentPrice != null && currentPrice != 0.0) {
            if ("BID" != direction && java.lang.Double.compare(priceDouble, currentPrice * 0.8) < 0) {
                FryingUtil.showToast(mContext, getString(R.string.trade_sale_over_price))
                return
            }
            if ("BID" == direction && java.lang.Double.compare(priceDouble, currentPrice * 1.2) > 0) {
                FryingUtil.showToast(mContext, getString(R.string.trade_buy_over_price))
                return
            }
        }
        val totalAmount = binding!!.fragmentHomePageTransactionHeader1.transactionQuota.text.toString().trim { it <= ' ' }
        val totalAmountDouble = CommonUtil.parseDouble(totalAmount)
        if (totalAmountDouble == null || totalAmountDouble == 0.0) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_count))
            return
        }
        val levelType = if (tabType == ConstData.TAB_LEVER) TransactionViewModel.LEVER_TYPE_LEVER else TransactionViewModel.LEVER_TYPE_COIN
        val createRunnable = Runnable {
            TradeApiServiceHelper.createTradeOrder(mContext, viewModel!!.getCurrentPair(), direction, totalAmount, price, levelType, object : NormalCallback<HttpRequestResultString?>() {
                override fun callback(returnData: HttpRequestResultString?) {
                    if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        binding!!.fragmentHomePageTransactionHeader1.price.setText("")
                        binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText("")
                        viewModel!!.getWalletLeverDetail()
                        FryingUtil.showToast(mContext, getString(R.string.trade_success))
                    } else {
                        FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                    }
                }
            })
        }
        if (levelType == TransactionViewModel.LEVER_TYPE_LEVER) {
            mContext?.let {
                FryingUtil.checkAndAgreeLeverProtocol(it, createRunnable)
            }
        } else {
            createRunnable.run()
        }

    }

    //撤销新单
    private fun cancelTradeOrder(tradeOrder: TradeOrder) {
        TradeApiServiceHelper.cancelTradeOrder(mContext, tradeOrder.id, tradeOrder.pair, tradeOrder.direction, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    adapter?.removeItem(tradeOrder)
                    adapter?.notifyDataSetChanged()
                } else {
                    FryingUtil.showToast(mContext, if (returnData == null) "null" else returnData.msg)
                }
            }
        })
    }

    override fun onPairStatusInit(pairStatus: PairStatus) {
        clearInput()
        binding!!.fragmentHomePageTransactionHeader1.price.filters = arrayOf(NumberFilter(), PointLengthFilter(pairStatus.precision))
        resetPriceLength()
        resetAmountLength()
        //清空当前委托
        if (mContext == null || CookieUtil.getUserInfo(mContext!!) == null) {
            showCurrentOrderList(null)
        }
        refreshTransactionHardViews()
        refreshSubmitButton()
        refreshData()
        if (!TextUtils.isEmpty(pairStatus.pair)) {
            binding!!.actionBarLayout.actionBarTitle.setText(pairStatus.pair!!.replace("_", "/"))
        }
        adapter?.setAmountPrecision(viewModel!!.getAmountLength())
        resetAmountLength()
        resetPriceLength()
        if (pairStatus.supportingPrecisionList != null) {
            onDeepChoose()
        }
    }

    override fun onPairStatusDataChanged(pairStatus: PairStatus) {
        CommonUtil.checkActivityAndRunOnUI(mContext) { updateCurrentPair(pairStatus) }
    }

    //用户信息被修改，刷新委托信息和钱包
    override fun onUserInfoChanged() {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            getTradeOrderCurrent()
            refreshCurrentWallet()
        }
    }

    override fun onTradeOrder(pair: String?, bidOrderList: List<TradeOrder?>?, askOrderList: List<TradeOrder?>?) {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            deepViewBinding!!.showBIDTradeOrders(pair, bidOrderList)
            deepViewBinding!!.showASKTradeOrders(pair, askOrderList)
        }
    }

    override fun onTradePairInfo(pairStatus: PairStatus?) {

    }

    override fun onWallet(observable: Observable<Pair<Wallet?, Wallet?>>?) {
        observable?.subscribe(HttpCallbackSimple(mContext, false, object : NormalCallback<Pair<Wallet?, Wallet?>?>() {
            override fun callback(returnData: Pair<Wallet?, Wallet?>?) {
                if (returnData != null) {
                    currentWallet = returnData.first
                    currentEstimatedWallet = returnData.second
                }
                refreshUsable()
            }

            override fun error(type: Int, error: Any) {
                refreshUsable()
            }
        }))
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
                        && TextUtils.equals(currentWallet?.coinType, viewModel!!.getCoinType()) && TextUtils.equals(currentEstimatedWallet?.coinType, viewModel!!.getSetName())) {
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
        if (tabType == ConstData.TAB_LEVER && TextUtils.equals(viewModel?.getCurrentPair(), leverDetail?.pair)) {
            this.leverDetail = leverDetail
            CommonUtil.checkActivityAndRunOnUI(mContext) {
                val checkRiskRate = leverDetail?.riskRate == null || leverDetail.riskRate == BigDecimal.ZERO
                binding?.actionBarLayout?.risk?.setText(String.format("%s%s",
                        if (checkRiskRate) nullAmount else if (leverDetail?.riskRate!! > BigDecimal(2)) ">200.00" else NumberUtil.formatNumberNoGroupHardScale(leverDetail?.riskRate!! * BigDecimal(100), 2),
                        if (checkRiskRate) nullAmount else "%"))
                if (leverDetail?.riskRate == null || leverDetail.riskRate == BigDecimal.ZERO || leverDetail?.riskRate!! >= BigDecimal(2)) {
                    binding?.actionBarLayout?.explodePrice?.setText(String.format("%s%s", nullAmount, nullAmount))
                } else {
                    val checkExplodePrice = leverDetail?.burstPrice == null || leverDetail.burstPrice == BigDecimal.ZERO
                    binding?.actionBarLayout?.explodePrice?.setText(String.format("%s%s", if (checkExplodePrice) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(leverDetail?.burstPrice, 9, 0, viewModel?.getPrecision()!!),
                            if (checkExplodePrice || leverDetail?.afterCoinType == null) nullAmount else leverDetail.afterCoinType))
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
            BlackRouter.getInstance().build(RouterConstData.WALLET_DETAIL).with(extras).go(mFragment)
        }, TRADE_INDEX)
    }

    override fun onCollectClick(transactionMorePopup: TransactionMorePopup, btnCollect: CheckedTextView) {
        viewModel!!.toggleDearPair(btnCollect.isChecked)
                ?.subscribe(HttpCallbackSimple(mContext, true, object : NormalCallback<HttpRequestResultString?>() {
                    override fun callback(result: HttpRequestResultString?) {
                        if (result != null && result.code == HttpRequestResult.SUCCESS) {
                            val showMsg = if (btnCollect.isChecked) getString(R.string.pair_collect_cancel_ok) else getString(R.string.pair_collect_add_ok)
                            FryingUtil.showToast(mContext, showMsg)
                            btnCollect.toggle()
                        } else {
                            FryingUtil.showToast(mContext, if (result == null) "null" else result.msg)
                        }
                    }
                }))
    }

    override fun onChatRoomClick(transactionMorePopup: TransactionMorePopup, chatRoomId: String?) {
        fryingHelper.checkUserAndDoing(Runnable {
            transactionMorePopup.dismiss()
            viewModel!!.checkIntoChatRoom()
                    ?.subscribe(HttpCallbackSimple(mContext, true, object : NormalCallback<HttpRequestResultString?>() {
                        override fun callback(returnData: HttpRequestResultString?) =
                                if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                                    intoChatRoom(chatRoomId)
                                } else {
                                    FryingUtil.showToast(mContext, if (returnData?.msg == null) "null" else returnData.msg)
                                }
                    }))
        }, 0)
    }

    override fun onTradeOrderFastClick(tradeOrder: TradeOrder) {
        binding!!.fragmentHomePageTransactionHeader1.price.setText(tradeOrder.formattedPrice)
        val scaleAnim = AnimationUtils.loadAnimation(mContext, R.anim.transaction_price_anim)
        binding!!.fragmentHomePageTransactionHeader1.price.startAnimation(scaleAnim)
        val amount = tradeOrder.beforeAmount ?: 0.0
        val price = CommonUtil.parseDouble(tradeOrder.formattedPrice) ?: 0.0
        var usableAmount = 0.0
        if (transactionType == 1) {
            usableAmount = if (currentEstimatedWallet != null && price != null && price != 0.0) {
                min(amount, currentEstimatedWallet?.coinAmount!!.toDouble() / price)
            } else {
                0.0
            }
        } else if (transactionType == 2) {
            usableAmount = if (currentWallet != null) {
                min(amount, currentWallet?.coinAmount!!.toDouble())
            } else {
                0.0
            }
        }
        if (usableAmount != 0.0) {
            binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText(NumberUtil.formatNumberNoGroup(usableAmount, RoundingMode.FLOOR, 0, viewModel!!.getAmountLength()))
        } else {
            binding!!.fragmentHomePageTransactionHeader1.transactionQuota.setText("0.0")
        }
    }

    override fun onDeepChanged(deep: Int) {
        onDeepChoose()
    }

    private fun updateCurrentPair(pairStatus: PairStatus) {
        val color = if (pairStatus.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) colorT3 else if (pairStatus.priceChangeSinceToday!! > 0) colorWin else colorLost
        binding!!.fragmentHomePageTransactionHeader1.currentPrice.setText(pairStatus.currentPriceFormat)
        binding!!.fragmentHomePageTransactionHeader1.currentPrice.setTextColor(color)
        binding!!.fragmentHomePageTransactionHeader1.currentPriceCny.setText(String.format("≈ %s", pairStatus.currentPriceCNYFormat))
        binding!!.actionBarLayout.currentPriceSince.setText(pairStatus.priceChangeSinceTodayFormat)
        binding!!.actionBarLayout.currentPriceSince.setTextColor(color)
        computePriceCNY()
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
            IMHelper.startWithIMGroupActivity(mContext!!, mContext, userIdHeader + userId, chatRoomId, RouterConstData.PUBLIC_CHAT_GROUP, bundle, null, null)
        }
    }
}