import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.GridView
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.black.base.adapter.BaseDataBindAdapter
import com.black.base.adapter.BaseViewPagerAdapter
import com.black.base.api.PairApiServiceHelper
import com.black.base.databinding.ListItemPageMainStatusBinding
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.Money
import com.black.base.model.NormalCallback
import com.black.base.model.clutter.*
import com.black.base.model.clutter.NoticeHome.NoticeHomeItem
import com.black.base.model.community.ChatRoomEnable
import com.black.base.model.socket.PairStatus
import com.black.base.model.user.UserInfo
import com.black.base.model.wallet.CoinInfo
import com.black.base.model.wallet.CoinInfoType
import com.black.base.model.wallet.Wallet
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.util.ConstData.CHOOSE_COIN_RECHARGE
import com.black.base.util.ConstData.CHOOSE_COIN_WITHDRAW
import com.black.base.view.FloatAdView
import com.black.base.widget.ObserveScrollView
import com.black.base.widget.VerticalTextView
import com.black.frying.adapter.HomeMainRiseFallAdapter
import com.black.frying.service.FutureService
import com.black.frying.view.MainMorePopup
import com.black.frying.view.MainMorePopup.OnMainMoreClickListener
import com.black.frying.viewmodel.MainViewModel
import com.black.im.util.IMHelper
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageMainFiexBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.tabs.TabLayout
import io.reactivex.Observable
import skin.support.content.res.SkinCompatResources
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 *先取symbolList,在取tickers,然后取kLine数据
 */
class HomePageMainFragmentFiex : BaseFragment(), View.OnClickListener,
    ObserveScrollView.ScrollListener, MainViewModel.OnMainModelListener, OnMainMoreClickListener {
    companion object {
        private const val STATUS_PAGE_COUNT = 2
        private var TAG = HomePageMainFragment::class.java.simpleName
    }

    private var userInfo: UserInfo? = null
    private var adapter: HomeMainRiseFallAdapter? = null

    private val hotPairMap = HashMap<String?, PairStatus?>()
    private val hardGridViewMap = HashMap<String?, GridView?>()

    private var statusAdapter: BaseViewPagerAdapter? = null
    private var chatFloatAdView: FloatAdView? = null

    var binding: FragmentHomePageMainFiexBinding? = null
    var layout: FrameLayout? = null
    private var imageLoader: ImageLoader? = null
    private var viewModel: MainViewModel? = null
    private var homeTabType: Int? = ConstData.HOME_TAB_HOT


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (layout != null) {
            return layout
        }
        imageLoader = ImageLoader(mContext!!)
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home_page_main_fiex,
            container,
            false
        )
        viewModel = MainViewModel(mContext!!, this)
        layout = binding?.root as FrameLayout
        StatusBarUtil.addStatusBarPadding(layout)
        binding!!.refreshLayout.setRefreshHolder(RefreshHolderFrying(activity!!))
        binding!!.refreshLayout.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                viewModel!!.getNoticeInfo()
//                viewModel!!.getHotPairs()
//                viewModel!!.getHomeTicker()
//                viewModel!!.getHomeKline()
                binding!!.refreshLayout.postDelayed(
                    { binding!!.refreshLayout.setRefreshing(false) },
                    300
                )
            }

        })
        binding!!.scrollView.addScrollListener(object : ObserveScrollView.ScrollListener {
            override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
            }

        })
        binding!!.noticeLayout.visibility = View.VISIBLE
        binding!!.noticeTextView.setTextColor(SkinCompatResources.getColor(activity, R.color.T1))
        binding!!.noticeTextView.setTextStillTime(3000)//设置停留时长间隔
        binding!!.noticeTextView.setAnimTime(300)//设置进入和退出的时间间隔

        binding!!.mainTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                homeTabType = tab.position
                adapter?.setType(homeTabType)
