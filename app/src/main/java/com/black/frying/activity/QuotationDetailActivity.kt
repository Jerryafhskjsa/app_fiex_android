package com.black.frying.activity

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewTreeObserver
import android.widget.CheckedTextView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.black.base.activity.BaseActionBarActivity
import com.black.base.model.HttpRequestResultData
import com.black.base.model.HttpRequestResultString
import com.black.base.model.socket.*
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.ConstData
import com.black.base.util.CookieUtil
import com.black.base.util.FryingUtil
import com.black.base.util.RouterConstData
import com.black.base.viewmodel.BaseViewModel
import com.black.base.widget.AnalyticChart.AnalyticChartHelper
import com.black.base.widget.AnalyticChart.Companion.BOLL
import com.black.base.widget.AnalyticChart.Companion.KDJ
import com.black.base.widget.AnalyticChart.TimeStep
import com.black.frying.adapter.QuotationDetailDealAdapter
import com.black.frying.view.KLineQuotaSelector
import com.black.frying.view.MoreTimeStepSelector
import com.black.frying.view.QuotationDetailDeepViewBinding
import com.black.frying.viewmodel.QuotationDetailViewModel
import com.black.frying.viewmodel.QuotationDetailViewModel.OnKLineModelListener
import com.black.im.util.IMHelper
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.router.annotation.Route
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.BR
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.ActivityQuotationDetailBinding
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.MODE_FIXED
import io.reactivex.Observable
import skin.support.content.res.SkinCompatResources
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

//行情详情 走势图
@Route(value = [RouterConstData.QUOTATION_DETAIL])
open class QuotationDetailActivity : BaseActionBarActivity(), View.OnClickListener, OnKLineModelListener {
    companion object {
        private const val MAX_SHOW_COUNT = 20
        private val TAB_TITLES = arrayOfNulls<String>(3) //标题
    }

    private var moreTimeStepSelector: MoreTimeStepSelector? = null

    private var lastTimeStepIndex = 0
    private var kLineQuotaSelector: KLineQuotaSelector? = null

    private var dealAdapter: QuotationDetailDealAdapter? = null
    private var quotationList: MutableList<TradeOrder?> = ArrayList()

    private var btnCollect: CheckedTextView? = null
    private var coinTypeView: TextView? = null
    private var chatRoomId: String? = null
    private var btnChatRoom: View? = null
    private var moreTimeStepTextView: TextView? = null

    private var colorT7 = 0
    private var colorT5 = 0
    private var colorT3 = 0
    private var bgT3: Drawable? = null
    private var bgT7: Drawable? = null
    private var bgT5: Drawable? = null

    var padding: Float? = 0f
    private var binding: ActivityQuotationDetailBinding? = null
    private var viewModel: QuotationDetailViewModel? = null
    private var deepBinding: QuotationDetailDeepViewBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_quotation_detail)
        viewModel = QuotationDetailViewModel(this, intent.getStringExtra(ConstData.PAIR), this)
        deepBinding = QuotationDetailDeepViewBinding(this, binding?.quotationDetailDeepLayout!!, MAX_SHOW_COUNT)

        colorT7 = SkinCompatResources.getColor(this, R.color.T7)
        colorT5 = SkinCompatResources.getColor(this, R.color.T5)
        colorT3 = SkinCompatResources.getColor(this, R.color.T3)
        bgT7 = SkinCompatResources.getDrawable(this, R.drawable.bg_t7_corner3)
        bgT5 = SkinCompatResources.getDrawable(this, R.drawable.bg_t5_corner3)
        bgT3 = SkinCompatResources.getDrawable(this, R.drawable.bg_t3_corner3)

        padding = resources?.getDimension(R.dimen.default_padding)

        TAB_TITLES[0] = getString(R.string.deep)
        TAB_TITLES[1] = getString(R.string.deal)
        TAB_TITLES[2] = getString(R.string.coin_description)

        binding?.quota?.setOnClickListener(this)

        kLineQuotaSelector = KLineQuotaSelector(this)
        kLineQuotaSelector?.setOnKLineQuotaSelectorListener(object : KLineQuotaSelector.OnKLineQuotaSelectorListener {
            override fun onSelect(type: Int?) {
                if (type != null) {
                    binding?.analyticChart?.setType(type)
                }
            }

        })
        binding?.analyticChart?.setType(BOLL or KDJ)
