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
import com.black.base.model.clutter.*
import com.black.base.model.clutter.NoticeHome.NoticeHomeItem
import com.black.base.model.community.ChatRoomEnable
import com.black.base.model.socket.PairStatus
import com.black.base.model.user.UserInfo
import com.black.base.model.wallet.Wallet
import com.black.base.net.HttpCallbackSimple
import com.black.base.util.*
import com.black.base.util.ConstData.CHOOSE_COIN_RECHARGE
import com.black.base.util.ConstData.CHOOSE_COIN_WITHDRAW
import com.black.base.view.FloatAdView
import com.black.base.widget.ObserveScrollView
import com.black.base.widget.VerticalTextView
import com.black.frying.adapter.HomeMainRiseFallAdapter
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
import com.google.android.material.tabs.TabLayout
import io.reactivex.Observable
import skin.support.content.res.SkinCompatResources

class HomePageMainFragmentFiex : BaseFragment(), View.OnClickListener, ObserveScrollView.ScrollListener, MainViewModel.OnMainModelListener,OnMainMoreClickListener {
    companion object {
        private const val STATUS_PAGE_COUNT = 2
        private const val TAG = "HomePageMainFragment"
    }

    private var userInfo: UserInfo? = null
    private var adapter: HomeMainRiseFallAdapter? = null

    private val hotPairMap = HashMap<String, PairStatus>()
    private val hardGridViewMap = HashMap<String, GridView>()

    private var statusAdapter: BaseViewPagerAdapter? = null
    private var chatFloatAdView: FloatAdView? = null

    var binding: FragmentHomePageMainFiexBinding? = null
    var layout: FrameLayout? = null
    private var imageLoader: ImageLoader? = null
    private var viewModel: MainViewModel? = null
    private var homeTabTyoe:Int? = ConstData.HOME_TAB_HOT


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        imageLoader = ImageLoader(mContext!!)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_page_main_fiex, container, false)
        viewModel = MainViewModel(mContext!!, this)
        layout = binding?.root as FrameLayout
        StatusBarUtil.addStatusBarPadding(layout)
        binding!!.refreshLayout.setRefreshHolder(RefreshHolderFrying(activity!!))
        binding!!.refreshLayout.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
//                viewModel!!.getNoticeInfo()
//                viewModel!!.getHotPairs()
                viewModel!!.getSymbolList()
//                viewModel!!.getHomeTicker()
//                viewModel!!.getHomeKline()
                binding!!.refreshLayout.postDelayed({ binding!!.refreshLayout.setRefreshing(false) }, 300)
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

        binding!!.statusViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
            }
        })

        binding!!.mainTab.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                homeTabTyoe = tab.position
//                viewModel!!.getRiseFallData(type)
                viewModel!!.getHomeSybolListData(homeTabTyoe!!)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(mContext, R.color.B2)
        binding!!.riseFallListView.divider = drawable
        binding!!.riseFallListView.dividerHeight = 25
        adapter = HomeMainRiseFallAdapter(mContext!!, null)
        binding!!.riseFallListView.adapter = adapter
        binding!!.riseFallListView.setOnItemClickListener { _, _, position, _ ->
            onQuotationPairStatusClick(adapter?.getItem(position)!!)
        }

        binding!!.btnNoticeMore.setOnClickListener(this)

        binding!!.btnUserinfo.setOnClickListener(this)
        binding!!.btnSearchMenu.setOnClickListener(this)
        binding!!.btnScanMenu.setOnClickListener(this)
        binding!!.btnMoreMenu.setOnClickListener(this)

        layout?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        })

