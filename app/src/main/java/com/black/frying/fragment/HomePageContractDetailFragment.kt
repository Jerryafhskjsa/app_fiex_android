package com.black.frying.fragment

import android.annotation.SuppressLint
import android.graphics.drawable.ColorDrawable
import android.os.*
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.black.base.fragment.BaseFragment
import com.black.base.lib.refreshlayout.defaultview.RefreshHolderFrying
import com.black.base.model.ContractRecordTabBean
import com.black.base.model.QuotationSet
import com.black.base.model.SuccessObserver
import com.black.base.model.socket.PairStatus
import com.black.base.service.DearPairService
import com.black.base.util.*
import com.black.base.widget.AutoHeightViewPager
import com.black.frying.adapter.HomeContractDetailAdapter
import com.black.frying.util.PairQuotationComparator
import com.black.lib.refresh.QRefreshLayout
import com.black.router.BlackRouter
import com.black.util.Callback
import com.black.util.CommonUtil
import com.fbsex.exchange.R
import com.fbsex.exchange.databinding.FragmentHomePageContractDetailBinding
import io.reactivex.Observer
import skin.support.content.res.SkinCompatResources
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author 合約記錄下面的列表頁
 */
class HomePageContractDetailFragment : BaseFragment(), AdapterView.OnItemClickListener,
    View.OnClickListener {
    private var type: ContractRecordTabBean? = null
    private var collect: String? = null

    private var binding: FragmentHomePageContractDetailBinding? = null
    private var mViewPager: AutoHeightViewPager? = null

    private var adapter: HomeContractDetailAdapter? = null
    private val dataList = ArrayList<PairStatus?>()
    private val dataMap: MutableMap<String?, PairStatus?> = HashMap()

    //异步获取数据
    private var handlerThread: HandlerThread? = null
    private var socketHandler: Handler? = null

    private var pairObserver: Observer<ArrayList<PairStatus?>?>? = null
    private var gettingPairsData: Boolean? = false

    /**
     * adapter height when viewpager in scrollview
     */
    fun setAutoHeightViewPager(viewPager: AutoHeightViewPager?) {
        mViewPager = viewPager
    }

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
            R.layout.fragment_home_page_contract_detail,
            container,
            false
        )
        val drawable = ColorDrawable()
        drawable.color = SkinCompatResources.getColor(activity, R.color.L1)
        drawable.alpha = (0xff * 0.3).toInt()
        binding?.listView?.divider = drawable
        binding?.listView?.dividerHeight = 1
        for(i in 0 until  5){
            var pairStatus = PairStatus()
            dataList.add(pairStatus)
        }
        adapter = HomeContractDetailAdapter(mContext!!, dataList)
        binding?.listView?.adapter = adapter
        binding?.listView?.onItemClickListener = this


        val emptyView = inflater.inflate(R.layout.list_view_empty_long, null)
        val group = binding?.listView?.parent as ViewGroup
        group.addView(emptyView)
        binding?.listView?.emptyView = emptyView

        if (arguments != null) {
            val position = arguments!!.getInt(AutoHeightViewPager.POSITION)
            binding?.root?.let { mViewPager?.setViewPosition(it, position) }
        }
        return binding?.root
    }


    override fun onResume() {
        super.onResume()
        handlerThread = HandlerThread(ConstData.SOCKET_HANDLER, Process.THREAD_PRIORITY_BACKGROUND)
        handlerThread?.start()
        socketHandler = Handler(handlerThread?.looper)
//        thisSetPairStatusData
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


    /**
     * 根据tab获取对应list数据
     */
    private val thisSetPairStatusData: Unit
        get() {
            postHandleTask(socketHandler, Runnable {
                SocketDataContainer.getPairsWithSet(
                    activity,
                    "USDT",
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
                                        adapter?.notifyDataSetChanged()
                                        gettingPairsData = false
                                    }
                                }
                            }
                            gettingPairsData = false
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


    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_action -> BlackRouter.getInstance().build(RouterConstData.DEAR_PAIR_SEARCH)
                .go(mContext) { _, _ -> }
        }
    }

    companion object {
        fun newInstance(type: ContractRecordTabBean?): HomePageContractDetailFragment {
            val args = Bundle()
            val fragment = HomePageContractDetailFragment()
            fragment.arguments = args
            fragment.type = type
            return fragment
        }
    }
}