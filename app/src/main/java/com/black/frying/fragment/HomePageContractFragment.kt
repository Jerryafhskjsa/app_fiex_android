package com.black.frying.fragment

import android.app.Activity
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.util.Pair
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckedTextView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.black.base.adapter.interfaces.OnItemClickListener
import com.black.base.api.TradeApiServiceHelper
import com.black.base.api.WalletApiServiceHelper
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.*
import com.black.base.model.future.FundingRateBean
import com.black.base.model.future.MarkPriceBean
import com.black.base.model.future.TickerBean
import com.black.base.model.socket.*
import com.black.base.model.trade.TradeOrderResult
import com.black.base.model.user.UserBalance
import com.black.base.model.wallet.CoinInfoType
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletLeverDetail
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.view.AlertMessageDialog
import com.black.base.view.ContractMultipleSelectWindow
import com.black.base.view.DeepControllerWindow
import com.black.base.view.PairStatusPopupWindow
import com.black.base.view.PairStatusPopupWindow.OnPairStatusSelectListener
import com.black.base.widget.AutoHeightViewPager
import com.black.frying.activity.HomePageActivity
import com.black.frying.adapter.EntrustCurrentHomeAdapter
import com.black.frying.service.FutureService
import com.black.frying.view.ContractDeepViewBinding
import com.black.frying.view.TransactionMorePopup
import com.black.frying.view.TransactionMorePopup.OnTransactionMoreClickListener
import com.black.frying.viewmodel.ContractViewModel
import com.black.im.util.IMHelper
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageContractBinding
import com.fbsex.exchange.databinding.FragmentHomePageContractHeader1Binding
import com.fbsex.exchange.databinding.FragmentHomePageContractHeaderBinding
import com.google.android.material.tabs.TabLayout
import io.reactivex.Observable
import skin.support.content.res.SkinCompatResources
import udesk.org.jivesoftware.smackx.time.packet.Time
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.pow