//        viewModel!!.getRiseFallData(1)
//        viewModel!!.getSymbolList()
//        viewModel!!.getHomeTicker()
//        viewModel!!.getHomeKline()
//        showChatFloatAdView()
        return layout
    }

    override fun getViewModel(): MainViewModel? {
        return viewModel
    }

    override fun onResume() {
        super.onResume()
        userInfo = if (activity == null) null else CookieUtil.getUserInfo(activity!!)
        binding!!.noticeTextView.startAutoScroll()
        viewModel!!.computeTotalAmount()
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
                        BlackRouter.getInstance().build(RouterConstData.RECHARGE).with(bundle).go(this)
                    }
                }
                CHOOSE_COIN_WITHDRAW -> {
                    val chooseWallet: Wallet? = data?.getParcelableExtra(ConstData.WALLET)
                    if (chooseWallet != null) {
                        val bundle = Bundle()
                        bundle.putInt(ConstData.WALLET_HANDLE_TYPE, ConstData.TAB_WITHDRAW)
                        bundle.putParcelable(ConstData.WALLET, chooseWallet)
                        BlackRouter.getInstance().build(RouterConstData.EXTRACT).with(bundle).go(this)
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
                bundle.putString(ConstData.URL, String.format(UrlConfig.getUrlNoticeAll(mContext), FryingUtil.getLanguageKey(mContext)))
                BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)
            }
            R.id.btn_userinfo -> BlackRouter.getInstance().build(RouterConstData.MINE).go(mContext)//用户信息
            R.id.btn_search_menu -> BlackRouter.getInstance().build(RouterConstData.DEAR_PAIR_SEARCH).go(mContext)
            R.id.btn_scan_menu ->  BlackRouter.getInstance().build(RouterConstData.CAPTURE)
                .withRequestCode(ConstData.SCANNIN_GREQUEST_CODE)
                .go(mContext)
            R.id.btn_more_menu -> mContext?.let { MainMorePopup(it).setOnMainMoreClickListener(this).show(v) }
        }
    }



    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
    }

    override fun onPairStatusDataChanged(observable: Observable<ArrayList<PairStatus?>?>?) {
        observable!!.subscribe { pairStatusList ->
            CommonUtil.checkActivityAndRunOnUI(activity) {
                val showGridViewList: MutableList<GridView> = ArrayList()
                for (pairStatus in pairStatusList!!) {
                    val hotPair = hotPairMap[pairStatus?.pair]
                    if (hotPair != null) {
                        hotPair.precision = pairStatus?.precision ?: 0
                        hotPair.currentPrice = (pairStatus?.currentPrice ?: 0.0)
                        hotPair.setCurrentPriceCNY(pairStatus?.currentPriceCNY, nullAmount)
                        hotPair.priceChangeSinceToday = (pairStatus?.priceChangeSinceToday)
                    }
                    val gridView = hardGridViewMap[pairStatus?.pair]
                    if (gridView != null && !showGridViewList.contains(gridView)) {
                        showGridViewList.add(gridView)
                    }
                }
                for (gridView in showGridViewList) {
//                    (gridView.adapter as GridViewAdapter).notifyDataSetChanged()
                }
//                viewModel!!.getRiseFallData(if (binding!!.risePairs.isChecked) 1 else 2)
            }
        }.run { }
    }

    override fun onRiseFallDataChanged(observable: Observable<ArrayList<PairStatus?>?>?) {
    }

    override fun onHomeTabDataChanged(observable: Observable<ArrayList<PairStatus?>?>?) {
        Log.d(TAG,"onHomeTabDataChanged")
        observable!!.subscribe {
            CommonUtil.checkActivityAndRunOnUI(activity) {
                adapter?.data = it
                adapter?.notifyDataSetChanged()
            }
        }.run { }
    }

    override fun onNoticeList(observable: Observable<NoticeHome?>?) {
        observable!!.subscribe(HttpCallbackSimple(mContext, false, object : Callback<NoticeHome?>() {
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
                            binding!!.noticeTextView.setOnItemClickListener(object : VerticalTextView.OnItemClickListener {
                                override fun onItemClick(position: Int) {
                                    val notice: NoticeHomeItem? = CommonUtil.getItemFromList(noticeList, position)
                                    if (notice != null) {
                                        val bundle = Bundle()
                                        bundle.putString(ConstData.TITLE, notice.title)
                                        bundle.putString(ConstData.URL, notice.html_url)
                                        BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)
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
        }))
    }

    override fun onHomeKLine(observable: Observable<HttpRequestResultDataList<HomeTickersKline?>?>?) {
        observable!!.subscribe(HttpCallbackSimple(mContext, false, object : Callback<HttpRequestResultDataList<HomeTickersKline?>?>() {
            override fun callback(returnData: HttpRequestResultDataList<HomeTickersKline?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
//                    tickersKline = returnData?.data as ArrayList<HomeTickersKline?>
                    Log.d(TAG,"onHomeKLine succ")
                    showTickersPairs(PairApiServiceHelper.getHomePagePairData())
                    viewModel!!.getHomeSybolListData(homeTabTyoe!!)
                } else {
                    Log.d(TAG,"onHomeKLine data null or fail")
                }
            }

            override fun error(type: Int, error: Any?) {
                Log.d(TAG,"onHomeKLine error")
            }
        }))
    }

    override fun onHomeTickers(observable: Observable<HttpRequestResultDataList<HomeTickers?>?>?) {
        observable!!.subscribe(HttpCallbackSimple(mContext, false, object : Callback<HttpRequestResultDataList<HomeTickers?>?>() {
            override fun callback(returnData: HttpRequestResultDataList<HomeTickers?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
//                    tickersData = returnData?.data as ArrayList<HomeTickers?>
                    Log.d(TAG,"onHomeTickers succ")
                    viewModel?.getHomeKline()
                } else {
                    Log.d(TAG,"onHomeTickers data null or fail")
                }
            }
            override fun error(type: Int, error: Any?) {
                Log.d(TAG,"onHomeTickers error")
            }
        }))
    }


    override fun onHomeSymbolList(observable: Observable<HttpRequestResultDataList<HomeSymbolList?>?>?) {
        observable!!.subscribe(HttpCallbackSimple(mContext, false, object : Callback<HttpRequestResultDataList<HomeSymbolList?>?>() {
            override fun callback(returnData: HttpRequestResultDataList<HomeSymbolList?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
//                    symbolListData = returnData?.data as ArrayList<HomeSymbolList?>
                    Log.d(TAG,"onHomeSymbolList succ")
                    viewModel?.getHomeTicker()
                } else {
                    Log.d(TAG,"onHomeSymbolList data null or fail")
                }
            }
            override fun error(type: Int, error: Any?) {
                Log.d(TAG,"onHomeSymbolList error")
            }
        }))
    }
    override fun onHeadBanner(observable: Observable<HttpRequestResultDataList<Banner?>?>?) {
    }

    override fun onMiddleBanner(observable: Observable<HttpRequestResultDataList<Banner?>?>?) {
    }

    override fun onHotPairs(observable: Observable<HttpRequestResultDataList<String?>?>?) {
        observable!!.subscribe(HttpCallbackSimple(mContext, false, object : Callback<HttpRequestResultDataList<String?>?>() {
            override fun callback(returnData: HttpRequestResultDataList<String?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    showHotPairs(returnData.data)
                } else {
                    showHotPairs(null)
                }
            }

            override fun error(type: Int, error: Any?) {
                showHotPairs(null)
            }
        }))
    }

    private fun showHotPairs(pairs: ArrayList<String?>?) {
        val viewList: MutableList<View> = ArrayList()
        if (pairs != null) {
            val pairCount = pairs.size
            if (pairCount > 0) {
                var pageCount = pairCount / STATUS_PAGE_COUNT
                pageCount = if (pairCount % STATUS_PAGE_COUNT > 0) pageCount + 1 else pageCount
                for (i in 0 until pageCount) {
                    val gridPairs: MutableList<PairStatus?> = ArrayList(STATUS_PAGE_COUNT)
                    val offset = i * STATUS_PAGE_COUNT
                    val rest = if (offset + STATUS_PAGE_COUNT <= pairCount) STATUS_PAGE_COUNT else pairCount - offset
                    val gridView = GridView(activity)
                    for (j in 0 until rest) {
                        val pair = pairs[offset + j]
                        pair?.let {
                            val pairStatus = PairStatus()
                            pairStatus.pair = pair
                            pairStatus.currentPrice = (pairStatus.currentPrice)
                            pairStatus.setCurrentPriceCNY(0.0, nullAmount)
                            pairStatus.priceChangeSinceToday = (pairStatus.priceChangeSinceToday)
                            gridPairs.add(pairStatus)
                            hotPairMap[it] = pairStatus
                            hardGridViewMap[it] = gridView
                        }
                    }
                    val adapter = GridViewAdapter(activity!!, gridPairs)
                    gridView.numColumns = STATUS_PAGE_COUNT
                    gridView.adapter = adapter
                    gridView.horizontalSpacing = 15
                    gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ -> onQuotationPairStatusClick(adapter.getItem(position)!!) }
                    viewList.add(gridView)
                }
            }
        }
        statusAdapter = BaseViewPagerAdapter(viewList)
        binding!!.statusViewPager.adapter = statusAdapter
        binding!!.statusIndicator.removeAllViews()
        for (i in viewList.indices) {
            binding!!.statusIndicator.addView(activity!!.layoutInflater.inflate(R.layout.view_main_status_dot, null))
        }
        if (viewList.size > 0) {
            binding!!.statusViewPager.currentItem = 0
        }
        viewModel!!.getAllPairStatus()
    }

    private fun showTickersPairs(pairs: ArrayList<PairStatus?>?) {
        //先临时取btc跟eth
        var ticketData = pairs?.filter { it?.pair == "BTC_USDT" || it?.pair == "ETH_USDT" }

        val viewList: MutableList<View> = ArrayList()
        if (ticketData != null) {
            val pairCount = ticketData.size
            if (pairCount > 0) {
                var pageCount = pairCount / STATUS_PAGE_COUNT
                pageCount = if (pairCount % STATUS_PAGE_COUNT > 0) pageCount + 1 else pageCount
                for (i in 0 until pageCount) {
                    val gridPairs: MutableList<PairStatus?> = ArrayList(STATUS_PAGE_COUNT)
                    val offset = i * STATUS_PAGE_COUNT
                    val rest = if (offset + STATUS_PAGE_COUNT <= pairCount) STATUS_PAGE_COUNT else pairCount - offset
                    val gridView = GridView(activity)
                    for (j in 0 until rest) {
                        gridPairs.add(ticketData[offset + j])
                    }
                    val adapter = GridViewAdapter(activity!!, gridPairs)
                    gridView.numColumns = STATUS_PAGE_COUNT
                    gridView.adapter = adapter
                    gridView.horizontalSpacing = 15
                    gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ -> onQuotationPairStatusClick(adapter.getItem(position)!!) }
                    viewList.add(gridView)
                }
            }
        }
        statusAdapter = BaseViewPagerAdapter(viewList)
        binding!!.statusViewPager.adapter = statusAdapter
        binding!!.statusIndicator.removeAllViews()
        for (i in viewList.indices) {
            binding!!.statusIndicator.addView(activity!!.layoutInflater.inflate(R.layout.view_main_status_dot, null))
        }
        if (viewList.size > 0) {
            binding!!.statusViewPager.currentItem = 0
        }
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
            BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).go(it)
        }
    }

    private fun refreshUserInfo() {
        if (isVisible) {
            viewModel!!.computeTotalAmount()
        }
    }

    //弹窗悬浮聊天界面
    private fun showChatFloatAdView() {
        if (chatFloatAdView == null && mContext != null) {
            chatFloatAdView = FloatAdView(mContext!!, 0, 0)
            chatFloatAdView!!.setOnClickListener(View.OnClickListener {
                fryingHelper.checkUserAndDoing(Runnable {
                    viewModel!!.checkIntoMainChat()
                            ?.subscribe(HttpCallbackSimple(mContext, true, object : NormalCallback<ChatRoomEnable>() {
                                override fun callback(chatRoomEnable: ChatRoomEnable?) {
                                    intoPublicChat(chatRoomEnable!!)
                                }
                            }))
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
            IMHelper.startWithIMGroupActivity(mContext!!, mContext, userIdHeader + userId, groupId, RouterConstData.PUBLIC_CHAT_GROUP, bundle, null, null)
        }
    }

    internal class GridViewAdapter(context: Context, data: MutableList<PairStatus?>?) : BaseDataBindAdapter<PairStatus?, ListItemPageMainStatusBinding>(context, data) {
        private var color4: Int = 0

        /**
         * 取x轴横坐标数据，整数均分递增
         */
        fun getKLineXdata(kLineData:HomeTickersKline?): Array<String?>? {
            var kDataList: ArrayList<HomeTickersKline.Kdata>? = kLineData?.k
            var size = kDataList?.size
            Log.d("kkkkk","size = "+size)
            var arrayX = size?.let { arrayOfNulls<String>(it) }
            for (i in 0 until size!!){
                arrayX?.set(i, i.toString())
            }
            return arrayX
        }

        /**
         * 取y轴纵坐标数据，最大值和最小值均分，0开始
         */
        fun getKlineData(kLineData:HomeTickersKline?):FloatArray{
            var kDataList:ArrayList<HomeTickersKline.Kdata>? = kLineData?.k
            Log.d("kkkkk","size = "+kDataList?.size)
            var yListSize = kDataList?.size
            var kPlistArray = FloatArray(yListSize!!)
            for (value in kDataList!!){
                var index = 0
                value?.p?.toFloatOrNull()?.let { kPlistArray[index] = it }
            }
            return kPlistArray
        }

        /**
         * k线数据转为FloatArray
         */
        fun getKineYdata(kLineData:HomeTickersKline?):FloatArray{
            var kDataList:ArrayList<HomeTickersKline.Kdata>? = kLineData?.k
            var yListSize = kDataList?.size
            var kPlistArray = FloatArray(yListSize!!)
            for (value in kDataList!!){
                var index = 0
                value?.p?.toFloatOrNull()?.let { kPlistArray[index] = it }
                index++
            }
            var yMax = kPlistArray.maxOrNull()
            var newYdata:ArrayList<Float> = ArrayList()
            var divide = yListSize?.let { yMax?.div(it.toFloat()) }
            var temp: Float? = null
            if (yListSize != null) {
                while (yListSize > -1){
                    if(temp == null){
                        temp = 0.0f
                    }else{
                        if (divide != null) {
                            temp += divide
                        }
                    }
                    if(yListSize == 0){
                        if(temp < yMax!!){
                            temp = yMax
                        }
                    }
                    newYdata.add(temp)
                    yListSize--
                }
            }
            return newYdata.toFloatArray()
        }
        override fun resetSkinResources() {
            super.resetSkinResources()
            colorWin = SkinCompatResources.getColor(context, R.color.T7)
            colorLost = SkinCompatResources.getColor(context, R.color.T5)
            color4 = SkinCompatResources.getColor(context, R.color.T3)
        }

        override fun getItemLayoutId(): Int {
            return R.layout.list_item_page_main_status
        }

        override fun bindView(position: Int, holder: ViewHolder<ListItemPageMainStatusBinding>?) {
            val pairStatus = getItem(position)
            val binding = holder?.dataBing
            val color = if (pairStatus!!.priceChangeSinceToday == null || pairStatus.priceChangeSinceToday == 0.0) colorDefault else if (pairStatus.priceChangeSinceToday!! > 0) colorWin else colorLost
            binding?.pairName?.text = if (pairStatus.pair == null) "null" else pairStatus.pair!!.replace("_", "/")
            binding?.pairPrice?.text = pairStatus.currentPriceFormat
            binding?.pairPrice?.setTextColor(color)
            binding?.pairSince?.text = pairStatus.priceChangeSinceTodayFormat
            binding?.pairSince?.setTextColor(colorDefault)
//            binding?.pairPriceCny?.text = String.format("¥ %s", pairStatus.currentPriceCNYFormat)
            binding?.pairPriceCny?.text = "≈"+pairStatus.currentPrice*6.5

//            Log.d("fffff","kLine1 = "+ pairStatus?.kLineDate?.k?.get(0)?.p)
            Log.d("fffff","kLine1 = "+ pairStatus?.kLineDate?.k?.get(0)?.p)
            //折线图假数据
            var xdata = arrayOf("0","1","2","3","4","5","6","7","8","9")
            var ydata:FloatArray = floatArrayOf(0.0f,10.0f,20.0f,30.0f,40.0f,50.0f,60.0f,70.0f,80.0f,90.0f)
            var linedata:FloatArray = floatArrayOf(5f,10f,6f,30f,5f,62.5f,6f,2f,3f,6f)


//            binding?.lineCart?.setChartdate(xdata,ydata,linedata, Color.BLACK)
            var kLineData = pairStatus?.kLineDate
            if(kLineData != null){
                getKLineXdata(kLineData)?.let { binding?.lineCart?.setChartdate(it,getKineYdata(kLineData),getKlineData(kLineData), Color.BLACK) }
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