//        binding?.analyticChart?.hideSub()
        binding?.analyticChart?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val params = binding?.analyticChart?.layoutParams
                params?.height = binding?.analyticChart?.width?.times(1.1)?.toInt()
                binding?.analyticChart?.layoutParams = params
                binding?.analyticChart?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        })
        selectKTab(binding?.analyticChart?.getTimeStep())
        binding?.analyticChart?.setAnalyticChartHelper(object : AnalyticChartHelper {
            override fun onLoadMore(page: Int) {
                viewModel?.listenKLineDataMore(page)
            }

        })

        binding?.loadingLayout?.visibility = View.GONE

        binding?.tab?.setSelectedTabIndicatorHeight(0)
        binding?.tab?.tabMode = MODE_FIXED
        for (i in TAB_TITLES.indices) {
            val tab = binding?.tab?.newTab()?.setText(TAB_TITLES[i])
            tab?.setCustomView(R.layout.view_k_line_tab)
            val textView: TextView? = tab?.customView?.findViewById(android.R.id.text1)
            textView?.text = TAB_TITLES[i]
            binding?.tab?.addTab(tab!!)
        }
        binding?.tab?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                val view = tab?.customView
                val textView: TextView? = view?.findViewById(android.R.id.text1)
                if (textView != null) {
                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL), Typeface.NORMAL)
                    textView.postInvalidate()
                }
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                val view = tab?.customView
                val textView: TextView? = view?.findViewById(android.R.id.text1)
                if (textView != null) {
                    textView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD), Typeface.BOLD)
                    textView.postInvalidate()
                }
                val checkedId: Int? = tab?.position
                selectTab(checkedId)
            }

        })

        binding?.btnBuy?.setOnClickListener(this)
        binding?.btnSale?.setOnClickListener(this)

        binding?.tab?.getTabAt(0)?.select()
        selectTab(0)

        initTimeStepTab()
        initDealLayout()
        initDescriptionLayout()

        checkAndShowChatRoomButton()
    }

    private fun initDealLayout() {
        val linearLayoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        linearLayoutManager.isSmoothScrollbarEnabled = true
        binding?.quotationDetailDealLayout?.quotationRecyclerView?.layoutManager = linearLayoutManager
        dealAdapter = QuotationDetailDealAdapter(this, BR.detailModel, quotationList)
        dealAdapter?.setAmountLength(viewModel!!.getAmountLength())
        binding?.quotationDetailDealLayout?.quotationRecyclerView?.adapter = dealAdapter
        binding?.quotationDetailDealLayout?.quotationRecyclerView?.isNestedScrollingEnabled = false
        binding?.quotationDetailDealLayout?.quotationRecyclerView?.setHasFixedSize(true)
        binding?.quotationDetailDealLayout?.quotationRecyclerView?.isFocusableInTouchMode = false
    }

    override fun getActionBarLayoutId(): Int {
        return R.layout.action_bar_quotation_detail
    }

    override fun isStatusBarDark(): Boolean {
        return !super.isStatusBarDark()
    }

    override fun initActionBarView(view: View) {
        coinTypeView = view.findViewById(R.id.action_bar_title)
        view.findViewById<View>(R.id.full_screen).setOnClickListener(this)
        view.findViewById<View>(R.id.btn_chat_room).also { btnChatRoom = it }.setOnClickListener(this)
        btnCollect = view.findViewById(R.id.btn_collect)
        btnCollect?.setOnClickListener(this)
    }

    private fun selectTab(position: Int?) {
        if (position == 0) {
            deepBinding!!.show()
            binding?.quotationDetailDealLayout?.root?.visibility = View.GONE
            binding?.quotationDetailDescriptionLayout?.root?.visibility = View.GONE
        } else if (position == 1) {
            deepBinding!!.hide()
            binding?.quotationDetailDealLayout?.root?.visibility = View.VISIBLE
            binding?.quotationDetailDescriptionLayout?.root?.visibility = View.GONE
        } else if (position == 2) {
            deepBinding!!.hide()
            binding?.quotationDetailDealLayout?.root?.visibility = View.GONE
            binding?.quotationDetailDescriptionLayout?.root?.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        selectKTab(binding?.analyticChart?.getTimeStep())
        viewModel!!.getPairDescription()
        viewModel!!.getTradePairInfo()
        viewModel!!.getChatRoomId()
        deepBinding!!.initList()
        viewModel!!.checkDearPair()
        viewModel!!.onResume()
        viewModel!!.getAllOrder()
        viewModel!!.getQuotationDeals()
//        listenKLineData()
        viewModel!!.getKLineDataFiex()

    }

    override fun getViewModel(): BaseViewModel<*>? {
        return viewModel
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_chat_room -> fryingHelper.checkUserAndDoing(Runnable { viewModel!!.checkIntoChatRoom() }, 0)
            R.id.full_screen -> {
                val bundle = Bundle()
                bundle.putString(ConstData.PAIR, intent.getStringExtra(ConstData.PAIR))
                BlackRouter.getInstance().build(RouterConstData.K_LINE_FULL).with(bundle).go(this) { routeResult, error ->
                    if (routeResult) {
                        viewModel!!.gotoLarge = true
                    }
                }
            }
            R.id.btn_collect -> viewModel!!.toggleDearPair(btnCollect!!.isChecked)
            R.id.btn_buy -> {
                val bundle = Bundle()
                bundle.putInt(ConstData.HOME_FRAGMENT_INDEX, 2)
                bundle.putInt(ConstData.TRANSACTION_INDEX, 1)
                bundle.putInt(ConstData.TRANSACTION_TYPE, ConstData.TAB_COIN)
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
            R.id.btn_sale -> {
                val bundle = Bundle()
                bundle.putInt(ConstData.HOME_FRAGMENT_INDEX, 2)
                bundle.putInt(ConstData.TRANSACTION_INDEX, 2)
                bundle.putInt(ConstData.TRANSACTION_TYPE, ConstData.TAB_COIN)
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
            R.id.quota -> kLineQuotaSelector!!.show(binding?.quota, binding?.analyticChart?.getType()!!)
            R.id.white_book -> openDescriptionLink(binding?.quotationDetailDescriptionLayout?.whiteBook?.text.toString())
            R.id.company_link -> openDescriptionLink(binding?.quotationDetailDescriptionLayout?.companyLink?.text.toString())
            R.id.search -> openDescriptionLink(binding?.quotationDetailDescriptionLayout?.search?.text.toString())
        }
    }

    private fun initTimeStepTab() {
        val pubSteps = arrayOf(TimeStep.NONE, TimeStep.MIN_15, TimeStep.HOUR_1, TimeStep.HOUR_4, TimeStep.DAY_1, TimeStep.MORE)
        val timeStepTab = findViewById<TabLayout>(R.id.k_time_step_tab)
        timeStepTab.setSelectedTabIndicatorHeight(0)
        timeStepTab.tabMode = MODE_FIXED
        val moreIndex = pubSteps.size - 1
        for (i in pubSteps.indices) {
            val timeStep = pubSteps[i]
            val tab = timeStepTab.newTab().setText(timeStep.toString())
            tab.tag = timeStep
            if (timeStep === TimeStep.MORE) {
                tab.setCustomView(R.layout.view_k_time_step_tab_more)
            } else {
                tab.setCustomView(R.layout.view_k_time_step_tab)
            }
            val textView = tab.customView!!.findViewById<View>(android.R.id.text1) as TextView
            if (timeStep === TimeStep.MORE) {
                moreTimeStepTextView = textView
            }
            textView.text = timeStep.toString()
            timeStepTab.addTab(tab)
        }
        moreTimeStepSelector = MoreTimeStepSelector(this)
        moreTimeStepSelector!!.setOnMoreTimeStepSelectorListener(object : MoreTimeStepSelector.OnMoreTimeStepSelectorListener {
            override fun onSelect(timeStep: TimeStep?) {
                if (timeStep != null) {
                    moreTimeStepTextView!!.text = timeStep.toString()
                    lastTimeStepIndex = moreIndex
                    selectKTab(timeStep)
                } else {
                    if (lastTimeStepIndex != moreIndex) {
                        timeStepTab.getTabAt(lastTimeStepIndex)!!.select()
                    }
                }
            }

        })
        timeStepTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                val timeStep = tab.tag as TimeStep?
                if (timeStep !== TimeStep.MORE) {
                    moreTimeStepTextView!!.text = TimeStep.MORE.toString()
                    if (lastTimeStepIndex != tab.position) {
                        selectKTab(timeStep)
                    }
                } else {
                    moreTimeStepSelector!!.show(binding?.timeStepLayout, binding?.analyticChart?.getTimeStep()!!)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                val timeStep = tab.tag as TimeStep?
                if (timeStep !== TimeStep.MORE) {
                    lastTimeStepIndex = tab.position
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                val timeStep = tab.tag as TimeStep?
                if (timeStep === TimeStep.MORE) {
                    moreTimeStepSelector!!.show(timeStepTab, binding?.analyticChart?.getTimeStep()!!)
                }
            }
        })
        val tab = timeStepTab.getTabAt(1)
        tab?.select()
    }

    private fun openDescriptionLink(link: String) {
        if (TextUtils.isEmpty(link) || TextUtils.equals(nullAmount, link)) {
        } else {
            openSystemBrowse(link)
        }
    }

    private fun selectKTab(timeStep: TimeStep?) {
        if (timeStep == null) {
            return
        }
        viewModel!!.finishListenKLine()
        if (binding?.analyticChart?.getTimeStep() != timeStep) {
            binding?.analyticChart?.setTimeStep(timeStep)
            listenKLineData()
        }
    }

    private fun initDescriptionLayout() {
        binding?.quotationDetailDescriptionLayout?.whiteBook?.setOnClickListener(this)
        binding?.quotationDetailDescriptionLayout?.companyLink?.setOnClickListener(this)
        binding?.quotationDetailDescriptionLayout?.search?.setOnClickListener(this)
    }

    private fun refreshDescription(description: PairDescription?) {
        binding?.quotationDetailDescriptionLayout?.coinName?.setText(String.format("%s(%s)", checkedString(description?.coinName), checkedString(description?.coin)))
        binding?.quotationDetailDescriptionLayout?.date?.setText(if (description?.issueDate == null) nullAmount else CommonUtil.formatTimestamp("yyyy/MM/dd", description.issueDate!!))
        binding?.quotationDetailDescriptionLayout?.total?.setText(if (description?.totalAmount == null) nullAmount else NumberUtil.formatNumberNoGroup(description.totalAmount))
        binding?.quotationDetailDescriptionLayout?.useTotal?.setText(if (description?.flowAmount == null) nullAmount else NumberUtil.formatNumberNoGroup(description.flowAmount))
        binding?.quotationDetailDescriptionLayout?.joinPrice?.setText(checkedString(description?.issuePrice))
        binding?.quotationDetailDescriptionLayout?.whiteBook?.setText(checkedString(description?.whitePaperLink))
        binding?.quotationDetailDescriptionLayout?.companyLink?.setText(checkedString(description?.projectLink))
        binding?.quotationDetailDescriptionLayout?.search?.setText(checkedString(description?.blockLink))
        binding?.quotationDetailDescriptionLayout?.description?.setText(if (description?.description == null) nullAmount else CommonUtil.removeHtml(description.description))
    }

    private fun checkedString(source: String?): String? {
        return source ?: nullAmount
    }

    override fun onKLineDataAll(items: ArrayList<KLineItem?>) {
        binding?.loadingLayout?.visibility = View.GONE
        refreshKLineChart(items)
    }

    override fun onKLineDataAdd(item: KLineItem) {
        binding?.analyticChart?.addData(item)
        binding?.analyticChart?.postInvalidate()
    }

    override fun onKLineDataMore(kLinePage: Int, items: ArrayList<KLineItem?>) {
        binding?.analyticChart?.addDataList(kLinePage, items)
        binding?.analyticChart?.postInvalidate()
    }

    override fun onPairChanged(pair: String?) {
        coinTypeView?.text = pair?.replace("_", "/") ?: "NULL"
        deepBinding!!.refreshTitles(pair)
    }

    override fun onPairStatusPrecision(precision: Int) {
        binding?.analyticChart?.setPrecision(precision)
        deepBinding!!.setPrecision(precision);
    }

    override fun onPairStatusAmountPrecision(amountPrecision: Int) {
        deepBinding!!.setAmountPrecision(amountPrecision);
    }

    override fun onPairStatusDataChanged(pairStatus: PairStatus?) {
        if (pairStatus == null) {
            finish()
            return
        }
        //刷新交易对信息
        binding?.price?.setText(pairStatus.currentPriceFormat)
        binding?.priceCny?.setText(String.format("≈%s CNY", pairStatus.currentPriceCNYFormat))
        binding?.percentage?.setText(pairStatus.priceChangeSinceTodayDisplay)
        if (pairStatus.priceChangeSinceToday != null && pairStatus.priceChangeSinceToday == 0.0) {
            binding?.price?.setTextColor(colorT3)
            binding?.percentage?.background = bgT3
        } else if (pairStatus.isDown) {
            binding?.price?.setTextColor(colorT5)
            binding?.percentage?.background = bgT5
        } else {
            binding?.price?.setTextColor(colorT7)
            binding?.percentage?.background = bgT7
        }
        binding?.high?.setText(pairStatus.maxPriceFormat)
        binding?.low?.setText(pairStatus.minPriceFormat)
        binding?.volumeH24?.setText(pairStatus.totalAmountFromat)
        binding?.analyticChart?.setCurrentPrice(pairStatus.currentPrice)
        binding?.quotationDetailDeepLayout?.depthChart?.setMiddlePrice(pairStatus.currentPrice)
    }

    override fun onTradeOrder(currentPrice: Double, bidOrderList: ArrayList<TradeOrder?>, askOrderList: ArrayList<TradeOrder?>) {
        deepBinding!!.refreshQuotationOrders(currentPrice, bidOrderList, askOrderList)
    }

    override fun onDeal(dealData: ArrayList<TradeOrder?>?) {
        if (dealData != null) {
            if (dealData.isNotEmpty()) {
                Collections.sort(dealData, TradeOrder.COMPARATOR_DEAL)
            }
            runOnUiThread { refreshQuotationDeals(dealData) }
        }
    }

    override fun onPairDeal(value: PairDeal) {

    }

    override fun onPairDescription(observable: Observable<HttpRequestResultData<PairDescription?>?>?) {
        observable!!.subscribe(HttpCallbackSimple(this, false,
                object : Callback<HttpRequestResultData<PairDescription?>?>() {
                    override fun error(type: Int, error: Any) {}
                    override fun callback(returnData: HttpRequestResultData<PairDescription?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS && returnData.data != null) {
                            refreshDescription(returnData.data)
                        }
                    }
                }
        ))
    }

    override fun onChatRoomId(chatRoomId: String?) {
        this.chatRoomId = chatRoomId
        checkAndShowChatRoomButton()
    }

    override fun onCheckIntoChatRoom(observable: Observable<HttpRequestResultString>?) {
        observable!!.subscribe(HttpCallbackSimple(this, true, object : NormalCallback<HttpRequestResultString?>() {
            override fun callback(returnData: HttpRequestResultString?) =
                    if (returnData?.code != null && returnData.code == HttpRequestResult.SUCCESS) {
                        intoChatRoom(chatRoomId!!)
                    } else {
                        FryingUtil.showToast(mContext, if (returnData?.msg == null) {
                            "null"
                        } else returnData.msg)
                    }
        }))
    }

    override fun onCheckDearPair(isDearPair: Boolean?) {
        btnCollect!!.isChecked = isDearPair!!
        if (btnCollect!!.isChecked) {
            btnCollect!!.setText(R.string.collect_delete)
        } else {
            btnCollect!!.setText(R.string.collect_add)
        }
    }

    override fun onToggleDearPair(isSuccess: Boolean?) {
        if (isSuccess!!) {
            val showMsg = if (btnCollect!!.isChecked) getString(R.string.pair_collect_cancel_ok) else getString(R.string.pair_collect_add_ok)
            FryingUtil.showToast(mContext, showMsg)
            btnCollect!!.toggle()
            if (btnCollect!!.isChecked) {
                btnCollect!!.setText(R.string.collect_delete)
            } else {
                btnCollect!!.setText(R.string.collect_add)
            }
        }
    }

    internal class OrderViewHolder {
        var buyLayout: View? = null
        var buyIndexView: TextView? = null
        var buyAmountView: TextView? = null
        var buyPriceView: TextView? = null
        var saleLayout: View? = null
        var salePriceView: TextView? = null
        var saleAmountView: TextView? = null
        var saleIndexView: TextView? = null
    }

    //刷新当前成交数据
    private fun refreshQuotationDeals(dealOrders: MutableList<TradeOrder?>) {
        //显示列表
        dealAdapter?.data = if (dealOrders.size > MAX_SHOW_COUNT) dealOrders.subList(0, MAX_SHOW_COUNT) else dealOrders
        dealAdapter?.setAmountLength(viewModel!!.getAmountLength())
        dealAdapter?.notifyDataSetChanged()
    }

    private fun refreshKLineChart(kLineItems: ArrayList<KLineItem?>) {
        try {
            binding?.analyticChart?.setData(kLineItems)
            binding?.analyticChart?.invalidate()
        } catch (e: Exception) {
            FryingUtil.printError(e)
        }
    }


    private fun listenKLineData() {
        binding?.loadingLayout?.visibility = View.VISIBLE
        viewModel!!.listenKLineData(binding?.analyticChart?.getTimeStep()!!)
    }

    private fun checkAndShowChatRoomButton() {
        if (TextUtils.isEmpty(chatRoomId)) {
            btnChatRoom!!.visibility = View.GONE
        } else {
            btnChatRoom!!.visibility = View.VISIBLE
        }
    }

    private fun intoChatRoom(chatRoomId: String) {
        val userInfo = CookieUtil.getUserInfo(mContext)
        if (userInfo == null) {
            FryingUtil.showToast(mContext, "请先登录系统")
        } else {
            val userIdHeader = IMHelper.getUserIdHeader(mContext)
            val userId = userInfo.id
            val groupId = chatRoomId
            val groupName = "FBSexer"
            val bundle = Bundle()
            bundle.putString(ConstData.IM_GROUP_ID, groupId)
            bundle.putString(ConstData.IM_GROUP_NAME, groupName)
            IMHelper.startWithIMGroupActivity(this, mContext, userIdHeader + userId, groupId, RouterConstData.PUBLIC_CHAT_GROUP, bundle, null, null)
        }
    }

}