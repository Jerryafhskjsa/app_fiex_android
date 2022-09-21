import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.*
import androidx.databinding.DataBindingUtil
import androidx.viewpager.widget.ViewPager
import com.black.base.adapter.BaseDataBindAdapter
import com.black.base.adapter.BaseViewPagerAdapter
import com.black.base.databinding.ListItemPageMainStatusBinding
import com.black.base.fragment.BaseFragment
import com.black.base.lib.banner.FryingBanner
import com.black.base.lib.banner.FryingUrlImageNormalBanner
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.HttpRequestResultDataList
import com.black.base.model.Money
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
import com.black.frying.viewmodel.MainViewModel
import com.black.im.util.IMHelper
import com.black.lib.refresh.QRefreshLayout
import com.black.net.HttpRequestResult
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.CommonUtil
import com.black.util.ImageUtil
import com.black.util.NumberUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageMainBinding
import io.reactivex.Observable
import skin.support.content.res.SkinCompatResources

class HomePageMainFragment : BaseFragment(), View.OnClickListener, ObserveScrollView.ScrollListener, MainViewModel.OnMainModelListener {
    companion object {
        private const val STATUS_PAGE_COUNT = 3
    }

    private var userInfo: UserInfo? = null

    var imageBanner: FryingUrlImageNormalBanner? = null
    var middleBanner: FryingUrlImageNormalBanner? = null
    var checkRadio: RadioButton? = null

    private var adapter: HomeMainRiseFallAdapter? = null

    private val hotPairMap = HashMap<String, PairStatus>()
    private val hardGridViewMap = HashMap<String, GridView>()

    private var statusAdapter: BaseViewPagerAdapter? = null
    private var chatFloatAdView: FloatAdView? = null

    var binding: FragmentHomePageMainBinding? = null
    var layout: FrameLayout? = null
    private var imageLoader: ImageLoader? = null
    private var viewModel: MainViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (layout != null) {
            return layout
        }
        imageLoader = ImageLoader(mContext!!)
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_page_main, container, false)
        viewModel = MainViewModel(mContext!!, this)
        layout = binding?.root as FrameLayout
        StatusBarUtil.addStatusBarPadding(layout)
        binding!!.refreshLayout.setRefreshHolder(RefreshHolderFrying(activity!!))
        binding!!.refreshLayout.setOnRefreshListener(object : QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                viewModel!!.getAllBanner()
                viewModel!!.getMiddleBanner()
                viewModel!!.computeTotalAmount()
                viewModel!!.getNoticeInfo()
                viewModel!!.getHotPairs()
                binding!!.refreshLayout.postDelayed({ binding!!.refreshLayout.setRefreshing(false) }, 300)
            }

        })

        binding!!.scrollView.addScrollListener(object : ObserveScrollView.ScrollListener {
            override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
            }

        })

        binding!!.moneyLayout.setOnClickListener(this)
        binding!!.totalMoney.tag = "$$nullAmount"
        binding!!.totalMoneyCny.tag = "¥$nullAmount"

        refreshMoneys()

        binding!!.btnMainEye.setOnClickListener {
            mContext?.let {
                CookieUtil.setMainEyeStatus(it, binding!!.btnMainEye.isChecked)
                refreshMoneys()
            }
        }

        imageBanner = if (activity == null) null else FryingUrlImageNormalBanner(activity!!)
        imageBanner?.setScale(0.3333f, binding?.bannerLayout)
        binding?.bannerLayout?.addView(imageBanner?.bannerView)

        middleBanner = if (activity == null) null else FryingUrlImageNormalBanner(activity!!)
        middleBanner?.setScale(0.2087f, binding?.middleBannerLayout)
        binding?.middleBannerLayout?.addView(middleBanner?.bannerView)
        binding?.middleBannerLayout?.visibility = View.GONE

        binding!!.noticeLayout.visibility = View.GONE
        binding!!.noticeTextView.setTextColor(SkinCompatResources.getColor(activity, R.color.T1))
        binding!!.noticeTextView.setTextStillTime(3000)//设置停留时长间隔
        binding!!.noticeTextView.setAnimTime(300)//设置进入和退出的时间间隔

        binding!!.statusViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                val radioButton: RadioButton? = binding?.statusIndicator?.getChildAt(position) as RadioButton?
                if (radioButton != null) {
                    radioButton.isChecked = true
                    checkRadio = radioButton
                }
            }

            override fun onPageSelected(position: Int) {
            }
        })

        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(mContext, R.color.L1_ALPHA30)
        binding!!.riseFallListView.divider = drawable
        binding!!.riseFallListView.dividerHeight = 1
        adapter = HomeMainRiseFallAdapter(mContext!!, null)
        binding!!.riseFallListView.adapter = adapter
        binding!!.riseFallListView.setOnItemClickListener { _, _, position, _ ->
            onQuotationPairStatusClick(adapter?.getItem(position)!!)
        }

        binding!!.btnMainMenu.setOnClickListener(this)
        binding!!.btnNoticeMore.setOnClickListener(this)
        binding!!.btnC2c.setOnClickListener(this)
        binding!!.btnExchange.setOnClickListener(this)
        binding!!.btnWidthdraw.setOnClickListener(this)
        binding!!.risePairs.setOnClickListener(this)
        binding!!.fallPairs.setOnClickListener(this)

        //获取banner
        viewModel!!.getAllBanner()
        viewModel!!.getMiddleBanner()
        layout?.viewTreeObserver?.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                layout?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            }
        })

        selectRiseFall(1)
