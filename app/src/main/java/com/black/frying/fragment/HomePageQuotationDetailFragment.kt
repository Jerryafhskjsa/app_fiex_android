package com.black.frying.fragment

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Process
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import com.black.base.fragment.BaseFragment
import com.black.base.model.SuccessObserver
import com.black.base.model.socket.PairStatus
import com.black.base.util.*
import com.black.frying.adapter.HomeQuotationDetailAdapter
import com.black.frying.util.PairQuotationComparator
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageQuotationDetailBinding
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import java.util.*

/**
 * @author 行情tab页
 */
class HomePageQuotationDetailFragment : BaseFragment(), AdapterView.OnItemClickListener, View.OnClickListener {
    private var set: String? = null
    private var collect: String? = null

    private var binding: FragmentHomePageQuotationDetailBinding? = null

    private var adapter: HomeQuotationDetailAdapter? = null
    private val dataList = ArrayList<PairStatus?>()
    private val dataMap: MutableMap<String, PairStatus?> = HashMap()
    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null
    private var comparator = PairQuotationComparator(PairQuotationComparator.NORMAL, PairQuotationComparator.NORMAL, PairQuotationComparator.NORMAL)
    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = createPairObserver()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (binding != null) {
            return binding?.root
        }
        collect = getString(R.string.pair_collect)

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home_page_quotation_detail, container, false)
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
        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
        thisSetPairStatusData
        if (pairObserver == null) {
            pairObserver = createPairObserver()
        }
        SocketDataContainer.subscribePairObservable(pairObserver)
        if (TextUtils.equals(set, collect)) {
            SocketDataContainer.refreshDearPairs(mContext)
        }
    }

    override fun onStop() {
        super.onStop()
        if (pairObserver != null) {
            SocketDataContainer.removePairObservable(pairObserver)
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
        adapter?.resetSkinResources()
        adapter?.notifyDataSetChanged()
    }

    private fun createPairObserver(): Observer<ArrayList<PairStatus?>?> {
        return object : SuccessObserver<ArrayList<PairStatus?>?>() {
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
    private fun updatePairStatusData(value: ArrayList<PairStatus?>) {
        postHandleTask(socketHandler, Runnable {
            synchronized(dataMap) {
                synchronized(dataList) {
                    var hasPairListChanged = false
                    for (pairStatus in value) {
                        pairStatus?.pair?.let {
                            val showPair = dataMap[it]
                            if (showPair != null) {
                                if (TextUtils.equals(set, collect) && !pairStatus.is_dear) {
                                    //如果是自选，并且交易对现在不再是自选，删除该交易对
                                    adapter?.removeItem(pairStatus)
                                    dataList.remove(pairStatus)
                                    dataMap.remove(it)
                                    hasPairListChanged = true
                                } else {
                                    //重新设置相关参数
                                    showPair.currentPrice = (pairStatus.currentPrice)
                                    showPair.setCurrentPriceCNY(pairStatus.currentPriceCNY, nullAmount)
                                    showPair.priceChangeSinceToday = (pairStatus.priceChangeSinceToday)
                                    showPair.totalAmount = (pairStatus.totalAmount)
                                }
                            } else {
                                pairStatus.currentPrice = (pairStatus.currentPrice)
                                pairStatus.setCurrentPriceCNY(pairStatus.currentPriceCNY, nullAmount)
                                pairStatus.priceChangeSinceToday = (pairStatus.priceChangeSinceToday)
                                pairStatus.totalAmount = (pairStatus.totalAmount)
                                //判断是不是该交易对的
                                if (TextUtils.equals(set, collect)) {
                                    if (pairStatus.is_dear) {
                                        dataList.add(pairStatus)
                                        dataMap[it] = pairStatus
                                        adapter?.addItem(pairStatus)
                                        hasPairListChanged = true
                                    } else {

                                    }
                                } else {
                                    if (TextUtils.equals(pairStatus.setName, set)) {
                                        dataList.add(pairStatus)
                                        dataMap[it] = pairStatus
                                        adapter?.addItem(pairStatus)
                                        hasPairListChanged = true
                                    } else {

                                    }
                                }
                            }
                        }
                    }
                    mContext?.runOnUiThread {
                        if (hasPairListChanged && isVisible) {
                            adapter?.sortData(comparator)
                        }
                        adapter?.notifyDataSetChanged()
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
                SocketDataContainer.getPairsWithSet(activity, set, object : Callback<ArrayList<PairStatus?>?>() {
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
                                    if (isVisible) {
                                        adapter?.sortData(comparator)
                                    }
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
            BlackRouter.getInstance().build(RouterConstData.QUOTATION_DETAIL).go(it)
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
            R.id.btn_action -> BlackRouter.getInstance().build(RouterConstData.DEAR_PAIR_SEARCH).go(mContext) { _, _ -> }
        }
    }

    companion object {
        fun newInstance(tab: String?): HomePageQuotationDetailFragment {
            val args = Bundle()
            val fragment = HomePageQuotationDetailFragment()
            fragment.arguments = args
            fragment.set = tab
            return fragment
        }
    }
}