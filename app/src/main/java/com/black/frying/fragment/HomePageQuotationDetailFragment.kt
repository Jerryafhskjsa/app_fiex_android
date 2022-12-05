package com.black.frying.fragment

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.QuotationSet
import com.black.base.model.SuccessObserver
import com.black.base.model.socket.PairStatus
import com.black.base.service.DearPairService
import com.black.base.util.*
import com.black.frying.FryingApplication
import com.black.frying.adapter.HomeQuotationDetailAdapter
import com.black.frying.util.PairQuotationComparator
import com.black.lib.refresh.QRefreshLayout
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageQuotationDetailBinding
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author 行情tab页
 */
class HomePageQuotationDetailFragment : BaseFragment(), AdapterView.OnItemClickListener,
    View.OnClickListener {
    private var set: String? = null//现货(自选，eth，usdt) 合约(自选 u本位 币本位)
    private var tabTag: String? = null//现货，合约
    private var collect: String? = null

    private var binding: FragmentHomePageQuotationDetailBinding? = null

    private var adapter: HomeQuotationDetailAdapter? = null
    private val dataList = ArrayList<PairStatus?>()
    private val dataMap: MutableMap<String?, PairStatus?> = HashMap()
    private val dearPairs = java.util.ArrayList<String?>()

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null
    private var comparator = PairQuotationComparator(
        PairQuotationComparator.NORMAL,
        PairQuotationComparator.NORMAL,
        PairQuotationComparator.NORMAL,
        PairQuotationComparator.NORMAL
    )
    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = null
    private var futureTickerObserver: Observer<ArrayList<PairStatus?>?>? = null
    private var gettingPairsData: Boolean? = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (binding != null) {
            return binding?.root
        }
        collect = getString(R.string.pair_collect)

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home_page_quotation_detail,
            container,
            false
        )
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.L1)
        drawable.alpha = (0xff * 0.3).toInt()
        binding?.listView?.divider = drawable
        binding?.listView?.dividerHeight = 1
        adapter = HomeQuotationDetailAdapter(mContext!!, dataList)
        binding?.listView?.adapter = adapter
        binding?.listView?.onItemClickListener = this
        //解决add empty view之后下拉刷新异常
        binding?.listView?.setOnTouchListener(object : View.OnTouchListener {
            @SuppressLint("ClickableViewAccessibility")
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_MOVE -> {
                        if (binding?.listView?.getFirstVisiblePosition() == 0 &&
                            binding?.listView?.getChildAt(0)?.getTop()!! >= 0
                        ) {//或者 listView.getChildAt(0).getTop() >= listView.getListPaddingTop())
                            binding?.marketRefreshLayout?.setEnabled(true)
                        } else {
                            binding?.marketRefreshLayout?.setEnabled(false)
                        }
                    }
                }
                return false
            }
        })
        if (collect != null && collect.equals(set, ignoreCase = true)) {
            val emptyView = inflater.inflate(R.layout.list_view_empty_pair, null)
            emptyView.findViewById<View>(R.id.btn_action).setOnClickListener(this)
            val group = binding?.listView?.parent as ViewGroup
            group.addView(emptyView)
            binding?.listView?.emptyView = emptyView
        } else {
            val emptyView = inflater.inflate(R.layout.list_view_empty_long, null)
            val group = binding?.listView?.parent as ViewGroup
            group.addView(emptyView)
            binding?.listView?.emptyView = emptyView
        }
        binding?.marketRefreshLayout?.setRefreshHolder(RefreshHolderFrying(activity!!))
        binding?.marketRefreshLayout?.setOnRefreshListener(object :
            QRefreshLayout.OnRefreshListener {
            override fun onRefresh() {
                binding!!.marketRefreshLayout.postDelayed({
                    binding!!.marketRefreshLayout.setRefreshing(
                        false
                    )
                }, 100)
            }
        })
        return binding?.root
    }


    override fun onResume() {
        super.onResume()
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
        when (tabTag) {
            getString(R.string.spot) -> {
                if (pairObserver == null) {
                    pairObserver = createPairObserver()
                }
                SocketDataContainer.subscribePairObservable(pairObserver)
            }
            getString(R.string.futures) -> {
                if (futureTickerObserver == null) {
                    futureTickerObserver = createFutureTickerObserver()
                }
                SocketDataContainer.subscribeFuturePairObservable(futureTickerObserver)
            }
        }

        if (TextUtils.equals(set, collect)) {
            gettingPairsData = true
            DearPairService.getDearPairList(
                context,
                socketHandler,
                object : Callback<java.util.ArrayList<String?>?>() {
                    override fun error(type: Int, error: Any) {
                        thisSetPairStatusData
                    }

                    override fun callback(returnData: java.util.ArrayList<String?>?) {
                        if (returnData != null && returnData.isNotEmpty()) {
                            dearPairs.clear()
                            dearPairs.addAll(returnData)
                        }
                        thisSetPairStatusData
                    }
                })
        } else {
            thisSetPairStatusData
        }
    }

    override fun onStop() {
        super.onStop()
        when (tabTag) {
            getString(R.string.spot) -> {
                if (pairObserver != null) {
                    SocketDataContainer.removePairObservable(pairObserver)
                    pairObserver == null
                }
            }
            getString(R.string.futures) -> {
                if (futureTickerObserver != null) {
                    SocketDataContainer.removeFuturePairObservable(futureTickerObserver)
                    futureTickerObserver == null
                }
            }
        }

        if (socketHandler != null) {
            socketHandler?.removeMessages(0)
            socketHandler = null
        }
        if (handlerThread != null) {
            handlerThread?.quit()
        }
    }

    override fun doResetSkinResources() {
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.L1_ALPHA30)
        binding?.listView?.divider = drawable
        binding?.listView?.dividerHeight = 1