//                viewModel!!.getRiseFallData(type)
                viewModel!!.getHomeSybolListData(
                    homeTabType!!,
                    PairApiServiceHelper.getSymboleListPairData(mContext)
                )

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(mContext, R.color.B2)
        binding!!.riseFallListView.divider = drawable
        binding!!.riseFallListView.dividerHeight = 15
        adapter = HomeMainRiseFallAdapter(mContext!!, homeTabType, null)
        binding!!.riseFallListView.adapter = adapter
        binding!!.riseFallListView.setOnItemClickListener { _, _, position, _ ->
            onQuotationPairStatusClick(adapter?.getItem(position)!!)
        }

        binding!!.btnNoticeMore.setOnClickListener(this)
        binding!!.c2c.setOnClickListener(this)
        binding!!.btnUserinfo.setOnClickListener(this)
        binding!!.btnSearchMenu.setOnClickListener(this)
        binding!!.btnScanMenu.setOnClickListener(this)
        binding!!.btnMoreMenu.setOnClickListener(this)
        layout?.viewTreeObserver?.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        })
        FutureService.initFutureData(mContext)
        return layout
    }

    override fun getViewModel(): MainViewModel? {
        return viewModel
    }

    override fun onResume() {
        super.onResume()
        userInfo = if (activity == null) null else CookieUtil.getUserInfo(activity!!)
        binding!!.noticeTextView.startAutoScroll()
//        viewModel!!.computeTotalAmount()
    }

    override fun onPause() {
        super.onPause()
        binding!!.noticeTextView.stopAutoScroll()
    }

    override fun doResetSkinResources() {
        //主要是listview的分割线有问题
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(mContext, R.color.L1_ALPHA30)
        binding!!.riseFallListView.divider = drawable
        adapter?.resetSkinResources()
        adapter?.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CHOOSE_COIN_RECHARGE -> {
                    val chooseWallet: Wallet? = data?.getParcelableExtra(ConstData.WALLET)
                    if (chooseWallet != null) {
                        val bundle = Bundle()
                        bundle.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_EXCHANGE)
                        bundle.putParcelable(ConstData.WALLET, chooseWallet)
                        BlackRouter.getInstance().build(RouterConstData.RECHARGE).with(bundle)
                            .go(this)
                    }
                }
                CHOOSE_COIN_WITHDRAW -> {
                    val chooseWallet: Wallet? = data?.getParcelableExtra(ConstData.WALLET)
                    if (chooseWallet != null) {
                        val bundle = Bundle()
                        bundle.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_WITHDRAW)
                        bundle.putParcelable(ConstData.WALLET, chooseWallet)
                        BlackRouter.getInstance().build(RouterConstData.EXTRACT).with(bundle)
                            .go(this)
                    }
                }
                ConstData.SCANNIN_GREQUEST_CODE -> {
                    val bundle = data?.extras
                    val scanResult = bundle?.getString("result")
//                    binding?.extractAddress?.setText(scanResult ?: "")
                }
            }
        }
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.btn_notice_more -> {
                val bundle = Bundle()
                bundle.putString(ConstData.TITLE, getString(R.string.notice))
                bundle.putString(
                    ConstData.URL,
                    String.format(
                        UrlConfig.getUrlNoticeAll(mContext),
                        FryingUtil.getLanguageKey(mContext)
                    )
                )
                BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)
            }
            R.id.btn_userinfo -> BlackRouter.getInstance().build(RouterConstData.MINE)
                .go(mContext)//用户信息
            R.id.c2c -> BlackRouter.getInstance().build(RouterConstData.C2C_NEW)
                .go(mContext)
            R.id.btn_search_menu -> BlackRouter.getInstance()
                .build(RouterConstData.DEAR_PAIR_SEARCH).go(mContext)
            R.id.btn_scan_menu -> BlackRouter.getInstance().build(RouterConstData.CAPTURE)
                .withRequestCode(ConstData.SCANNIN_GREQUEST_CODE)
                .go(mContext)
            R.id.btn_more_menu -> mContext?.let {
                MainMorePopup(it).setOnMainMoreClickListener(this).show(v)
            }
        }
    }


    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
    }

    override fun onPairStatusDataChanged(observable: Observable<ArrayList<PairStatus?>?>?) {
        observable!!.subscribe { updatePairData ->
            CommonUtil.checkActivityAndRunOnUI(activity) {
                for (pairStatus in updatePairData!!) {
                    val hotPair = hotPairMap[pairStatus?.pair]
                    if (hotPair != null) {
                        hotPair.precision = pairStatus?.precision ?: 0
                        hotPair.currentPrice = (pairStatus?.currentPrice ?: 0.0)
                        hotPair.tradeVolume = pairStatus?.tradeVolume
                        hotPair.setCurrentPriceCNY(pairStatus?.currentPriceCNY, nullAmount)
                        hotPair.priceChangeSinceToday = (pairStatus?.priceChangeSinceToday)
                        val gridView = hardGridViewMap[pairStatus?.pair]
                        if (gridView != null && hotPair?.currentPrice!! > 0) {
                            var gridViewAdapter = gridView?.adapter as GridViewAdapter
                            gridViewAdapter.updateItem(pairStatus?.pair, hotPair)
                            gridViewAdapter.notifyDataSetChanged()
                        }
                    }
                }
                homeTabType?.let {
                    if (updatePairData != null) {
                        viewModel?.updateHomeSymbolListData(
                            it,
                            updatePairData,
                            PairApiServiceHelper.getSymboleListPairData(mContext)
                        )
                    }
                }
            }
        }.run { }
    }


    override fun onRiseFallDataChanged(observable: Observable<ArrayList<PairStatus?>?>?) {
    }

    override fun onCoinlistConfig(data: ArrayList<CoinInfoType?>?) {
    }

    override fun onHomeTabDataChanged(observable: Observable<ArrayList<PairStatus?>?>?) {
        Log.d(TAG, "onHomeTabDataChanged")
        observable!!.subscribe {
            CommonUtil.checkActivityAndRunOnUI(activity) {
                adapter?.data = it
                adapter?.notifyDataSetChanged()
            }
        }.run { }
    }

    override fun onNoticeList(observable: Observable<NoticeHome?>?) {
        observable!!.subscribe(
            HttpCallbackSimple(
                mContext,
                false,
                object : Callback<NoticeHome?>() {
                    override fun callback(returnData: NoticeHome?) =
                        if (returnData?.articles != null && returnData.articles!!.isNotEmpty()) {
                            //显示公告
                            val noticeTitles = ArrayList<String?>()
                            val noticeList = returnData.articles
                            for (notice in returnData.articles!!) {
                                noticeTitles.add(notice?.title.toString())
                            }
                            if (noticeTitles.isNotEmpty()) {
                                binding!!.noticeTextView.setTextList(noticeTitles)
                                binding!!.noticeLayout.visibility = View.VISIBLE
                                binding!!.noticeTextView.setOnItemClickListener(object :
                                    VerticalTextView.OnItemClickListener {
                                    override fun onItemClick(position: Int) {
                                        val notice: NoticeHomeItem? =
                                            CommonUtil.getItemFromList(noticeList, position)
                                        if (notice != null) {
                                            val bundle = Bundle()
                                            bundle.putString(ConstData.TITLE, notice.title)
                                            bundle.putString(ConstData.URL, notice.html_url)
                                            BlackRouter.getInstance()
                                                .build(RouterConstData.WEB_VIEW).with(bundle)
                                                .go(mContext)
                                        }
                                    }

                                })
                            } else {
//                            binding!!.noticeLayout.visibility = View.GONE
                            }
                        } else {
//                        binding!!.noticeLayout.visibility = View.GONE
                        }

                    override fun error(type: Int, error: Any?) {
//                binding!!.noticeLayout.visibility = View.GONE
                    }
                })
        )
    }

    override fun onHomeKLine(observable: Observable<ArrayList<PairStatus?>?>?) {
        observable!!.subscribe(
            HttpCallbackSimple(
                mContext,
                false,
                object : Callback<ArrayList<PairStatus?>?>() {
                    override fun callback(returnData: ArrayList<PairStatus?>?) {
                        if (returnData != null) {
                            Log.d(TAG, "onHomeKLine succ")
                            showTickersPairs(returnData)
                            var pairListData = PairApiServiceHelper.getSymboleListPairData(context)
                            viewModel!!.getHomeSybolListData(homeTabType!!, pairListData)
                        } else {
                            Log.d(TAG, "onHomeKLine data null or fail")
                        }
                    }

                    override fun error(type: Int, error: Any?) {
                        Log.d(TAG, "onHomeKLine error")
                    }
                })
        )
    }

    override fun onHomeTickers(observable: Observable<ArrayList<PairStatus?>?>?) {
        observable!!.subscribe(
            HttpCallbackSimple(
                mContext,
                false,
                object : Callback<ArrayList<PairStatus?>?>() {
                    override fun callback(tickers: ArrayList<PairStatus?>?) {
                        if (tickers != null) {
//                    tickersData = returnData?.data as ArrayList<HomeTickers?>
                            Log.d(TAG, "onHomeTickers succ")
                            viewModel?.getHomeKline(tickers)
                        } else {
                            Log.d(TAG, "onHomeTickers data null or fail")
                        }
                    }

                    override fun error(type: Int, error: Any?) {
                        Log.d(TAG, "onHomeTickers error")
                    }
                })
        )
    }

    override fun onHeadBanner(observable: Observable<HttpRequestResultDataList<Banner?>?>?) {
    }

    override fun onMiddleBanner(observable: Observable<HttpRequestResultDataList<Banner?>?>?) {
    }

    override fun onHotPairs(observable: Observable<HttpRequestResultDataList<String?>?>?) {
        observable!!.subscribe(
            HttpCallbackSimple(
                mContext,
                false,
                object : Callback<HttpRequestResultDataList<String?>?>() {
                    override fun callback(returnData: HttpRequestResultDataList<String?>?) {
                        if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                        } else {
                        }
                    }

                    override fun error(type: Int, error: Any?) {
                    }
                })
        )
    }

    private fun showTickersPairs(pairs: ArrayList<PairStatus?>?) {
        //先临时取btc跟eth
        var ticketData = pairs?.filter { it?.pair == "BTC_USDT" || it?.pair == "ETH_USDT" }
        val viewList: MutableList<View> = ArrayList()
        if (ticketData != null) {
            val pairCount = ticketData.size
            if (pairCount > 0) {
                var pageCount = pairCount / STATUS_PAGE_COUNT
//                pageCount = if (pairCount % STATUS_PAGE_COUNT > 0) pageCount + 1 else pageCount
                pageCount = 1//暂时取1，如果有更多的交易对需要展示，在修改该值
                for (i in 0 until pageCount) {
                    val gridPairs: MutableList<PairStatus?> = ArrayList(STATUS_PAGE_COUNT)
                    val offset = i * STATUS_PAGE_COUNT
                    val rest =
                        if (offset + STATUS_PAGE_COUNT <= pairCount) STATUS_PAGE_COUNT else pairCount - offset
                    val gridView = GridView(activity)
                    for (j in 0 until rest) {
                        val pairStatus = ticketData[offset + j]
                        var pairName = pairStatus?.pair
                        var temp = hardGridViewMap[pairStatus?.pair]
                        if (temp != null) {
                            if (pairStatus?.currentPrice!! > 0) {
                                var gridViewAdapter = temp?.adapter as GridViewAdapter
                                gridViewAdapter.updateItem(pairStatus?.pair, pairStatus)
                                gridViewAdapter.notifyDataSetChanged()
                                hotPairMap[pairName] = pairStatus
                            }
                        } else {
                            gridPairs.add(pairStatus)
                            hotPairMap[pairName] = pairStatus
                            val adapter = GridViewAdapter(activity!!, gridPairs)
                            gridView?.numColumns = STATUS_PAGE_COUNT
                            gridView?.adapter = adapter
                            gridView?.scrollBarSize = 0
                            gridView?.horizontalSpacing = 15
                            gridView?.onItemClickListener =
                                AdapterView.OnItemClickListener { _, _, position, _ ->
                                    onQuotationPairStatusClick(adapter.getItem(position)!!)
                                }
                            hardGridViewMap[pairName] = gridView
                            viewList.add(gridView)
                        }
                    }
                }
                Log.d(TAG, "viewList.size = " + viewList.size)
                if (viewList.size > 0) {
                    if (statusAdapter == null) {
                        statusAdapter = BaseViewPagerAdapter(viewList)
                    } else {
                        statusAdapter?.setViewList(viewList)
                    }
                    binding!!.statusViewPager.adapter = statusAdapter
                }
            }
        }
//        viewModel!!.getAllPairStatus()
    }

    //用户信息被修改，刷新委托信息和钱包
    override fun onUserInfoChanged() {
    }

    override fun onMoney(observable: Observable<Money?>?) {

    }

    override fun getMoneyCallback(): Callback<Money?>? {
        return null
    }

    //行情项目点击响应
    private fun onQuotationPairStatusClick(pairStatus: PairStatus) {
        //选择的交易对，进入详情
        activity?.let {
            CookieUtil.setCurrentPair(it, pairStatus.pair)
            sendPairChangedBroadcast(SocketUtil.COMMAND_PAIR_CHANGED)
            val bundle = Bundle()
            bundle.putString(ConstData.PAIR, pairStatus.pair)
            BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).with(bundle).go(it)
        }
    }

    //弹窗悬浮聊天界面
    private fun showChatFloatAdView() {
        if (chatFloatAdView == null && mContext != null) {
            chatFloatAdView = FloatAdView(mContext!!, 0, 0)
            chatFloatAdView!!.setOnClickListener(View.OnClickListener {
                fryingHelper.checkUserAndDoing(Runnable {
                    viewModel!!.checkIntoMainChat()
                        ?.subscribe(
                            HttpCallbackSimple(
                                mContext,
                                true,
                                object : NormalCallback<ChatRoomEnable>(mContext!!) {
                                    override fun callback(chatRoomEnable: ChatRoomEnable?) {
                                        intoPublicChat(chatRoomEnable!!)
                                    }
                                })
                        )
                }, 0)
            })
            val bitmap = ImageUtil.getBitmapFromRes(context, R.drawable.icon_chat)
            chatFloatAdView!!.setLottieBitmap(bitmap)
        }
        chatFloatAdView!!.show(layout)
    }

    private fun intoPublicChat(chatRoomEnable: ChatRoomEnable) {
        if (mContext == null) {
            return
        }
        val userInfo = CookieUtil.getUserInfo(mContext!!)
        if (userInfo == null) {
            FryingUtil.showToast(mContext, "请先登录系统")
        } else {
            val userIdHeader = IMHelper.getUserIdHeader(mContext!!)
            val userId = userInfo.id
            val groupId = if (FryingUtil.isReal(mContext!!)) "@TGS#3WMZE2BG6" else "@TGS#3TJVTVBGJ"
            val groupName = "FBSexer"
            val bundle = Bundle()
            bundle.putString(ConstData.IM_GROUP_ID, groupId)
            bundle.putString(ConstData.IM_GROUP_NAME, groupName)
            bundle.putParcelable(ConstData.IM_CHAT_ROOM_ENABLE, chatRoomEnable)
            IMHelper.startWithIMGroupActivity(
                mContext!!,
                mContext,
                userIdHeader + userId,
                groupId,
                RouterConstData.PUBLIC_CHAT_GROUP,
                bundle,
                null,
                null
            )
        }
    }

    internal class GridViewAdapter(context: Context, data: MutableList<PairStatus?>?) :
        BaseDataBindAdapter<PairStatus?, ListItemPageMainStatusBinding>(context, data) {
        private var brokenLine: LineChart? = null

        /**
         * k线数据转为FloatArray
         */
        fun getKineYdata(kLineData: HomeTickersKline?): ArrayList<Entry> {
            var kDataList: ArrayList<HomeTickersKline.Kdata>? = kLineData?.k
            var yListSize = kDataList?.size
            var kPlistArray = FloatArray(yListSize!!)
            val dataEntry = java.util.ArrayList<Entry>()
            var index = 0
            for (value in kDataList!!) {
                value?.p?.toFloatOrNull()?.let { kPlistArray[index] = it }
                var entry = value?.p?.toFloat()?.let { Entry(index.toFloat(), it, null) }
                if (entry != null) {
                    dataEntry.add(entry)
                }
                index++
            }
            return dataEntry
        }

        override fun resetSkinResources() {
            super.resetSkinResources()
            colorWin = SkinCompatResources.getColor(context, R.color.T10)
            colorLost = SkinCompatResources.getColor(context, R.color.T9)
            colorDefault = SkinCompatResources.getColor(context, R.color.T3)
        }

        fun updateItem(pairName: String?, pairStatus: PairStatus?) {
            for (i in data!!.indices) {
                if (pairName.equals(data!![i]?.pair)) {
                    updateItem(i, pairStatus)
                }
            }
        }

        fun initBrokenline(brokenLine: LineChart?, values: ArrayList<Entry>, color: Int?) {
            brokenLine?.setDrawBorders(false)
            brokenLine?.isAutoScaleMinMaxEnabled = true
            brokenLine?.setBackgroundColor(Color.WHITE)
            // disable description text
            brokenLine?.description?.isEnabled = false
            // enable touch gestures
            brokenLine?.setTouchEnabled(false)
            brokenLine?.setDrawGridBackground(false)
            brokenLine?.isDragEnabled = false
            brokenLine?.setScaleEnabled(false)
            brokenLine?.setPinchZoom(false)
            var xAxis = brokenLine?.getXAxis()
            xAxis?.setDrawGridLines(false)
            xAxis?.isEnabled = false
            brokenLine?.axisRight?.isEnabled = false
            brokenLine?.axisLeft?.isEnabled = false
            // get the legend (only possible after setting data)
            val l: Legend? = brokenLine?.legend
            l?.isEnabled = false

            var set1 = LineDataSet(values, " ")
            if (brokenLine?.data != null &&
                brokenLine?.data?.dataSetCount!! > 0
            ) {
                set1 = brokenLine?.data.getDataSetByIndex(0) as LineDataSet
                set1?.values = values
                set1.notifyDataSetChanged()
                brokenLine?.data.notifyDataChanged()
                brokenLine?.notifyDataSetChanged()
            } else {
                set1.setDrawIcons(false)
                // black lines and points
                if (color != null) {
                    set1.color = color
                }
                if (color != null) {
                    set1.setCircleColor(color)
                }
                // line thickness and point size
                set1.lineWidth = 2f
                set1.circleRadius = 1f
                set1.setDrawValues(false)
                // draw points as solid circles
                set1.setDrawCircleHole(false)
                val dataSets = java.util.ArrayList<ILineDataSet>()
                dataSets.add(set1)
                val data = LineData(dataSets)
                brokenLine?.data = data

            }
        }

        override fun getItemLayoutId(): Int {
            return R.layout.list_item_page_main_status
        }

        override fun bindView(position: Int, holder: ViewHolder<ListItemPageMainStatusBinding>?) {
            val pairStatus = getItem(position)
            val binding = holder?.dataBing
            val styleChange = StyleChangeUtil.getStyleChangeSetting(context)?.styleCode
            if (styleChange == 1) {
                val color =
                    if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) colorDefault!! else if (pairStatus.priceChangeSinceToday!! > 0) colorWin!! else colorLost!!
                val bgWinLose =
                    if (pairStatus!!.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) context.getDrawable(
                        R.drawable.hot_item_bg_corner_default
                    ) else if (pairStatus.priceChangeSinceToday!! > 0) context.getDrawable(R.drawable.hot_item_bg_corner_up) else context.getDrawable(
                        R.drawable.hot_item_bg_corner_down
                    )
                pairStatus.setCurrentPriceCNY(pairStatus.currentPriceCNY, nullAmount)
                binding?.gridIndicator?.setBackgroundColor(color)
                binding?.raiseDownBg?.background = bgWinLose
                binding?.pairName?.text =
                    if (pairStatus.pair == null) "null" else pairStatus.pair!!.replace("_", "/")
                binding?.pairPrice?.setTextColor(color)
                binding?.pairSince?.text = pairStatus.priceChangeSinceTodayFormat
                binding?.pairSince?.setTextColor(context.getColor(R.color.T4))
                val exChangeRates = ExchangeRatesUtil.getExchangeRatesSetting(context)?.rateCode
                if (exChangeRates == 0) {
                    binding?.pairPrice?.text = pairStatus.currentPriceFormat
                    binding?.pairPriceCny?.text =
                        String.format("%s CNY", pairStatus.currentPriceCNYFormat)
                } else {
                    binding?.pairPrice?.text = pairStatus.currentPriceFormat
                    binding?.pairPriceCny?.text =
                        String.format("%s USD", pairStatus.currentPriceFormat)
                }
                val kLineData = pairStatus.kLineData
                if (kLineData != null) {
                    brokenLine = binding?.lineCart
                    val brokenLineData = getKineYdata(pairStatus.kLineData)
                    initBrokenline(brokenLine, brokenLineData, color)
                }
            }
            if (styleChange == 0) {
                val color =
                    if (pairStatus?.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) colorDefault!! else if (pairStatus.priceChangeSinceToday!! < 0) colorWin!! else colorLost!!
                val bgWinLose =
                    if (pairStatus!!.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) context.getDrawable(
                        R.drawable.hot_item_bg_corner_default
                    ) else if (pairStatus.priceChangeSinceToday!! < 0) context.getDrawable(R.drawable.hot_item_bg_corner_up) else context.getDrawable(
                        R.drawable.hot_item_bg_corner_down
                    )
                pairStatus.setCurrentPriceCNY(pairStatus.currentPriceCNY, nullAmount)
                binding?.gridIndicator?.setBackgroundColor(color)
                binding?.raiseDownBg?.background = bgWinLose
                binding?.pairName?.text =
                    if (pairStatus.pair == null) "null" else pairStatus.pair!!.replace("_", "/")
                binding?.pairPrice?.setTextColor(color)
                binding?.pairSince?.text = pairStatus.priceChangeSinceTodayFormat
                binding?.pairSince?.setTextColor(context.getColor(R.color.T4))
                binding?.pairPrice?.text = pairStatus.currentPriceFormat
                val exChangeRates = ExchangeRatesUtil.getExchangeRatesSetting(context)?.rateCode
                if (exChangeRates == 0) {
                    binding?.pairPrice?.text = pairStatus.currentPriceFormat
                    binding?.pairPriceCny?.text =
                        String.format("%s CNY", pairStatus.currentPriceCNYFormat)
                } else {
                    binding?.pairPrice?.text = pairStatus.currentPriceFormat
                    binding?.pairPriceCny?.text =
                        String.format("%s USD", pairStatus.currentPriceFormat)
                }
                val kLineData = pairStatus.kLineData
                if (kLineData != null) {
                    brokenLine = binding?.lineCart
                    val brokenLineData = getKineYdata(pairStatus.kLineData)
                    initBrokenline(brokenLine, brokenLineData, color)
                }
            }
        }
    }

    override fun onCancelClick(mainMorePopup: MainMorePopup) {
        mainMorePopup.dismiss()
    }

    override fun onNotifyClick(mainMorePopup: MainMorePopup) {
    }

    override fun onCustomServiceClick(mainMorePopup: MainMorePopup) {
    }

    override fun onDoScanClick(mainMorePopup: MainMorePopup) {
    }

    override fun onFlashExchangeClick(mainMorePopup: MainMorePopup) {
    }

    override fun onInformationClick(mainMorePopup: MainMorePopup) {
    }
}