//首页合约
@Route(
    value = [RouterConstData.HOME_CONTRACT],
    fragmentParentPath = RouterConstData.HOME_PAGE,
    fragmentIndex = 3
)
class HomePageContractFragment : BaseFragment(),
    View.OnClickListener,
    OnSeekBarChangeListener,
    EntrustCurrentHomeAdapter.OnHandleClickListener,
    OnItemClickListener,
    OnTransactionMoreClickListener,
    ContractViewModel.OnContractModelListener,
    ContractDeepViewBinding.OnTransactionDeepListener {
    companion object {
        private var TAG = HomePageContractFragment::class.java.simpleName
        fun newSelfInstance(tag: String?): HomePageContractFragment {
            val args = Bundle()
            val fragment = HomePageContractFragment()
            fragment.arguments = args
//            fragment.tag = tag
            return fragment
        }
    }


    private var colorWin = 0
    private var colorLost = 0
    private var colorT3 = 0

    private var transactionType = ConstData.FUTURE_OPERATE_OPEN // 1开仓,2平仓
    private var tabType = ConstData.TAB_COIN

    private var countProgressBuy: Drawable? = null
    private var countProgressSale: Drawable? = null
    private var currentOrderType: String? = "LIMIT"
    private var currentUnitType: String? = "USDT"
    private var currentTimeInForceType: String? = "GTC"
    private var inputNumber: Boolean? = false//是否手动输入数量
    private var isDear: Boolean? = null

    private var recordViewPager: AutoHeightViewPager? = null
    private var recordTab: TabLayout? = null
    private var headerView: FragmentHomePageContractHeaderBinding? = null
    private var header1View: FragmentHomePageContractHeader1Binding? = null


    //开仓倍数设置
    private var buyMultiChooseBean: ContractMultiChooseBean? = null

    //平仓倍数设置
    private var sellMultiChooseBean: ContractMultiChooseBean? = null

    //是否选中止盈止损
    private var withLimitFlag: Boolean? = false

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
    private var binding: FragmentHomePageContractBinding? = null
    private var viewModel: ContractViewModel? = null
    private var deepViewBinding: ContractDeepViewBinding? = null

    /**
     * 用户资产
     */
    private var userBalance: ArrayList<UserBalance?>? = null
    private var tabData: ArrayList<ContractRecordTabBean?>? = null
    private var recordFragmentList: MutableList<Fragment?>? = null
    private var currentTabPosition: Int = 0

    private var fundRateTime: Long? = null
    private val mFundRateTimerHandler = Handler()
    private val fundRateTimer = object : Runnable {
        override fun run() {
            fundRateTime = fundRateTime?.minus(1)
            if (fundRateTime!! <= 0) {
            } else {
                mFundRateTimerHandler.postDelayed(this, ConstData.ONE_SECOND_MILLIS.toLong())
                headerView?.tvRateTime?.text = TimeUtil.formatSeconds(fundRateTime!!)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            R.layout.fragment_home_page_contract,
            container,
            false
        )
        layout = binding?.root
        viewModel = ContractViewModel(mContext!!, this)

        binding!!.refreshLayout.setRefreshHolder(RefreshHolderFrying(activity!!))
        binding!!.refreshLayout.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                binding!!.refreshLayout.postDelayed(
                    { binding!!.refreshLayout.setRefreshing(false) },
                    300
                )
            }

        })
        deepViewBinding = ContractDeepViewBinding(
            mContext!!,
            viewModel!!,
            binding!!.fragmentHomePageContractHeader1
        )
        deepViewBinding?.setOnTransactionDeepListener(this)
        initData()
        initActionBar()
        initHeader()
        initHeader1()
        initHeader2()
        return layout
    }

    private fun initData() {
        buyMultiChooseBean = ContractMultiChooseBean()
        buyMultiChooseBean?.maxMultiple = 100
        buyMultiChooseBean?.defaultMultiple = 10
        buyMultiChooseBean?.orientation = "BUY"
        buyMultiChooseBean?.type = 0
        binding?.fragmentHomePageContractHeader?.buyMultiple?.text =
            getMultipleDes(buyMultiChooseBean)
        sellMultiChooseBean = ContractMultiChooseBean()
        sellMultiChooseBean?.maxMultiple = 100
        sellMultiChooseBean?.defaultMultiple = 10
        sellMultiChooseBean?.orientation = "SELL"
        sellMultiChooseBean?.type = 0
        binding?.fragmentHomePageContractHeader?.sellMultiple?.text =
            getMultipleDes(sellMultiChooseBean)
    }

    override fun getViewModel(): ContractViewModel? {
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

        FutureService.getContractSize("btc_usdt")
//        FutureService.initMarkPrice(mContext)
//        FutureService.getPositionAdl(mContext)
//        FutureService.getBalanceByCoin(mContext)
//          FutureService.getCurrentPosition(mContext)
//        FutureService.getUserStepRate(mContext)
//        FutureService.getOrderPosition(mContext)
        FutureService.getAvailableOpenData(BigDecimal("10000"), 5, BigDecimal(1000), BigDecimal.ZERO)
//        FutureService.createOrder(mContext,"BUY","LIMIT","btc_usdt","LONG","16880".toDouble(),"GTC",100)
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

    private fun initActionBar() {
        binding?.actionBarLayout?.btnTransactionMemu?.setOnClickListener(this)
        binding?.actionBarLayout?.headCharts?.setOnClickListener(this)
        binding?.actionBarLayout?.headTransactionMore?.setOnClickListener(this)
        binding?.actionBarLayout?.riskInfo?.setOnClickListener(this)
        binding?.actionBarLayout?.leverHandle?.setOnClickListener(this)
        binding?.actionBarLayout?.imgCollect?.setOnClickListener(this)
    }

    private fun initHeader() {
        headerView = binding?.fragmentHomePageContractHeader
        headerView?.linBuyMultiple?.setOnClickListener(this)
        headerView?.linSellMultiple?.setOnClickListener(this)
    }

    //开仓平仓
    private fun initHeader1() {
        header1View = binding!!.fragmentHomePageContractHeader1
        header1View?.contractWithLimit?.setOnCheckedChangeListener { _, isChecked ->
            withLimitFlag = isChecked
            binding!!.fragmentHomePageContractHeader1.linStopValue.visibility = if (isChecked) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
        header1View?.linOrderType?.setOnClickListener(this)
        header1View?.linUnitType?.setOnClickListener(this)
        header1View?.linLimitType?.setOnClickListener(this)
        header1View?.btnBuy?.setOnClickListener(this)
        header1View?.btnSale?.setOnClickListener(this)
        header1View?.price?.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                computeTotal()
                computePriceCNY()
                refreshSubmitButton()
            }

            override fun afterTextChanged(s: Editable) {
                header1View?.price?.setSelection(s.toString().length)
            }
        })
        header1View?.transactionQuota?.filters =
            arrayOf(NumberFilter(), PointLengthFilter(4))
        header1View?.transactionQuota?.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                inputNumber = true
                computeTotal()
                refreshSubmitButton()
                val count = CommonUtil.parseDouble(
                    header1View?.transactionQuota?.text.toString()
                        .trim { it <= ' ' })
                if (count != null) {
                    val max: BigDecimal? = getMaxAmount()
                    if (max != null) {
                        var countB = count?.let { BigDecimal(it) }
                        var progress = (countB?.divide(max, 2, BigDecimal.ROUND_HALF_DOWN))?.times(
                            BigDecimal(100)
                        )
                        header1View?.countBar?.progress =
                            progress?.toInt()!!
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                inputNumber = false
                header1View?.transactionQuota?.setSelection(s.toString().length)
            }
        })
        countProgressBuy =
            SkinCompatResources.getDrawable(mContext, R.drawable.bg_transaction_progress_bar_buy)
        countProgressSale =
            SkinCompatResources.getDrawable(mContext, R.drawable.bg_transaction_progress_bar_sale)
        header1View?.priceSub?.setOnClickListener(this)
        header1View?.priceAdd?.setOnClickListener(this)
        header1View?.amountAdd?.setOnClickListener(this)
        header1View?.amountSub?.setOnClickListener(this)
        header1View?.useable?.setText(getString(R.string.number_default))
        header1View?.useableUnit?.setText(getString(R.string.number_default))
        header1View?.useableBuy?.setText(getString(R.string.number_default))
        header1View?.useableBuyUnit?.setText(getString(R.string.number_default))
        header1View?.countBar?.setOnSeekBarChangeListener(this)
        header1View?.btnHandle?.setOnClickListener(this)
        header1View?.btnHandle1?.setOnClickListener(this)
        viewModel?.setCurrentPairorderType(currentOrderType)
        refreshOrderType(currentOrderType)
        deepViewBinding!!.init()
    }

    private fun initHeader2() {
        recordTab = binding!!.fragmentHomePageContractHeader2?.contractTab
        recordViewPager = binding!!.fragmentHomePageContractHeader2?.contractRecordViewPager
//        binding!!.fragmentHomePageContractHeader2.totalCurrent.setOnClickListener(this)
        initRecordTab()
    }

    private fun refreshView() {
        clearInput()
        refreshUsable()
        refreshTransactionHardViews()
        refreshSubmitButton()
    }

    //初始化记录相关的tab
    private fun initRecordTab() {
        recordTab?.setTabTextColors(
            SkinCompatResources.getColor(activity, R.color.C5),
            SkinCompatResources.getColor(activity, R.color.C1)
        )
        recordTab?.tabMode = TabLayout.MODE_SCROLLABLE
        if (tabData == null) {
            tabData = ArrayList()
        }
        var tab1 = ContractRecordTabBean()
        tab1.name = getString(R.string.contract_record_tab1)
        tab1.amount = 0
        tab1.type = ConstData.CONTRACT_REC_HOLD_AMOUNT
        var tab2 = ContractRecordTabBean()
        tab2.name = getString(R.string.contract_record_tab2)
        tab2.amount = 0
        tab2.type = ConstData.CONTRACT_REC_WITH_LIMIE
        var tab3 = ContractRecordTabBean()
        tab3.name = getString(R.string.contract_record_tab3)
        tab3.amount = 0
        tab3.type = ConstData.CONTRACT_REC_CURRENT
        tabData?.add(tab1)
        tabData?.add(tab2)
        tabData?.add(tab3)
        if (tabData != null && tabData!!.isNotEmpty()) {
            val tabSize = tabData!!.size
            if (recordFragmentList != null) {
                return
            }
            recordFragmentList = ArrayList(tabSize)
            for (i in 0 until tabSize) {
                val tabData = tabData!![i]
                var fragment: BaseFragment? = null
                try {
                    when (tabData?.type) {
                        ConstData.CONTRACT_REC_HOLD_AMOUNT -> fragment =
                            HomePageContractDetailFragment.newInstance(tabData)
                        ConstData.CONTRACT_REC_WITH_LIMIE, ConstData.CONTRACT_REC_CURRENT -> fragment =
                            EmptyFragment.newInstance("")
                    }
                    if (fragment is HomePageContractDetailFragment) {
                        fragment?.setAutoHeightViewPager(recordViewPager)
                    }
                    if (fragment is EmptyFragment) {
                        fragment?.setAutoHeightViewPager(recordViewPager)
                    }
                    var bundle = Bundle()
                    bundle.putInt(AutoHeightViewPager.POSITION, i)
                    fragment?.arguments = bundle
                    recordFragmentList?.add(fragment)
                } catch (throwable: Throwable) {
                    FryingUtil.printError(throwable)
                }
            }
            recordViewPager?.adapter = object : FragmentStatePagerAdapter(childFragmentManager) {
                override fun getItem(position: Int): Fragment {
                    if (recordFragmentList!![position] is HomePageContractDetailFragment) {
                        return recordFragmentList!![position] as HomePageContractDetailFragment
                    } else if (recordFragmentList!![position] is EmptyFragment) {
                        return recordFragmentList!![position] as EmptyFragment
                    } else {
                        return recordFragmentList!![position] as EmptyFragment
                    }
                }

                override fun getCount(): Int {
                    return recordFragmentList!!.size
                }

                override fun getPageTitle(position: Int): CharSequence? {
                    return tabData!![position]?.name
                }
            }
            recordViewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    recordViewPager?.updateHeight(position)
                }

                override fun onPageScrollStateChanged(state: Int) {
                }

            })
            recordViewPager?.updateHeight(0)
            recordTab?.setupWithViewPager(
                binding?.fragmentHomePageContractHeader2?.contractRecordViewPager,
                true
            )
            var tabCount = recordTab?.tabCount ?: 0
            for (i in 0 until (tabCount)) {
                val set = tabData!![i]
                try {
                    val tab = recordTab?.getTabAt(i)
                    if (tab != null) {
                        tab.setCustomView(R.layout.view_home_quotation_tab)
                        if (tab.customView != null) {
                            val textView =
                                tab.customView!!.findViewById<View>(android.R.id.text1) as TextView
                            textView.text = set?.name
                        }
                    }
                } catch (throwable: Throwable) {
                    FryingUtil.printError(throwable)
                }
            }
            recordViewPager?.currentItem = currentTabPosition
            recordTab?.getTabAt(currentTabPosition)?.select()
            recordTab?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                var textSize12 = resources.getDimensionPixelSize(R.dimen.text_size_12).toFloat()
                var textSize14 = resources.getDimensionPixelSize(R.dimen.text_size_14).toFloat()
                override fun onTabSelected(tab: TabLayout.Tab) {
                    val view = tab.customView
                    val textView =
                        if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                    textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize14)
                    recordViewPager?.currentItem = tab.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {
                    val view = tab.customView
                    val textView =
                        if (view == null) null else view.findViewById<View>(android.R.id.text1) as TextView
                    textView?.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize12)
                }

                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
    }

    private fun getMultipleDes(bean: ContractMultiChooseBean?): String? {
        var des: String? = null
        var typeDes: String? = null
        var orientationDes: String? = null
        var multiDes: String? = null
        typeDes = if (bean?.type == 0) {
            getString(R.string.contract_fiexble_position)
        } else {
            getString(R.string.contract_all_position)
        }
        multiDes = bean?.defaultMultiple.toString() + "X"
        orientationDes = if (bean?.orientation.equals("BUY")) {
            getString(R.string.contract_raise)
        } else {
            getString(R.string.contract_down)
        }
        des = typeDes + multiDes + orientationDes
        return des
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showOrientationMultipleDialog(bean: ContractMultiChooseBean?) {
        ContractMultipleSelectWindow(mContext as Activity,
            getString(R.string.contract_adjust),
            bean,
            object : ContractMultipleSelectWindow.OnReturnListener {
                override fun onReturn(
                    window: ContractMultipleSelectWindow,
                    item: ContractMultiChooseBean?
                ) {
                    if (item?.orientation.equals("BUY")) {
                        buyMultiChooseBean = item
                        binding?.fragmentHomePageContractHeader?.buyMultiple?.text =
                            getMultipleDes(buyMultiChooseBean)
                    } else {
                        sellMultiChooseBean = item
                        binding?.fragmentHomePageContractHeader?.sellMultiple?.text =
                            getMultipleDes(sellMultiChooseBean)
                    }
                }
            }).show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(v: View) {
        when (v.id) {
            R.id.lin_buy_multiple -> {
                showOrientationMultipleDialog(buyMultiChooseBean)
            }
            R.id.lin_sell_multiple -> {
                showOrientationMultipleDialog(sellMultiChooseBean)
            }
            R.id.tab_transaction_coin -> {
                if (tabType != ConstData.TAB_COIN) {
                    changeTabType(ConstData.TAB_COIN)
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
//                                    binding?.fragmentHomePageContractHeader1?.relVolume?.visibility = View.VISIBLE
                            } else if (currentOrderType.equals("MARKET")) {
//                                    binding?.fragmentHomePageContractHeader1?.relVolume?.visibility = View.GONE
                            }
                        }
                    }).show()
            }
            R.id.lin_unit_type -> {
                DeepControllerWindow(mContext as Activity,
                    getString(R.string.select_order_type),
                    currentUnitType,
                    viewModel?.getCurrentUnitTypeList() as List<String?>?,
                    object : DeepControllerWindow.OnReturnListener<String?> {
                        override fun onReturn(
                            window: DeepControllerWindow<String?>,
                            item: String?
                        ) {
                            refreshUnitType(item)
                            currentUnitType = item
                            viewModel?.setCurrentUnitType(item)
                            if (currentOrderType.equals("LIMIT")) {
                            } else if (currentOrderType.equals("MARKET")) {
                            }
                        }
                    }).show()
            }
            R.id.lin_limit_type -> {
                DeepControllerWindow(mContext as Activity,
                    getString(R.string.select_order_type),
                    currentTimeInForceType,
                    viewModel?.getCurrentTimeInForceTypeList() as List<String?>?,
                    object : DeepControllerWindow.OnReturnListener<String?> {
                        override fun onReturn(
                            window: DeepControllerWindow<String?>,
                            item: String?
                        ) {
                            refreshTimeInForceType(item)
                            currentTimeInForceType = item
                            viewModel?.setCurrentTimeInForceType(item)
                        }
                    }).show()
            }
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
                    }
                }))
            R.id.btn_transaction_memu -> mContext?.let {
                var setData = ArrayList<QuotationSet?>(3)
                var optionalUbaseSet = QuotationSet()
                optionalUbaseSet.coinType = getString(R.string.usdt)
                optionalUbaseSet.name = getString(R.string.usdt_base)
                setData?.add(optionalUbaseSet)
                var optionalCoinBaseSet = QuotationSet()
                optionalCoinBaseSet.coinType = getString(R.string.usd)
                optionalCoinBaseSet.name = getString(R.string.coin_base)
                setData?.add(optionalCoinBaseSet)
                PairStatusPopupWindow.getInstance(
                    it,
                    PairStatusPopupWindow.TYPE_FUTURE_ALL,
                    setData
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
            R.id.price_sub -> {
                var currentInputPrice = CommonUtil.parseDouble(
                    binding!!.fragmentHomePageContractHeader1.price.text.toString()
                        .trim { it <= ' ' })
                currentInputPrice = currentInputPrice ?: 0.toDouble()
                val onUnitPrice: Double = getOnUnitPrice()
                if (currentInputPrice > 0) {
                    currentInputPrice -= onUnitPrice
                    currentInputPrice = max(currentInputPrice, 0.0)
                    binding!!.fragmentHomePageContractHeader1.price.setText(
                        String.format(
                            "%." + viewModel!!.getPrecision() + "f",
                            currentInputPrice
                        )
                    )
                }
            }
            R.id.price_add -> {
                var currentInputPrice = CommonUtil.parseDouble(
                    binding!!.fragmentHomePageContractHeader1.price.text.toString()
                        .trim { it <= ' ' })
                currentInputPrice = currentInputPrice ?: 0.toDouble()
                val onUnitPrice: Double = getOnUnitPrice()
                currentInputPrice += onUnitPrice
                binding!!.fragmentHomePageContractHeader1.price.setText(
                    String.format(
                        "%." + viewModel!!.getPrecision() + "f",
                        currentInputPrice
                    )
                )
            }
            R.id.amount_add -> {
                var currentInputAmount = CommonUtil.parseDouble(
                    binding!!.fragmentHomePageContractHeader1.transactionQuota.text.toString()
                        .trim { it <= ' ' })
                currentInputAmount = currentInputAmount ?: 0.toDouble()
                val onUnitAmount: Double = getOnUnitAmount()
                currentInputAmount += onUnitAmount
                binding!!.fragmentHomePageContractHeader1.transactionQuota.setText(
                    String.format(
                        "%." + viewModel!!.getAmountLength() + "f",
                        currentInputAmount
                    )
                )
            }
            R.id.amount_sub -> {
                var currentInputAmount = CommonUtil.parseDouble(
                    binding!!.fragmentHomePageContractHeader1.transactionQuota.text.toString()
                        .trim { it <= ' ' })
                currentInputAmount = currentInputAmount ?: 0.toDouble()
                val onUnitAmount: Double = getOnUnitAmount()
                if (currentInputAmount > 0) {
                    currentInputAmount -= onUnitAmount
                    currentInputAmount = max(currentInputAmount, 0.0)
                    binding!!.fragmentHomePageContractHeader1.transactionQuota.setText(
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
                    if (transactionType != ConstData.FUTURE_OPERATE_OPEN) {
                        transactionType = ConstData.FUTURE_OPERATE_OPEN
                        //refreshSeekBar();
                        refreshView()
                    }
                }
            }
            R.id.btn_sale -> context?.let {
                if (CookieUtil.getUserInfo(it) == null) {
                    fryingHelper.checkUserAndDoing(Runnable { }, TRADE_INDEX)
                } else {
                    if (transactionType != ConstData.FUTURE_OPERATE_CLOSE) {
                        transactionType = ConstData.FUTURE_OPERATE_CLOSE
                        //                    refreshSeekBar();
                        refreshView()
                    }
                }
            }
            R.id.btn_handle_1 -> {

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
                                            if (transactionType == ConstData.FUTURE_OPERATE_OPEN) { //买入
                                                createOrder("BUY")
                                            } else if (transactionType == ConstData.FUTURE_OPERATE_CLOSE) { //卖出
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
        viewModel!!.setTabType(tabType)
        viewModel!!.changePairSocket()
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
        binding!!.fragmentHomePageContractHeader1.countProgress.progress = progress
        val amountPercent = progress.toDouble() / seekBar.max
        val max: BigDecimal? = getMaxAmount()
        if (!inputNumber!!) {
            if (max == null || max == BigDecimal.ZERO) {
                binding!!.fragmentHomePageContractHeader1.transactionQuota.setText("0.00")
            } else {
                binding!!.fragmentHomePageContractHeader1.transactionQuota.setText(
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
        binding!!.fragmentHomePageContractHeader1.price.resetRes()
        binding!!.fragmentHomePageContractHeader1.transactionQuota.resetRes()
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
                binding!!.fragmentHomePageContractHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountTwenty.isChecked = false
                binding!!.fragmentHomePageContractHeader1.amountFourty.isChecked = false
                binding!!.fragmentHomePageContractHeader1.amountSixty.isChecked = false
                binding!!.fragmentHomePageContractHeader1.amountEighty.isChecked = false
                binding!!.fragmentHomePageContractHeader1.amountAll.isChecked = false
            }
            1 -> {
                binding!!.fragmentHomePageContractHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountTwenty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountFourty.isChecked = false
                binding!!.fragmentHomePageContractHeader1.amountSixty.isChecked = false
                binding!!.fragmentHomePageContractHeader1.amountEighty.isChecked = false
                binding!!.fragmentHomePageContractHeader1.amountAll.isChecked = false
            }
            2 -> {
                binding!!.fragmentHomePageContractHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountTwenty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountFourty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountSixty.isChecked = false
                binding!!.fragmentHomePageContractHeader1.amountEighty.isChecked = false
                binding!!.fragmentHomePageContractHeader1.amountAll.isChecked = false
            }
            3 -> {
                binding!!.fragmentHomePageContractHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountTwenty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountFourty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountSixty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountEighty.isChecked = false
                binding!!.fragmentHomePageContractHeader1.amountAll.isChecked = false
            }
            4 -> {
                binding!!.fragmentHomePageContractHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountTwenty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountFourty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountSixty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountEighty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountAll.isChecked = false
            }
            5 -> {
                binding!!.fragmentHomePageContractHeader1.amountZero.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountTwenty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountFourty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountSixty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountEighty.isChecked = true
                binding!!.fragmentHomePageContractHeader1.amountAll.isChecked = true
            }
        }
    }

    //处理点击，撤销订单
    override fun onHandleClick(tradeOrder: TradeOrderFiex) {
        //新订单可以撤销
    }

    //计算最大交易数量
    private fun getMaxAmount(): BigDecimal? {
        if (transactionType == ConstData.FUTURE_OPERATE_OPEN) {
            val usable = currentBalanceSell?.availableBalance
            val price =
                CommonUtil.parseDouble(binding!!.fragmentHomePageContractHeader1.price.text.toString())
            return if (usable == null || price == null || price == 0.0) null else BigDecimal(usable).divide(
                BigDecimal(price),
                2,
                BigDecimal.ROUND_HALF_DOWN
            )
        } else if (transactionType == ConstData.FUTURE_OPERATE_CLOSE) {
            return currentBalanceBuy?.availableBalance?.toBigDecimal()
        }
        return null
    }


    private fun resetAmountLength() {
        binding!!.fragmentHomePageContractHeader1.transactionQuota.filters =
            arrayOf(NumberFilter(), PointLengthFilter(viewModel!!.getAmountLength()))
    }

    private fun resetPriceLength() {
        binding!!.fragmentHomePageContractHeader1.price.filters = arrayOf(
            NumberFilter(), PointLengthFilter(
                viewModel?.getPrecision()
                    ?: 8
            )
        )
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
            binding!!.fragmentHomePageContractHeader1.price.text.toString().trim { it <= ' ' })
        val count = CommonUtil.parseDouble(
            binding!!.fragmentHomePageContractHeader1.transactionQuota.text.toString()
                .trim { it <= ' ' })
        if (price != null) {
            if (count != null && (count != 0.0)) {
                if (currentOrderType.equals("LIMIT")) {
//                    binding!!.fragmentHomePageContractHeader1.tradeValue.setText(NumberUtil.formatNumberNoGroup(price * count, RoundingMode.FLOOR, viewModel!!.getAmountLength(), viewModel!!.getAmountLength())+viewModel!!.getSetName())
                }
            } else { //只有价格
                if (transactionType == ConstData.FUTURE_OPERATE_OPEN) {
                    binding!!.fragmentHomePageContractHeader1.actionType.setText(R.string.buy_usable)
                    binding!!.fragmentHomePageContractHeader1.useableBuyUnit.setText(viewModel!!.getCoinType())
                    if (price > 0 && currentBalanceSell != null) {
                        //总的钱数除以输入价格
                        binding!!.fragmentHomePageContractHeader1.useableBuy.setText(
                            NumberUtil.formatNumberNoGroup(
                                currentBalanceSell?.availableBalance!!.toDouble()
                                    .div(price.toDouble()),
                                RoundingMode.FLOOR,
                                viewModel!!.getAmountLength(),
                                viewModel!!.getAmountLength()
                            )
                        )
                    } else {
                        binding!!.fragmentHomePageContractHeader1.useableBuy.setText("0.0")
                    }
                } else if (transactionType == ConstData.FUTURE_OPERATE_CLOSE) {
                    binding!!.fragmentHomePageContractHeader1.useableBuyUnit.setText(viewModel!!.getSetName())
                    binding!!.fragmentHomePageContractHeader1.actionType.setText(R.string.sale_usable)
                    if (price > 0 && currentBalanceBuy != null) {
                        //总的钱数乘以输入价格
                        binding!!.fragmentHomePageContractHeader1.useableBuy.setText(
                            NumberUtil.formatNumberNoGroup(
                                currentBalanceBuy?.availableBalance!!.toDouble() * price.toDouble(),
                                RoundingMode.FLOOR,
                                viewModel!!.getAmountLength(),
                                viewModel!!.getAmountLength()
                            )
                        )
                    } else {
                        binding!!.fragmentHomePageContractHeader1.useableBuy.setText("0.0")
                    }
                }
                if (currentOrderType.equals("LIMIT")) {
                    if (price != null && count != null) {
//                        binding!!.fragmentHomePageContractHeader1.tradeValue.setText(NumberUtil.formatNumberNoGroup(price * count!!, RoundingMode.FLOOR, viewModel!!.getAmountLength(), viewModel!!.getAmountLength())+viewModel!!.getSetName())
                    }
                }
            }
        } else {
            if (transactionType == ConstData.FUTURE_OPERATE_OPEN) {
                binding!!.fragmentHomePageContractHeader1.actionType.setText(R.string.buy_usable)
                binding!!.fragmentHomePageContractHeader1.useableBuy.setText("0.0")
                binding!!.fragmentHomePageContractHeader1.useableBuyUnit.setText(viewModel!!.getCoinType())
            } else if (transactionType == ConstData.FUTURE_OPERATE_CLOSE) {
                binding!!.fragmentHomePageContractHeader1.useableBuyUnit.setText(viewModel!!.getSetName())
                binding!!.fragmentHomePageContractHeader1.useableBuy.setText("0.0")
                binding!!.fragmentHomePageContractHeader1.actionType.setText(R.string.sale_usable)
            }
        }
    }

    //计算当前输入价格CNY
    private fun computePriceCNY() {
        val price = CommonUtil.parseDouble(
            binding!!.fragmentHomePageContractHeader1.price.text.toString().trim { it <= ' ' })
        if (price != null && price > 0 && viewModel!!.getCurrentPriceCNY() != null && viewModel!!.getCurrentPrice() != 0.0) {
            binding!!.fragmentHomePageContractHeader1.priceCny.setText(
                "≈" + NumberUtil.formatNumberNoGroup(
                    viewModel!!.getCurrentPriceCNY()!! * price / viewModel!!.getCurrentPrice(),
                    4,
                    4
                )
            )
        } else {
            binding!!.fragmentHomePageContractHeader1.priceCny.setText(
                "≈" + NumberUtil.formatNumberNoGroup(
                    0.0f,
                    4,
                    4
                )
            )
        }
    }

    private fun clearInput() {
        binding!!.fragmentHomePageContractHeader1.price.setText("")
        binding!!.fragmentHomePageContractHeader1.transactionQuota.setText("")
        binding!!.fragmentHomePageContractHeader1.countBar.progress = 0
        binding!!.fragmentHomePageContractHeader1.countProgress.progress = 0
        binding!!.fragmentHomePageContractHeader1.amountTwenty.isChecked = false
        binding!!.fragmentHomePageContractHeader1.amountFourty.isChecked = false
        binding!!.fragmentHomePageContractHeader1.amountSixty.isChecked = false
        binding!!.fragmentHomePageContractHeader1.amountEighty.isChecked = false
        binding!!.fragmentHomePageContractHeader1.amountAll.isChecked = false
        refreshSubmitButton()
    }

    //交易对切换，刷新控件，刷新交易对信息，刷新深度，刷新数据,通知交易对改变
    fun onPairStatusChanged(pairStatus: PairStatus) {
        clearInput()
        deepViewBinding!!.clearASKTradeOrders()
        deepViewBinding!!.clearBIDTradeOrders()
        mContext?.let {
            CookieUtil.setCurrentFutureUPair(it, pairStatus.pair)
        }
        viewModel!!.getCurrentPairStatus(pairStatus.pair)
        viewModel!!.changePairSocket()
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
                binding!!.fragmentHomePageContractHeader1?.linPrice.visibility = View.GONE
                binding!!.fragmentHomePageContractHeader1?.linPrinceCny.visibility = View.GONE
            }
            "LIMIT" -> {
                typeDes = getString(R.string.order_type_limit)
                binding!!.fragmentHomePageContractHeader1?.linPrice.visibility = View.VISIBLE
                binding!!.fragmentHomePageContractHeader1?.linPrinceCny.visibility = View.VISIBLE
            }
        }
        binding!!.fragmentHomePageContractHeader1?.orderType.text = typeDes
    }

    private fun refreshUnitType(type: String?) {
        binding!!.fragmentHomePageContractHeader1?.unitType.text = type
    }

    private fun refreshTimeInForceType(type: String?) {
        binding!!.fragmentHomePageContractHeader1?.withLimitType.text = type
    }

    private fun refreshSubmitButton() {
        val userInfo = if (mContext == null) null else CookieUtil.getUserInfo(mContext!!)
        if (userInfo == null) {
            binding!!.fragmentHomePageContractHeader1.btnHandle.isEnabled = true
        } else {
            val price = binding!!.fragmentHomePageContractHeader1.price.text.toString()
            val count = binding!!.fragmentHomePageContractHeader1.transactionQuota.text.toString()
            if (currentOrderType.equals("LIMIT")) {

            } else if (currentOrderType.equals("MARKET")) {

            }
        }
    }

    //刷新交易区控件
    private fun refreshTransactionHardViews() {
        if (transactionType == ConstData.FUTURE_OPERATE_OPEN) {
            binding!!.fragmentHomePageContractHeader1.contractWithLimit.visibility = View.VISIBLE
            if (withLimitFlag == true) {
                binding!!.fragmentHomePageContractHeader1.linStopValue.visibility = View.VISIBLE
            } else {
                binding!!.fragmentHomePageContractHeader1.linStopValue.visibility = View.GONE
            }

            binding!!.fragmentHomePageContractHeader1.btnBuy.isChecked = true
            binding!!.fragmentHomePageContractHeader1.btnSale.isChecked = false
            binding!!.fragmentHomePageContractHeader1.countProgress.progressDrawable =
                countProgressBuy
            binding!!.fragmentHomePageContractHeader1.amountZero.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageContractHeader1.amountTwenty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageContractHeader1.amountFourty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageContractHeader1.amountSixty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageContractHeader1.amountEighty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            binding!!.fragmentHomePageContractHeader1.amountAll.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
        } else if (transactionType == ConstData.FUTURE_OPERATE_CLOSE) {
            binding!!.fragmentHomePageContractHeader1.contractWithLimit.visibility = View.GONE
            binding!!.fragmentHomePageContractHeader1.linStopValue.visibility = View.GONE
            binding!!.fragmentHomePageContractHeader1.btnBuy.isChecked = false
            binding!!.fragmentHomePageContractHeader1.btnSale.isChecked = true
            binding!!.fragmentHomePageContractHeader1.countProgress.progressDrawable =
                countProgressSale
            binding!!.fragmentHomePageContractHeader1.amountZero.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageContractHeader1.amountTwenty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageContractHeader1.amountFourty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageContractHeader1.amountSixty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageContractHeader1.amountEighty.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
            binding!!.fragmentHomePageContractHeader1.amountAll.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_sale)
        }
        if (!TextUtils.isEmpty(viewModel!!.getCurrentPair())) {
            binding!!.actionBarLayout.actionBarTitle.setText(viewModel!!.getCoinType())
            binding!!.actionBarLayout.pairSetName.setText("/" + viewModel!!.getSetName())
            binding!!.fragmentHomePageContractHeader1.deepPriceP.text =
                getString(R.string.brackets, viewModel!!.getSetName())
            binding!!.fragmentHomePageContractHeader1.deepAmountName.text =
                getString(R.string.brackets, viewModel!!.getCoinType())
            if (transactionType == ConstData.FUTURE_OPERATE_OPEN) {
                binding!!.fragmentHomePageContractHeader1.useableUnit.setText(viewModel!!.getSetName())
                binding!!.fragmentHomePageContractHeader1.useableBuyUnit.setText(viewModel!!.getCoinType())
                binding!!.fragmentHomePageContractHeader1.useableFreezUnit.setText(viewModel!!.getSetName())
                binding!!.fragmentHomePageContractHeader1.btnHandle.setText(getString(R.string.contract_buy_raise))
                binding!!.fragmentHomePageContractHeader1.btnHandle1.setText(getString(R.string.contract_sell_fall))
            } else if (transactionType == ConstData.FUTURE_OPERATE_CLOSE) {
                binding!!.fragmentHomePageContractHeader1.useableUnit.setText(viewModel!!.getCoinType())
                binding!!.fragmentHomePageContractHeader1.useableBuyUnit.setText(viewModel!!.getSetName())
                binding!!.fragmentHomePageContractHeader1.useableFreezUnit.setText(viewModel!!.getCoinType())
                binding!!.fragmentHomePageContractHeader1.btnHandle.setText(getString(R.string.contract_buy_fall))
                binding!!.fragmentHomePageContractHeader1.btnHandle1.setText(getString(R.string.contract_sell_raise))
            }
        }
        if (mContext == null || CookieUtil.getUserInfo(mContext!!) == null) {
            binding!!.fragmentHomePageContractHeader1.btnHandle.setText(R.string.login)
        }
        refreshCurrentWallet()
    }

    private fun refreshDeepView() {
        var deep = viewModel!!.getPrecisionDeep(viewModel!!.getPrecision())
        binding!!.fragmentHomePageContractHeader1.deep.setText(
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
        viewModel?.getIndexPrice(viewModel?.getCurrentPair())
        viewModel?.getMarketPrice(viewModel?.getCurrentPair())
        viewModel?.getSymbolTicker(viewModel?.getCurrentPair())
        viewModel?.getFundRate(viewModel?.getCurrentPair())
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
                    binding!!.fragmentHomePageContractHeader1.useable.setText(
                        NumberUtil.formatNumberNoGroup(
                            currentBalanceSell?.availableBalance?.toDoubleOrNull(),
                            RoundingMode.FLOOR,
                            0,
                            8
                        )
                    )
                    binding!!.fragmentHomePageContractHeader1.freezAmount.setText(
                        NumberUtil.formatNumberNoGroup(
                            currentBalanceSell?.freeze?.toDoubleOrNull(),
                            RoundingMode.FLOOR,
                            0,
                            8
                        )
                    )
                } else {
                    binding!!.fragmentHomePageContractHeader1.useable.setText("0.0")
                }
                binding!!.fragmentHomePageContractHeader1.actionType.setText(R.string.buy_usable)
                binding!!.fragmentHomePageContractHeader1.useableBuy.setText("0.0")
            } else if (transactionType == 2) {
                if (currentBalanceBuy != null) {
                    binding!!.fragmentHomePageContractHeader1.useable.setText(
                        NumberUtil.formatNumberNoGroup(
                            currentBalanceBuy?.availableBalance?.toDoubleOrNull(),
                            RoundingMode.FLOOR,
                            0,
                            8
                        )
                    )
                    binding!!.fragmentHomePageContractHeader1.freezAmount.setText(
                        NumberUtil.formatNumberNoGroup(
                            currentBalanceBuy?.freeze?.toDoubleOrNull(),
                            RoundingMode.FLOOR,
                            0,
                            8
                        )
                    )
                } else {
                    binding!!.fragmentHomePageContractHeader1.useable.setText("0.0")
                }
                binding!!.fragmentHomePageContractHeader1.useableBuy.setText("0.0")
                binding!!.fragmentHomePageContractHeader1.actionType.setText(R.string.sale_usable)
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
                    }

                    override fun callback(returnData: HttpRequestResultData<TradeOrderResult?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                            Log.d(
                                TAG,
                                "getTradeOrderCurrent data.size = " + returnData.data?.items?.size
                            )
                        }
                    }
                })
        }
    }

    //下单
    private fun createOrder(direction: String) {
        var price: String? =
            binding!!.fragmentHomePageContractHeader1.price.text.toString().trim { it <= ' ' }
        val priceDouble = CommonUtil.parseDouble(price)
        if (currentOrderType.equals("LIMIT")) {
            if (priceDouble == null || priceDouble == 0.0) {
                FryingUtil.showToast(mContext, getString(R.string.alert_input_price))
                return
            }
            val currentPrice =
                CommonUtil.parseDouble(binding!!.fragmentHomePageContractHeader1.currentPrice.text.toString())
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
        val totalAmount = binding!!.fragmentHomePageContractHeader1.transactionQuota.text.toString()
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
                            binding!!.fragmentHomePageContractHeader1.price.setText("")
                            binding!!.fragmentHomePageContractHeader1.transactionQuota.setText("")
//                        binding!!.fragmentHomePageContractHeader1.tradeValue.setText(NumberUtil.formatNumberNoGroup(0, RoundingMode.FLOOR, viewModel!!.getAmountLength(), viewModel!!.getAmountLength())+viewModel!!.getSetName())
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

    //更新涨跌幅和当前价格
    override fun onPairQuotation(tickerBean: TickerBean?) {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            updatePriceSince(tickerBean?.r)
            updateCurrentPairPrice(tickerBean?.c)
        }
    }

    override fun onPairStatusInit(pairStatus: PairStatus?) {
        clearInput()
        binding!!.fragmentHomePageContractHeader1.price.filters = arrayOf(NumberFilter(),
            pairStatus?.precision?.let { PointLengthFilter(it) })
        resetPriceLength()
        resetAmountLength()
        refreshTransactionHardViews()
        refreshSubmitButton()
        refreshData()
        if (!TextUtils.isEmpty(pairStatus?.pair)) {
            binding!!.actionBarLayout.actionBarTitle.setText(viewModel!!.getCoinType())
            binding!!.actionBarLayout.pairSetName.setText("/" + viewModel!!.getSetName())
        }
        resetAmountLength()
        resetPriceLength()
        if (pairStatus?.supportingPrecisionList != null) {
            onDeepChoose()
        }
        currentOrderType = "LIMIT"
        currentUnitType = "USDT"
        currentTimeInForceType = "GTC"
        viewModel?.setCurrentPairorderType(currentOrderType)
        viewModel?.setCurrentUnitType(currentUnitType)
        viewModel?.setCurrentTimeInForceType(currentTimeInForceType)
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
    }

    override fun onTradeOrderFastClick(tradeOrder: TradeOrder) {
        if (currentOrderType.equals("MARKET")) {
            return
        }
        binding!!.fragmentHomePageContractHeader1.price.setText(tradeOrder.formattedPrice)
        val scaleAnim = AnimationUtils.loadAnimation(mContext, R.anim.transaction_price_anim)
        binding!!.fragmentHomePageContractHeader1.price.startAnimation(scaleAnim)
//        val amount = binding!!.fragmentHomePageContractHeader1.transactionQuota.text.toString().toDouble()
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
//            binding!!.fragmentHomePageContractHeader1.transactionQuota.setText(NumberUtil.formatNumberNoGroup(usableAmount, RoundingMode.FLOOR, 0, viewModel!!.getAmountLength()))
//        } else {
//            binding!!.fragmentHomePageContractHeader1.transactionQuota.setText("0.0")
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

    override fun onIndexPirce(indexPrice: MarkPriceBean?) {
        header1View?.indexPrice?.text = indexPrice?.p
    }

    override fun onMarketPrice(marketPrice: MarkPriceBean?) {
        header1View?.tagPrice?.text = marketPrice?.p
    }

    override fun onFundingRate(fundRate: FundingRateBean?) {
        Log.d("iiiiii", "onFundingRate")
        headerView?.tvFundRate?.text = fundRate?.fundingRate
        updateFundTime(fundRate?.nextCollectionTime)
    }

    private fun updateFundTime(nextCollectionTime: Long?) {
        fundRateTime = (nextCollectionTime?.minus(System.currentTimeMillis()))?.div(1000)
        mFundRateTimerHandler.removeCallbacks(fundRateTimer)
        mFundRateTimerHandler.post(fundRateTimer)
    }

    private fun updateCurrentPair(pairStatus: PairStatus) {
        val color =
            if (pairStatus.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) colorT3 else if (pairStatus.priceChangeSinceToday!! > 0) colorWin else colorLost
        header1View?.currentPrice?.setText(pairStatus.currentPriceFormat)
        header1View?.currentPrice?.setTextColor(color)
        header1View?.currentPriceCny?.setText(
            String.format(
                "≈ %s",
                pairStatus.currentPriceCNYFormat
            )
        )
        computePriceCNY()
    }

    private fun updateCurrentPairPrice(price: String?) {
        header1View?.currentPrice?.setText(price)
        if (price != null && price.toDouble() > 0 && viewModel!!.getCurrentPriceCNY() != null && viewModel!!.getCurrentPrice() != 0.0) {
            header1View?.currentPriceCny?.setText(
                "≈" + NumberUtil.formatNumberNoGroup(
                    viewModel!!.getCurrentPriceCNY()!! * price.toDouble() / viewModel!!.getCurrentPrice(),
                    4,
                    4
                )
            )
        } else {
            header1View?.currentPriceCny?.setText(
                "≈" + NumberUtil.formatNumberNoGroup(
                    0.0f,
                    4,
                    4
                )
            )
        }
    }

    //更新涨跌幅
    private fun updatePriceSince(since: String?) {
        var since = since?.toDouble()
        var background: Drawable?
        var color: Int?
        var styleChange = StyleChangeUtil.getStyleChangeSetting(mContext!!)?.styleCode
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
            if (since < 0) {//跌
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


}