//        adapter?.resetSkinResources()
//        adapter?.notifyDataSetChanged()
    }

    private fun createPairObserver(): Observer<ArrayList<PairStatus?>?> {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onSuccess(value: ArrayList<PairStatus?>?) {
                activity ?: return
                value?.let { updatePairStatusData(it) }
            }
        }
    }

    private fun createFutureTickerObserver(): Observer<ArrayList<PairStatus?>?> {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onSuccess(value: ArrayList<PairStatus?>?) {
                activity ?: return
                value?.let { updatePairStatusData(it) }
            }
        }
    }


    /**
     * 更新交易对数据到ui
     *
     * @param value
     */
    @RequiresApi(Build.VERSION_CODES.N)
    private fun updatePairStatusData(value: ArrayList<PairStatus?>) {
        postHandleTask(socketHandler, Runnable {
            synchronized(dataMap) {
                synchronized(dataList) {
                    var hasPairListChanged = false
                    //size>1，表示更新所有数据，会造成行情页面空白,size = 1表示单条行情更新，此处需要优化
                    if (value.size > 1) {
                        return@Runnable
                    }
                    var setType:String? = null
                    when(tabTag){
                        getString(R.string.futures) ->{
                            when(set){
                                getString(R.string.usdt_base) -> {
                                    setType = getString(R.string.usdt).lowercase()
                                }
                                getString(R.string.coin_base) -> {
                                    setType = getString(R.string.usd)
                                }
                            }
                        }
                        getString(R.string.spot) -> {
                            setType = set
                        }
                    }
                    for (pairStatus in value) {
                        pairStatus?.pair?.let {
                            var showPair = dataMap[it]
                            var isDear = dearPairs.contains(it)
                            if (showPair != null) {
                                //更新已有的交易对
                                if (TextUtils.equals(set, collect)) {
                                    //如果是自选，并且交易对现在不再是自选，删除该交易对
                                    if (gettingPairsData == false) {
                                        if (!isDear) {
                                            if (dataMap.containsKey(it)) {
                                                dataMap.remove(it)
                                                hasPairListChanged = true
                                            }
                                        } else {
                                            if (pairStatus?.pair.equals(showPair.pair)) {
                                                showPair.currentPrice = (pairStatus.currentPrice)
                                                showPair.setCurrentPriceCNY(
                                                    pairStatus.currentPriceCNY,
                                                    nullAmount
                                                )
                                                showPair.priceChangeSinceToday =
                                                    (pairStatus.priceChangeSinceToday)
                                                showPair.tradeVolume = (pairStatus.tradeVolume)
                                                showPair.tradeAmount = (pairStatus.tradeAmount)
                                                dataMap.replace(it, showPair)
                                                hasPairListChanged = true
                                            }
                                        }
                                    }
                                } else {
                                    //更新行情相关数据
                                    if (pairStatus?.setName.equals(setType) && pairStatus?.pair.equals(
                                            showPair.pair
                                        )
                                    ) {
                                        showPair.currentPrice = (pairStatus.currentPrice)
                                        showPair.setCurrentPriceCNY(
                                            pairStatus.currentPriceCNY,
                                            nullAmount
                                        )
                                        showPair.priceChangeSinceToday =
                                            (pairStatus.priceChangeSinceToday)
                                        showPair.tradeVolume = (pairStatus.tradeVolume)
                                        showPair.tradeAmount = (pairStatus.tradeAmount)
                                        dataMap.replace(it, showPair)
                                        hasPairListChanged = true
                                    }
                                }
                            } else {
                                //是新的交易对
                                if (TextUtils.equals(set, collect) && isDear) {
                                    if (pairStatus?.is_dear != null && pairStatus.is_dear) {
                                        showPair = PairStatus()
                                        showPair.currentPrice = (pairStatus.currentPrice)
                                        showPair.setCurrentPriceCNY(
                                            pairStatus.currentPriceCNY,
                                            nullAmount
                                        )
                                        showPair.priceChangeSinceToday =
                                            (pairStatus.priceChangeSinceToday)
                                        showPair.tradeVolume = (pairStatus.tradeVolume)
                                        showPair.tradeAmount = (pairStatus.tradeAmount)
                                        dataMap[it] = showPair
                                        hasPairListChanged = true
                                    }
                                } else {
                                    if (pairStatus?.setName.equals(setType)) {
                                        showPair = PairStatus()
                                        showPair.currentPrice = (pairStatus.currentPrice)
                                        showPair.setCurrentPriceCNY(
                                            pairStatus.currentPriceCNY,
                                            nullAmount
                                        )
                                        showPair.priceChangeSinceToday =
                                            (pairStatus.priceChangeSinceToday)
                                        showPair.tradeVolume = (pairStatus.tradeVolume)
                                        showPair.tradeAmount = (pairStatus.tradeAmount)
                                        dataMap[it] = showPair
                                        hasPairListChanged = true
                                    }
                                }
                            }
                        }
                    }
                    mContext?.runOnUiThread {
                        if (hasPairListChanged) {
                            var result = ArrayList<PairStatus?>()
                            dataMap.forEach {
                                result.add(it.value)
                            }
                            adapter?.data = result
                            updateCompare(comparator)
                        }
                    }
                }
            }
        })
    }


    /**
     * 根据tab获取对应list数据
     */
    private val thisSetPairStatusData: Unit
        get() {
            postHandleTask(socketHandler, Runnable {
                if (tabTag.equals(getString(R.string.spot))) {
                    SocketDataContainer.getPairsWithSet(
                        activity,
                        set,
                        object : Callback<ArrayList<PairStatus?>?>() {
                            override fun error(type: Int, error: Any) {
                                gettingPairsData = false
                            }

                            override fun callback(returnData: ArrayList<PairStatus?>?) {
                                if (returnData == null) {
                                    gettingPairsData = false
                                    return
                                }
                                synchronized(dataMap) {
                                    synchronized(dataList) {
                                        dataMap.clear()
                                        dataList.clear()
                                        dataList.addAll(returnData)
                                        for (pairStatus in returnData) {
                                            pairStatus?.pair?.let {
                                                dataMap[it] = pairStatus
                                            }
                                        }
                                        mContext?.runOnUiThread {
                                            adapter?.data = dataList
                                            adapter?.sortData(comparator)
                                            adapter?.notifyDataSetChanged()
                                            gettingPairsData = false
                                        }
                                    }
                                }
                                gettingPairsData = false
                            }
                        })
                }
                if (tabTag.equals(getString(R.string.futures))) {
                    var pairStatusType: ConstData.PairStatusType? = null
                    when (set) {
                        getString(R.string.usdt_base) -> pairStatusType =
                            ConstData.PairStatusType.FUTURE_U
                        getString(R.string.coin_base) -> pairStatusType =
                            ConstData.PairStatusType.FUTURE_COIN
                    }
                    SocketDataContainer.getFuturesPairsWithSet(
                        activity,
                        pairStatusType,
                        object : Callback<ArrayList<PairStatus?>?>() {
                            override fun error(type: Int, error: Any) {
                                gettingPairsData = false
                            }

                            override fun callback(returnData: ArrayList<PairStatus?>?) {
                                if (returnData == null) {
                                    gettingPairsData = false
                                    return
                                }
                                synchronized(dataMap) {
                                    synchronized(dataList) {
                                        dataMap.clear()
                                        dataList.clear()
                                        dataList.addAll(returnData)
                                        for (pairStatus in returnData) {
                                            pairStatus?.pair?.let {
                                                dataMap[it] = pairStatus
                                            }
                                        }
                                        mContext?.runOnUiThread {
                                            adapter?.data = dataList
                                            adapter?.sortData(comparator)
                                            adapter?.notifyDataSetChanged()
                                            gettingPairsData = false
                                        }
                                    }
                                }
                                gettingPairsData = false
                            }
                        })
                }
            })
        }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        activity?.let {
            val pairStatus = adapter?.getItem(position)
            if (tabTag == getString(R.string.spot)) {
                CookieUtil.setCurrentPair(it, pairStatus?.pair)
                sendPairChangedBroadcast(SocketUtil.COMMAND_PAIR_CHANGED)
                val bundle = Bundle()
                bundle.putString(ConstData.PAIR, pairStatus?.pair)
                BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).with(bundle)
                    .go(it)
            }
            if (tabTag == getString(R.string.futures)) {

            }
        }
    }

    fun updateCompare(comparator: PairQuotationComparator) {
        this.comparator = comparator
        if (isVisible) {
            CommonUtil.checkActivityAndRunOnUI(mContext) {
                adapter?.sortData(this.comparator)
                adapter?.notifyDataSetChanged()
            }
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_action -> BlackRouter.getInstance().build(RouterConstData.DEAR_PAIR_SEARCH)
                .go(mContext) { _, _ -> }
        }
    }

    companion object {
        fun newInstance(tab: QuotationSet?, tabTag: String?): HomePageQuotationDetailFragment {
            val args = Bundle()
            val fragment = HomePageQuotationDetailFragment()
            fragment.arguments = args
            fragment.set = tab?.coinType
            fragment.tabTag = tabTag
            var context = FryingApplication.instance()
//            if(tabTag.equals(context.getString(R.string.futures))){
//                if(tab?.coinType.equals(context.getString(R.string.usdt_base))){
//                    fragment.set = context.getString(R.string.usdt).lowercase()
//                }
//                if(tab?.coinType.equals(context.getString(R.string.coin_base))){
//                    fragment.set = context.getString(R.string.usd)
//                }
//            }
            Log.d("iiiiii"," fragment.set = "+ fragment.set)
            Log.d("iiiiii"," fragment.tabTag = "+ fragment.tabTag)
            return fragment
        }
    }
}