package com.black.frying.fragment

import android.graphics.drawable.ColorDrawable
import android.os.*
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
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
    private var set: String? = null
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
                }, 300)
            }

        })
        return binding?.root
    }


    override fun onResume() {
        super.onResume()
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
        if (pairObserver == null) {
            pairObserver = createPairObserver()
        }
        SocketDataContainer.subscribePairObservable(pairObserver)
        if (TextUtils.equals(set, collect)) {
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
        if (pairObserver != null) {
            SocketDataContainer.removePairObservable(pairObserver)
            pairObserver == null
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
                    for (pairStatus in value) {
                        Log.d("uuuuuu", "set = " + set)
                        Log.d("uuuuuu", "pair = " + pairStatus?.pair)
                        Log.d("uuuuuu", "isDear = " + pairStatus?.is_dear)
                        Log.d("uuuuuu", "tradeAmount = " + pairStatus?.tradeAmount)
                        Log.d("uuuuuu", "tradeVolume = " + pairStatus?.tradeVolume)
                        pairStatus?.pair?.let {
                            var showPair = dataMap[it]
                            var isDear = dearPairs.contains(it)
                            Log.d("uuuuuu", "showPair = " + showPair)
                            if (showPair != null) {
                                //更新已有的交易对
                                if (TextUtils.equals(set, collect)) {
                                    //如果是自选，并且交易对现在不再是自选，删除该交易对
                                    if (!isDear) {
                                        if(dataMap.containsKey(it)){
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
                                } else {
                                    //更新行情相关数据
                                    if (pairStatus?.setName.equals(set) && pairStatus?.pair.equals(showPair.pair)) {
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
                                    if (pairStatus?.setName.equals(set)) {
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
//                            adapter?.sortData(comparator)
//                            adapter?.notifyDataSetChanged()
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
                SocketDataContainer.getPairsWithSet(
                    activity,
                    set,
                    object : Callback<ArrayList<PairStatus?>?>() {
                        override fun error(type: Int, error: Any) {}
                        override fun callback(returnData: ArrayList<PairStatus?>?) {
                            if (returnData == null) {
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
                                    }
                                }
                            }
                        }
                    })
            })
        }

    override fun onItemClick(parent: AdapterView<*>?, view: View, position: Int, id: Long) {
        activity?.let {
            val pairStatus = adapter?.getItem(position)
            CookieUtil.setCurrentPair(it, pairStatus?.pair)
            sendPairChangedBroadcast(SocketUtil.COMMAND_PAIR_CHANGED)
            val bundle = Bundle()
            bundle.putString(ConstData.PAIR, pairStatus?.pair)
            BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).with(bundle).go(it)
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
        fun newInstance(tab: QuotationSet?): HomePageQuotationDetailFragment {
            val args = Bundle()
            val fragment = HomePageQuotationDetailFragment()
            fragment.arguments = args
            fragment.set = tab?.coinType
            return fragment
        }
    }
}