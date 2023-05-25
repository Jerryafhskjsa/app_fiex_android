package com.black.frying.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Typeface
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
import android.view.*
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
import com.black.base.api.C2CApiServiceHelper
import com.black.base.api.FutureApiServiceHelper
import com.black.base.api.TradeApiServiceHelper
import com.black.base.filter.NumberFilter
import com.black.base.filter.PointLengthFilter
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.*
import com.black.base.model.future.*
import com.black.base.model.socket.*
import com.black.base.model.trade.TradeOrderResult
import com.black.base.model.user.UserBalance
import com.black.base.model.wallet.Wallet
import com.black.base.model.wallet.WalletLeverDetail
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.view.AlertMessageDialog
import com.black.base.view.ContractMultipleSelectWindow
import com.black.base.view.DeepControllerWindow
import com.black.base.view.PairStatusPopupWindow
import com.black.base.view.PairStatusPopupWindow.OnPairStatusSelectListener
import com.black.base.widget.AnalyticChart
import com.black.base.widget.AnalyticChart2
import com.black.base.widget.AutoHeightViewPager
import com.black.frying.activity.HomePageActivity
import com.black.frying.activity.QuotationDetailActivity
import com.black.frying.adapter.EntrustCurrentHomeAdapter
import com.black.frying.service.FutureService
import com.black.frying.view.ContractDeepViewBinding
import com.black.frying.view.FutureMSG
import com.black.frying.view.KLineQuotaSelector
import com.black.frying.view.MoreTimeStepSelector
import com.black.frying.view.PositionSideSelector
import com.black.frying.view.PositionTypeSeletor
import com.black.frying.view.TransactionMorePopup
import com.black.frying.view.TransactionMorePopup.OnTransactionMoreClickListener
import com.black.frying.viewmodel.ContractViewModel
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.black.util.NumberUtils
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageContractBinding
import com.fbsex.exchange.databinding.FragmentHomePageContractHeader1Binding
import com.fbsex.exchange.databinding.FragmentHomePageContractHeaderBinding
import com.google.android.material.tabs.TabLayout
import io.reactivex.Observable
import retrofit2.http.Field
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.pow

const val LIMIT = "LIMIT"
const val MARKET = "MARKET"
const val PLAN = "PLAN"