//        showChatFloatAdView()

        return layout
    }

    override fun getViewModel(): MainViewModel? {
        return viewModel
    }

    override fun onResume() {
        super.onResume()
        userInfo = if (activity == null) null else CookieUtil.getUserInfo(activity!!)
        imageLoader!!.loadImage(binding!!.btnMainMenu, if (userInfo == null) null else userInfo!!.headPortrait, R.drawable.icon_avatar, true)
        reloadUserInfo()
        binding!!.noticeTextView.startAutoScroll()
        if (checkRadio != null) {
            checkRadio!!.isChecked = true
        }
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
            }
        }
    }

    private fun refreshMoneys() {
        val isShow = if (activity == null) false else CookieUtil.getMainEyeStatus(activity!!)
        binding!!.btnMainEye.isChecked = isShow
        if (!isShow) {
            binding!!.totalMoney.setText("****")
            binding!!.totalMoneySpe.visibility = View.GONE
            binding!!.totalMoneyCny.visibility = View.GONE
        } else {
            formatMoneyValue(binding!!.totalMoney)
            binding!!.totalMoneySpe.visibility = View.VISIBLE
            binding!!.totalMoneyCny.visibility = View.VISIBLE
            formatMoneyValue(binding!!.totalMoneyCny)
        }
    }

    private fun formatMoneyValue(moneyView: TextView) {
        val stringTag: String? = moneyView.tag as String
        if (stringTag == null) {
            moneyView.text = getString(R.string.number_default)
        } else {
            moneyView.text = stringTag
        }
    }

    //切换涨跌幅tab
    private fun selectRiseFall(type: Int) {
        when (type) {
            1 -> {
                binding!!.risePairs.isChecked = true
                binding!!.fallPairs.isChecked = false
            }
            2 -> {
                binding!!.risePairs.isChecked = false
                binding!!.fallPairs.isChecked = true
            }
        }
        viewModel!!.getRiseFallData(type)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.money_layout -> BlackRouter.getInstance().build(RouterConstData.WALLET).go(mContext)
            R.id.btn_main_menu -> BlackRouter.getInstance().build(RouterConstData.MINE).go(mContext)
            R.id.btn_notice_more -> {
                val bundle = Bundle()
                bundle.putString(ConstData.TITLE, getString(R.string.notice))
                bundle.putString(ConstData.URL, String.format(UrlConfig.getUrlNoticeAll(mContext), FryingUtil.getLanguageKey(mContext)))
                BlackRouter.getInstance().build(RouterConstData.WEB_VIEW).with(bundle).go(mContext)
            }
            R.id.btn_c2c -> BlackRouter.getInstance().build(RouterConstData.C2C_NEW).go(mContext)
            R.id.btn_exchange -> {
                val extras = Bundle()
                BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                        .withRequestCode(CHOOSE_COIN_RECHARGE)
                        .with(extras)
                        .go(this@HomePageMainFragment)
            }
            R.id.btn_widthdraw -> {
                val extras = Bundle()
                BlackRouter.getInstance().build(RouterConstData.WALLET_CHOOSE_COIN)
                        .withRequestCode(CHOOSE_COIN_WITHDRAW)
                        .with(extras)
                        .go(this@HomePageMainFragment)
            }
            R.id.btn_rush -> BlackRouter.getInstance().build(RouterConstData.PROMOTIONS).go(mContext)
            R.id.rise_pairs -> selectRiseFall(1)
            R.id.fall_pairs -> selectRiseFall(2)
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
                    (gridView.adapter as GridViewAdapter).notifyDataSetChanged()
                }
                viewModel!!.getRiseFallData(if (binding!!.risePairs.isChecked) 1 else 2)
            }
        }.run { }
    }

    override fun onRiseFallDataChanged(observable: Observable<ArrayList<PairStatus?>?>?) {
        observable!!.subscribe {
            CommonUtil.checkActivityAndRunOnUI(activity) {
                adapter?.data = it
                adapter?.notifyDataSetChanged()
            }
        }.run { }
    }

    override fun onHomeTabDataChanged(observable: Observable<ArrayList<PairStatus?>?>?) {

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
                            binding!!.noticeLayout.visibility = View.GONE
                        }
                    } else {
                        binding!!.noticeLayout.visibility = View.GONE
                    }

            override fun error(type: Int, error: Any?) {
                binding!!.noticeLayout.visibility = View.GONE
            }
        }))
    }

    override fun onHeadBanner(observable: Observable<HttpRequestResultDataList<Banner?>?>?) {
        observable!!.subscribe(HttpCallbackSimple(mContext, false, object : Callback<HttpRequestResultDataList<Banner?>?>() {
            override fun callback(returnData: HttpRequestResultDataList<Banner?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    //显示banner
                    if (returnData.data != null && returnData.data!!.isNotEmpty()) {
                        showHeadBanner(returnData.data)
                    } else {
                        showHeadBanner(null)
                    }
                } else {
                    showHeadBanner(null)
                }
            }

            override fun error(type: Int, error: Any?) {
                showHeadBanner(null)
            }

            private fun showHeadBanner(bannerList: List<Banner?>?) {
                if (bannerList == null || bannerList.isEmpty()) {
                    binding!!.bannerLayout.visibility = View.GONE
                } else {
                    binding!!.bannerLayout.visibility = View.VISIBLE
                    imageBanner?.setData(bannerList)
                    imageBanner?.startScroll()
                    imageBanner?.setOnBannerItemClickListener(object : FryingBanner.OnBannerItemClickListener<Banner?> {
                        override fun onItemClick(item: Banner?) {
                            if (item != null && !TextUtils.isEmpty(item.linkUrl)) {
                                val bundle = Bundle()
                                bundle.putString(ConstData.TITLE, item.title)
                                bundle.putString(ConstData.URL, item.linkUrl)
                                BlackRouter.getInstance().build(item.linkUrl).with(bundle).go(mContext)
                            }
                        }

                    })
                }
            }
        }))
    }

    override fun onMiddleBanner(observable: Observable<HttpRequestResultDataList<Banner?>?>?) {
        observable!!.subscribe(HttpCallbackSimple(mContext, false, object : Callback<HttpRequestResultDataList<Banner?>?>() {
            override fun callback(returnData: HttpRequestResultDataList<Banner?>?) {
                if (returnData != null && returnData.code == HttpRequestResult.SUCCESS) {
                    //显示banner
                    if (returnData.data != null && returnData.data!!.isNotEmpty()) {
                        showMiddleBanners(returnData.data)
                    } else {
                        showMiddleBanners(null)
                    }
                } else {
                    showMiddleBanners(null)
                }
            }

            override fun error(type: Int, error: Any?) {
                showMiddleBanners(null)
            }

            private fun showMiddleBanners(bannerList: List<Banner?>?) {
                if (bannerList == null || bannerList.isEmpty()) {
                    binding!!.middleBannerLayout.visibility = View.GONE
                } else {
                    binding!!.middleBannerLayout.visibility = View.VISIBLE
                    middleBanner?.setData(bannerList)
                    middleBanner?.startScroll()
                    middleBanner?.setOnBannerItemClickListener(object : FryingBanner.OnBannerItemClickListener<Banner?> {
                        override fun onItemClick(item: Banner?) {
                            if (item != null && !TextUtils.isEmpty(item.linkUrl)) {
                                val bundle = Bundle()
                                bundle.putString(ConstData.TITLE, item.title)
                                bundle.putString(ConstData.URL, item.linkUrl)
                                BlackRouter.getInstance().build(item.linkUrl).with(bundle).go(mContext)
                            }
                        }

                    })
                }
            }
        }))
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

    override fun onHomeKLine(observable: Observable<HttpRequestResultDataList<HomeTickersKline?>?>?) {

    }

    override fun onCoinlistConfig(coinConfigList: ArrayList<CoinInfoType?>?) {
    }

    override fun onHomeSymbolList(observable: Observable<HttpRequestResultDataList<HomeSymbolList?>?>?) {

    }

    override fun onHomeTickers(observable: Observable<HttpRequestResultDataList<HomeTickers?>?>?) {

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
            (binding!!.statusIndicator.getChildAt(0) as RadioButton).isChecked = true
            checkRadio = binding!!.statusIndicator.getChildAt(0) as RadioButton?
        }
        viewModel!!.getAllPairStatus()
    }

    //用户信息被修改，刷新委托信息和钱包
    override fun onUserInfoChanged() {
        refreshUserInfo()
    }

    override fun onMoney(observable: Observable<Money?>?) {
        if (observable == null) {
            binding!!.moneyLayout.visibility = View.GONE
            binding!!.logoLayout.visibility = View.VISIBLE
            binding!!.totalMoney.tag = "$$nullAmount"
            binding!!.totalMoneyCny.tag = "¥$nullAmount"
            refreshMoneys()
        } else {
            observable.subscribe(HttpCallbackSimple<Money>(mContext, false, object : Callback<Money>() {
                override fun callback(money: Money?) {
                    binding!!.moneyLayout.visibility = View.VISIBLE
                    binding!!.logoLayout.visibility = View.GONE
                    binding!!.totalMoney.tag = "$" + if (money?.usdt == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(money.usdt, 10, 2, 2)
                    binding!!.totalMoneyCny.tag = "¥" + if (money?.cny == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(money.cny, 10, 2, 2)
                    refreshMoneys()
                }

                override fun error(type: Int, error: Any?) {
                    binding!!.moneyLayout.visibility = View.GONE
                    binding!!.logoLayout.visibility = View.VISIBLE
                    binding!!.totalMoney.tag = "$$nullAmount"
                    binding!!.totalMoneyCny.tag = "¥$nullAmount"
                    refreshMoneys()
                }
            }))
        }
    }

    override fun getMoneyCallback(): Callback<Money?>? {
        return object : Callback<Money?>() {
            override fun callback(money: Money?) {
                binding!!.moneyLayout.visibility = View.VISIBLE
                binding!!.logoLayout.visibility = View.GONE
                binding!!.totalMoney.tag = "$" + if (money?.usdt == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(money.usdt, 10, 2, 2)
                binding!!.totalMoneyCny.tag = "¥" + if (money?.cny == null) nullAmount else NumberUtil.formatNumberDynamicScaleNoGroup(money.cny, 10, 2, 2)
                refreshMoneys()
            }

            override fun error(type: Int, error: Any?) {
                binding!!.moneyLayout.visibility = View.GONE
                binding!!.logoLayout.visibility = View.VISIBLE
                binding!!.totalMoney.tag = "$$nullAmount"
                binding!!.totalMoneyCny.tag = "¥$nullAmount"
                refreshMoneys()
            }

        }
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

    //拉取用户数据
    private fun reloadUserInfo() {
        if (context != null && CookieUtil.getUserInfo(context!!) != null) {
            getUserInfo(object : Callback<UserInfo?>() {
                override fun callback(result: UserInfo?) {
                    if (result != null) {
                        userInfo = result
                        imageLoader!!.loadImage(binding!!.btnMainMenu, if (userInfo == null) null else userInfo!!.headPortrait, R.drawable.icon_avatar, true)
                    }
                }

                override fun error(type: Int, error: Any) {}
            })
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
            binding?.pairSince?.setTextColor(color)
            binding?.pairPriceCny?.text = String.format("¥ %s", pairStatus.currentPriceCNYFormat)
        }
    }
}