//首页合约简介
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
        var TAB_CROSSED = "CROSSED" // 全仓
        var TAB_ISOLATED = "ISOLATED" //逐仓
    }


    private var colorWin = 0
    private var colorLost = 0
    private var colorT3 = 0

    private var moreTimeStepSelector: MoreTimeStepSelector? = null
    private var kLineQuotaSelector: PositionSideSelector? = null
    private var positionTypeSelector: PositionTypeSeletor? = null
    private var futureMSG: FutureMSG? = null
    private var moreTimeStepTextView: TextView? = null
    private var kLinePage = 0
    private var lastTimeStepIndex = 0
    private var transactionType = ConstData.FUTURE_OPERATE_OPEN // 1开仓,2平仓
    private var tabType = ConstData.TAB_COIN

    private var countProgressBuy: Drawable? = null
    private var countProgressSale: Drawable? = null
    private var rates: Double? = C2CApiServiceHelper.coinUsdtPrice?.usdt
    private var exchanged = 0
    private var currentOrderType: String? = LIMIT
    private var currentUnitType: String? = "USDT"
    private var currentTimeInForceType: String? = "GTC"
    private var inputNumber: Boolean? = false//是否手动输入数量
    private var isDear: Boolean? = null
    private var token: String? = null
    private var recordViewPager: AutoHeightViewPager? = null
    private var recordTab: TabLayout? = null
    private var headerView: FragmentHomePageContractHeaderBinding? = null
    private var header1View: FragmentHomePageContractHeader1Binding? = null
    private var priceInputFlag: Boolean? = false
    private var leverage: Int? = 10
    private var balanceDetailBean1: BalanceDetailBean? = null
    private var contractSize: BigDecimal? = BigDecimal.ZERO
    private var userStepRate: UserStepRate? = null

    //交易对杠杆分层
    private var leverageBracket: LeverageBracketBean? = null


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
    private var list: MutableList<String>? = null
    private var positionType: String? = null

    /**
     * 用户资产
     */
    private var userBalance: ArrayList<UserBalance?>? = null
    private var tabData: ArrayList<ContractRecordTabBean?>? = null
    private var recordFragmentList: MutableList<Fragment?>? = null
    private var positionTabListener: ContractPositionTabFragment.OnTabModelListener? = null
    private var profitTabListener: ContractCurrentFragment.OnTabModelListener? = null
    private var planTabListener: ContractPlanTabFragment.OnTabModelListener? = null

    private var currentTabPosition: Int = 0

    private var fundRateTime: Long? = null
    private val mFundRateTimerHandler = Handler()
    private val fundRateTimer = object : Runnable {
        override fun run() {
            fundRateTime = fundRateTime?.minus(1)
            if (fundRateTime!! <= 0) {
                viewModel?.getFundRate(viewModel?.getCurrentPair())
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
        exchanged = ExchangeRatesUtil.getExchangeRatesSetting(mContext as HomePageActivity)!!.rateCode
        if (exchanged == 0) {
            rates = C2CApiServiceHelper.coinUsdtPrice?.usdt ?: 0.0
        }
        if (exchanged == 1) {
            rates = C2CApiServiceHelper.coinUsdtPrice?.usdtToUsd ?: 0.0
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
        binding!!.actionBarLayout.actionBarTitle
        binding!!.refreshLayout.setRefreshHolder(RefreshHolderFrying(activity!!))
        binding!!.refreshLayout.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {

                binding!!.refreshLayout.postDelayed(
                    { binding!!.refreshLayout.setRefreshing(false) },
                    300
                )
            }

        })
        kLineQuotaSelector = PositionSideSelector(mContext!!)
        kLineQuotaSelector?.setOnKLineQuotaSelectorListener(object : PositionSideSelector.OnKLineQuotaSelectorListener {
            override fun onSelect(type: String?) {
                if (type != null) {
                    if (type == LIMIT){
                        header1View?.orderType?.text = getString(R.string.order_type_limit)
                    }
                    else if (type == MARKET){
                        header1View?.orderType?.text =  getString(R.string.order_type_market)
                    }
                    else{
                        header1View?.orderType?.text = "计划委托"
                    }
                    currentOrderType = type
                    refreshOrderType(type)
                }
            }

        })
        positionTypeSelector = PositionTypeSeletor(mContext!!)
        positionTypeSelector?.setOnKLineQuotaSelectorListener(object : PositionTypeSeletor.OnKLineQuotaSelectorListener {
            override fun onSelect(type: String?) {
                if (type != null) {
                    if (type == "CROSSED"){
                        header1View?.cang?.text = getString(R.string.contract_all_position)
                    }
                    else{
                        header1View?.cang?.text = getString(R.string.contract_fiexble_position)
                    }
                    positionType = type
                    viewModel?.changeType("LONG", type)
                    viewModel?.changeType("SHORT", type)
                }
                }
        })
        futureMSG = FutureMSG(mContext!!)
        futureMSG?.setOnKLineQuotaSelectorListener(object : FutureMSG.OnKLineQuotaSelectorListener {
            override fun onSelect(type: String?) {
                if (type != null) {
                    if (type == "0"){
                        BlackRouter.getInstance().build(RouterConstData.FUTURE_MSG_ACTIVITY).go(mContext)
                    }
                    else if(type == "1") {
                        BlackRouter.getInstance().build(RouterConstData.FUTURE_COUNT_ACTIVITY).go(mContext)
                    }
                    else if(type == "2") {
                        BlackRouter.getInstance().build(RouterConstData.USER_SETTING).go(mContext)
                    }
                    else if(type == "3") {
                        Currentdialog3()
                    }
                }
            }
        })
        deepViewBinding = ContractDeepViewBinding(
            mContext!!,
            viewModel!!,
            binding!!.fragmentHomePageContractHeader1
        )
        deepViewBinding?.setOnTransactionDeepListener(this)
        initActionBar()
        initHeader()
        initHeader1()
        initHeader2()
        initTimeStepTab()
        return layout
    }

    private fun initTimeStepTab() {
        val pubSteps = arrayOf(AnalyticChart2.TimeStep2.NONE,AnalyticChart2.TimeStep2.MIN_1,AnalyticChart2.TimeStep2.MIN_5,AnalyticChart2.TimeStep2.MIN_15,AnalyticChart2.TimeStep2.MIN_30, AnalyticChart2.TimeStep2.HOUR_1,AnalyticChart2.TimeStep2.HOUR_4, AnalyticChart2.TimeStep2.DAY_1,AnalyticChart2.TimeStep2.WEEK_1)
        val timeStepTab = headerView!!.kTimeStepTab
        timeStepTab.tabMode = TabLayout.MODE_SCROLLABLE
        for (i in pubSteps.indices) {
            val timeStep = pubSteps[i]
            val tab = timeStepTab.newTab().setText(timeStep.toString())
            tab.tag = timeStep
            timeStepTab.addTab(tab)
        }
        timeStepTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val timeStep = tab.tag as AnalyticChart2.TimeStep2?
                if (timeStep != null) {
                    if (lastTimeStepIndex != tab.position) {
                        selectKTab(timeStep)
                    }
                } else {
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val timeStep = tab.tag as AnalyticChart2.TimeStep2?
                if (timeStep != null) {
                    lastTimeStepIndex = tab.position
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                val timeStep = tab.tag as AnalyticChart2.TimeStep2?
                if (timeStep != null) {
                }
            }
        })
        val tab = timeStepTab.getTabAt(1)
        tab?.select()
    }
    private fun selectKTab(timeStep: AnalyticChart2.TimeStep2?) {
        if (timeStep == null) {
            return
        }
        viewModel!!.finishListenKLine()
        if (headerView?.analyticChart?.getTimeStep() != timeStep) {
            headerView?.analyticChart?.setTimeStep(timeStep)
            listenKLineData()
            var endTime = System.currentTimeMillis() - (headerView?.analyticChart?.getTimeStep()?.value?.times(
                1000*100
            ) ?: 0) * kLinePage
            var startTime = endTime - (headerView?.analyticChart?.getTimeStep()?.value?.times(1000*100) ?: 0)
            startTime = startTime.coerceAtLeast(1567296000)
            endTime = endTime.coerceAtLeast(1567296000)
            viewModel!!.getKLineDataFiex(headerView?.analyticChart?.getTimeStepRequestStr(),kLinePage,startTime,endTime)
        }
    }
    private fun listenKLineData() {
        headerView?.analyticChart?.let { viewModel!!.listenKLineData(it!!) }
    }
    private fun refreshKLineChart(kLineItems: ArrayList<KLineItem?>) {
        try {
            headerView?.analyticChart?.setData(kLineItems)
            headerView?.analyticChart?.invalidate()
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }
    private fun openDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.futrues_dialog, null)
        val dialog = Dialog(mContext!!, com.black.c2c.R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(com.black.c2c.R.id.btn_confirm).setOnClickListener { v ->
           getFutrueOpen()
            dialog.dismiss()
        }
        dialog.findViewById<View>(com.black.c2c.R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun isLimit() :Boolean{
        return currentOrderType == LIMIT;
    }
    private fun isMarket() :Boolean{
        return currentOrderType == MARKET;
    }
    private fun isPlan() :Boolean{
        return currentOrderType == PLAN;
    }
    private fun buildByOrderType() {

    }

    private fun getFutrueOpen(){
        FutureApiServiceHelper.openAccount(
            context, false,
            object : Callback<HttpRequestResultBean<String?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(result: HttpRequestResultBean<String?>?) {
                    if (result?.returnCode == HttpRequestResult.SUCCESS) {
                        binding?.refreshLayout?.setRefreshing(true)
                       FryingUtil.showToast(context,getString(R.string.future_success))
                        oneDialog()
                    }
                }

            })
    }

    private fun oneDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.one_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            twoDialog()
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
    private fun twoDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.two_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
           threeDialog()
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
    private fun threeDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.three_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            fourDialog()
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
    private fun fourDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.four_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            fiveDialog()
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
    private fun fiveDialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.five_dialog, null)
        val dialog = Dialog(mContext!!,R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_confirm).setOnClickListener { v ->
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
            dialog.dismiss()
        }
    }
    private fun initAdjustLeverageData() {
        buyMultiChooseBean = ContractMultiChooseBean()
        buyMultiChooseBean?.maxMultiple = 100
        buyMultiChooseBean?.defaultMultiple = 10
        buyMultiChooseBean?.orientation = "BUY"
        buyMultiChooseBean?.type = 0
        buyMultiChooseBean?.symbol = viewModel?.getCurrentPair()
        binding?.fragmentHomePageContractHeader?.buyMultiple?.text =
            getMultipleDes(buyMultiChooseBean)
        sellMultiChooseBean = ContractMultiChooseBean()
        sellMultiChooseBean?.maxMultiple = 100
        sellMultiChooseBean?.defaultMultiple = 10
        sellMultiChooseBean?.orientation = "SELL"
        sellMultiChooseBean?.type = 1
        sellMultiChooseBean?.symbol = viewModel?.getCurrentPair()
        binding?.fragmentHomePageContractHeader?.sellMultiple?.text =
            getMultipleDes(sellMultiChooseBean)
    }

    override fun getViewModel(): ContractViewModel? {
        return viewModel
    }

    override fun onResume() {
        super.onResume()
        selectKTab(headerView?.analyticChart?.getTimeStep())
        viewModel?.setTabType(tabType)
        //viewModel?.getCurrentUserBalance(ConstData.BalanceType.SPOT)
        viewModel?.getCurrentPairDepth(50)
        viewModel?.getLeverageBracketDetail()
        viewModel?.getAggTicker()
        viewModel?.getCurrentDeal()
        viewModel?.onResume()
        viewModel?.initUserStepRate()
        initAdjustLeverageData()
        updateDear(isDear)
        contractSize = FutureService.getContractSize(viewModel?.getCurrentPair())
        header1View?.currentPriceLayout?.setOnClickListener(this)
        if (header1View?.tagPrice?.text.toString().isNotEmpty()) {
//            FutureService.getAvailableCloseData("10000", header1View?.tagPrice?.text.toString())
        }
        if (!LoginUtil.isFutureLogin(mContext)) {
            binding!!.fragmentHomePageContractHeader1.notLoginLayout.visibility = View.VISIBLE
            binding!!.fragmentHomePageContractHeader1.loginStatus.visibility = View.GONE
            binding!!.fragmentHomePageContractHeader1.notLoginBtn.setOnClickListener(this)


        } else {
            binding!!.fragmentHomePageContractHeader1.loginStatus.visibility = View.VISIBLE
            binding!!.fragmentHomePageContractHeader1.notLoginLayout.visibility = View.GONE
            getTradeOrderCurrent()
            Log.d("ttt--->", "Login")
        }
        priceInputFlag = false
        FutureApiServiceHelper.getAccountInfo(
            mContext as HomePageActivity,
            object : Callback<HttpRequestResultBean<AccountInfoBean?>?>() {
                override fun error(type: Int, error: Any?) {
                }

                override fun callback(result: HttpRequestResultBean<AccountInfoBean?>?) {
                    if (result != null && result.returnCode == HttpRequestResult.SUCCESS) {
                    }
                    else {
                        openDialog()
                    }
                }

            })
        var endTime = System.currentTimeMillis() - (headerView?.analyticChart?.getTimeStep()?.value?.times(1000*
                100
        ) ?: 0) * (kLinePage)
        var startTime = endTime - (headerView?.analyticChart?.getTimeStep()?.value?.times(1000*100) ?: 0)
        startTime = startTime.coerceAtLeast(1567296000)
        endTime = endTime.coerceAtLeast(1567296000)
        viewModel!!.getKLineDataFiex(headerView?.analyticChart?.getTimeStepRequestStr(),kLinePage,startTime,endTime)
//        FutureService.initMarkPrice(mContext)
//        FutureService.getPositionAdl(mContext)
//        FutureService.getBalanceByCoin(mContext)
//          FutureService.getCurrentPosition(mContext)
//        FutureService.getUserStepRate(mContext)
//        FutureService.getOrderPosition(mContext)
//        FutureService.getAvailableOpenData(
//            BigDecimal("16705"),
//            10,
//            BigDecimal.ZERO,
//            BigDecimal.ZERO
//        )
//        FutureService.createOrder(mContext,"BUY","LIMIT","btc_usdt","LONG","16880".toDouble(),"GTC",100)
//        FutureService.initFutureData(context)
    }

    override fun onStop() {
        super.onStop()
        priceInputFlag = true
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
                                    R.drawable.shoucang1
                                )
                            )
                        } else {
                            binding?.actionBarLayout?.imgCollect?.setImageDrawable(
                                mContext?.getDrawable(
                                    R.drawable.shoucang
                                )
                            )
                        }
                    }
                }))
        } else {
            isDear = dear
            if (isDear!!) {
                binding?.actionBarLayout?.imgCollect?.setImageDrawable(mContext?.getDrawable(R.drawable.shoucang1))
            } else {
                binding?.actionBarLayout?.imgCollect?.setImageDrawable(mContext?.getDrawable(R.drawable.shoucang))
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
        binding?.actionBarLayout?.exchange?.setOnClickListener(this)
        binding?.actionBarLayout?.riskInfo?.setOnClickListener(this)
        binding?.actionBarLayout?.leverHandle?.setOnClickListener(this)
        binding?.actionBarLayout?.imgCollect?.setOnClickListener(this)
        binding?.fragmentHomePageContractHeader2?.bills?.setOnClickListener(this)
    }


    private fun initHeader() {
        headerView = binding?.fragmentHomePageContractHeader
        headerView?.linBuyMultiple?.setOnClickListener(this)
        headerView?.linSellMultiple?.setOnClickListener(this)
        headerView?.shangla?.setOnClickListener(this)
        headerView?.xiala?.setOnClickListener(this)
        headerView?.total?.setOnClickListener(this)
        headerView?.rate?.setOnClickListener(this)

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
        header1View?.trassfer?.setOnClickListener(this)
        header1View?.cangwei?.setOnClickListener(this)
        header1View?.beishu?.setOnClickListener(this)
        header1View?.price?.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                computeTotal()
                computePriceCNY()
                    updateCanOpenAmount(s.toString())
            }

            override fun afterTextChanged(s: Editable) {
                header1View?.price?.setSelection(s.toString().length)
                if (s.toString().isNotEmpty()) {
                    priceInputFlag = true
                }
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
                val count = CommonUtil.parseDouble(
                    header1View?.transactionQuota?.text.toString()
                        .trim { it <= ' ' })
                if (count != null) {
                    val max: BigDecimal? = getMaxAmount()
                    if (max != null) {
                        if (max.compareTo(BigDecimal(0.00)) == 0) {
                            return
                        }
                        val countB = count.let { BigDecimal(it) }
                        val progress = (countB.divide(max, 2, BigDecimal.ROUND_HALF_DOWN))?.times(
                            BigDecimal(100)
                        )
                       header1View?.countBar?.progress =
                            progress?.toInt()!!
                        header1View?.useableBuy?.setText(count.toString())
                        header1View?.sellOutUnit?.setText(count.toString())
                    }
                }
            }

            override fun afterTextChanged(s: Editable) {
                inputNumber = false
                header1View?.transactionQuota?.setSelection(s.toString().length)
                updateBondAmount(header1View?.tagPrice?.text.toString(), s.toString())
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
        header1View?.useable?.setText("0.0000")
        header1View?.useableBuy?.setText("0.0000")
        header1View?.countBar?.setOnSeekBarChangeListener(this)
        header1View?.btnHandle?.setOnClickListener(this)
        header1View?.btnHandle1?.setOnClickListener(this)
        viewModel?.setCurrentPairOrderType(currentOrderType)
        refreshOrderType(currentOrderType)
        deepViewBinding!!.init()
    }

    private fun initHeader2() {
        recordTab = binding!!.fragmentHomePageContractHeader2.contractTab
        recordViewPager = binding!!.fragmentHomePageContractHeader2.contractRecordViewPager
//        binding!!.fragmentHomePageContractHeader2.totalCurrent.setOnClickListener(this)
        initRecordTab()
    }

    private fun refreshView() {
        clearInput()
        refreshUsable()
        refreshTransactionHardViews()
    }

    //初始化记录相关的tab
    private fun initRecordTab() {
        viewModel?.getPositionData()

        recordTab?.setTabTextColors(
            SkinCompatResources.getColor(activity, R.color.C5),
            SkinCompatResources.getColor(activity, R.color.C1)
        )
        recordTab?.tabMode = TabLayout.MODE_SCROLLABLE
        if (tabData == null) {
            tabData = ArrayList()
        }
        val tab1 = ContractRecordTabBean()
        tab1.amount = 0
        tab1.name = getString(R.string.contract_record_tab1, tab1.amount.toString())
        tab1.type = ConstData.CONTRACT_REC_HOLD_AMOUNT
        val tab3 = ContractRecordTabBean()
        tab3.amount = 0
        tab3.name = getString(R.string.contract_record_tab3, tab3.amount.toString())
        tab3.type = ConstData.CONTRACT_REC_CURRENT
        val tab2 = ContractRecordTabBean()
        tab2.amount = 0
        tab2.name = getString(R.string.contract_record_tab2, tab2.amount.toString())
        tab2.type = ConstData.CONTRACT_REC_WITH_LIMIE
        tabData?.add(tab1)
        tabData?.add(tab3)
        tabData?.add(tab2)
        positionTabListener = object : ContractPositionTabFragment.OnTabModelListener {
            override fun onCount(count: Int?) {
                updateTabTitles(ConstData.CONTRACT_REC_HOLD_AMOUNT, count)
            }
        }
        profitTabListener = object : ContractCurrentFragment.OnTabModelListener {
            override fun onCount(count: Int?) {
                updateTabTitles(ConstData.CONTRACT_REC_WITH_LIMIE, count)
            }
        }
        planTabListener = object : ContractPlanTabFragment.OnTabModelListener {
            override fun onCount(count: Int?) {
                updateTabTitles(ConstData.CONTRACT_REC_CURRENT, count)
            }
        }
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
                        ConstData.CONTRACT_REC_HOLD_AMOUNT -> {
                            fragment = ContractPositionTabFragment.newInstance(tabData)
                        }
                        ConstData.CONTRACT_REC_CURRENT -> fragment =
                            ContractPlanTabFragment.newInstance(tabData)
                        ConstData.CONTRACT_REC_WITH_LIMIE -> fragment =
                            ContractCurrentFragment.newInstance(tabData)

                    }
                    if (fragment is ContractPositionTabFragment) {
                        fragment.setAutoHeightViewPager(recordViewPager)
                        fragment.setOnTabModeListener(positionTabListener as ContractPositionTabFragment.OnTabModelListener)
                    }
                    if (fragment is ContractCurrentFragment) {
                        fragment.setAutoHeightViewPager(recordViewPager)
                        fragment.setOnTabModeListener(profitTabListener as ContractCurrentFragment.OnTabModelListener)
                    }
                    if (fragment is ContractPlanTabFragment) {
                        fragment.setAutoHeightViewPager(recordViewPager)
                        fragment.setOnTabModeListener(planTabListener as ContractPlanTabFragment.OnTabModelListener)
                    }
//                    if (fragment is EmptyFragment) {
//                        fragment?.setAutoHeightViewPager(recordViewPager)
//                    }
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
                    if (recordFragmentList!![position] is ContractPositionTabFragment) {
                        return recordFragmentList!![position] as ContractPositionTabFragment
                    } else if (recordFragmentList!![position] is ContractCurrentFragment) {
                        return recordFragmentList!![position] as ContractCurrentFragment
                    } else {
                        return recordFragmentList!![position] as ContractPlanTabFragment
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
//            recordTab?.getTabAt(0)?.text ="test"
        }
    }

    private fun updateTabTitles(tabType: Int?, amount: Int?) {
        when (tabType) {
            ConstData.CONTRACT_REC_HOLD_AMOUNT -> {
                recordTab?.getTabAt(0)?.text =
                    getString(R.string.contract_record_tab1, amount.toString())
            }
            ConstData.CONTRACT_REC_WITH_LIMIE -> {
                recordTab?.getTabAt(1)?.text =
                    getString(R.string.contract_record_tab2, amount.toString())
            }
            ConstData.CONTRACT_REC_CURRENT -> {
                recordTab?.getTabAt(2)?.text =
                    getString(R.string.contract_record_tab3, amount.toString())
            }
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
        des = "$typeDes $multiDes $orientationDes"
        return des
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showOrientationMultipleDialog(bean: ContractMultiChooseBean? , beishu: String?) {
        ContractMultipleSelectWindow(mContext as Activity,
            getString(R.string.contract_adjust),
            bean,
            beishu,
            object : ContractMultipleSelectWindow.OnReturnListener {
                override fun onReturn(
                    item: ContractMultiChooseBean?
                ) {
                        buyMultiChooseBean = item
                        binding?.fragmentHomePageContractHeader1?.cangBeishu?.text =
                            buyMultiChooseBean?.defaultMultiple.toString()
                }
            }).show()
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onClick(v: View) {
        if (CookieUtil.getUserInfo(context!!) == null) {
            //未登录，请求登陆
            fryingHelper.checkUserAndDoing(Runnable { }, TRADE_INDEX)
        } else {
        when (v.id) {
            R.id.cangwei -> {
                positionType =  if (binding?.fragmentHomePageContractHeader1?.cang?.text.toString() == getString(R.string.contract_all_position)) "CROSSED" else "ISOLATED"
                positionTypeSelector!!.show(header1View?.cang, positionType!!)
                /*
                DeepControllerWindow(mContext as Activity,
                    getString(R.string.cangwei_type),
                    positionType,
                    list,
                    object : DeepControllerWindow.OnReturnListener<String?> {
                        override fun onReturn(
                            window: DeepControllerWindow<String?>,
                            item: String?
                        ) {
                            if (positionType == item) {
                                return
                            } else {
                                positionType = item
                                binding?.fragmentHomePageContractHeader1?.cang?.text = item
                                if (item == getString(R.string.contract_fiexble_position)) {
                                    viewModel?.changeType("LONG", "ISOLATED")
                                    viewModel?.changeType("SHORT", "ISOLATED")
                                } else {
                                    viewModel?.changeType("LONG", "CROSSED")
                                    viewModel?.changeType("SHORT", "CROSSED")
                                }
                            }
                        }
                    }).show()

                 */
            }
            R.id.shangla -> {
                headerView?.shangla?.visibility = View.GONE
                headerView?.xiala?.visibility = View.VISIBLE
            }
            R.id.xiala -> {
                headerView?.shangla?.visibility = View.VISIBLE
                headerView?.xiala?.visibility = View.GONE
            }
            R.id.trassfer -> {
                BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(mContext)
            }
            R.id.exchange -> {
                BlackRouter.getInstance().build(RouterConstData.ASSET_TRANSFER).go(mContext)
            }
            R.id.current_price_layout -> {
                Currentdialog()
            }
            R.id.total -> {
                Currentdialog1()
            }
            R.id.rate -> {
                Currentdialog2()
            }
            R.id.not_login_btn -> {
                BlackRouter.getInstance().build(RouterConstData.LOGIN).go(mContext)
            }
            R.id.beishu -> {
                if (CookieUtil.getUserInfo(context!!) == null) {
                    //未登录，请求登陆
                    fryingHelper.checkUserAndDoing(Runnable { }, TRADE_INDEX)
                } else {
                    showOrientationMultipleDialog(buyMultiChooseBean ,binding?.fragmentHomePageContractHeader1?.cangBeishu?.text.toString())
                }
            }
            /*R.id.lin_buy_multiple -> {
                if (CookieUtil.getUserInfo(context!!) == null) {
                    //未登录，请求登陆
                    fryingHelper.checkUserAndDoing(Runnable { }, TRADE_INDEX)
                } else {
                    showOrientationMultipleDialog(buyMultiChooseBean)
                }
            }
            R.id.lin_sell_multiple -> {
                if (CookieUtil.getUserInfo(context!!) == null) {
                    //未登录，请求登陆
                    fryingHelper.checkUserAndDoing(Runnable { }, TRADE_INDEX)
                } else {
                    showOrientationMultipleDialog(sellMultiChooseBean)
                }
            }

             */
            R.id.tab_transaction_coin -> {
                if (tabType != ConstData.TAB_COIN) {
                    //changeTabType(ConstData.TAB_COIN)
                }
            }
            /*R.id.risk_info -> {
                if (mContext != null && tabType == ConstData.TAB_LEVER && TextUtils.equals(
                        viewModel?.getCurrentPair(),
                        leverDetail?.pair
                    ) && leverDetail?.burstRate != null && leverDetail?.burstRate != BigDecimal.ZERO
                ) {
                    leverDetail?.run {
                        AlertMessageDialog(
                            mContext!!,
                            getString(R.string.bao),
                            String.format(
                               getString(R.string.feng_xian),
                                NumberUtil.formatNumberNoGroupHardScale(
                                    leverDetail?.burstRate!! * BigDecimal(100), 2
                                )
                            )
                        )
                            .show()
                    }
                }
            }
             */
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
                currentOrderType =  if (binding?.fragmentHomePageContractHeader1?.orderType?.text.toString() == getString(R.string.order_type_limit)) LIMIT else if (binding?.fragmentHomePageContractHeader1?.orderType?.text.toString() == getString(R.string.order_type_market)) MARKET else PLAN
                kLineQuotaSelector!!.show(header1View?.linOrderType, currentOrderType!!)
                /*DeepControllerWindow(mContext as Activity,
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
                            viewModel?.setCurrentPairOrderType(item)
                            if (currentOrderType.equals(LIMIT)) {
                                fillLimitPrice()
//                                    binding?.fragmentHomePageContractHeader1?.relVolume?.visibility = View.VISIBLE
                            } else if (currentOrderType.equals("MARKET")) {
                            }
                            else {
                                fillLimitPrice()
                            }
                        }
                    }).show()

                 */
            }
            R.id.lin_unit_type -> {
                currentUnitType = binding?.fragmentHomePageContractHeader1?.unitType?.text.toString()
                DeepControllerWindow(mContext as Activity,
                    "请选择换算单位",
                    currentUnitType,
                    viewModel?.getCurrentUnitTypeList() as List<String?>?,
                    object : DeepControllerWindow.OnReturnListener<String?> {
                        override fun onReturn(
                            window: DeepControllerWindow<String?>,
                            item: String?
                        ) {
                            if(item == currentUnitType){
                                return
                            }
                            else {
                                refreshUnitType(item)
                                currentUnitType = item
                                viewModel?.setCurrentUnitType(item)
                                refreshTransactionHardViews()
                                header1View?.transactionQuota?.setText("0.0")
                            }
                        }
                    }).show()
            }
            R.id.lin_limit_type -> {
                currentTimeInForceType = binding?.fragmentHomePageContractHeader1?.withLimitType?.text.toString()
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
                bundle.putInt(ConstData.NAME,1)
                BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).with(bundle)
                    .go(mContext)
            }
            R.id.head_transaction_more ->
            futureMSG!!.show(binding!!.actionBarLayout.headTransactionMore, null)
                /*viewModel!!.checkDearPair()
                ?.subscribe(HttpCallbackSimple(mContext, true, object : Callback<Boolean>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(returnData: Boolean) {
                    }
                }))

                 */
            R.id.btn_transaction_memu -> mContext?.let {
                val setData = ArrayList<QuotationSet?>(3)
                val optionalUbaseSet = QuotationSet()
                optionalUbaseSet.coinType = getString(R.string.usdt)
                optionalUbaseSet.name = getString(R.string.usdt_base)
                setData.add(optionalUbaseSet)
                val optionalCoinBaseSet = QuotationSet()
                optionalCoinBaseSet.coinType = getString(R.string.usd)
                optionalCoinBaseSet.name = getString(R.string.coin_base)
                setData.add(optionalCoinBaseSet)
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
            //卖出/开空
            R.id.btn_handle_1 -> {
                var positionSide: String = "SHORT"
                var orderSide: String = "SELL"
                when (transactionType) {
                    ConstData.FUTURE_OPERATE_OPEN -> {
                        orderSide = "SELL"
                        positionSide = "SHORT"
                    }
                   /* ConstData.FUTURE_OPERATE_CLOSE -> {
                        positionSide = "LONG"
                        orderSide = "SELL"
                    }

                    */
                }
                createOrderFuture(positionSide, orderSide)
            }
            //买入/开多
            R.id.btn_handle -> mContext?.let {
                if (CookieUtil.getUserInfo(it) == null) {
                    //未登录，请求登陆
                    fryingHelper.checkUserAndDoing(Runnable { }, TRADE_INDEX)
                } else {
                    Log.d("4444", "4")
                    var positionSide: String = "LONG"
                    var orderSide: String = "BUY"
                    when (transactionType) {
                        ConstData.FUTURE_OPERATE_OPEN -> {
                            positionSide = "LONG"
                            orderSide = "BUY"
                        }
                        /*ConstData.FUTURE_OPERATE_CLOSE -> {
                            positionSide = "SHORT"
                            orderSide = "SELL"
                        }

                         */

                    }
                    createOrderFuture(positionSide, orderSide)
                }
            }

            R.id.bills -> {
                BlackRouter.getInstance().build(RouterConstData.CONTRACT_BILL_ACTIVITY)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .go(mContext)
            }
        }
        }
    }

    private fun getMarketPrice(): BigDecimal {
        val price = header1View?.currentPrice?.text?.toString() ?: ""
        return NumberUtil.toBigDecimal(price)
    }
    private fun initLimitPrice(){
        val price =  binding?.fragmentHomePageContractHeader1?.price?.text?:"0.0"
        if (price.isEmpty()){
            fillLimitPrice()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun fillLimitPrice() {
        val price = header1View?.currentPrice?.text?.toString() ?: "0.0"
        val decimal = NumberUtil.toBigDecimal(price)?: BigDecimal.ZERO
        if (decimal == BigDecimal.ZERO) {
            binding?.fragmentHomePageContractHeader1?.price?.setText("0.0")
        } else {
            binding?.fragmentHomePageContractHeader1?.price?.setText(decimal.toString())
        }
    }

    private fun Currentdialog(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.current_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
    private fun Currentdialog1(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.zong_quan_yi_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
    private fun Currentdialog2(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.zi_jin_rate_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->

            dialog.dismiss()
        }
    }
    @SuppressLint("SetTextI18n")
    private fun Currentdialog3(){
        val contentView = LayoutInflater.from(mContext).inflate(R.layout.future_rate_dialog, null)
        val dialog = Dialog(mContext!!, R.style.AlertDialog)
        val window = dialog.window
        if (window != null) {
            val params = window.attributes
            //设置背景昏暗度
            params.dimAmount = 0.2f
            params.gravity = Gravity.CENTER
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            window.attributes = params
        }
        //设置dialog的宽高为屏幕的宽高
        val display = resources.displayMetrics
        val layoutParams =
            ViewGroup.LayoutParams(display.widthPixels, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.setContentView(contentView, layoutParams)
        dialog.show()
        dialog.findViewById<TextView>(R.id.one).text = "+" + String.format("%.3f",BigDecimal(100) * BigDecimal(userStepRate?.makerFee)) + "%"
        dialog.findViewById<TextView>(R.id.two).text = "+" +  String.format("%.3f",BigDecimal(100) * BigDecimal(userStepRate?.takerFee)) + "%"
        dialog.findViewById<TextView>(R.id.three).text = userStepRate?.discountLevel.toString()
        dialog.findViewById<View>(R.id.btn_cancel).setOnClickListener { v ->
            dialog.dismiss()
        }
        dialog.findViewById<View>(R.id.four).setOnClickListener { v ->

        }
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
//            val usable = currentBalanceSell?.availableBalance
            val usable = viewModel?.balanceDetailBean?.availableBalance
            val price = binding!!.fragmentHomePageContractHeader1.sellUseable.text.toString()
            if (usable == null || price.isEmpty()){
                return null
            }
           else {
                val newUsable: BigDecimal = if (usable.toDouble() == 0.0) BigDecimal.ZERO else NumberUtils.toBigDecimal(usable)
                val newPrice: BigDecimal = if (price.toDouble() == 0.0) BigDecimal.ZERO else NumberUtils.toBigDecimal(price)
                return newPrice
            }

        } else if (transactionType == ConstData.FUTURE_OPERATE_CLOSE) {
            return currentBalanceBuy?.availableBalance?.toBigDecimal()
        }
        return null
    }


    private fun resetAmountLength() {
        binding!!.fragmentHomePageContractHeader1.currentPrice.setText("")
        binding!!.fragmentHomePageContractHeader1.currentPriceCny.setText("")
        binding!!.fragmentHomePageContractHeader1.tagPrice.setText("")
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
        //价格输入清空
        binding?.fragmentHomePageContractHeader1?.price?.setText("")
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
        /*
        val price = CommonUtil.parseDouble(
            header1View?.price?.text.toString().trim { it <= ' ' })
        val count = CommonUtil.parseDouble(
            header1View?.transactionQuota?.text.toString()
                .trim { it <= ' ' })
        if (price != null) {
            if (count != null && (count != 0.0)) {
                if (currentOrderType.equals(LIMIT)) {
//                    binding!!.fragmentHomePageContractHeader1.tradeValue.setText(NumberUtil.formatNumberNoGroup(price * count, RoundingMode.FLOOR, viewModel!!.getAmountLength(), viewModel!!.getAmountLength())+viewModel!!.getSetName())
                }
            } else { //只有价格
                if (transactionType == ConstData.FUTURE_OPERATE_OPEN) {
                    if (price > 0 && currentBalanceSell != null) {
                        //总的钱数除以输入价格
                        header1View?.useableBuy?.setText(
                            NumberUtil.formatNumberNoGroup(
                                currentBalanceSell?.availableBalance!!.toDouble()
                                    .div(price.toDouble()),
                                RoundingMode.FLOOR,
                                viewModel!!.getAmountLength(),
                                viewModel!!.getAmountLength()
                            )
                        )
                    } else {
                        header1View?.useableBuy?.setText("0.0000")
                    }
                } else if (transactionType == ConstData.FUTURE_OPERATE_CLOSE) {
                    if (price > 0 && currentBalanceBuy != null) {
                        //总的钱数乘以输入价格
                        header1View?.useableBuy?.setText(
                            NumberUtil.formatNumberNoGroup(
                                currentBalanceBuy?.availableBalance!!.toDouble() * price.toDouble(),
                                RoundingMode.FLOOR,
                                viewModel!!.getAmountLength(),
                                viewModel!!.getAmountLength()
                            )
                        )
                    } else {
                        header1View?.useableBuy?.setText("0.0000")
                    }
                }
                if (currentOrderType.equals(LIMIT)) {
                    if (price != null && count != null) {
//                        binding!!.fragmentHomePageContractHeader1.tradeValue.setText(NumberUtil.formatNumberNoGroup(price * count!!, RoundingMode.FLOOR, viewModel!!.getAmountLength(), viewModel!!.getAmountLength())+viewModel!!.getSetName())
                    }
                }
            }
        } else {
            if (transactionType == ConstData.FUTURE_OPERATE_OPEN) {
                header1View?.useableBuy?.setText("0.0000")
            } else if (transactionType == ConstData.FUTURE_OPERATE_CLOSE) {
                header1View?.useableBuy?.setText("0.0000")
            }
        }

         */
    }

    /**
     * 计算可开多/空的数量
     */
    private fun updateCanOpenAmount(price: String?) {
        currentUnitType = binding?.fragmentHomePageContractHeader1?.unitType?.text.toString()
        binding?.fragmentHomePageContractHeader1?.useableBuyType?.setText(currentUnitType)
        binding?.fragmentHomePageContractHeader1?.useableSaleType?.setText(currentUnitType)
        if (price?.isEmpty() == true || !LoginUtil.isFutureLogin(context) || price == null || BigDecimal(price) == BigDecimal.ZERO) {
            return
        }
        val longLeverage = leverage?: 10
        val shortLeverage = leverage?: 10
        val availableOpenData = FutureService.getAvailableOpenData(
            BigDecimal(price), longLeverage, shortLeverage, BigDecimal.ZERO,
            BigDecimal.ZERO
        )
        when (transactionType) {
            ConstData.FUTURE_OPERATE_OPEN -> {
                //可开多数量
                if (currentUnitType == "USDT") {
                    header1View?.useable?.text =
                        String.format("%.4f", availableOpenData.longMaxOpen.toFloat())
                    header1View?.sellUseable?.text =
                        String.format("%.4f", availableOpenData.shortMaxOpen.toFloat())
                }
                else{
                    header1View?.useable?.text =
                        String.format("%.4f", availableOpenData.longMaxOpen.div(BigDecimal(price)).toFloat())
                    header1View?.sellUseable?.text =
                        String.format("%.4f", availableOpenData.shortMaxOpen.div(BigDecimal(price)).toFloat())
                }
            }
            ConstData.FUTURE_OPERATE_CLOSE -> {
                if (price.isNotEmpty() && header1View?.tagPrice?.text.toString()
                        .isNotEmpty()
                ) {
                    val closeData: CloseData = FutureService.getAvailableCloseData(
                        price,
                        header1View?.tagPrice?.text.toString()
                    )
                    if (closeData != null) {
                        //可开多数量
                        header1View?.useable?.text =
                            String.format("%.2f", closeData.long.toFloat())
                        //可开空数量
                        header1View?.sellUseable?.text =
                            String.format("%.2f", closeData.short.toFloat())
                        //多仓持仓
                        header1View?.freezAmount?.text =
                            String.format("%.2f", closeData.longPosition.toFloat())
                        //空仓持仓
                        header1View?.sellBond?.text =
                            String.format("%.2f", closeData.shortPosition.toFloat())
                    }
                }
            }
        }

    }

    /**
     * 计算需要的保证金
     */
    private fun updateBondAmount(inputPrice: String?, amount: String?) {
        currentUnitType = binding!!
            .fragmentHomePageContractHeader1.unitType.text.toString()
        if (inputPrice?.isEmpty() == true || amount?.isEmpty() == true || !LoginUtil.isFutureLogin(
                mContext
            )
        ) {
            return
        }
        val longLeverage = leverage
        val shortLeverage = leverage
        var availableOpenData: AvailableOpenData? = null
        if (currentUnitType == "USDT") {
            availableOpenData = FutureService.getAvailableOpenData(
                BigDecimal(inputPrice), longLeverage!!, shortLeverage!!, BigDecimal(amount),
                BigDecimal.ZERO
            )
        }
        else{
            availableOpenData = FutureService.getAvailableOpenData(
                BigDecimal(inputPrice), longLeverage!!, shortLeverage!!, BigDecimal(amount)*BigDecimal(inputPrice),
                BigDecimal.ZERO
            )
        }

        when (transactionType) {
            ConstData.FUTURE_OPERATE_OPEN -> {

                header1View?.freezAmount?.text =
                    String.format("%.4f", availableOpenData.longMargin.toFloat())

                header1View?.sellBond?.text =
                    String.format("%.4f", availableOpenData.shortMargin.toFloat())
            }
            ConstData.FUTURE_OPERATE_CLOSE -> {

            }
        }
    }

    //计算当前输入价格CNY
    private fun computePriceCNY() {
        val price = CommonUtil.parseDouble(
            header1View?.price?.text.toString().trim { it <= ' ' })
        if (price != null && price > 0) {
            header1View?.priceCny?.setText(
                "≈" + NumberUtil.formatNumberNoGroup(
                    price * (rates ?: 0.0),
                    4,
                    4
                )
                + if (exchanged  == 0) "CNY" else "USD"
            )
        } else {
            header1View?.priceCny?.setText(
                "≈" + NumberUtil.formatNumberNoGroup(
                    0.0f,
                    4,
                    4
                )
                + if (exchanged == 0) "CNY" else "USD"
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
    }

    //交易对切换，刷新控件，刷新交易对信息，刷新深度，刷新数据,通知交易对改变
    fun onPairStatusChanged(pairStatus: PairStatus) {
        clearInput()
        deepViewBinding!!.clearASKTradeOrders()
        deepViewBinding!!.clearBIDTradeOrders()
        viewModel!!.getCurrentPairStatus(pairStatus.pair)
        viewModel!!.changePairSocket()
        resetPriceLength()
        resetAmountLength()
        refreshCurrentWallet()
        refreshTransactionHardViews()
        refreshUsable()
        refreshData()
        currentUnitType = "USDT"
        currentTimeInForceType = "GTC"
        viewModel?.setCurrentPairOrderType(currentOrderType)
        refreshOrderType(currentOrderType)
    }

    private fun refreshOrderType(type: String?) {
        var typeDes: String? = null
        when (type) {
            MARKET -> {
                typeDes = getString(R.string.order_type_market)
                binding!!.fragmentHomePageContractHeader1.linPrice.visibility = View.GONE
                binding!!.fragmentHomePageContractHeader1.linMarketPrice.visibility = View.VISIBLE
                binding!!.fragmentHomePageContractHeader1.weituoPrice.visibility = View.GONE
                //binding!!.fragmentHomePageContractHeader1.linPrinceCny.visibility = View.GONE
                //binding!!.fragmentHomePageContractHeader1.linLimitType.visibility = View.GONE
            }
            LIMIT -> {
                typeDes = getString(R.string.order_type_limit)
                binding!!.fragmentHomePageContractHeader1.linPrice.visibility = View.VISIBLE
                binding!!.fragmentHomePageContractHeader1.linMarketPrice.visibility = View.GONE
                binding!!.fragmentHomePageContractHeader1.weituoPrice.visibility = View.GONE
                //binding!!.fragmentHomePageContractHeader1.linPrinceCny.visibility = View.VISIBLE
                //binding!!.fragmentHomePageContractHeader1.linLimitType.visibility = View.VISIBLE
            }
            PLAN -> {
                typeDes = getString(R.string.jihua_price)
                binding!!.fragmentHomePageContractHeader1.linPrice.visibility = View.VISIBLE
                binding!!.fragmentHomePageContractHeader1.linMarketPrice.visibility = View.GONE
                binding!!.fragmentHomePageContractHeader1.weituoPrice.visibility = View.VISIBLE
            }
        }
        binding!!.fragmentHomePageContractHeader1.orderType.text = typeDes
    }

    private fun refreshUnitType(type: String?) {
        binding!!.fragmentHomePageContractHeader1.unitType.text = type
    }

    private fun refreshTimeInForceType(type: String?) {
        binding!!.fragmentHomePageContractHeader1.withLimitType.text = type
    }

    //刷新交易区控件
    private fun refreshTransactionHardViews() {
        if (transactionType == ConstData.FUTURE_OPERATE_OPEN) {
            header1View?.contractWithLimit?.visibility = View.VISIBLE
            if (withLimitFlag == true) {
                header1View?.linStopValue?.visibility = View.VISIBLE
            } else {
                header1View?.linStopValue?.visibility = View.GONE
            }
            header1View?.tvBuyCount?.text = getString(R.string.contract_can_buy_raise)
            header1View?.tvSellCount?.text = getString(R.string.contract_can_buy_fall)
            header1View?.actionBuyFreez?.text = getString(R.string.contract_bond_empty)
            header1View?.actionSellFreez?.text = getString(R.string.contract_bond_empty)
            header1View?.btnBuy?.isChecked = true
            header1View?.btnSale?.isChecked = false
            header1View?.countProgress?.progressDrawable =
                countProgressBuy
            header1View?.amountZero?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            header1View?.amountTwenty?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            header1View?.amountFourty?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            header1View?.amountSixty?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            header1View?.amountEighty?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            header1View?.amountAll?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
        } else if (transactionType == ConstData.FUTURE_OPERATE_CLOSE) {
            header1View?.contractWithLimit?.visibility = View.GONE
            header1View?.linStopValue?.visibility = View.GONE
            header1View?.tvBuyCount?.text = getString(R.string.contract_can_sell_fail)
            header1View?.tvSellCount?.text = getString(R.string.contract_can_sell_raise)
            header1View?.actionBuyFreez?.text = getString(R.string.hold_amount)
            header1View?.actionSellFreez?.text = getString(R.string.hold_amount)
            header1View?.btnBuy?.isChecked = false
            header1View?.btnSale?.isChecked = true
            header1View?.countProgress?.progressDrawable =
                countProgressSale
            header1View?.amountZero?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            header1View?.amountTwenty?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            header1View?.amountFourty?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            header1View?.amountSixty?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            header1View?.amountEighty?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
            header1View?.amountAll?.buttonDrawable =
                SkinCompatResources.getDrawable(mContext, R.drawable.icon_transaction_count_buy)
        }
        if (!TextUtils.isEmpty(viewModel!!.getCurrentPair())) {
            binding!!.actionBarLayout.actionBarTitle.setText(
                viewModel!!.getCoinType().toString().uppercase()
            )
            binding!!.actionBarLayout.pairSetName.setText(
                "/" + viewModel!!.getSetName().toString().uppercase()
            )
            header1View?.deepPriceP?.text =
                getString(R.string.brackets, viewModel!!.getSetName().toString().uppercase())
            header1View?.deepAmountName?.text =
                getString(R.string.brackets, currentUnitType)
            if (transactionType == ConstData.FUTURE_OPERATE_OPEN) {
                header1View?.btnHandle?.setText(getString(R.string.contract_buy_raise))
                header1View?.btnHandle1?.setText(getString(R.string.contract_sell_fall))
            } else if (transactionType == ConstData.FUTURE_OPERATE_CLOSE) {
                header1View?.btnHandle?.setText(getString(R.string.contract_buy_fall))
                header1View?.btnHandle1?.setText(getString(R.string.contract_sell_raise))
            }
        }
        FutureService.updateCurrentSymbol(viewModel!!.getCurrentPair().toString())
        if (mContext == null || CookieUtil.getUserInfo(mContext!!) == null) {
            header1View?.btnHandle?.setText(R.string.login)
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

    private fun refreshShuju() {
        viewModel!!.initBalanceByCoin(mContext)
        viewModel!!.getPositionData()
        viewModel!!.getProfitData(Constants.UNFINISHED)
        viewModel!!.getPlanData(Constants.UNFINISHED)
        viewModel!!.getLimitPricePlanData()

    }
    private fun refreshData() {
        viewModel!!.getAllOrder()
        viewModel?.getCurrentPairDepth(50)
        viewModel?.getIndexPrice(viewModel?.getCurrentPair())
        viewModel?.getMarketPrice(viewModel?.getCurrentPair())
        viewModel?.getSymbolTicker(viewModel?.getCurrentPair())
        viewModel?.getFundRate(viewModel?.getCurrentPair())
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

    /**
     * 下单
     * orderSide 买卖方向：BUY;SELL
     * orderType 订单类型：LIMIT；MARKET
     * origQty 数量（张）
     * timeInForce：GTC;IOC;FOK;GTX
     * positionSide：LONG;SHORT
     * 数量 = 输入数量/(输入价格*合约面值)
     */
    private fun createOrderFuture(positionSide: String, orderSide: String) {
        var orderType: String? =if (binding?.fragmentHomePageContractHeader1?.orderType?.text.toString() == getString(R.string.order_type_limit)) LIMIT else if (binding?.fragmentHomePageContractHeader1?.orderType?.text.toString() == getString(R.string.order_type_market)) MARKET else PLAN
        val price: String = header1View?.price?.text.toString().trim()
        var priceNum = NumberUtil.toBigDecimal(price)?: BigDecimal.ZERO
        if (priceNum == BigDecimal.ZERO) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_price))
            return
        }
        val timeInForce: String = currentTimeInForceType?:"GTC"
        val num = NumberUtil.toBigDecimal(viewModel?.getContractSize()?:"0.0001")?: BigDecimal.ZERO
        if (num == BigDecimal.ZERO) {
            FryingUtil.showToast(mContext, "合约面值未获取，无法下单")
            return
        }
        val origQty = header1View?.transactionQuota?.text.toString().trim()
        val origQtyNum = NumberUtil.toBigDecimal(origQty)
        var totalAmountInt: BigDecimal = BigDecimal.ZERO
        if (origQtyNum == BigDecimal.ZERO) {
            FryingUtil.showToast(mContext, getString(R.string.alert_input_count))
            return
        }
        currentUnitType = binding?.fragmentHomePageContractHeader1?.unitType?.text.toString()
        if (currentUnitType == "USDT") {
            if (origQtyNum <= num * (priceNum)) {
                FryingUtil.showToast(
                    mContext,
                    "单笔下单应大于" + String.format("%.4f", num * (priceNum)) + currentUnitType
                )
                //FryingUtil.showToast(mContext, "单笔下单应大于0.0001" + "BTC")
                return
            }
            if (origQtyNum > NumberUtils.toBigDecimal("100000")) {
                FryingUtil.showToast(mContext, "单笔交易不能大于100000")
                return
            }
            totalAmountInt = (origQtyNum) / (num * (priceNum))
        }
        else{
            if (origQtyNum <= num) {
                FryingUtil.showToast(
                    mContext,
                    "单笔下单应大于" + String.format("%.4f", num) + currentUnitType
                )
                //FryingUtil.showToast(mContext, "单笔下单应大于0.0001" + "BTC")
                return
            }
            if (origQtyNum * priceNum > NumberUtils.toBigDecimal("100000")) {
                FryingUtil.showToast(mContext,NumberUtils.toBigDecimal("100000").div(priceNum).toString())
                return
            }
            totalAmountInt = (origQtyNum) / (num)
        }
        if (orderType.equals("MARKET")) {
            priceNum = BigDecimal.ZERO
        }
        //止盈止损
        var tigerStop: Boolean? = header1View?.contractWithLimit?.isChecked
        val tigerProfit: String = header1View?.stopSurplus?.text.toString().trim()
        var tigerProfitValue = NumberUtil.toBigDecimal(tigerProfit)
//        if (tigerProfitValue == BigDecimal.ZERO) {
//            tigerProfitValue = tigerProfit.toFloat()
//        }

        val tigerLose: String = header1View?.stopLose?.text.toString().trim()
        val tigerLoseValue =  NumberUtil.toBigDecimal(tigerLose)
        var stopPrice:BigDecimal? = null
        var triggerPriceType: String? = null
        var entrustType: String? = null
        if (currentOrderType == "PLAN")
        {
            if (header1View?.price?.text == null){
                FryingUtil.showToast(mContext, getString(R.string.alert_input_price))
                return
            }
            entrustType = "STOP"
            triggerPriceType = "MARK_PRICE"
            val price2: String = header1View?.priceA?.text.toString().trim()
            stopPrice = NumberUtil.toBigDecimal(price2)
            val createRunnable = Runnable {
                FutureApiServiceHelper.createOrderPlan(context,
                    orderSide,
                    orderType!!,
                    viewModel?.getCurrentPair(),
                    positionSide,
                    priceNum.toDouble(),
                    timeInForce,
                    totalAmountInt.toInt(),
                    if (tigerStop == true) tigerProfitValue else null,
                    if (tigerStop == true) tigerLoseValue else null,
                    stopPrice?.toDouble(),
                    triggerPriceType,
                    entrustType,
                    object : Callback<HttpRequestResultBean<String>?>() {
                        override fun callback(returnData: HttpRequestResultBean<String>?) {
                            if (returnData != null) {
                                //Log.d("iiiiii-->createFutureOrder", returnData.result.toString())
                                header1View?.price?.setText(price)
                                header1View?.transactionQuota?.setText("0.0")
                                FryingUtil.showToast(context, getString(R.string.trade_success))
                                refreshShuju()
                                initHeader2()
                                updateOpenAvailableData()
                                /**
                                 * todo 刷新持仓列表
                                 */
                            }
                            else{
                                FryingUtil.showToast(context, returnData?.msg)
                            }
                        }

                        override fun error(type: Int, error: Any?) {
                            FryingUtil.showToast(context, error.toString())
                        }

                    })
            }
            createRunnable.run()
        }
        else {
            val createRunnable = Runnable {
                FutureApiServiceHelper.createOrder(context,
                    orderSide,
                    orderType!!,
                    viewModel?.getCurrentPair(),
                    positionSide,
                    if (currentOrderType == "LIMIT") priceNum.toDouble() else null,
                    if (currentOrderType == "LIMIT") timeInForce else "IOC",
                    totalAmountInt.toInt(),
                    if (tigerStop == true) tigerProfitValue else null,
                    if (tigerStop == true) tigerLoseValue else null,
                    false,
                    true,
                    /*stopPrice?.toDouble(),
                triggerPriceType,
                entrustType,*/
                    object : Callback<HttpRequestResultBean<String>?>() {
                        override fun callback(returnData: HttpRequestResultBean<String>?) {
                            if (returnData != null) {
                                //Log.d("iiiiii-->createFutureOrder", returnData.result.toString())
                                header1View?.price?.setText(price)
                                header1View?.transactionQuota?.setText("0.0")
                                FryingUtil.showToast(context, getString(R.string.trade_success))
                                refreshShuju()
                                initHeader2()
                                updateOpenAvailableData()
                                /**
                                 * todo 刷新持仓列表
                                 */
                            }
                        }

                        override fun error(type: Int, error: Any?) {
                            Log.d("iiiiii-->createFutureOrder--error", error.toString())
                            FryingUtil.showToast(context, error.toString())
                        }

                    })
            }
            createRunnable.run()
        }
    }

    fun withTimerGetCurrentTradeOrder() {
        var count = 0
        var timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                getTradeOrderCurrent()
                count++
                if (count >= 3) {
                    timer.cancel()
                }
            }
        }, Date(), 1000)
    }


    override fun onPairDeal(value: PairDeal) {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            updateCurrentPairPrice(value.p)
        }
    }

    override fun onPlanData(data: PlanUnionBean?) {
        updateTabTitles(ConstData.CONTRACT_REC_CURRENT, data?.limitPriceList?.size!!)
        updateTabTitles(ConstData.CONTRACT_REC_WITH_LIMIE,data.planList?.size!! )
    }

    override fun onProfitData(data: ArrayList<ProfitsBean?>?) {
        //updateTabTitles(ConstData.CONTRACT_REC_WITH_LIMIE, data?.size)
    }
//初始化杠杆倍数方向
    @SuppressLint("SetTextI18n")
    override fun onPositionData(data: ArrayList<PositionBean?>?) {
        updateTabTitles(ConstData.CONTRACT_REC_HOLD_AMOUNT, data?.size)
       if ( data?.size != null && data.size > 0){
           if (positionType == ConstData.TAB_CROSSED) {
               binding!!.fragmentHomePageContractHeader1.cang.text =
                   getString(R.string.contract_all_position)
           }
           else {
               binding!!.fragmentHomePageContractHeader1.cang.text =
                   getString(R.string.contract_fiexble_position)
           }
           binding!!.fragmentHomePageContractHeader1.cangBeishu.text = data[0]!!.leverage.toString()
           leverage = data[0]!!.leverage
           val price = header1View?.price?.text
           updateCanOpenAmount(price.toString())
       }
    else{
           binding!!.fragmentHomePageContractHeader1.cangBeishu.text = "10"
           leverage = 10
           val price = header1View?.price?.text
           updateCanOpenAmount(price.toString())
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

    //更新涨跌幅和当前价格
    override fun onPairQuotation(tickerBean: PairQuotation?) {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            updatePriceSince(tickerBean?.r)
            updateCurrentPairPrice(tickerBean?.c)
//            initInputPriceValue(tickerBean?.c)
            updateOpenAvailableData()
        }
    }

    /**
     * 当行情价格发生变化的时候，需要更新可开空的数量
     */
     fun updateOpenAvailableData() {
        val price = header1View?.price?.text
        updateCanOpenAmount(price.toString())
    }


    override fun onPairStatusInit(pairStatus: PairStatus?) {
        clearInput()
        header1View?.price?.filters = arrayOf(NumberFilter(),
            pairStatus?.precision?.let { PointLengthFilter(it) })
        resetPriceLength()
        resetAmountLength()
        refreshTransactionHardViews()
        refreshData()
        if (!TextUtils.isEmpty(pairStatus?.pair)) {
            binding!!.actionBarLayout.actionBarTitle.setText(
                viewModel!!.getCoinType().toString().uppercase()
            )
            binding!!.actionBarLayout.pairSetName.setText(
                "/" + viewModel!!.getSetName().toString().uppercase()
            )
        }
        resetAmountLength()
        resetPriceLength()
        if (pairStatus?.supportingPrecisionList != null) {
            onDeepChoose()
        }
        currentUnitType = "USDT"
        currentTimeInForceType = "GTC"
        viewModel?.setCurrentPairOrderType(currentOrderType)
        viewModel?.setCurrentUnitType(currentUnitType)
        viewModel?.setCurrentTimeInForceType(currentTimeInForceType)
    }


    override fun onUserTradeOrderChanged(userTradeOrder: TradeOrderFiex?) {
        Log.d(TAG, "onUserTradeOrderChanged,executedQty = " + userTradeOrder?.executedQty)
        initHeader2()

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

    /**
     * 指数价格更新
     */
    @SuppressLint("SetTextI18n")
    override fun onIndexPirce(indexPrice: IndexPriceBean?) {
        val price = BigDecimal(indexPrice?.p).setScale(2, RoundingMode.DOWN)
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            header1View?.indexPrice?.text = price.toString()
        }
    }

    /**
     * 标记价格更新
     */
    override fun onMarketPrice(marketPrice: MarkPriceBean?) {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            header1View?.tagPrice?.text = marketPrice?.p
        }
    }

    /**
     * 费率更新
     */
    override fun onFundingRate(fundRate: FundingRateBean?) {
        Log.d("iiiiii", "onFundingRate")
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            val rate: String? = NumberUtil.formatNumberNoGroup(
                fundRate?.fundingRate?.toFloat()?.times(100),
                RoundingMode.FLOOR,
                4,
                4
            ) + "%"
            headerView?.tvFundRate?.text = rate
            updateFundTime(fundRate?.nextCollectionTime)
        }
    }

    /**
     * 用户表费率更新
     */
    override fun onUserFundingRate(fundRate: UserStepRate?) {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            userStepRate = fundRate
        }
    }

    override fun onLeverageDetail(data: LeverageBracketBean?) {
        leverageBracket = data
    }
    /**
     * 更新总权益
     */
    override fun updateTotalProfit(totalProfit: String, available: String) {
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            binding?.fragmentHomePageContractHeader?.totalProfitValue?.text = totalProfit
            binding?.fragmentHomePageContractHeader1?.amount?.text = available

        }
    }

    override fun futureBalance(balanceDetailBean: BalanceDetailBean?) {
        balanceDetailBean1 = balanceDetailBean
        CommonUtil.checkActivityAndRunOnUI(mContext) {
            //binding?.fragmentHomePageContractHeader1?.amount?.setText(balanceDetailBean1?.availableBalance.toString())
        }
    }

    override fun onLeverPairConfigCheck(hasLeverConfig: Boolean) {
    }
    override fun onTradeOrderFastClick(tradeOrder: TradeOrder) {
        if (currentOrderType.equals("MARKET")) {
            return
        }
        header1View?.price?.setText(tradeOrder.formattedPrice?: "0.0")
        val scaleAnim = AnimationUtils.loadAnimation(mContext, R.anim.transaction_price_anim)
        header1View?.price?.startAnimation(scaleAnim)
    }

    override fun onDeepChanged(deep: Deep) {
        onDeepChoose()
    }

    private fun updateFundTime(nextCollectionTime: Long?) {
        fundRateTime = (nextCollectionTime?.minus(System.currentTimeMillis()))?.div(1000)
        mFundRateTimerHandler.removeCallbacks(fundRateTimer)
        mFundRateTimerHandler.post(fundRateTimer)
    }

    private fun initInputPriceValue(price: String?) {
        if (header1View?.price?.text?.isEmpty() == true) {
            if (priceInputFlag == false) {
                header1View?.price?.setText(price)
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateCurrentPairPrice(price: String?) {
        if (price != null && price.toDouble() > 0) {
            header1View?.currentPrice?.setText(price)
            headerView?.analyticChart?.setCurrentPrice(price.toDouble())
//            Log.d("ttt---->rmb", C2CApiServiceHelper?.coinUsdtPrice?.toString())
            if (C2CApiServiceHelper.coinUsdtPrice?.usdt == null) {
                return
            }
            else {
                header1View?.currentPriceCny?.text = "≈" + NumberUtil.formatNumberNoGroup(rates!! * price.toDouble(), 4, 4) + if (exchanged == 0) "CNY" else "USD"

            }
        }
        else {
            header1View?.currentPriceCny?.setText(
                "≈" + "0.0000"
                + if (exchanged == 0) "CNY" else "USD"
            )
        }
        initLimitPrice()
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
           // binding!!.actionBarLayout.currentPriceSince.background = background
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
           // binding!!.actionBarLayout.currentPriceSince.background = background
            binding!!.actionBarLayout.currentPriceSince.setTextColor(color!!)
        }
    }
    override fun onKLineDataAll(items: ArrayList<KLineItem?>) {
        if(items.isNotEmpty()){
            Log.d("121212",kLinePage.toString())
            refreshKLineChart(items)
        }
    }

    override fun onKLineDataAdd(item: KLineItem) {
        if(item != null){
            headerView?.analyticChart?.addData(item)
            headerView?.analyticChart?.postInvalidate()
        }
    }

    override fun onKLineDataMore(kLinePage: Int, items: ArrayList<KLineItem?>) {
        headerView?.analyticChart?.setLoadingMore(false)
        if(items.isNotEmpty()){
            Log.d("121212",kLinePage.toString())
            headerView?.analyticChart?.addDataList(kLinePage, items)
            headerView?.analyticChart?.postInvalidate()
        }
    }

    override fun onKLineLoadingMore() {
        headerView?.analyticChart?.setLoadingMore(false)
    }
    //刷新当前钱包
    private fun refreshCurrentWallet() {
        /*currentWallet = null
        currentEstimatedWallet = null
        viewModel!!.getCurrentWallet(tabType)

         */
    }

    private fun refreshUsable() {
        /*activity?.runOnUiThread {
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
                    binding!!.fragmentHomePageContractHeader1.useable.setText("0.0000")
                }
            }
            else if (transactionType == 2) {
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

                    binding!!.fragmentHomePageContractHeader1.useable.setText("0.0000")
                }
                binding!!.fragmentHomePageContractHeader1.useableBuy.setText("0.0000")
            }

        }*/
    }
   /* override fun onUserBalanceChanged(userBalance: UserBalance?) {
      Log.d(TAG, "onUserBalanceChanged,coin = " + userBalance?.coin)
      if (userBalance?.coin.equals(currentBalanceBuy?.coin)) {
          currentBalanceBuy = userBalance
          header1View!!.useableBuy.text = currentBalanceBuy?.availableBalance.toString()
      }
      if (userBalance?.coin.equals(currentBalanceSell?.coin)) {
          header1View!!.useableBuy.text = currentBalanceBuy?.availableBalance.toString()
          currentBalanceSell = userBalance
      }
      refreshUsable()
  }

    */
    /*override fun onWallet(observable: Observable<Pair<Wallet?, Wallet?>>?) {
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

        */

    /* override fun getUserBalanceCallback(): Callback<Pair<UserBalance?, UserBalance?>> {
         return object : Callback<Pair<UserBalance?, UserBalance?>>() {
             override fun callback(returnData: Pair<UserBalance?, UserBalance?>?) {
                 if (returnData != null) {
                     //currentBalanceBuy = returnData.first
                     //currentBalanceSell = returnData.second
                 }
                // refreshUsable()
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

     */


    /*override fun getWalletCallback(): Callback<Pair<Wallet?, Wallet?>> {
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

     */

    /* private var leverDetail: WalletLeverDetail? = null
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

     */


    /* override fun onRechargeClick(transactionMorePopup: TransactionMorePopup) {
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


     */
    /* override fun onBillClick(transactionMorePopup: TransactionMorePopup) {
         fryingHelper.checkUserAndDoing(Runnable {
             //点击账户详情
             val extras = Bundle()
             extras.putString(ConstData.ROUTER_COIN_TYPE, viewModel!!.getCoinType())
             BlackRouter.getInstance().build(RouterConstData.WALLET_DETAIL).with(extras)
                 .go(mFragment)
         }, TRADE_INDEX)
     }

     */

    /* override fun onCollectClick(
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

     */

    /* override fun onChatRoomClick(transactionMorePopup: TransactionMorePopup, chatRoomId: String?) {
     }
     */



/* override fun onUserBanlance(observable: Observable<HttpRequestResultDataList<UserBalance?>?>?) {
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

 */

}
